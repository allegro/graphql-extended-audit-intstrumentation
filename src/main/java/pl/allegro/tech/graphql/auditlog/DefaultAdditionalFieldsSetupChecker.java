package pl.allegro.tech.graphql.auditlog;

import java.util.List;

class DefaultAdditionalFieldsSetupChecker implements AdditionalFieldsSetupChecker {
  private final List<FieldSetup> fieldsSetup;

  public DefaultAdditionalFieldsSetupChecker(List<FieldSetup> fieldsSetup) {
    this.fieldsSetup = fieldsSetup;
  }

  @Override
  public boolean shouldBeAdditional(String objectName, String fieldName) {
    return fieldsSetup.stream()
        .filter(json -> json.objectName().equals(objectName))
        .anyMatch(json -> json.fieldName().equals(fieldName));
  }
}
