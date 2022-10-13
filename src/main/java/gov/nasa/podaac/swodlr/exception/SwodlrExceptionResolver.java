package gov.nasa.podaac.swodlr.exception;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.validation.ConstraintViolationException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionSystemException;

@Component
public class SwodlrExceptionResolver extends DataFetcherExceptionResolverAdapter {
  @Override
  public List<GraphQLError> resolveToMultipleErrors(Throwable ex, DataFetchingEnvironment env) {
    if (ex instanceof SwodlrException) {
      return Collections.singletonList(
        GraphqlErrorBuilder.newError(env).message(ex.getMessage()).build()
      );
    } else if (ex instanceof TransactionSystemException) {
      var rootThrowable = ExceptionUtils.getRootCause(ex);
      if (!(rootThrowable instanceof ConstraintViolationException constraintViolationException)) {
        return null;
      }

      List<GraphQLError> errors = new ArrayList<>();
      var violations = constraintViolationException.getConstraintViolations();

      for (var violation : violations) {
        var error = GraphqlErrorBuilder
            .newError(env)
            .errorType(ErrorType.ValidationError)
            .extensions(Collections.singletonMap(
                "property",
                violation.getPropertyPath().toString()
            ))
            .message(violation.getMessage())
            .build();

        errors.add(error);
      }

      return Collections.unmodifiableList(errors);
    } else {
      return null;
    }
  }
}
