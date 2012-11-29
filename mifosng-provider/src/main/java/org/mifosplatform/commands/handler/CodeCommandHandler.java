package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandDeserializerService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandSerializerService;
import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.service.ChangeDetectionService;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.codes.service.CodeWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CodeCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final ChangeDetectionService changeDetectionService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final CodeWritePlatformService writePlatformService;
    
    @Autowired
    public CodeCommandHandler(final PlatformSecurityContext context, final ChangeDetectionService changeDetectionService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandDeserializerService commandDeserializerService,
            final CodeWritePlatformService writePlatformService) {
        this.context = context;
        this.changeDetectionService = changeDetectionService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandDeserializerService = commandDeserializerService;
        this.writePlatformService = writePlatformService;
    }

    /*
     * Used when users with 'create' capability create a command. If 'maker-checker' is not
     * enabled for this specific command then the 'creator' is also marked 'as the checker' and command
     * automatically is processed and changes state of system.
     */
    public CommandSource handle(final CommandSource commandSource, final String apiRequestBodyInJson) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        CommandSource commandSourceResult = commandSource.copy();

        final Long resourceId = commandSource.resourceId();
        final CodeCommand command = this.apiDataConversionService.convertApiRequestJsonToCodeCommand(resourceId, apiRequestBodyInJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCodeCommandToJson(command);
        commandSourceResult.updateJsonTo(commandSerializedAsJson);
        
        if (commandSource.isCreate()) {
            try {
                Long newResourceId = this.writePlatformService.createCode(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            try {
                final String jsonOfChangesOnly = this.changeDetectionService.detectChangesOnUpdate(commandSource.resourceName(), commandSource.resourceId(), commandSerializedAsJson);
                commandSourceResult.updateJsonTo(jsonOfChangesOnly);

                final CodeCommand changesOnly = this.commandDeserializerService.deserializeCodeCommand(resourceId, jsonOfChangesOnly, false);
                this.writePlatformService.updateCode(changesOnly);
                
                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isDelete()) {
            try {
                this.writePlatformService.deleteCode(command);
                commandSourceResult.markAsChecked(maker, asToday);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        }

        return commandSourceResult;
    }

    /*
     * Used when users with 'checker' capability approve a command.
     */
    public CommandSource handle(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();
        
        Long resourceId = commandSourceResult.resourceId();
        final CodeCommand command = this.commandDeserializerService.deserializeCodeCommand(resourceId, commandSourceResult.json(), true);
        
            if (commandSourceResult.isCreate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "CREATE_CODE_MAKER");
                context.authenticatedUser().validateHasPermissionTo("CREATE_USER_MAKER", allowedPermissions);
                
                resourceId = this.writePlatformService.createCode(command);
                commandSourceResult.updateResourceId(resourceId);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isUpdate()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "UPDATE_CODE_MAKER");
                context.authenticatedUser().validateHasPermissionTo("UPDATE_USER_MAKER", allowedPermissions);
                
                this.writePlatformService.updateCode(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            } else if (commandSourceResult.isDelete()) {
                final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "DELETE_CODE_MAKER");
                context.authenticatedUser().validateHasPermissionTo("DELETE_USER_MAKER", allowedPermissions);
                
                this.writePlatformService.deleteCode(command);
                commandSourceResult.markAsChecked(checker, new LocalDate());
            }

        return commandSourceResult;
    }
}