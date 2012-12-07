package org.mifosplatform.portfolio.client.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.data.CommandSourceData;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.commands.service.PortfolioCommandsReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/clients")
@Component
@Scope("singleton")
public class ClientsApiResource {

    private final Set<String> CLIENT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "officeId", "officeName", "externalId",
            "firstname", "lastname", "joinedDate", "displayName", "clientOrBusinessName", "allowedOffices", "imagePresent"));

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioCommandsReadPlatformService commandSourceReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientsApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final DefaultToApiJsonSerializer<ClientData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper, final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioCommandsReadPlatformService commandsReadPlatformService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.clientReadPlatformService = readPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.apiDataConversionService = apiDataConversionService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.commandSourceReadPlatformService = commandsReadPlatformService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllIndividualClients(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") final String sqlSearch,
            @QueryParam("officeId") final Integer officeId, @QueryParam("externalId") final String externalId,
            @QueryParam("displayName") final String displayName, @QueryParam("firstName") final String firstName,
            @QueryParam("lastName") final String lastName, @QueryParam("underHierarchy") final String hierarchy) {

        context.authenticatedUser().validateHasReadPermission("CLIENT");

        final String extraCriteria = getClientCriteria(sqlSearch, officeId, externalId, displayName, firstName, lastName, hierarchy);

        final Collection<ClientData> clients = this.clientReadPlatformService.retrieveAllIndividualClients(extraCriteria);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, clients, CLIENT_DATA_PARAMETERS);
    }

    private String getClientCriteria(String sqlSearch, Integer officeId, String externalId, String displayName, String firstName,
            String lastName, String hierarchy) {

        String extraCriteria = "";

        if (sqlSearch != null) {
            extraCriteria = " and (" + sqlSearch + ")";
        }

        if (officeId != null) {
            extraCriteria += " and office_id = " + officeId;
        }

        if (externalId != null) {
            extraCriteria += " and external_id like " + ApiParameterHelper.sqlEncodeString(externalId);
        }

        if (displayName != null) {
            extraCriteria += " and concat(ifnull(firstname, ''), if(firstname > '',' ', '') , ifnull(lastname, '')) like "
                    + ApiParameterHelper.sqlEncodeString(displayName);
        }

        if (firstName != null) {
            extraCriteria += " and firstname like " + ApiParameterHelper.sqlEncodeString(firstName);
        }

        if (lastName != null) {
            extraCriteria += " and lastname like " + ApiParameterHelper.sqlEncodeString(lastName);
        }

        if (hierarchy != null) {
            extraCriteria += " and o.hierarchy like " + ApiParameterHelper.sqlEncodeString(hierarchy + "%");
        }

        if (StringUtils.isNotBlank(extraCriteria)) {
            extraCriteria = extraCriteria.substring(4);
        }

        return extraCriteria;
    }

    @GET
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClient(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("CLIENT");

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);
        if (settings.isTemplate()) {
            final List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup());
            clientData = ClientData.templateOnTop(clientData, allowedOffices);
        }
        if (settings.isCommandIdPassed()) {
            clientData = handleRequestToIntegrateProposedChangesFromCommand(clientId, settings.getCommandId(), clientData);
        }

        // pick up possibility of changes by default - might push down a layer
        // into the retrieve client method
        final Collection<ClientData> dataChanges = retrieveAllUnprocessedDataChanges(clientId);
        clientData = ClientData.integrateChanges(clientData, clientData.currentChange(), dataChanges);

        return this.toApiJsonSerializer.serialize(settings, clientData, CLIENT_DATA_PARAMETERS);
    }

    private ClientData handleRequestToIntegrateProposedChangesFromCommand(final Long clientId, final Long commandId,
            final ClientData clientData) {
        final CommandSourceData entry = this.commandSourceReadPlatformService.retrieveById(commandId);
        final ClientData currentChange = this.apiDataConversionService.convertInternalJsonFormatToClientDataChange(clientId, entry.json());

        final Collection<ClientData> dataChanges = null; // retrieveAllUnprocessedDataChanges(clientId);
        return ClientData.integrateChanges(clientData, currentChange, dataChanges);
    }

    private Collection<ClientData> retrieveAllUnprocessedDataChanges(final Long clientId) {
        Collection<ClientData> dataChanges = new ArrayList<ClientData>();

        Collection<CommandSourceData> unprocessedChanges = this.commandSourceReadPlatformService.retrieveUnprocessChangesByResourceId(
                "clients", clientId);
        for (CommandSourceData commandSourceData : unprocessedChanges) {
            ClientData change = this.apiDataConversionService.convertInternalJsonFormatToClientDataChange(clientId,
                    commandSourceData.json());
            dataChanges.add(change);
        }

        if (dataChanges.isEmpty()) {
            dataChanges = null;
        }

        return dataChanges;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newClientDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("CLIENT");

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        ClientData clientData = this.clientReadPlatformService.retrieveNewClientDetails();
        if (settings.isCommandIdPassed()) {
            clientData = handleRequestToIntegrateProposedChangesFromCommand(null, settings.getCommandId(), clientData);
        }

        return this.toApiJsonSerializer.serialize(settings, clientData, CLIENT_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createClient(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER", "CREATE_CLIENT");
        context.authenticatedUser().validateHasPermissionTo("CREATE_CLIENT", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "clients", null,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClient(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER", "UPDATE_CLIENT");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_CLIENT", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "clients", clientId,
                apiRequestBodyAsJson);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteClient(@PathParam("clientId") final Long clientId) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER", "DELETE_CLIENT");
        context.authenticatedUser().validateHasPermissionTo("DELETE_CLIENT", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", "clients", clientId, "{}");

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{clientId}/loans")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClientAccount(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("CLIENT");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final ClientAccountSummaryCollectionData clientAccount = this.clientReadPlatformService.retrieveClientAccountDetails(clientId);

        return this.apiJsonSerializerService.serializeClientAccountSummaryCollectionDataToJson(prettyPrint, responseParameters,
                clientAccount);
    }
}