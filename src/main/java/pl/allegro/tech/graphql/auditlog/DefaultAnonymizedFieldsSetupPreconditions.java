package pl.allegro.tech.graphql.auditlog;

import java.util.List;

class DefaultAnonymizedFieldsSetupPreconditions implements AnonymizedFieldsSetupPreconditions {

  private final List<FieldSetup> fieldsSetup;

  public DefaultAnonymizedFieldsSetupPreconditions(List<FieldSetup> fieldsSetup) {
    this.fieldsSetup = fieldsSetup;
  }

  @Override
  public boolean shouldAnonymize(String objectName, String fieldName) {
    return fieldsSetup.stream()
        .filter(json -> json.objectName().equals(objectName))
        .anyMatch(json -> json.fieldName().equals(fieldName));
  }
}
