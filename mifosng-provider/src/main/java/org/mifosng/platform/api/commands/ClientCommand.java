package org.mifosng.platform.api.commands;

import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosng.platform.client.service.ClientCommandValidator;

public class ClientCommand {

	private final Long id;
	private final String externalId;
	private final String firstname;
	private final String lastname;
	private final String clientOrBusinessName;
	private final Long officeId;
	private final LocalDate joiningDate;
	
	private final Set<String> modifiedParameters;
	
	private final ClientCommandValidator validator;

	public ClientCommand(
			final Set<String> modifiedParameters, 
			final Long id, 
			final String externalId, 
			final String firstname, 
			final String lastname, 
			final String clientOrBusinessName, 
			final Long officeId, 
			final LocalDate joiningDate) {
		this.modifiedParameters = modifiedParameters;
		this.id = id;
		this.externalId = externalId;
		this.firstname = firstname;
		this.lastname = lastname;
		this.clientOrBusinessName = clientOrBusinessName;
		this.officeId = officeId;
		this.joiningDate = joiningDate;
		this.validator = new ClientCommandValidator(this);
	}

	public Long getId() {
		return id;
	}

	public String getExternalId() {
		return externalId;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public String getClientOrBusinessName() {
		return clientOrBusinessName;
	}

	public Long getOfficeId() {
		return officeId;
	}

	public LocalDate getJoiningDate() {
		return joiningDate;
	}
	
	public boolean isFirstnameChanged() {
		return this.modifiedParameters.contains("firstname");
	}
	
	public boolean isLastnameChanged() {
		return this.modifiedParameters.contains("lastname");
	}
	
	public boolean isClientOrBusinessNameChanged() {
		return this.modifiedParameters.contains("clientOrBusinessName");
	}
	
	public boolean isExternalIdChanged() {
		return this.modifiedParameters.contains("externalId");
	}
	
	public boolean isJoiningDateChanged() {
		return this.modifiedParameters.contains("joiningDate");
	}
	
	public boolean isOfficeChanged() {
		return this.modifiedParameters.contains("officeId");
	}

	public void validateForCreate() {
		this.validator.validateForCreate();
	}
}