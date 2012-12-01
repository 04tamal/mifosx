package org.mifosplatform.commands.handler;

import java.util.Arrays;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.commands.domain.CommandSource;
import org.mifosplatform.commands.exception.UnsupportedCommandException;
import org.mifosplatform.infrastructure.core.api.PortfolioCommandDeserializerService;
import org.mifosplatform.infrastructure.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.infrastructure.office.service.OfficeWritePlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.infrastructure.user.domain.AppUser;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OfficeTransactionCommandHandler implements CommandSourceHandler {

    private final PlatformSecurityContext context;
    private final PortfolioCommandDeserializerService commandDeserializerService;
    private final OfficeWritePlatformService writePlatformService;

    @Autowired
    public OfficeTransactionCommandHandler(final PlatformSecurityContext context,
            final PortfolioCommandDeserializerService commandDeserializerService, final OfficeWritePlatformService writePlatformService) {
        this.context = context;
        this.commandDeserializerService = commandDeserializerService;
        this.writePlatformService = writePlatformService;
    }

    @Override
    public CommandSource handleCommandWithSupportForRollback(final CommandSource commandSource) {

        final AppUser maker = context.authenticatedUser();
        final LocalDate asToday = new LocalDate();

        final BranchMoneyTransferCommand command = this.commandDeserializerService.deserializeOfficeTransactionCommand(
                commandSource.json(), false);

        CommandSource commandSourceResult = commandSource.copy();

        Long newResourceId = null;

        if (commandSource.isCreate()) {
            try {
                newResourceId = this.writePlatformService.externalBranchMoneyTransfer(command);
                commandSourceResult.markAsChecked(maker, asToday);
                commandSourceResult.updateResourceId(newResourceId);
            } catch (RollbackTransactionAsCommandIsNotApprovedByCheckerException e) {
                // swallow this rollback transaction by design
            }
        } else if (commandSource.isUpdate()) {
            throw new UnsupportedCommandException(commandSource.commandName());
        } else if (commandSource.isDelete()) { throw new UnsupportedCommandException(commandSource.commandName()); }

        return commandSourceResult;
    }

    @Override
    public CommandSource handleCommandForCheckerApproval(final CommandSource commandSourceResult) {

        final AppUser checker = context.authenticatedUser();

        Long resourceId = commandSourceResult.resourceId();
        final BranchMoneyTransferCommand command = this.commandDeserializerService.deserializeOfficeTransactionCommand(
                commandSourceResult.json(), true);

        if (commandSourceResult.isCreate()) {
            final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                    "CREATE_OFFICETRANSACTION_CHECKER");
            context.authenticatedUser().validateHasPermissionTo("CREATE_OFFICETRANSACTION_CHECKER", allowedPermissions);

            resourceId = this.writePlatformService.externalBranchMoneyTransfer(command);
            commandSourceResult.updateResourceId(resourceId);
            commandSourceResult.markAsChecked(checker, new LocalDate());
        } else if (commandSourceResult.isUpdate()) {
            throw new UnsupportedCommandException(commandSourceResult.commandName());
        } else if (commandSourceResult.isDelete()) { throw new UnsupportedCommandException(commandSourceResult.commandName()); }

        return commandSourceResult;
    }
}