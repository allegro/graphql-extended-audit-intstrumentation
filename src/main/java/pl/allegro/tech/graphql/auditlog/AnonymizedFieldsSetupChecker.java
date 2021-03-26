package pl.allegro.tech.graphql.auditlog;

public interface AnonymizedFieldsSetupChecker {
  boolean shouldAnonymize(String objectName, String fieldName);
}
