package org.mifosplatform.portfolio.client.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.exceptions.DocumentManagementException;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepository;
import org.mifosplatform.infrastructure.codes.exception.CodeValueNotFoundException;
import org.mifosplatform.infrastructure.configuration.service.ConfigurationDomainService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.core.domain.Base64EncodedImage;
import org.mifosplatform.infrastructure.core.exception.PlatformDataIntegrityException;
import org.mifosplatform.infrastructure.core.service.FileUtils;
import org.mifosplatform.infrastructure.office.domain.Office;
import org.mifosplatform.infrastructure.office.domain.OfficeRepository;
import org.mifosplatform.infrastructure.office.exception.OfficeNotFoundException;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.client.command.ClientCommand;
import org.mifosplatform.portfolio.client.command.ClientCommandValidator;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommand;
import org.mifosplatform.portfolio.client.command.ClientIdentifierCommandValidator;
import org.mifosplatform.portfolio.client.command.NoteCommand;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.client.domain.ClientIdentifier;
import org.mifosplatform.portfolio.client.domain.ClientIdentifierRepository;
import org.mifosplatform.portfolio.client.domain.ClientRepository;
import org.mifosplatform.portfolio.client.domain.Note;
import org.mifosplatform.portfolio.client.domain.NoteRepository;
import org.mifosplatform.portfolio.client.exception.ClientIdentifierNotFoundException;
import org.mifosplatform.portfolio.client.exception.ClientNotFoundException;
import org.mifosplatform.portfolio.client.exception.DuplicateClientIdentifierException;
import org.mifosplatform.portfolio.client.exception.NoteNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

    private final static Logger logger = LoggerFactory.getLogger(ClientWritePlatformServiceJpaRepositoryImpl.class);

    private final PlatformSecurityContext context;
    private final ClientRepository clientRepository;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final OfficeRepository officeRepository;
    private final NoteRepository noteRepository;
    private final CodeValueRepository codeValueRepository;
    private final ConfigurationDomainService configurationDomainService;

    @Autowired
    public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final ClientRepository clientRepository,
            final ClientIdentifierRepository clientIdentifierRepository, final OfficeRepository officeRepository,
            final NoteRepository noteRepository, final CodeValueRepository codeValueRepository,
            final ConfigurationDomainService configurationDomainService) {
        this.context = context;
        this.clientRepository = clientRepository;
        this.clientIdentifierRepository = clientIdentifierRepository;
        this.officeRepository = officeRepository;
        this.noteRepository = noteRepository;
        this.codeValueRepository = codeValueRepository;
        this.configurationDomainService = configurationDomainService;
    }

    @Transactional
    @Override
    public EntityIdentifier deleteClient(final ClientCommand command) {

        final Long clientId = command.getId();
        final Client client = this.clientRepository.findOne(clientId);
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        client.delete();
        this.clientRepository.save(client);

        if (this.configurationDomainService.isMakerCheckerEnabledForTask("DELETE_CLIENT") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        return new EntityIdentifier(client.getId());
    }

    /*
     * Guaranteed to throw an exception no matter what the data integrity issue
     * is.
     */
    private void handleDataIntegrityIssues(final ClientCommand command, final DataIntegrityViolationException dve) {

        Throwable realCause = dve.getMostSpecificCause();
        if (realCause.getMessage().contains("external_id")) { throw new PlatformDataIntegrityException(
                "error.msg.client.duplicate.externalId", "Client with externalId `" + command.getExternalId() + "` already exists",
                "externalId", command.getExternalId()); }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.client.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    @Transactional
    @Override
    public Long createClient(final ClientCommand command) {

        try {
            context.authenticatedUser();

            final ClientCommandValidator validator = new ClientCommandValidator(command);
            validator.validateForCreate();

            final Office clientOffice = this.officeRepository.findOne(command.getOfficeId());
            if (clientOffice == null) { throw new OfficeNotFoundException(command.getOfficeId()); }

            String firstname = command.getFirstname();
            String lastname = command.getLastname();
            if (StringUtils.isNotBlank(command.getClientOrBusinessName())) {
                lastname = command.getClientOrBusinessName();
                firstname = null;
            }

            final Client newClient = Client.newClient(clientOffice, firstname, lastname, command.getJoiningDate(), command.getExternalId());

            this.clientRepository.save(newClient);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("CREATE_CLIENT") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return newClient.getId();
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public EntityIdentifier updateClientDetails(final ClientCommand command) {

        try {
            context.authenticatedUser();

            final ClientCommandValidator validator = new ClientCommandValidator(command);
            validator.validateForUpdate();

            Office clientOffice = null;
            Long officeId = command.getOfficeId();
            if (command.isOfficeChanged() && officeId != null) {
                clientOffice = this.officeRepository.findOne(officeId);
                if (clientOffice == null) { throw new OfficeNotFoundException(command.getOfficeId()); }
            }

            final Client clientForUpdate = this.clientRepository.findOne(command.getId());
            if (clientForUpdate == null || clientForUpdate.isDeleted()) { throw new ClientNotFoundException(command.getId()); }
            clientForUpdate.update(clientOffice, command);

            this.clientRepository.saveAndFlush(clientForUpdate);

            if (this.configurationDomainService.isMakerCheckerEnabledForTask("UPDATE_CLIENT") && !command.isApprovedByChecker()) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

            return new EntityIdentifier(clientForUpdate.getId());
        } catch (DataIntegrityViolationException dve) {
            handleDataIntegrityIssues(command, dve);
            return new EntityIdentifier(Long.valueOf(-1));
        }
    }

    @Transactional
    @Override
    public EntityIdentifier addClientNote(final NoteCommand command) {

        context.authenticatedUser();

        final Client clientForUpdate = this.clientRepository.findOne(command.getClientId());
        if (clientForUpdate == null) { throw new ClientNotFoundException(command.getClientId()); }

        final Note note = Note.clientNote(clientForUpdate, command.getNote());

        this.noteRepository.save(note);

        return new EntityIdentifier(note.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier updateNote(final NoteCommand command) {

        context.authenticatedUser();

        final Note noteForUpdate = this.noteRepository.findOne(command.getId());
        if (noteForUpdate == null || noteForUpdate.isNotAgainstClientWithIdOf(command.getClientId())) { throw new NoteNotFoundException(
                command.getId(), command.getClientId(), "client"); }

        noteForUpdate.update(command.getNote());

        return new EntityIdentifier(noteForUpdate.getId());
    }

    @Transactional
    @Override
    public Long addClientIdentifier(final ClientIdentifierCommand command) {

        String documentTypeLabel = null;
        Long documentTypeId = null;
        final String documentKey = command.getDocumentKey();

        try {
            context.authenticatedUser();

            final ClientIdentifierCommandValidator validator = new ClientIdentifierCommandValidator(command);
            validator.validateForCreate();

            final Client client = this.clientRepository.findOne(command.getClientId());
            if (client == null || client.isDeleted()) { throw new ClientNotFoundException(command.getClientId()); }

            final CodeValue documentType = this.codeValueRepository.findOne(command.getDocumentTypeId());
            if (documentType == null) { throw new CodeValueNotFoundException(command.getDocumentTypeId()); }
            documentTypeId = documentType.getId();
            documentTypeLabel = documentType.label();

            final ClientIdentifier clientIdentifier = ClientIdentifier.createNew(client, documentType, documentKey,
                    command.getDescription());

            this.clientIdentifierRepository.save(clientIdentifier);

            return clientIdentifier.getId();
        } catch (DataIntegrityViolationException dve) {
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, dve);
            return Long.valueOf(-1);
        }
    }

    @Transactional
    @Override
    public EntityIdentifier updateClientIdentifier(final ClientIdentifierCommand command) {

        String documentTypeLabel = null;
        Long documentTypeId = null;
        String documentKey = command.getDocumentKey();

        try {
            context.authenticatedUser();

            final ClientIdentifierCommandValidator validator = new ClientIdentifierCommandValidator(command);
            validator.validateForUpdate();

            CodeValue documentType = null;
            documentTypeId = command.getDocumentTypeId();
            if (command.isDocumentTypeChanged() && documentTypeId != null) {
                documentType = this.codeValueRepository.findOne(documentTypeId);
                if (documentType == null) { throw new CodeValueNotFoundException(command.getDocumentTypeId()); }
                documentTypeId = documentType.getId();
                documentTypeLabel = documentType.label();
            }

            final ClientIdentifier clientIdentifierForUpdate = this.clientIdentifierRepository.findOne(command.getId());
            if (clientIdentifierForUpdate == null) { throw new ClientIdentifierNotFoundException(command.getId()); }

            // TODO - KW - why need to check what changed when integrity
            // violation occurs?
            if (command.isDocumentTypeChanged() && command.isDocumentKeyChanged()) {
                documentTypeId = command.getDocumentTypeId();
                documentKey = command.getDocumentKey();
            } else if (command.isDocumentTypeChanged() && !command.isDocumentKeyChanged()) {
                documentTypeId = command.getDocumentTypeId();
                documentKey = clientIdentifierForUpdate.getDocumentKey();
            } else if (!command.isDocumentTypeChanged() && command.isDocumentKeyChanged()) {
                documentTypeId = clientIdentifierForUpdate.getDocumentType().getId();
                documentKey = clientIdentifierForUpdate.getDocumentKey();
            }

            clientIdentifierForUpdate.update(command, documentType);

            this.clientIdentifierRepository.saveAndFlush(clientIdentifierForUpdate);

            return new EntityIdentifier(clientIdentifierForUpdate.getId());
        } catch (DataIntegrityViolationException dve) {
            handleClientIdentifierDataIntegrityViolation(documentTypeLabel, documentTypeId, documentKey, dve);
            return new EntityIdentifier(Long.valueOf(-1));
        }
    }

    private void handleClientIdentifierDataIntegrityViolation(final String documentTypeLabel, final Long documentTypeId,
            final String documentKey, final DataIntegrityViolationException dve) {

        if (dve.getMostSpecificCause().getMessage().contains("unique_client_identifier")) {
            throw new DuplicateClientIdentifierException(documentTypeLabel);
        } else if (dve.getMostSpecificCause().getMessage().contains("unique_identifier_key")) { throw new DuplicateClientIdentifierException(
                documentTypeId, documentTypeLabel, documentKey); }

        logAsErrorUnexpectedDataIntegrityException(dve);
        throw new PlatformDataIntegrityException("error.msg.clientIdentifier.unknown.data.integrity.issue",
                "Unknown data integrity issue with resource.");
    }

    private void logAsErrorUnexpectedDataIntegrityException(final DataIntegrityViolationException dve) {
        logger.error(dve.getMessage(), dve);
    }

    @Transactional
    @Override
    public EntityIdentifier deleteClientIdentifier(final ClientIdentifierCommand command) {
        final ClientIdentifier clientIdentifier = this.clientIdentifierRepository.findOne(command.getId());
        if (clientIdentifier == null) { throw new ClientIdentifierNotFoundException(command.getId()); }
        this.clientIdentifierRepository.delete(clientIdentifier);

        return new EntityIdentifier(command.getId());
    }

    @Transactional
    @Override
    public EntityIdentifier saveOrUpdateClientImage(Long clientId, String imageName, InputStream inputStream) {
        try {
            final Client client = this.clientRepository.findOne(clientId);
            String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            String imageLocation = FileUtils.saveToFileSystem(inputStream, imageUploadLocation, imageName);

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException(imageName);
        }
    }

    @Transactional
    @Override
    public EntityIdentifier deleteClientImage(final Long clientId) {

        final Client client = this.clientRepository.findOne(clientId);
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        // delete image from the file system
        if (StringUtils.isNotEmpty(client.getImageKey())) {
            FileUtils.deleteClientImage(clientId, client.getImageKey());
        }
        return updateClientImage(clientId, client, null);
    }

    @Override
    public EntityIdentifier saveOrUpdateClientImage(final Long clientId, final Base64EncodedImage encodedImage) {
        try {
            final Client client = this.clientRepository.findOne(clientId);
            final String imageUploadLocation = setupForClientImageUpdate(clientId, client);

            final String imageLocation = FileUtils.saveToFileSystem(encodedImage, imageUploadLocation, "image");

            return updateClientImage(clientId, client, imageLocation);
        } catch (IOException ioe) {
            logger.error(ioe.getMessage(), ioe);
            throw new DocumentManagementException("image");
        }
    }

    private String setupForClientImageUpdate(final Long clientId, final Client client) {
        if (client == null || client.isDeleted()) { throw new ClientNotFoundException(clientId); }

        final String imageUploadLocation = FileUtils.generateClientImageParentDirectory(clientId);
        // delete previous image from the file system
        if (StringUtils.isNotEmpty(client.getImageKey())) {
            FileUtils.deleteClientImage(clientId, client.getImageKey());
        }

        /** Recursively create the directory if it does not exist **/
        if (!new File(imageUploadLocation).isDirectory()) {
            new File(imageUploadLocation).mkdirs();
        }
        return imageUploadLocation;
    }

    private EntityIdentifier updateClientImage(final Long clientId, final Client client, final String imageLocation) {
        client.setImageKey(imageLocation);
        this.clientRepository.save(client);

        return new EntityIdentifier(clientId);
    }
}