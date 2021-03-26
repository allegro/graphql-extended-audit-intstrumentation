package pl.allegro.tech.graphql.auditlog;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.execution.FetchedValue;
import graphql.execution.instrumentation.InstrumentationContext;
import graphql.execution.instrumentation.InstrumentationState;
import graphql.execution.instrumentation.SimpleInstrumentation;
import graphql.execution.instrumentation.parameters.InstrumentationCreateStateParameters;
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldCompleteParameters;
import graphql.execution.instrumentation.parameters.InstrumentationFieldFetchParameters;
import graphql.language.Argument;
import graphql.language.BooleanValue;
import graphql.language.EnumValue;
import graphql.language.IntValue;
import graphql.language.StringValue;
import graphql.language.Value;
import graphql.language.VariableReference;
import graphql.schema.DataFetcher;
import io.vavr.collection.LinkedHashMap;
import io.vavr.control.Option;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.tech.graphql.auditlog.AuditLogItem.OperationType;
import pl.allegro.tech.graphql.auditlog.model.ResultData;
import pl.allegro.tech.graphql.auditlog.model.UserId;

class AuditLogInstrumentation extends SimpleInstrumentation {

  private static final Logger logger = LoggerFactory.getLogger(
      AuditLogInstrumentation.class);

  private final Clock clock;
  private final AuditLogSender actionLogSender;
  private final AuditLogAdditionalFieldFetcher
      actionLogAdditionalFieldFetcher;
  private final AuditLogAnonymizer actionLogAnonymizer;
  private final UserProvider userProvider;

  public AuditLogInstrumentation(
      Clock clock,
      AuditLogSender actionLogSender,
      UserProvider userProvider,
      AuditLogAnonymizer actionLogAnonymizer,
      AuditLogAdditionalFieldFetcher actionLogAdditionalFieldFetcher
  ) {
    this.clock = requireNonNull(clock);
    this.actionLogSender = requireNonNull(actionLogSender);
    this.userProvider = requireNonNull(userProvider);
    this.actionLogAdditionalFieldFetcher = requireNonNull(actionLogAdditionalFieldFetcher);
    this.actionLogAnonymizer = requireNonNull(actionLogAnonymizer);
  }

  @Override
  public InstrumentationState createState(InstrumentationCreateStateParameters parameters) {
    return new AuditLogInstrumentationState();
  }

  @Override
  public ExecutionInput instrumentExecutionInput(
      ExecutionInput executionInput, InstrumentationExecutionParameters parameters) {
    registerVariablesInState(executionInput, parameters);
    return super.instrumentExecutionInput(executionInput, parameters);
  }

  private void registerVariablesInState(
      ExecutionInput executionInput, InstrumentationExecutionParameters parameters) {
    try {
      AuditLogInstrumentationState instrumentationState = parameters.getInstrumentationState();
      instrumentationState.registerVariables(executionInput.getVariables());
    } catch (Exception e) {
      logger.error("Error on collecting audit log", e);
    }
  }

  @Override
  public DataFetcher<?> instrumentDataFetcher(
      DataFetcher<?> dataFetcher, InstrumentationFieldFetchParameters parameters) {
    registerActionInState(parameters);
    return dataFetcher;
  }

  private void registerActionInState(InstrumentationFieldFetchParameters parameters) {
    try {
      AuditLogInstrumentationState instrumentationState = parameters.getInstrumentationState();
      String outputType =
          InstrumentationUtil.extractObjectTypeFromFieldParameters(parameters).getName();
      String actionName = parameters.getField().getName();
      if (outputType.equals("Query") || outputType.equals("Mutation")) {
        Map<String, Object> variables = instrumentationState.registeredVariables();
        Map<String, Object> arguments =
            parameters.getEnvironment().getMergedField().getSingleField().getArguments().stream()
                .collect(
                    Collectors.toMap(
                        Argument::getName, argument -> argumentValue(argument, variables)));
        OperationType operationType = OperationType.valueOf(outputType.toUpperCase());
        instrumentationState.registerAction(operationType, actionName, arguments);
      }
    } catch (Exception e) {
      logger.error("Error on collecting action log", e);
    }
  }

  private Object argumentValue(Argument argument, Map<String, Object> variables) {
    return extractNativeValue(argument.getValue(), variables)
        .orElseGet(Optional::empty);
  }

