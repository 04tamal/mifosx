package org.mifosng.platform.fund.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "org_fund", uniqueConstraints={
		@UniqueConstraint(columnNames = {"name"}, name="fund_name_org"), 
		@UniqueConstraint(columnNames = {"external_id"}, name="fund_externalid_org")
})
public class Fund extends AbstractAuditableCustom<AppUser, Long> {
	
	@SuppressWarnings("unused")
	@Column(name = "name")
	private String name;
	
	@SuppressWarnings("unused")
	@Column(name = "external_id", length=100)
	private String externalId;

	public static Fund createNew(final String fundName, final String externalId) {
		return new Fund(fundName, externalId);
	}
	
	protected Fund() {
	}

	private Fund(final String fundName, final String externalId) {
		this.name = StringUtils.defaultIfEmpty(fundName, null);
		this.externalId = StringUtils.defaultIfEmpty(externalId, null);
	}

	public void update(final FundCommand command) {
		
		if (command.isNameChanged()) {
			this.name = StringUtils.defaultIfEmpty(command.getName(), null);
		}
		
		if (command.isExternalIdChanged()) {
			this.externalId = StringUtils.defaultIfEmpty(command.getExternalId(), null);
		}
	}
}