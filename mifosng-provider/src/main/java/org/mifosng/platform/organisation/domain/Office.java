package org.mifosng.platform.organisation.domain;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.exceptions.CannotUpdateOfficeWithParentOfficeSameAsSelf;
import org.mifosng.platform.exceptions.RootOfficeParentCannotBeUpdated;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.user.domain.AppUser;

@Entity
@Table(name = "org_office", uniqueConstraints={
												@UniqueConstraint(columnNames = {"name"}, name="name_org"), 
												@UniqueConstraint(columnNames = {"external_id"}, name="externalid_org")
})
public class Office extends AbstractAuditableCustom<AppUser, Long> {

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private final List<Office> children = new LinkedList<Office>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Office       parent;

    @Column(name = "name", nullable = false, length=100)
	private String name;
    
    @Column(name = "hierarchy", nullable = false, length=50)
	private String hierarchy;

    @SuppressWarnings("unused")
	@Column(name = "opening_date", nullable = false)
    @Temporal(TemporalType.DATE)
	private Date openingDate;

    @SuppressWarnings("unused")
	@Column(name = "external_id", length=100)
	private String externalId;

    public static Office headOffice(final String name, final LocalDate openingDate, final String externalId) {
        return new Office(null, name, openingDate, externalId);
    }
    
    public static Office createNew(final Office parent, final String name, final LocalDate openingDate, final String externalId) {
		return new Office(parent, name, openingDate, externalId);
	}

    protected Office() {
        this.openingDate = null;
        this.parent = null;
        this.name = null;
        this.externalId = null;
    }

    private Office(final Office parent, final String name, final LocalDate openingDate, final String externalId) {
        this.parent = parent;
        this.openingDate = openingDate.toDateMidnight().toDate();
        if (parent != null) {
            this.parent.addChild(this);
        } 
        
        if (StringUtils.isNotBlank(name)) {
        	this.name = name.trim();
        } else {
        	this.name = null;
        }
		if (StringUtils.isNotBlank(externalId)) {
			this.externalId = externalId.trim();
		} else {
			this.externalId = null;
		}
    }

	private void addChild(final Office office) {
        this.children.add(office);
    }
    
	public void update(final OfficeCommand command) {
		
		if (command.isNameChanged()) {
			this.name = StringUtils.defaultIfEmpty(command.getName(), null);
		}
		
		if (command.isExternalIdChanged()) {
			this.externalId = StringUtils.defaultIfEmpty(command.getExternalId(), null);
		}
		
		if (command.isOpeningDateChanged()) {
			if (command.getOpeningDate() != null) {
				this.openingDate = command.getOpeningDate().toDate();
			} else {
				this.openingDate = null;
			}
		}
	}
	
	public void update(final Office newParent) {
		
		if (this.parent == null) {
			throw new RootOfficeParentCannotBeUpdated();
		}
		
		if (this.identifiedBy(newParent.getId())) {
			throw new CannotUpdateOfficeWithParentOfficeSameAsSelf(this.getId(), newParent.getId());
		}
		
		this.parent = newParent;
		generateHierarchy();
	}

	public boolean identifiedBy(final Long id) {
		return getId().equals(id);
	}

	public void generateHierarchy() {
		
		if (parent != null) {
			this.hierarchy = this.parent.hierarchyOf(this.getId());
		} else {
			this.hierarchy = ".";
		}
	}

	private String hierarchyOf(Long id) {
		return this.hierarchy + id.toString() + ".";
	}
	
    public String getName() {
    	return this.name;
    }

	public String getHierarchy() {
		return hierarchy;
	}

	public boolean hasParentOf(final Office office) {
		boolean isParent = false;
		if (this.parent != null) {
			isParent = this.parent.equals(office);
		}
		return isParent;
	}
	
	public boolean doesNotHaveAnOfficeInHierarchyWithId(Long officeId) {
		return !this.hasAnOfficeInHierarchyWithId(officeId);
	}
	
	private boolean hasAnOfficeInHierarchyWithId(Long officeId) {
		
		boolean match = false;
		
		if (identifiedBy(officeId)) {
			match = true;
		}
		
		if (!match) {
			for (Office child : this.children) {
				boolean result = child.hasAnOfficeInHierarchyWithId(officeId);
				
				if (result) {
					match = result;
					break;
				}
			}
		}
		
		return match;
	}
}