  private Optional<Object> extractNativeValue(Value value, Map<String, Object> variables) {
    if (value instanceof StringValue) {
      return Optional.of(((StringValue) value).getValue());
    } else if (value instanceof EnumValue) {
      return Optional.of(((EnumValue) value).getName());
    } else if (value instanceof IntValue) {
      return Optional.of(((IntValue) value).getValue());
    } else if (value instanceof BooleanValue) {
      return Optional.of(((BooleanValue) value).isValue());
    } else if (value instanceof VariableReference) {
      String variableName = ((VariableReference) value).getName();
      if (variables.containsKey(variableName) && variables.get(variableName) == null) {
        return Optional.empty();
      } else if (Optional.ofNullable(variables.get(variableName)).isPresent()) {
        return Optional.of(variables.get(variableName));
      } else {
        throw new IllegalStateException(format("Can't find variable with name %s", variableName));
      }
    } else {
      logger.info(format("Unknown value type %s for name", value.getClass().getName()));
      return Optional.of(value);
    }
  }

  @Override
  public InstrumentationContext<ExecutionResult> beginFieldComplete(
      InstrumentationFieldCompleteParameters parameters) {
    registerAdditionalField(parameters);
    registerTypeForPathInState(parameters);
    return super.beginFieldComplete(parameters);
  }

  private void registerAdditionalField(InstrumentationFieldCompleteParameters parameters) {
    actionLogAdditionalFieldFetcher
        .objectAdditionalFields(parameters)
        .forEach(
            additionalField -> {
              AuditLogInstrumentationState
                  instrumentationState =
                  parameters.getInstrumentationState();
              instrumentationState.registerAdditionalField(
                  additionalField.path(), additionalField.resultData());
            });
  }

  private void registerTypeForPathInState(InstrumentationFieldCompleteParameters parameters) {
    try {
      String originalFieldName = parameters.getField().getName();
      Object fetchedValue = ((FetchedValue) parameters.getFetchedValue()).getFetchedValue();
      Optional<String> typeName =
          Optional.ofNullable(fetchedValue)
              .map(it -> it.getClass().getSimpleName());
      List<String> path =
          parameters.getExecutionStepInfo().getPath().toList().stream()
              .map(Object::toString)
              .collect(Collectors.toList());
      AuditLogInstrumentationState instrumentationState = parameters.getInstrumentationState();
      instrumentationState.registerTypeForPath(path, typeName);
      instrumentationState.registerOriginalFieldName(path, originalFieldName);
    } catch (Exception e) {
      logger.error("Error on collecting action log", e);
    }
  }

  @Override
  public InstrumentationContext<ExecutionResult> beginFieldListComplete(
      InstrumentationFieldCompleteParameters parameters) {
    registerArrayItemTypesForPathInState(parameters);
    return super.beginFieldListComplete(parameters);
  }

  private void registerArrayItemTypesForPathInState(
      InstrumentationFieldCompleteParameters parameters) {
    try {
      AuditLogInstrumentationState instrumentationState = parameters.getInstrumentationState();
      List<String> path =
          parameters.getExecutionStepInfo().getPath().toList().stream()
              .map(Object::toString)
              .collect(Collectors.toList());
      List<Object> fetchedValues =
          new ArrayList<>((Collection<Object>) parameters.getFetchedValue());
      IntStream.range(0, fetchedValues.size())
          .forEach(
              i -> {
                Optional<String> typeName =
                    Optional.ofNullable(fetchedValues.get(i))
                        .map(it -> it.getClass().getSimpleName());
                instrumentationState.registerTypeForPath(
                    addToList(path, String.valueOf(i)), typeName);
              });
    } catch (Exception e) {
      logger.error("Error on collecting action log", e);
    }
  }

  @Override
  public CompletableFuture<ExecutionResult> instrumentExecutionResult(
      ExecutionResult executionResult, InstrumentationExecutionParameters parameters) {
    sendAuditLog(executionResult, parameters);
    return CompletableFuture.completedFuture(executionResult);
  }

  private void sendAuditLog(
      ExecutionResult executionResult, InstrumentationExecutionParameters parameters) {
    try {
      AuditLogItem actionLogItem = createActionLogItem(executionResult, parameters);
      actionLogSender.send(actionLogItem);
      AuditLogItem anonymizedActionLog = actionLogAnonymizer.anonymizeActionLogItem(actionLogItem);
      actionLogSender.sendAnonymized(anonymizedActionLog);
    } catch (Exception e) {
      logger.error(
          format(
              "Error on collecting action log for query %s with variables %s returned result %s",
              parameters.getQuery(),
              parameters.getVariables(),
              executionResult.toString()),
          e);
    }
  }

