package org.mifosplatform.organisation.office.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.serialization.CommandSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.organisation.office.data.OfficeTransactionData;
import org.mifosplatform.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/officetransactions")
@Component
@Scope("singleton")
public class OfficeTransactionsApiResource {
    
    private final String resourceNameForReadPermissions = "OFFICE";
    private final String resourceNameForPermissions = "OFFICETRANSACTION";
    
    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService readPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final CommandSerializer commandSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public OfficeTransactionsApiResource(final PlatformSecurityContext context, 
            final OfficeReadPlatformService readPlatformService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final CommandSerializer commandSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOfficeTransactions(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForReadPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<OfficeTransactionData> officeTransactions = this.readPlatformService.retrieveAllOfficeTransactions();

        return this.apiJsonSerializerService.serializeOfficeTransactionDataToJson(prettyPrint, responseParameters, officeTransactions);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String newOfficeTransactionDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForReadPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final OfficeTransactionData officeTransactionData = this.readPlatformService.retrieveNewOfficeTransactionDetails();

        return this.apiJsonSerializerService.serializeOfficeTransactionDataToJson(prettyPrint, responseParameters, officeTransactionData);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String transferMoneyFrom(final String apiRequestBodyAsJson) {

        // TODO - complete permissions for office transactions when more
        // functionality add or it is replaced by simple accounting equivalent (JPW)
        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);
        
        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER", "CREATE_OFFICETRANSACTION");
        context.authenticatedUser().validateHasPermissionTo("CREATE_OFFICETRANSACTION", allowedPermissions);

        final BranchMoneyTransferCommand command = this.apiDataConversionService.convertApiRequestJsonToBranchMoneyTransferCommand(apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);
        
        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "officetransactions", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }
}