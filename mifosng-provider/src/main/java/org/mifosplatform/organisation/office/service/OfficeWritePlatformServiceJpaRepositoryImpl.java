package org.mifosplatform.organisation.office.service;

import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.security.exception.NoAuthorizationException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrency;
import org.mifosplatform.organisation.monetary.domain.ApplicationCurrencyRepository;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.monetary.exception.CurrencyNotFoundException;
import org.mifosplatform.organisation.monetary.service.ConfigurationDomainService;
import org.mifosplatform.organisation.office.command.BranchMoneyTransferCommand;
import org.mifosplatform.organisation.office.command.OfficeCommand;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.organisation.office.domain.OfficeRepository;
import org.mifosplatform.organisation.office.domain.OfficeTransaction;
import org.mifosplatform.organisation.office.domain.OfficeTransactionRepository;
import org.mifosplatform.organisation.office.exception.OfficeNotFoundException;
import org.mifosplatform.portfolio.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosplatform.useradministration.domain.AppUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OfficeWritePlatformServiceJpaRepositoryImpl implements OfficeWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(OfficeWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final OfficeRepository officeRepository;
    private final OfficeTransactionRepository officeMonetaryTransferRepository;
    private final ApplicationCurrencyRepository applicationCurrencyRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public OfficeWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final OfficeRepository officeRepository,
            final OfficeTransactionRepository officeMonetaryTransferRepository,
            final ApplicationCurrencyRepository applicationCurrencyRepository, final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.officeRepository = officeRepository;
        this.officeMonetaryTransferRepository = officeMonetaryTransferRepository;
        this.applicationCurrencyRepository = applicationCurrencyRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public Long createOffice(final OfficeCommand command) {

        try {
            final AppUser currentUser = context.authenticatedUser();
            command.validateForCreate();

            final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getParentId());

            final Office office = Office.createNew(parent, command.getName(), command.getOpeningDate(), command.getExternalId());

            // pre save to generate id for use in office hierarchy
            this.officeRepository.save(office);

            office.generateHierarchy();

            this.officeRepository.save(office);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_OFFICE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return office.getId();
        } catch (DataIntegrityViolationException dve) {
            handleOfficeDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long updateOffice(final OfficeCommand command) {

        try {
            final AppUser currentUser = context.authenticatedUser();
            command.validateForUpdate();

            final Office office = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getId());

            office.update(command);

            if (command.isParentChanged()) {
                final Office parent = validateUserPriviledgeOnOfficeAndRetrieve(currentUser, command.getParentId());
                office.update(parent);
            }

            this.officeRepository.save(office);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_OFFICE") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return office.getId();
        } catch (DataIntegrityViolationException dve) {
            handleOfficeDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public Long externalBranchMoneyTransfer(final BranchMoneyTransferCommand command) {

        context.authenticatedUser();
        command.validateBranchTransfer();

        Office fromOffice = null;
        if (command.getFromOfficeId() != null) {
            fromOffice = this.officeRepository.findOne(command.getFromOfficeId());
        }
        Office toOffice = null;
        if (command.getToOfficeId() != null) {
            toOffice = this.officeRepository.findOne(command.getToOfficeId());
        }

        if (fromOffice == null && toOffice == null) { throw new OfficeNotFoundException(command.getToOfficeId()); }

        final String currencyCode = command.getCurrencyCode();
        final ApplicationCurrency appCurrency = this.applicationCurrencyRepository.findOneByCode(currencyCode);
        if (appCurrency == null) { throw new CurrencyNotFoundException(currencyCode); }

        final MonetaryCurrency currency = new MonetaryCurrency(appCurrency.getCode(), appCurrency.getDecimalPlaces());
        final Money amount = Money.of(currency, command.getTransactionAmount());

        final OfficeTransaction entity = OfficeTransaction.create(fromOffice, toOffice, command.getTransactionDate(), amount,
                command.getDescription());

        this.officeMonetaryTransferRepository.save(entity);

        if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_OFFICETRANSACTION") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        return entity.getId();
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleOfficeDataIntegrityIssues(final OfficeCommand command, DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("externalid_org")) {
            throw new PlatformDataIntegrityException("error.msg.office.duplicate.externalId", "Office with externalId {0} already exists",
                    "externalId", command.getExternalId());
        } else if (realCause.getMessage().contains("name_org")) { throw new PlatformDataIntegrityException(
                "error.msg.office.duplicate.name", "Office with name {0} already exists", "name", command.getName()); }

        logger.error(dve.getMessage(), dve);
        throw new PlatformDataIntegrityException("error.msg.office.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    /*
     * used to restrict modifying operations to office that are either the users
     * office or lower (child) in the office hierarchy
     */
    private Office validateUserPriviledgeOnOfficeAndRetrieve(AppUser currentUser, Long officeId) {

        Office userOffice = this.officeRepository.findOne(currentUser.getOffice().getId());
        if (userOffice == null) { throw new OfficeNotFoundException(currentUser.getOffice().getId()); }

        if (userOffice.doesNotHaveAnOfficeInHierarchyWithId(officeId)) { throw new NoAuthorizationException(
                "User does not have sufficient priviledges to act on the provided office."); }

        Office officeToReturn = userOffice;
        if (!userOffice.identifiedBy(officeId)) {
            officeToReturn = this.officeRepository.findOne(officeId);
            if (officeToReturn == null) { throw new OfficeNotFoundException(officeId); }
        }

        return officeToReturn;
    }
}