package org.mifosng.platform.user.service;

import static org.mifosng.platform.Specifications.rolesThatMatch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mifosng.data.command.RoleCommand;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.Permission;
import org.mifosng.platform.user.domain.PermissionRepository;
import org.mifosng.platform.user.domain.Role;
import org.mifosng.platform.user.domain.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleWritePlatformServiceJpaRepositoryImpl implements RoleWritePlatformService {

	private final PlatformSecurityContext context;
	private final RoleRepository roleRepository;

	private final PermissionRepository permissionRepository;
	
	@Autowired
	public RoleWritePlatformServiceJpaRepositoryImpl(final PlatformSecurityContext context, final RoleRepository roleRepository, final PermissionRepository permissionRepository) {
		this.context = context;
		this.roleRepository = roleRepository;
		this.permissionRepository = permissionRepository;
	}
	
	@Transactional
	@Override
	public Long createRole(final RoleCommand command) {
		
		AppUser currentUser = context.authenticatedUser();
		
		RoleCommandValidator validator = new RoleCommandValidator(command);
		validator.validateForCreate();

		List<Long> selectedPermissionIds = new ArrayList<Long>();
		for (String selectedId : command.getSelectedItems()) {
			selectedPermissionIds.add(Long.valueOf(selectedId));
		}

		List<Permission> selectedPermissions = new ArrayList<Permission>();
		Collection<Permission> allPermissions = this.permissionRepository.findAll();
		for (Permission permission : allPermissions) {
			if (selectedPermissionIds.contains(permission.getId())) {
				selectedPermissions.add(permission);
			}
		}

		Role entity = new Role(currentUser.getOrganisation(), command.getName(), command.getDescription(), selectedPermissions);
				
		this.roleRepository.save(entity);

		return entity.getId();
	}
	
	@Transactional
	@Override
	public Long updateRole(RoleCommand command) {
		
		AppUser currentUser = context.authenticatedUser();
		
		RoleCommandValidator validator = new RoleCommandValidator(command);
		validator.validateForUpdate();

		List<Long> selectedPermissionIds = new ArrayList<Long>();
		for (String selectedId : command.getSelectedItems()) {
			selectedPermissionIds.add(Long.valueOf(selectedId));
		}

		List<Permission> selectedPermissions = new ArrayList<Permission>();
		Collection<Permission> allPermissions = this.permissionRepository
				.findAll();
		for (Permission permission : allPermissions) {
			if (selectedPermissionIds.contains(permission.getId())) {
				selectedPermissions.add(permission);
			}
		}
		
		Role role = this.roleRepository.findOne(rolesThatMatch(currentUser.getOrganisation(), command.getId()));
		role.update(command.getName(), command.getDescription(), selectedPermissions);
		
		this.roleRepository.save(role);
		
		return role.getId();
	}
}