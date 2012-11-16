package org.mifosng.platform.saving.service;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawInterestCommand;
import org.mifosng.platform.api.commands.DepositAccountWithdrawalCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionApprovalCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.springframework.security.access.prepost.PreAuthorize;

public interface DepositAccountWritePlatformService {

	@PreAuthorize(value = "hasRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'CREATE_DEPOSITACCOUNT')")
	EntityIdentifier createDepositAccount(DepositAccountCommand command);

	@PreAuthorize(value = "hasRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'UPDATE_DEPOSITACCOUNT')")
	EntityIdentifier updateDepositAccount(DepositAccountCommand command);

	@PreAuthorize(value = "hasRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'DELETE_DEPOSITACCOUNT')")
	EntityIdentifier deleteDepositAccount(Long productId);

	// NOTE - took out permissions relating to doing things with deposit
	// accounts in the past as doesn't appear to be code for it
	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'APPROVE_DEPOSITACCOUNT')")
	EntityIdentifier approveDepositApplication(
			DepositStateTransitionApprovalCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'REJECT_DEPOSITACCOUNT')")
	EntityIdentifier rejectDepositApplication(
			DepositStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WITHDRAW_DEPOSITACCOUNT')")
	EntityIdentifier withdrawDepositApplication(
			DepositStateTransitionCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'APPROVALUNDO_DEPOSITACCOUNT')")
	EntityIdentifier undoDepositApproval(UndoStateTransitionCommand undoCommand);

	/*
	 * @PreAuthorize(value =
	 * "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', '')"
	 * ) EntityIdentifier matureDepositApplication(DepositStateTransitionCommand
	 * command);
	 */

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'WITHDRAWAL_DEPOSITACCOUNT')")
	EntityIdentifier withdrawDepositAccountMoney(
			DepositAccountWithdrawalCommand command);

	@PreAuthorize(value = "hasAnyRole('ALL_FUNCTIONS', 'PORTFOLIO_MANAGEMENT_SUPER_USER', 'INTEREST_DEPOSITACCOUNT')")
	EntityIdentifier withdrawDepositAccountInterestMoney(
			DepositAccountWithdrawInterestCommand command);

	@PreAuthorize(value = "hasRole('ALL_FUNCTIONS', 'ORGANISATION_ADMINISTRATION_SUPER_USER', 'RENEW_DEPOSITACCOUNT')")
	EntityIdentifier renewDepositAccount(DepositAccountCommand command);

}