package org.mifosplatform.portfolio.client.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.data.CommandSourceData;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.commands.service.PortfolioCommandsReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiConstants;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.NoteCommand;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.data.NoteData;
import org.mifosplatform.portfolio.client.exception.ImageNotFoundException;
import org.mifosplatform.portfolio.client.service.ClientReadPlatformService;
import org.mifosplatform.portfolio.client.service.ClientWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.lowagie.text.pdf.codec.Base64;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataBodyPart;
import com.sun.jersey.multipart.FormDataParam;

@Path("/clients")
@Component
@Scope("singleton")
public class ClientsApiResource {

    private final static Logger logger = LoggerFactory.getLogger(ClientsApiResource.class);

    private final PlatformSecurityContext context;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ClientWritePlatformService clientWritePlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final CommandSerializer commandSerializerService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioCommandsReadPlatformService commandSourceReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public ClientsApiResource(final PlatformSecurityContext context, final ClientReadPlatformService readPlatformService,
            final ClientWritePlatformService clientWritePlatformService, final OfficeReadPlatformService officeReadPlatformService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final CommandSerializer commandSerializerService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioCommandsReadPlatformService commandsReadPlatformService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.clientReadPlatformService = readPlatformService;
        this.clientWritePlatformService = clientWritePlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
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

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<ClientData> clients = this.clientReadPlatformService.retrieveAllIndividualClients(extraCriteria);

        return this.apiJsonSerializerService.serializeClientDataToJson(prettyPrint, responseParameters, clients);
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

        logger.info("extraCriteria; " + extraCriteria);

        return extraCriteria;
    }

    @GET
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClient(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("CLIENT");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        final Long commandId = ApiParameterHelper.commandId(uriInfo.getQueryParameters());

        ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);
        if (template) {
            final List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>(officeReadPlatformService.retrieveAllOfficesForLookup());
            clientData = ClientData.templateOnTop(clientData, allowedOffices);
        }
        if (commandId != null) {
            clientData = handleRequestToIntegrateProposedChangesFromCommand(clientId, commandId, clientData);
        }

        // pick up possibility of changes by default - might push down a layer
        // into the retrieve client method
        final Collection<ClientData> dataChanges = retrieveAllUnprocessedDataChanges(clientId);
        clientData = ClientData.integrateChanges(clientData, clientData.currentChange(), dataChanges);

        return this.apiJsonSerializerService.serializeClientDataToJson(prettyPrint, responseParameters, clientData);
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

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
        final Long makerCheckerId = ApiParameterHelper.commandId(uriInfo.getQueryParameters());

        ClientData clientData = this.clientReadPlatformService.retrieveNewClientDetails();
        if (makerCheckerId != null) {
            clientData = handleRequestToIntegrateProposedChangesFromCommand(null, makerCheckerId, clientData);
        }

