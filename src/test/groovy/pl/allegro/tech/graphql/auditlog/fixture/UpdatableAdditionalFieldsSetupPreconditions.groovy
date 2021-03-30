package pl.allegro.tech.graphql.auditlog.fixture

import pl.allegro.tech.graphql.auditlog.AdditionalFieldsSetupPreconditions
import pl.allegro.tech.graphql.auditlog.FieldSetup

class UpdatableAdditionalFieldsSetupPreconditions implements AdditionalFieldsSetupPreconditions {
    private final List<FieldSetup> overriddenAdditionalFields

    UpdatableAdditionalFieldsSetupPreconditions() {
        overriddenAdditionalFields = new ArrayList<>()
    }

    @Override
    boolean shouldBeAdditional(String objectName, String fieldName) {
        return overriddenAdditionalFields.stream()
                .filter(json -> json.objectName().equals(objectName))
                .anyMatch(json -> json.fieldName().equals(fieldName))
    }


    void withAdditionalField(String objectName, String fieldName) {
        overriddenAdditionalFields.add(new FieldSetup(objectName, fieldName))
    }

    void reset() {
        overriddenAdditionalFields.removeAll { true }
    }
}
