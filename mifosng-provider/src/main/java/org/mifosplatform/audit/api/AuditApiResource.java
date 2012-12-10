package org.mifosplatform.audit.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/audit")
@Component
@Scope("singleton")
public class AuditApiResource {

	private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<String>(
			Arrays.asList("id", "apiOperation", "resource", "resourceId",
					"maker", "madeOnDate", "checker", "checkedOnDate",
					"commandAsJson"));
	private final String resourceNameForPermissions = "AUDIT";

	private final PlatformSecurityContext context;
	private final AuditReadPlatformService auditReadPlatformService;
	private final ApiRequestParameterHelper apiRequestParameterHelper;
	private final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializer;

	@Autowired
	public AuditApiResource(final PlatformSecurityContext context,
			final AuditReadPlatformService auditReadPlatformService,
			final ApiRequestParameterHelper apiRequestParameterHelper,
			final DefaultToApiJsonSerializer<AuditData> toApiJsonSerializer) {
		this.context = context;
		this.auditReadPlatformService = auditReadPlatformService;
		this.apiRequestParameterHelper = apiRequestParameterHelper;
		this.toApiJsonSerializer = toApiJsonSerializer;
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAuditEntries(@Context final UriInfo uriInfo,
			@QueryParam("apiOperation") final String apiOperation,
			@QueryParam("resource") final String resource,
			@QueryParam("resourceId") final Long resourceId) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);

        final String extraCriteria = getExtraCriteria(apiOperation, resource, resourceId);

		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());

		final Collection<AuditData> auditEntries = this.auditReadPlatformService
				.retrieveAuditEntries(extraCriteria, settings.isIncludeJson());

		return this.toApiJsonSerializer.serialize(settings, auditEntries,
				RESPONSE_DATA_PARAMETERS);
	}

	@GET
    @Path("{auditId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAuditEntry(@PathParam("auditId") final Long auditId, @Context final UriInfo uriInfo) {

		context.authenticatedUser().validateHasReadPermission(
				resourceNameForPermissions);


		final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper
				.process(uriInfo.getQueryParameters());

		final AuditData auditEntry = this.auditReadPlatformService
				.retrieveAuditEntry(auditId);

		return this.toApiJsonSerializer.serialize(settings, auditEntry,
				RESPONSE_DATA_PARAMETERS);
	}
	
	
    private String getExtraCriteria(String apiOperation, String resource, Long resourceId) {

        String extraCriteria = "";

        if (apiOperation != null) {
            extraCriteria += " and aud.api_operation like " + ApiParameterHelper.sqlEncodeString(apiOperation);
        }
        if (resource != null) {
            extraCriteria += " and aud.api_resource like " + ApiParameterHelper.sqlEncodeString(resource);
        }
        if (resourceId != null) {
            extraCriteria += " and aud.resource_id = " + resourceId;
        }


        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }

}