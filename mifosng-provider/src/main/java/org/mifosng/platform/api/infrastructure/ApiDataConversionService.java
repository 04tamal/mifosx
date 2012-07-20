package org.mifosng.platform.api.infrastructure;

import java.util.Set;

import org.mifosng.platform.api.commands.AdjustLoanTransactionCommand;
import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.commands.ClientCommand;
import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.commands.LoanStateTransitionCommand;
import org.mifosng.platform.api.commands.LoanTransactionCommand;
import org.mifosng.platform.api.commands.NoteCommand;
import org.mifosng.platform.api.commands.OfficeCommand;
import org.mifosng.platform.api.commands.OrganisationCurrencyCommand;
import org.mifosng.platform.api.commands.RoleCommand;
import org.mifosng.platform.api.commands.SubmitLoanApplicationCommand;
import org.mifosng.platform.api.commands.UserCommand;
import org.mifosng.platform.api.data.AppUserData;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.data.OfficeData;
import org.mifosng.platform.api.data.OfficeTransactionData;
import org.mifosng.platform.api.data.PermissionData;
import org.mifosng.platform.api.data.RoleData;

public interface ApiDataConversionService {
	
	//
	String convertPermissionDataToJson(boolean prettyPrint, Set<String> responseParameters, PermissionData... permissions);
	
	String convertRoleDataToJson(boolean prettyPrint, Set<String> responseParameters, RoleData... roles);
	
	String convertAppUserDataToJson(boolean prettyPrint, Set<String> responseParameters, AppUserData... users);
	
	String convertOfficeTransactionDataToJson(boolean prettyPrint, Set<String> responseParameters, OfficeTransactionData... officeTransactions);
	//

	FundCommand convertJsonToFundCommand(Long resourceIdentifier, String json);
	
	String convertFundDataToJson(boolean prettyPrint, Set<String> responseParameters, FundData... funds);

	OfficeCommand convertJsonToOfficeCommand(Long resourceIdentifier, String json);
	
	String convertOfficeDataToJson(boolean prettyPrint, Set<String> responseParameters, OfficeData... offices);

	RoleCommand convertJsonToRoleCommand(Long resourceIdentifier, String json);

	UserCommand convertJsonToUserCommand(Long resourceIdentifier, String json);

	BranchMoneyTransferCommand convertJsonToBranchMoneyTransferCommand(String jsonRequestBody);
	
	LoanProductCommand convertJsonToLoanProductCommand(Long resourceIdentifier, String json);

	ClientCommand convertJsonToClientCommand(Long resourceIdentifier, String jsonRequestBody);

	SubmitLoanApplicationCommand convertJsonToSubmitLoanApplicationCommand(String jsonRequestBody);

	LoanStateTransitionCommand convertJsonToLoanStateTransitionCommand(Long resourceIdentifier, String jsonRequestBody);

	LoanTransactionCommand convertJsonToLoanTransactionCommand(Long resourceIdentifier, String jsonRequestBody);

	AdjustLoanTransactionCommand convertJsonToAdjustLoanTransactionCommand(
			Long loanId, Long transactionId, String jsonRequestBody);

	OrganisationCurrencyCommand convertJsonToOrganisationCurrencyCommand(String jsonRequestBody);

	NoteCommand convertJsonToNoteCommand(Long resourceIdentifier, Long clientId, String jsonRequestBody);
}