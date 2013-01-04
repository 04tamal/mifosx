package org.mifosplatform.infrastructure.dataqueries.handler;

import java.util.Map;

import org.mifosplatform.commands.handler.NewCommandSourceHandler;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.mifosplatform.infrastructure.dataqueries.service.ReadWriteNonCoreDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateOneToManyDatatableEntryCommandHandler implements
		NewCommandSourceHandler {

	private final ReadWriteNonCoreDataService writePlatformService;

	@Autowired
	public UpdateOneToManyDatatableEntryCommandHandler(
			final ReadWriteNonCoreDataService writePlatformService) {
		this.writePlatformService = writePlatformService;
	}

	@Transactional
	@Override
	public EntityIdentifier processCommand(final JsonCommand command) {

		Map<String, Object> changes = this.writePlatformService.updateDatatableEntryOneToMany(command.entityName(), command.resourceId(), command.subResourceId(), command);

		return EntityIdentifier.subResourceResult(command.resourceId(), command.subResourceId(),
				command.commandId(), changes);
	}
}