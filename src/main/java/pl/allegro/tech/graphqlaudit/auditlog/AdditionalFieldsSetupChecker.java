package pl.allegro.tech.graphqlaudit.auditlog;

public interface AdditionalFieldsSetupChecker {
  boolean shouldBeAdditional(String objectName, String fieldName);
}
