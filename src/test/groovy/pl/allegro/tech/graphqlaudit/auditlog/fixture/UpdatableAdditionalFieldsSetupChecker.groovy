package pl.allegro.tech.graphqlaudit.auditlog.fixture

import pl.allegro.tech.graphqlaudit.auditlog.AdditionalFieldsSetupChecker
import pl.allegro.tech.graphqlaudit.auditlog.FieldSetup

class UpdatableAdditionalFieldsSetupChecker implements AdditionalFieldsSetupChecker {
    private final List<FieldSetup> overriddenAdditionalFields

    UpdatableAdditionalFieldsSetupChecker() {
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
