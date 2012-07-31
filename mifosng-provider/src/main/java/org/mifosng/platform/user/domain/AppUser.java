package org.mifosng.platform.user.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.infrastructure.PlatformUser;
import org.mifosng.platform.organisation.domain.Office;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

@Entity
@Table(name = "admin_appuser", uniqueConstraints=@UniqueConstraint(columnNames = {"username"}, name="username_org"))
public class AppUser extends AbstractAuditableCustom<AppUser, Long> implements PlatformUser {

    @Column(name = "email", nullable=false, length=100)
    private String       email;

    @Column(name = "username", nullable=false, length=100)
    private String       username;
    
    @Column(name = "firstname", nullable=false, length=100)
    private String       firstname;
    
    @Column(name = "lastname", nullable=false, length=100)
    private String       lastname;

    @Column(name = "password", nullable=false)
    private String             password;

    @Column(name = "nonexpired", nullable=false)
    private final boolean      accountNonExpired;

    @Column(name = "nonlocked", nullable=false)
    private final boolean      accountNonLocked;

    @Column(name = "nonexpired_credentials", nullable=false)
    private final boolean      credentialsNonExpired;

    @Column(name = "enabled", nullable=false)
    private final boolean      enabled;

    @Column(name = "firsttime_login_remaining", nullable=false)
    private boolean      firstTimeLoginRemaining;

    @ManyToOne
    @JoinColumn(name = "office_id")
    private Office office;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "admin_appuser_role", joinColumns = @JoinColumn(name = "appuser_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role>    roles;
    

	public static AppUser createNew(final Office office,
			final Set<Role> allRoles, final String username, final String email,
			final String firstname, final String lastname, final String password) {
		
		boolean userEnabled = true;
		boolean userAccountNonExpired = true;
		boolean userCredentialsNonExpired = true;
		boolean userAccountNonLocked = true;
		
		Collection<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
		authorities.add(new SimpleGrantedAuthority("DUMMY_ROLE_NOT_USED_OR_PERSISTED_TO_AVOID_EXCEPTION"));
		
		User user = new User(username, password, userEnabled, userAccountNonExpired, userCredentialsNonExpired, userAccountNonLocked, authorities);
		return new AppUser(office, user, allRoles, email, firstname, lastname);
	}

    protected AppUser() {
        this.office = null;
        this.email = null;
        this.username = null;
        this.password = null;
        this.accountNonExpired = false;
        this.accountNonLocked = false;
        this.credentialsNonExpired = false;
        this.enabled = false;
        this.roles = new HashSet<Role>();
        this.firstTimeLoginRemaining = true;
    }

    public AppUser(final Office office, final User user, final Set<Role> roles, final String email, final String firstname, final String lastname) {
        this.office = office;
        this.email = email.trim();
        this.username = user.getUsername().trim();
        this.firstname = firstname.trim();
        this.lastname = lastname.trim();
        this.password = user.getPassword().trim();
        this.accountNonExpired = user.isAccountNonExpired();
        this.accountNonLocked = user.isAccountNonLocked();
        this.credentialsNonExpired = user.isCredentialsNonExpired();
        this.enabled = user.isEnabled();
        this.roles = roles;
		this.firstTimeLoginRemaining = true;
    }

	@Override
	public boolean isFirstTimeLoginRemaining() {
		return this.firstTimeLoginRemaining;
	}

	@Override
	public void updateUsernamePasswordOnFirstTimeLogin(final String newUsername,
			final String newPasswordEncoded) {
		if (this.username.equals(newUsername)) {
			throw new UsernameMustBeDifferentException();
		}
		this.username = newUsername;
		updatePasswordOnFirstTimeLogin(newPasswordEncoded);
		this.firstTimeLoginRemaining = false;
	}

	@Override
	public void updatePasswordOnFirstTimeLogin(final String newPasswordEncoded) {
		if (this.password.equals(newPasswordEncoded)) {
			throw new PasswordMustBeDifferentException();
		}
		this.password = newPasswordEncoded;
		this.firstTimeLoginRemaining = false;
	}

	public void updatePassword(final String encodePassword) {
		this.password = encodePassword;
	}
	
	public void update(final Set<Role> allRoles, final Office office, final UserCommand userCommand) {
		
		if (userCommand.isRolesChanged() && !allRoles.isEmpty()) {
			this.roles.clear();
			this.roles = allRoles;
		}
		if (userCommand.isOfficeChanged()) {
			this.office = office;
		}
		if (userCommand.isUsernameChanged()) {
			this.username = userCommand.getUsername();
		}
		if (userCommand.isFirstnameChanged()) {
			this.firstname = userCommand.getFirstname();
		}
		if (userCommand.isLastnameChanged()) {
			this.lastname = userCommand.getLastname();
		}
		if (userCommand.isEmailChanged()) {
			this.email = userCommand.getEmail();
		}
	}

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return this.populateGrantedAuthorities();
    }

    private List<GrantedAuthority> populateGrantedAuthorities() {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        for (Role role :this.roles) {
            Collection<Permission> permissions = role.getPermissions();
            for (Permission permission : permissions) {
                grantedAuthorities.add(new SimpleGrantedAuthority(permission.code()));
            }
        }
        return grantedAuthorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

    public String getEmail() {
        return this.email;
    }

    public Set<Role> getRoles() {
        return this.roles;
    }

    public Office getOffice() {
        return this.office;
    }

	public boolean canNotApproveLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_APPROVE_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotRejectLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_REJECT_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotWithdrawByClientLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_WITHDRAW_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotDisburseLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_DISBURSE_LOAN_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}

	public boolean canNotMakeRepaymentOnLoanInPast() {
		return hasNotPermissionForAnyOf("CAN_MAKE_LOAN_REPAYMENT_IN_THE_PAST_ROLE", "PORTFOLIO_MANAGEMENT_SUPER_USER_ROLE");
	}
	
	public boolean hasNotPermissionForAnyOf(final String... permissionCodes) {
		boolean hasNotPermission = true;
		for (String permissionCode : permissionCodes) {
			boolean checkPermission = this.hasPermissionTo(permissionCode);
			if (checkPermission) {
				hasNotPermission = false;
				break;
			}
		}
		return hasNotPermission;
	}

	private boolean hasPermissionTo(final String permissionCode) {
		boolean match = false;
		for (Role role : this.roles) {
			if (role.hasPermissionTo(permissionCode)) {
				match = true;
				break;
			}
		}

		return match;
	}
}