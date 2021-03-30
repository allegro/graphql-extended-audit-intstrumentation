package pl.allegro.tech.graphql.auditlog;

public interface AnonymizedFieldsSetupPreconditions {
  boolean shouldAnonymize(String objectName, String fieldName);
}
