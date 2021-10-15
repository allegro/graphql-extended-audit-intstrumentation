package pl.allegro.tech.graphql.auditlog;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import pl.allegro.tech.graphql.auditlog.model.ResultData;
import pl.allegro.tech.graphql.auditlog.model.UserId;

public class AuditLogItem {

  private final  String id;
  private final List<Operation> operations;
  private final List<ResultData> resultData;
  private final List<Map<String, Object>> errors;
  private final UserId userId;
  private final Instant executionDate;

  AuditLogItem(String id,
      List<Operation> operations,
      List<ResultData> resultData,
      List<Map<String, Object>> errors, UserId userId, Instant executionDate) {
    checkArgument(
        operations.size() == resultData.size(),
        format(
            "Size of operations (%s) and resultData (%s) data have to be equal.",
            operations.size(),
            resultData.size()
        )

    );
    this.id = id;
    this.operations = operations;
    this.resultData = resultData;
    this.errors = errors;
    this.userId = userId;
    this.executionDate = executionDate;
  }

  public String id() {
    return id;
  }

  public List<Operation> operations() {
    return operations;
  }

  public List<ResultData> resultData() {
    return resultData;
  }

  public List<Map<String, Object>> errors() {
    return errors;
  }

  public UserId userId() {
    return userId;
  }

  public Instant executionDate() {
    return executionDate;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AuditLogItem that = (AuditLogItem) o;
    return Objects.equals(id, that.id) && Objects
        .equals(operations, that.operations) && Objects.equals(resultData, that.resultData)
        && Objects.equals(errors, that.errors) && Objects
        .equals(userId, that.userId) && Objects.equals(executionDate, that.executionDate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, operations, resultData, errors, userId, executionDate);
  }

  public static class Operation {

    private final OperationType operationType;
    private final String operationName;
    private final Map<String, Object> arguments;

    public Operation(OperationType operationType, String operationName,
        Map<String, Object> arguments) {
      this.operationType = operationType;
      this.operationName = operationName;
      this.arguments = arguments;
    }

    public OperationType operationType() {
      return operationType;
    }

    public String operationName() {
      return operationName;
    }

    public Map<String, Object> arguments() {
      return arguments;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Operation operation = (Operation) o;
      return operationType == operation.operationType && Objects
          .equals(operationName, operation.operationName) && Objects
          .equals(arguments, operation.arguments);
    }

    @Override
    public int hashCode() {
      return Objects.hash(operationType, operationName, arguments);
    }
  }

  public enum OperationType {
    MUTATION,
    QUERY
  }
}
