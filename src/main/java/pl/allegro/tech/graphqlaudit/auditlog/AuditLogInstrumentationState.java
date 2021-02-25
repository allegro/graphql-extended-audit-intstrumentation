package pl.allegro.tech.graphqlaudit.auditlog;

import static java.lang.String.format;

import graphql.execution.instrumentation.InstrumentationState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import pl.allegro.tech.graphqlaudit.auditlog.model.ResultData;

class AuditLogInstrumentationState implements InstrumentationState {
  private final Map<String, Optional<String>> pathToType;
  private final Map<String, String> pathToOriginalFieldName;
  private final List<AuditLogItem.Operation> operations;
  private final Map<String, Object> variables;
  private final List<AdditionalField> additionalFields;

  AuditLogInstrumentationState() {
    pathToType = new ConcurrentHashMap<>();
    pathToOriginalFieldName = new ConcurrentHashMap<>();
    operations = new ArrayList<>();
    variables = new HashMap<>();
    additionalFields = new CopyOnWriteArrayList<>();
  }

  void registerTypeForPath(List<String> path, Optional<String> typeName) {
    pathToType.put(pathAsString(path), typeName);
  }

  private String pathAsString(List<String> path) {
    return path.stream().collect(Collectors.joining("/", "/", ""));
  }

  String getTypeName(List<String> path) {
    return Optional.ofNullable(pathToType.get(pathAsString(path)))
        .orElseThrow(
            () ->
                new NullPointerException(
                    format("Can't find type for path %s", pathAsString(path))))
        .orElse("null");
  }

  void registerAction(
      AuditLogItem.OperationType operationType, String actionName, Map<String, Object> arguments) {
    operations.add(new AuditLogItem.Operation(operationType, actionName, arguments));
  }

  List<AuditLogItem.Operation> getOperations() {
    return operations;
  }

  void registerVariables(Map<String, Object> variables) {
    this.variables.putAll(variables);
  }

  Map<String, Object> registeredVariables() {
    return new HashMap<>(variables);
  }

  void registerOriginalFieldName(List<String> path, String originalFieldName) {
    pathToOriginalFieldName.put(pathAsString(path), originalFieldName);
  }

  String getOriginalFieldName(List<String> path) {
    return Optional.ofNullable(pathToOriginalFieldName.get(pathAsString(path)))
        .orElseThrow(
            () ->
                new NullPointerException(
                    format("Can't find original field name for path %s", pathAsString(path))));
  }

  void registerAdditionalField(List<String> path, CompletableFuture<ResultData> resultData) {
    additionalFields.add(new AdditionalField(path, resultData));
  }

  List<CompletableFuture<ResultData>> getAdditionalFields(List<String> path) {
    return additionalFields.stream()
        .filter(it -> pathAsString(it.path).equals(pathAsString(path)))
        .map(it -> it.resultData)
        .collect(Collectors.toList());
  }

  private static class AdditionalField {
    private final List<String> path;
    private final CompletableFuture<ResultData> resultData;

    private AdditionalField(List<String> path, CompletableFuture<ResultData> resultData) {
      this.path = path;
      this.resultData = resultData;
    }
  }
}
