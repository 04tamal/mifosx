package org.mifosplatform.infrastructure.codes.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.codes.data.CodeData;
import org.mifosplatform.infrastructure.codes.service.CodeReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/codes")
@Component
@Scope("singleton")
public class CodesApiResource {

    private CodeReadPlatformService readPlatformService;
    private PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final PlatformSecurityContext context;

    private final String resourceNameForPermissions = "CODE";

    @Autowired
    public CodesApiResource(final PlatformSecurityContext context, final CodeReadPlatformService readPlatformService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveCodes(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<CodeData> codes = this.readPlatformService.retrieveAllCodes();

        return this.apiJsonSerializerService.serializeCodeDataToJson(prettyPrint, responseParameters, codes);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createCode(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "CREATE_CODE");
        context.authenticatedUser().validateHasPermissionTo("CREATE_CODE", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "codes", null,
                apiRequestBodyAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @GET
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreiveOffice(@PathParam("codeId") final Long codeId, @Context final UriInfo uriInfo) {

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final CodeData code = this.readPlatformService.retrieveCode(codeId);

        return this.apiJsonSerializerService.serializeCodeDataToJson(prettyPrint, responseParameters, code);
    }

    @PUT
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateCode(@PathParam("codeId") final Long codeId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "UPDATE_CODE");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_CODE", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "codes", codeId,
                apiRequestBodyAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @DELETE
    @Path("{codeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteCodeApplication(@PathParam("codeId") final Long codeId) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "DELETE_CODE");
        context.authenticatedUser().validateHasPermissionTo("DELETE_CODE", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", "codes", codeId, "{}");

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}