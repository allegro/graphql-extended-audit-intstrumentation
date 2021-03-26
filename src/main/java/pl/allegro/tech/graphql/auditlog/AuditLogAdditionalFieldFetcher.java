package pl.allegro.tech.graphql.auditlog;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import graphql.execution.FetchedValue;
import graphql.execution.instrumentation.parameters.InstrumentationFieldCompleteParameters;
import graphql.language.Field;
import graphql.language.InlineFragment;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.DataFetchingEnvironmentImpl;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLNamedOutputType;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLTypeUtil;
import graphql.schema.GraphQLUnionType;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.allegro.tech.graphql.auditlog.model.ResultData;

class AuditLogAdditionalFieldFetcher {

  private static final Logger logger =
      LoggerFactory.getLogger(AuditLogAdditionalFieldFetcher.class);

  private final AdditionalFieldsSetupChecker additionalFieldsSetupChecker;

  public AuditLogAdditionalFieldFetcher(
      AdditionalFieldsSetupChecker additionalFieldsSetupChecker) {
    this.additionalFieldsSetupChecker = requireNonNull(additionalFieldsSetupChecker);
  }


  List<AdditionalField> objectAdditionalFields(InstrumentationFieldCompleteParameters parameters) {

    if (extractObjectType(parameters)) {
      return fieldSetups(parameters)
          .stream()
          .filter(
              it -> additionalFieldsSetupChecker
                  .shouldBeAdditional(it.objectName(), it.fieldName()))
          .flatMap(
              it -> objectAdditionalFields(parameters, it.objectName(), it.fieldName()).stream())
          .collect(Collectors.toList());
    } else {
      return List.of();
    }
  }

  private Optional<AdditionalField> objectAdditionalFields(
      InstrumentationFieldCompleteParameters parameters, String objectName, String fieldName) {
    return extractObjectType(parameters, objectName, fieldName)
        .filter(it -> !queryContainsAdditionalField(parameters, objectName, fieldName))
        .map(
            objectType -> {
              GraphQLOutputType fieldDefinitionType =
                  objectType.getFieldDefinition(fieldName).getType();
              Object fieldValue = fetchField(fieldName, objectType, parameters);
              List<String> path = getObjectPath(parameters);
              CompletableFuture<ResultData> resultData =
                  toResultData(fieldDefinitionType, fieldName, fieldValue);
              return new AdditionalField(path, resultData);
            });
  }

  private List<FieldSetup> fieldSetups(InstrumentationFieldCompleteParameters parameters) {
    return parameters
        .getExecutionStrategyParameters()
        .getField()
        .getSingleField()
        .getSelectionSet()
        .getSelections()
        .stream()
        .filter(it -> it instanceof InlineFragment)
        .map(it -> (InlineFragment) it)
        .flatMap(
            it -> {
              var objectName = it.getTypeCondition().getName();
              return fieldSetupStream(it, objectName);
            }).collect(Collectors.toList());
  }

  private Stream<FieldSetup> fieldSetupStream(InlineFragment it, String objectName) {
    return it.getSelectionSet()
        .getSelections()
        .stream()
        .filter(selection -> selection instanceof Field)
        .map(field -> (Field) field)
        .map(Field::getName)
        .map(fieldName -> new FieldSetup(objectName, fieldName));
  }

  private List<String> getObjectPath(InstrumentationFieldCompleteParameters parameters) {
    return parameters.getTypeInfo().getPath().toList().stream()
        .map(String::valueOf)
        .collect(Collectors.toList());
  }

  private boolean extractObjectType(
      InstrumentationFieldCompleteParameters parameters) {
    GraphQLOutputType type = parameters.getField().getType();
    if (type instanceof GraphQLUnionType) {
      return true;
    } else if (type instanceof GraphQLObjectType) {
      return true;
    }

    return false;
  }

  private Optional<GraphQLObjectType> extractObjectType(
      InstrumentationFieldCompleteParameters parameters, String objectName, String fieldName) {
    GraphQLOutputType type = parameters.getField().getType();
    if (type instanceof GraphQLUnionType) {
      return ((GraphQLUnionType) type).getTypes().stream()
          .filter(it1 -> it1.getName().equals(objectName))
          .findAny()
          .filter(it -> it instanceof GraphQLObjectType)
          .map(it -> (GraphQLObjectType) it);
    } else if (type instanceof GraphQLObjectType) {
      if (((GraphQLObjectType) type).getName().equals(objectName)) {
        return Optional.of((GraphQLObjectType) type);
      }
    }
    return Optional.empty();
  }

