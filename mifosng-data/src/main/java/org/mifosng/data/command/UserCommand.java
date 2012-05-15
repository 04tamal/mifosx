package org.mifosng.data.command;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * create or update user details command.
 */
@XmlRootElement
public class UserCommand implements Serializable {

	private Long id;
	private String username;
	private String firstname;
	private String lastname;
	private String password;
	private String email;
	private Long officeId;
	
	private String[] notSelectedItems;
	private String[] selectedItems;

	public UserCommand() {
		//
	}
	
	public String getDisplayName() {
		return new StringBuilder(this.lastname).append(", ").append(this.firstname).toString();
	}

	public String getUsername() {
		return this.username;
	}

	public void setUsername(final String username) {
		this.username = username;
	}

	public String getPassword() {
		return this.password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(final String email) {
		this.email = email;
	}

	public Long getOfficeId() {
		return this.officeId;
	}

	public void setOfficeId(final Long officeId) {
		this.officeId = officeId;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String[] getNotSelectedItems() {
		return notSelectedItems;
	}

	public void setNotSelectedItems(String... notSelectedItems) {
		this.notSelectedItems = notSelectedItems;
	}

	public String[] getSelectedItems() {
		return selectedItems;
	}

	public void setSelectedItems(String... selectedItems) {
		this.selectedItems = selectedItems;
	}
}