package org.mifosng.platform.api.errorhandling;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.mifosng.platform.api.data.ApiGlobalErrorResponse;
import org.mifosng.platform.infrastructure.InvalidTenantIdentiferException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * An {@link ExceptionMapper} to map {@link InvalidTenantIdentiferException} thrown by platform during authentication into a HTTP API friendly format.
 * 
 * The {@link InvalidTenantIdentiferException} is thrown by spring security on platform when a request contains an invalid tenant identifier.
 */
@Provider
@Component
@Scope("singleton")
public class InvalidTenantIdentifierExceptionMapper implements ExceptionMapper<InvalidTenantIdentiferException> {

	@Override
	public Response toResponse(final InvalidTenantIdentiferException e) {
		return Response.status(Status.UNAUTHORIZED).entity(ApiGlobalErrorResponse.invalidTenantIdentifier()).type(MediaType.APPLICATION_JSON).build();
	}
}