        return this.apiJsonSerializerService.serializeClientDataToJson(prettyPrint, responseParameters, clientData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createClient(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER", "CREATE_CLIENT");
        context.authenticatedUser().validateHasPermissionTo("CREATE_CLIENT", allowedPermissions);

        final ClientCommand command = this.apiDataConversionService.convertApiRequestJsonToClientCommand(null, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "clients", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @PUT
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateClient(@PathParam("clientId") final Long clientId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER", "UPDATE_CLIENT");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_CLIENT", allowedPermissions);

        final ClientCommand command = this.apiDataConversionService.convertApiRequestJsonToClientCommand(clientId, apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "clients", clientId,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @DELETE
    @Path("{clientId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteClient(@PathParam("clientId") final Long clientId) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "PORTFOLIO_MANAGEMENT_SUPER_USER", "DELETE_CLIENT");
        context.authenticatedUser().validateHasPermissionTo("DELETE_CLIENT", allowedPermissions);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("DELETE", "clients", clientId, "{}");

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
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

    @GET
    @Path("{clientId}/notes")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllClientNotes(@PathParam("clientId") final Long clientId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("CLIENTNOTE");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<NoteData> notes = this.clientReadPlatformService.retrieveAllClientNotes(clientId);

        return this.apiJsonSerializerService.serializeNoteDataToJson(prettyPrint, responseParameters, notes);
    }

    @POST
    @Path("{clientId}/notes")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNewClientNote(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {

        final NoteCommand command = this.apiDataConversionService.convertJsonToNoteCommand(null, clientId, jsonRequestBody);

        final EntityIdentifier identifier = this.clientWritePlatformService.addClientNote(command);

        return Response.ok().entity(identifier).build();
    }

    @GET
    @Path("{clientId}/notes/{noteId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveClientNote(@PathParam("clientId") final Long clientId, @PathParam("noteId") final Long noteId,
            @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission("CLIENTNOTE");

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final NoteData note = this.clientReadPlatformService.retrieveClientNote(clientId, noteId);

        return this.apiJsonSerializerService.serializeNoteDataToJson(prettyPrint, responseParameters, note);
    }

    @PUT
    @Path("{clientId}/notes/{noteId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateClientNote(@PathParam("clientId") final Long clientId, @PathParam("noteId") final Long noteId,
            final String jsonRequestBody) {

        final NoteCommand command = this.apiDataConversionService.convertJsonToNoteCommand(noteId, clientId, jsonRequestBody);

        final EntityIdentifier identifier = this.clientWritePlatformService.updateNote(command);

        return Response.ok().entity(identifier).build();
    }

    /**
     * Upload images through multi-part form upload
     */
    @POST
    @Path("{clientId}/image")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNewClientImage(@PathParam("clientId") final Long clientId, @HeaderParam("Content-Length") Long fileSize,
            @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") FormDataBodyPart bodyPart) {

        // TODO: vishwas might need more advances validation (like reading magic
        // number) for handling malicious clients
        // and clients not setting mime type
        FileUtils.validateClientImageNotEmpty(fileDetails.getFileName());
        FileUtils.validateImageMimeType(bodyPart.getMediaType().toString());
        FileUtils.validateFileSizeWithinPermissibleRange(fileSize, fileDetails.getFileName(), ApiConstants.MAX_FILE_UPLOAD_SIZE_IN_MB);

        // logger.debug(bodyPart.getMediaType().toString());

        final EntityIdentifier entityIdentifier = this.clientWritePlatformService.saveOrUpdateClientImage(clientId,
                fileDetails.getFileName(), inputStream);

        return Response.ok().entity(entityIdentifier).build();
    }

    /**
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @POST
    @Path("{clientId}/image")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response addNewClientImage(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {

        final Base64EncodedImage base64EncodedImage = FileUtils.extractImageFromDataURL(jsonRequestBody);

        final EntityIdentifier entityIdentifier = this.clientWritePlatformService.saveOrUpdateClientImage(clientId, base64EncodedImage);

        return Response.ok().entity(entityIdentifier).build();
    }

    /**
     * Returns a base 64 encoded client image
     */
    @GET
    @Path("{clientId}/image")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response retrieveClientImage(@PathParam("clientId") final Long clientId) {

        context.authenticatedUser().validateHasReadPermission("CLIENTIMAGE");

        final ClientData clientData = this.clientReadPlatformService.retrieveIndividualClient(clientId);

        if (clientData.imageKeyDoesNotExist()) { throw new ImageNotFoundException("clients", clientId); }
        return Response.ok().entity(Base64.encodeFromFile(clientData.imageKey())).build();
    }

    /**
     * This method is added only for consistency with other URL patterns and for
     * maintaining consistency of usage of the HTTP "verb" at the client side
     */
    @PUT
    @Path("{clientId}/image")
    @Consumes({ MediaType.MULTIPART_FORM_DATA })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateClientImage(@PathParam("clientId") final Long clientId, @HeaderParam("Content-Length") Long fileSize,
            @FormDataParam("file") InputStream inputStream, @FormDataParam("file") FormDataContentDisposition fileDetails,
            @FormDataParam("file") FormDataBodyPart bodyPart) {
        return addNewClientImage(clientId, fileSize, inputStream, fileDetails, bodyPart);
    }

    @DELETE
    @Path("{clientId}/image")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response deleteClientImage(@PathParam("clientId") final Long clientId) {
        this.clientWritePlatformService.deleteClientImage(clientId);
        return Response.ok(new EntityIdentifier(clientId)).build();
    }

    /**
     * This method is added only for consistency with other URL patterns and for
     * maintaining consistency of usage of the HTTP "verb" at the client side
     * 
     * Upload image as a Data URL (essentially a base64 encoded stream)
     */
    @PUT
    @Path("{clientId}/image")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateClientImage(@PathParam("clientId") final Long clientId, final String jsonRequestBody) {
        return addNewClientImage(clientId, jsonRequestBody);
    }
}