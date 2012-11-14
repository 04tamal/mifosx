package org.mifosng.platform.charge.service;

import org.mifosng.platform.api.commands.ChargeCommand;
import org.springframework.security.access.prepost.PreAuthorize;

public interface ChargeWritePlatformService {

	@PreAuthorize(value = "hasAnyRole('ORGANISATION_ADMINISTRATION_SUPER_USER', 'CREATE_CHARGE')")
    Long createCharge(final ChargeCommand command);

	@PreAuthorize(value = "hasAnyRole('ORGANISATION_ADMINISTRATION_SUPER_USER', 'UPDATE_CHARGE')")
    Long updateCharge(final ChargeCommand command);

	@PreAuthorize(value = "hasAnyRole('ORGANISATION_ADMINISTRATION_SUPER_USER', 'DELETE_CHARGE')")
    Long deleteCharge(final Long chargeId);
}
