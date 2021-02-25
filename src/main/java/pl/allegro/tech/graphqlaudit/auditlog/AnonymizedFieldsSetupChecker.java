package pl.allegro.tech.graphqlaudit.auditlog;

public interface AnonymizedFieldsSetupChecker {
  boolean shouldAnonymize(String objectName, String fieldName);
}
