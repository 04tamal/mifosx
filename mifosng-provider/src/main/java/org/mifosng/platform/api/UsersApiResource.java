package org.mifosng.platform.api;

import java.util.ArrayList;
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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.AppUserData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.OfficeLookup;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.service.AppUserReadPlatformService;
import org.mifosng.platform.user.service.AppUserWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/users")
@Component
@Scope("singleton")
public class UsersApiResource {

    @Autowired
    private AppUserReadPlatformService appUserReadPlatformService;

    @Autowired
    private OfficeReadPlatformService officeReadPlatformService;

    @Autowired
    private AppUserWritePlatformService appUserWritePlatformService;

    @Autowired
    private PortfolioApiDataConversionService apiDataConversionService;

    @Autowired
    private PortfolioApiJsonSerializerService apiJsonSerializerService;

    @Autowired
    private PlatformSecurityContext context;
    
    private final String resourceNameForPermissions = "USER";

    @GET
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveUsers(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<AppUserData> users = this.appUserReadPlatformService.retrieveAllUsers();

        return this.apiJsonSerializerService.serializeAppUserDataToJson(prettyPrint, responseParameters, users);
    }

    @GET
    @Path("{userId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveUser(@PathParam("userId") final Long userId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

        AppUserData user = this.appUserReadPlatformService.retrieveUser(userId);
        if (template) {
            List<OfficeLookup> offices = new ArrayList<OfficeLookup>(this.officeReadPlatformService.retrieveAllOfficesForLookup());
            user = new AppUserData(user, offices);
        }

        return this.apiJsonSerializerService.serializeAppUserDataToJson(prettyPrint, responseParameters, user);
    }

    @GET
    @Path("template")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String newUserDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final AppUserData user = this.appUserReadPlatformService.retrieveNewUserDetails();

        return this.apiJsonSerializerService.serializeAppUserDataToJson(prettyPrint, responseParameters, user);
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createUser(final String jsonRequestBody) {

        final UserCommand command = this.apiDataConversionService.convertJsonToUserCommand(null, jsonRequestBody);

        final Long userId = this.appUserWritePlatformService.createUser(command);
        return Response.ok().entity(new EntityIdentifier(userId)).build();
    }

    @DELETE
    @Path("{userId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteUser(@PathParam("userId") final Long userId) {

        this.appUserWritePlatformService.deleteUser(userId);

        return Response.ok(new EntityIdentifier(userId)).build();
    }

    @PUT
    @Path("{userId}")
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateUser(@PathParam("userId") final Long userId, final String jsonRequestBody) {

        final UserCommand command = this.apiDataConversionService.convertJsonToUserCommand(userId, jsonRequestBody);

        final AppUser loggedInUser = context.authenticatedUser();

        if (loggedInUser.hasIdOf(userId)) {
            this.appUserWritePlatformService.updateUsersOwnAccountDetails(command);
        } else {
            this.appUserWritePlatformService.updateUser(command);
        }

        return Response.ok().entity(new EntityIdentifier(userId)).build();
    }
}