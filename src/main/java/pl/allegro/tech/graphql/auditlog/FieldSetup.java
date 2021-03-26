package pl.allegro.tech.graphql.auditlog;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FieldSetup {
  private final String objectName;
  private final String fieldName;

  @JsonCreator
  FieldSetup(
      @JsonProperty("objectName") String objectName,
      @JsonProperty("fieldName") String fieldName
  ) {
    this.objectName = objectName;
    this.fieldName = fieldName;
  }

  public String objectName() {
    return objectName;
  }

  public String fieldName() {
    return fieldName;
  }
}
