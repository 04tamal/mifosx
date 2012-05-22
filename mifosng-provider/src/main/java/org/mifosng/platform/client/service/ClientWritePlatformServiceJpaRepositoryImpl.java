package org.mifosng.platform.client.service;

import static org.mifosng.platform.Specifications.*;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.command.ClientCommand;
import org.mifosng.data.command.NoteCommand;
import org.mifosng.platform.client.domain.Client;
import org.mifosng.platform.client.domain.ClientRepository;
import org.mifosng.platform.client.domain.Note;
import org.mifosng.platform.client.domain.NoteRepository;
import org.mifosng.platform.organisation.domain.Office;
import org.mifosng.platform.organisation.domain.OfficeRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientWritePlatformServiceJpaRepositoryImpl implements ClientWritePlatformService {

	private final PlatformSecurityContext context;
	private final ClientRepository clientRepository;
	private final OfficeRepository officeRepository;
	private final NoteRepository noteRepository;

	@Autowired
	public ClientWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final ClientRepository clientRepository, 
			final OfficeRepository officeRepository, NoteRepository noteRepository) {
		this.context = context;
		this.clientRepository = clientRepository;
		this.officeRepository = officeRepository;
		this.noteRepository = noteRepository;
	}
	
	@Transactional
	@Override
	public Long enrollClient(final ClientCommand command) {

		AppUser currentUser = context.authenticatedUser();
		
		ClientCommandValidator validator = new ClientCommandValidator(command);
		validator.validate();

		Office clientOffice = this.officeRepository.findOne(officesThatMatch(currentUser.getOrganisation(), command.getOfficeId()));
		
		String firstname = command.getFirstname();
		String lastname = command.getLastname();
		if (StringUtils.isNotBlank(command.getFullname())) {
			lastname = command.getFullname();
			firstname = null;
		}

		Client newClient = Client.newClient(currentUser.getOrganisation(), clientOffice, firstname, lastname, command.getJoiningDate(), command.getExternalId());
				
		this.clientRepository.save(newClient);

		return newClient.getId();
	}
	
	@Transactional
	@Override
	public EntityIdentifier updateClientDetails(ClientCommand command) {
		
		AppUser currentUser = context.authenticatedUser();
		
		//EnrollClientCommandValidator validator = new EnrollClientCommandValidator(command);
		//validator.validate();

		Office clientOffice = this.officeRepository.findOne(officesThatMatch(currentUser.getOrganisation(), command.getOfficeId()));
		
		String firstname = command.getFirstname();
		String lastname = command.getLastname();
		if (StringUtils.isNotBlank(command.getFullname())) {
			lastname = command.getFullname();
			firstname = null;
		}

		Client clientForUpdate = this.clientRepository.findOne(clientsThatMatch(currentUser.getOrganisation(), command.getId()));
		
		clientForUpdate.update(clientOffice, firstname, lastname, command.getExternalId(), command.getJoiningDate());
				
		this.clientRepository.save(clientForUpdate);

		return new EntityIdentifier(clientForUpdate.getId());
	}
	
	@Transactional
	@Override
	public EntityIdentifier addClientNote(NoteCommand command) {
		
		AppUser currentUser = context.authenticatedUser();
		
		// FIXME - check resource is found for current organisation - throw PlatformXXXException if not found.
		Client clientForUpdate = this.clientRepository.findOne(command.getClientId());
		
		Note note = Note.clientNote(currentUser.getOrganisation(), clientForUpdate, command.getNote());
		
		this.noteRepository.save(note);
		
		return new EntityIdentifier(note.getId());
	}

	@Transactional
	@Override
	public EntityIdentifier updateNote(NoteCommand command) {
		
		AppUser currentUser = context.authenticatedUser();
		
		// FIXME - check resource is found for current organisation - throw PlatformXXXException if not found.
		Note noteForUpdate = this.noteRepository.findOne(command.getId());
		
		noteForUpdate.update(command.getNote());
		
		return new EntityIdentifier(noteForUpdate.getId());
	}
}