  private boolean queryContainsAdditionalField(
      InstrumentationFieldCompleteParameters parameters, String objectName, String fieldName) {
    return parameters
        .getExecutionStrategyParameters()
        .getField()
        .getSingleField()
        .getSelectionSet()
        .getSelections()
        .stream()
        .filter(it -> it instanceof InlineFragment)
        .map(it -> (InlineFragment) it)
        .filter(it -> it.getTypeCondition().getName().equals(objectName))
        .findAny()
        .stream()
        .flatMap(it -> it.getSelectionSet().getSelections().stream())
        .filter(it -> it instanceof Field)
        .map(it -> (Field) it)
        .anyMatch(it -> it.getName().equals(fieldName));
  }

  private CompletableFuture<ResultData> toResultData(
      GraphQLOutputType fieldDefinition, String fieldName, Object fieldValue) {
    if (fieldValue instanceof CompletableFuture) {
      return ((CompletableFuture<Object>) fieldValue)
          .thenApply(value -> getSyncResultData(fieldDefinition, fieldName, value));
    } else {
      return CompletableFuture.completedFuture(
          getSyncResultData(fieldDefinition, fieldName, fieldValue));
    }
  }

  private ResultData getSyncResultData(
      GraphQLOutputType fieldDefinition, String fieldName, Object value) {
    if (value == null || (value instanceof Optional && ((Optional) value).isEmpty())) {
      return new ResultData.NullResultData(fieldName);
    }
    Object unwrappedValue = (value instanceof Optional) ? ((Optional<?>) value).get() : value;
    if (GraphQLTypeUtil.unwrapAll(fieldDefinition) instanceof GraphQLScalarType) {
      String typeName = unwrappedValue.getClass().getSimpleName();
      Object graphQLValue =
          ((GraphQLScalarType) fieldDefinition).getCoercing().serialize(unwrappedValue);
      return new ResultData.ScalarResultData(fieldName, typeName, String.valueOf(graphQLValue));
    }
    throw new IllegalStateException(
        format(" Only scalar types are supported as additional action log field."
                + "Can't get data for %s, unknown %s graphQLType", value,
            fieldDefinition.getClass().getSimpleName()));
  }

  private Object fetchField(
      String fieldName,
      GraphQLNamedOutputType object,
      InstrumentationFieldCompleteParameters parameters) {
    if (object instanceof GraphQLObjectType) {
      GraphQLFieldDefinition fieldDefinition = ((GraphQLObjectType) object)
          .getFieldDefinition(fieldName);
      DataFetcher<?> dataFetcher =
          parameters
              .getExecutionContext()
              .getGraphQLSchema()
              .getCodeRegistry()
              .getDataFetcher((GraphQLObjectType) object, fieldDefinition);
      Object fetchedValue = ((FetchedValue) parameters.getFetchedValue()).getFetchedValue();
      DataFetchingEnvironment environment =
          DataFetchingEnvironmentImpl.newDataFetchingEnvironment(parameters.getExecutionContext())
              .source(fetchedValue)
              .fieldType(fieldDefinition.getType())
              .parentType(fieldDefinition.getType())
              .build();
      try {
        if (fetchedValue == null) {
          return null;
        } else {
          return dataFetcher.get(environment);
        }
      } catch (Exception ex) {
        logger.warn(
            format("An error occurred when fetching action log additional field %s:%s",
                object.getName(),
                fieldName
            ),
            ex);
        return null;
      }
    }
    throw new IllegalStateException(
        String.format("Unsupported object type %s for field %s:%s",
            object.getClass().getSimpleName(),
            object.getName(),
            fieldName
        )
    );
  }

  static class AdditionalField {

    private final List<String> path;
    private final CompletableFuture<ResultData> resultData;

    public AdditionalField(List<String> path,
        CompletableFuture<ResultData> resultData) {
      this.path = path;
      this.resultData = resultData;
    }

    public List<String> path() {
      return path;
    }

    public CompletableFuture<ResultData> resultData() {
      return resultData;
    }
  }
}
