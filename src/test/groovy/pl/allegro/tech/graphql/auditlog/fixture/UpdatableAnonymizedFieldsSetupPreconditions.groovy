package pl.allegro.tech.graphql.auditlog.fixture

import pl.allegro.tech.graphql.auditlog.AnonymizedFieldsSetupPreconditions
import pl.allegro.tech.graphql.auditlog.FieldSetup

class UpdatableAnonymizedFieldsSetupPreconditions implements AnonymizedFieldsSetupPreconditions {
    private final List<FieldSetup> overriddenAnonymizedFields

    UpdatableAnonymizedFieldsSetupPreconditions() {
        overriddenAnonymizedFields = new ArrayList<>()
    }

    @Override
    boolean shouldAnonymize(String objectName, String fieldName) {
        return overriddenAnonymizedFields.stream()
                .filter(json -> json.objectName().equals(objectName))
                .anyMatch(json -> json.fieldName().equals(fieldName))
    }

    List<FieldSetup> anonymizedFieldsSetup() {
        return overriddenAnonymizedFields
    }

    void withAnonymizedField(String objectName, String fieldName) {
        overriddenAnonymizedFields.add(new FieldSetup(objectName, fieldName))
    }

    void reset() {
        overriddenAnonymizedFields.removeAll { true }
    }
}
