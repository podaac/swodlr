package gov.nasa.podaac.swodlr.exception;

import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;

@Component
public class SwodlrExceptionResolver extends DataFetcherExceptionResolverAdapter {
    @Override
    public GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (!(ex instanceof SwodlrException))
		    return null;

        return GraphqlErrorBuilder.newError(env).message(ex.getMessage()).build();
	}
}
