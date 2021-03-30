package pl.allegro.tech.graphql.auditlog;

public interface AdditionalFieldsSetupPreconditions {
  boolean shouldBeAdditional(String objectName, String fieldName);
}
