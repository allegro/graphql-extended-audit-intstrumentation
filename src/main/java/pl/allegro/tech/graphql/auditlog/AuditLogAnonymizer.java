package pl.allegro.tech.graphql.auditlog;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import java.util.UUID;
import java.util.stream.Collectors;
import pl.allegro.tech.graphql.auditlog.model.ResultData;
import pl.allegro.tech.graphql.auditlog.model.ResultData.ArrayResultData;
import pl.allegro.tech.graphql.auditlog.model.ResultData.ObjectResultData;

class AuditLogAnonymizer {

  private final AnonymizedFieldsSetupChecker anonymizedFieldsSetupChecker;

  public AuditLogAnonymizer(AnonymizedFieldsSetupChecker anonymizedFieldsSetupChecker) {
    this.anonymizedFieldsSetupChecker = requireNonNull(anonymizedFieldsSetupChecker);
  }

  AuditLogItem anonymizeActionLogItem(AuditLogItem actionLogItem) {
    var resultData =
        actionLogItem.resultData().stream()
            .map(this::resultDataItemValue)
            .collect(Collectors.toList());

    return new AuditLogItem(
        UUID.randomUUID().toString(),
        actionLogItem.operations(),
        resultData,
        actionLogItem.errors(),
        actionLogItem.userId(),
        actionLogItem.executionDate()
    );
  }

  private ResultData resultDataItemValue(ResultData resultData, String parentName) {

    if (anonymizedFieldsSetupChecker.shouldAnonymize(resultData.getName(), parentName)) {
      return resultData.anonymize();
    }
    if (resultData instanceof ResultData.ObjectResultData) {
      var fields =
          ((ObjectResultData) resultData).getFields().stream()
              .map(it -> resultDataItemValue(it, ((ObjectResultData) resultData).getTypeName()))
              .collect(Collectors.toList());
      return new ResultData.ObjectResultData(
          resultData.getName(), ((ObjectResultData) resultData).getTypeName(), fields);
    } else if (resultData instanceof ResultData.ScalarResultData
        || resultData instanceof ResultData.NullResultData
        || resultData instanceof ResultData.IntrospectionResultData) {
      return resultData;
    } else if (resultData instanceof ResultData.ArrayResultData) {
      return new ResultData.ArrayResultData(
          resultData.getName(),
          ((ArrayResultData) resultData).getItems().stream()
              .map(this::resultDataItemValue)
              .collect(Collectors.toList()));
    } else {
      throw new IllegalStateException(
          format(
              "Can't handle %s type in result data type",
              resultData.getClass().getSimpleName())
      );
    }
  }

  private ResultData resultDataItemValue(ResultData resultData) {
    return resultDataItemValue(resultData, "uselessParentName");
  }
}
