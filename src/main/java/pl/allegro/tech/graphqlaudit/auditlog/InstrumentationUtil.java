package pl.allegro.tech.graphqlaudit.auditlog;

import graphql.execution.instrumentation.parameters.InstrumentationFieldParameters;
import graphql.schema.GraphQLNonNull;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;

class InstrumentationUtil {

  private InstrumentationUtil() { }

  static GraphQLObjectType extractObjectTypeFromFieldParameters(
      InstrumentationFieldParameters parameters) {
    GraphQLOutputType objectType = parameters.getExecutionStepInfo().getParent().getType();
    if (objectType instanceof GraphQLNonNull) {
      GraphQLType wrappedType = ((GraphQLNonNull) objectType).getWrappedType();
      return (GraphQLObjectType) wrappedType;
    } else if (objectType instanceof GraphQLObjectType) {
      return (GraphQLObjectType) objectType;
    } else {
      throw new IllegalStateException(
          "Unknown graphql type " + objectType.getClass().getSimpleName());
    }
  }
}