  private AuditLogItem createActionLogItem(
      ExecutionResult executionResult,
      InstrumentationExecutionParameters parameters) {
    Object outputData = executionResult.getData();
    List<Map<String, Object>> errors = executionResult.getErrors().stream()
        .map(GraphQLError::toSpecification)
        .collect(Collectors.toList());
    AuditLogInstrumentationState instrumentationState = parameters.getInstrumentationState();
    UserId user = userProvider.currentUser();
    return new AuditLogItem(
        UUID.randomUUID().toString(),
        instrumentationState.getOperations(),
        rootResultData(outputData, instrumentationState),
        errors,
        user,
        clock.instant()
    );
  }

  private List<ResultData> rootResultData(
      Object outputData, AuditLogInstrumentationState instrumentationState) {
    return resultData(outputData, List.of(), instrumentationState);
  }

  private List<ResultData> resultData(
      Object outputData, List<String> path, AuditLogInstrumentationState instrumentationState) {
    if (outputData == null) {
      return List.of();
    } else if (outputData instanceof Map) {
      return LinkedHashMap.ofAll((Map<String, Object>) outputData)
          .mapKeys(it -> addToList(path, it))
          .flatMap(it -> createResultDataFor(instrumentationState, it._1, it._2))
          .toJavaList();
    } else {
      throw new IllegalStateException(
          format(
              "Can't handle %s type in output data type", outputData.getClass().getSimpleName()));
    }
  }

  private Option<ResultData> createResultDataFor(
      AuditLogInstrumentationState instrumentationState,
      List<String> itemPath,
      Object itemValue) {
    String fieldName = fieldNameForPath(itemPath, instrumentationState);
    if (isMetadataField(fieldName)) {
      if (itemPath.size() <= 1) {
        String typeName = typeNameForPath(itemPath, instrumentationState);
        return Option.of(new ResultData.IntrospectionResultData(fieldName, typeName));
      }
      return Option.none();
    }
    return Option.of(
        resultDataItemValue(
            fieldName,
            itemValue,
            itemPath,
            instrumentationState));
  }

  private boolean isMetadataField(String fieldName) {
    return fieldName.startsWith("__");
  }

  private ResultData resultDataItemValue(
      String name,
      Object value,
      List<String> path,
      AuditLogInstrumentationState instrumentationState) {
    if (value == null) {
      return new ResultData.NullResultData(name);
    } else if (value instanceof Map) {
      String typeName = typeNameForPath(path, instrumentationState);
      List<ResultData> fields = resultData(value, path, instrumentationState);
      List<ResultData> additionalFields =
          instrumentationState.getAdditionalFields(path).stream()
              .map(CompletableFuture::join) // blocking operation!
              .collect(Collectors.toList());
      List<ResultData> allFields =
          io.vavr.collection.List.ofAll(fields).appendAll(additionalFields).toJavaList();
      return new ResultData.ObjectResultData(name, typeName, allFields);
    } else if (value instanceof String) {
      String typeName = typeNameForPath(path, instrumentationState);
      return new ResultData.ScalarResultData(name, typeName, (String) value);
    } else if (value instanceof Boolean || value instanceof Integer || value instanceof Float) {
      String typeName = typeNameForPath(path, instrumentationState);
      return new ResultData.ScalarResultData(name, typeName, String.valueOf(value));
    } else if (value instanceof List) {
      List<Object> listValue = (List<Object>) value;
      List<ResultData> items =
          IntStream.range(0, listValue.size())
              .mapToObj(
                  i -> {
                    List<String> itemPath = addToList(path, Integer.toString(i));
                    return resultDataItemValue(
                        String.valueOf(i), listValue.get(i), itemPath, instrumentationState);
                  })
              .collect(Collectors.toList());
      return new ResultData.ArrayResultData(name, items);
    } else {
      throw new IllegalStateException(
          format(
              "Can't handle %s type in output data type", value.getClass().getSimpleName()));
    }
  }

  private List<String> addToList(List<String> oldList, String element) {
    ArrayList<String> list = new ArrayList<>(oldList);
    list.add(element);
    return list;
  }

  private String typeNameForPath(
      List<String> path, AuditLogInstrumentationState instrumentationState) {
    return instrumentationState.getTypeName(path);
  }

  private String fieldNameForPath(
      List<String> path, AuditLogInstrumentationState instrumentationState) {
    return instrumentationState.getOriginalFieldName(path);
  }
}
