package pl.allegro.tech.graphql.auditlog.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public interface ResultData {

  String getType();

  String getName();

  ResultData anonymize();

  class ScalarResultData implements
      ResultData {

    private final String name;
    private final String typeName;
    private final String value;

    public ScalarResultData(String name, String typeName, String value) {
      this.name = name;
      this.typeName = typeName;
      this.value = value;
    }

    @Override
    public String getType() {
      return "scalar";
    }

    @Override
    public String getName() {
      return name;
    }

    public String getTypeName() {
      return typeName;
    }

    public String getValue() {
      return value;
    }

    @Override
    public AnonymizedResultData anonymize() {
      return new AnonymizedResultData(name, typeName);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ScalarResultData)) {
        return false;
      }
      ScalarResultData that = (ScalarResultData) o;
      return name.equals(that.name) && typeName.equals(that.typeName) && value.equals(that.value);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, typeName, value);
    }
  }

  class ObjectResultData
      implements ResultData {

    private final String name;
    private final String typeName;
    private final List<? extends ResultData> fields;

    public ObjectResultData(String name, String typeName,
        List<? extends ResultData> fields) {
      this.name = name;
      this.typeName = typeName;
      this.fields = fields;
    }

    @Override
    public String getType() {
      return "object";
    }

    @Override
    public String getName() {
      return name;
    }

    public String getTypeName() {
      return typeName;
    }

    public List<? extends ResultData> getFields() {
      return fields;
    }

    @Override
    public ObjectResultData anonymize() {
      var objectStream = fields.stream().map(ResultData::anonymize).collect(Collectors.toList());
      return new ObjectResultData(name, typeName, objectStream);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ObjectResultData)) {
        return false;
      }
      ObjectResultData that = (ObjectResultData) o;
      return Objects.equals(name, that.name) && Objects
          .equals(typeName, that.typeName) && Objects.equals(fields, that.fields);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, typeName, fields);
    }
  }

  class ArrayResultData implements
      ResultData {

    private final String name;
    private final List<? extends ResultData> items;

    public ArrayResultData(String name,
        List<? extends ResultData> items) {
      this.name = name;
      this.items = items;
    }

    @Override
    public String getType() {
      return "array";
    }

    @Override
    public String getName() {
      return name;
    }

    public List<? extends ResultData> getItems() {
      return items;
    }

    @Override
    public ArrayResultData anonymize() {
      var anonymizedItems = items.stream().map(ResultData::anonymize).collect(Collectors.toList());
      return new ArrayResultData(name, anonymizedItems);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof ArrayResultData)) {
        return false;
      }
      ArrayResultData that = (ArrayResultData) o;
      return Objects.equals(name, that.name) && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, items);
    }
  }

  class NullResultData implements ResultData {

    private final String name;

    public NullResultData(String name) {
      this.name = name;
    }

    @Override
    public String getType() {
      return "null";
    }

    public String getTypeName() {
      return "null";
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public AnonymizedResultData anonymize() {
      return new AnonymizedResultData(name, getTypeName());
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof NullResultData)) {
        return false;
      }
      NullResultData that = (NullResultData) o;
      return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name);
    }
  }

  class AnonymizedResultData implements
      ResultData {

    private final String name;
    private final String typeName;

    public AnonymizedResultData(String name, String typeName) {
      this.name = name;
      this.typeName = typeName;
    }

    @Override
    public String getType() {
      return "anonymized";
    }

    @Override
    public String getName() {
      return name;
    }

    public String getTypeName() {
      return typeName;
    }

    @Override
    public AnonymizedResultData anonymize() {
      return this;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof AnonymizedResultData)) {
        return false;
      }
      AnonymizedResultData that = (AnonymizedResultData) o;
      return Objects.equals(name, that.name) && Objects
          .equals(typeName, that.typeName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, typeName);
    }
  }

  class IntrospectionResultData implements
      ResultData {

    private final String name;
    private final String typeName;

    public IntrospectionResultData(String name, String typeName) {
      this.name = name;
      this.typeName = typeName;
    }

    @Override
    public String getType() {
      return "introspection";
    }

    @Override
    public String getName() {
      return name;
    }

    public String getTypeName() {
      return typeName;
    }

    @Override
    public AnonymizedResultData anonymize() {
      return new AnonymizedResultData(name, typeName);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof IntrospectionResultData)) {
        return false;
      }
      IntrospectionResultData that = (IntrospectionResultData) o;
      return Objects.equals(name, that.name) && Objects
          .equals(typeName, that.typeName);
    }

    @Override
    public int hashCode() {
      return Objects.hash(name, typeName);
    }
  }
}
