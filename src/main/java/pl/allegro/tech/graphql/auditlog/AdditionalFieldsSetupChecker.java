package pl.allegro.tech.graphql.auditlog;

public interface AdditionalFieldsSetupChecker {
  boolean shouldBeAdditional(String objectName, String fieldName);
}
