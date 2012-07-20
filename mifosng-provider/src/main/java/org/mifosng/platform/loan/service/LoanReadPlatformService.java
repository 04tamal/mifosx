package org.mifosng.platform.loan.service;

import java.util.Collection;

import org.mifosng.platform.api.data.LoanAccountSummaryData;
import org.mifosng.platform.api.data.LoanBasicDetailsData;
import org.mifosng.platform.api.data.LoanPermissionData;
import org.mifosng.platform.api.data.LoanRepaymentPeriodDatajpw;
import org.mifosng.platform.api.data.LoanTransactionData;
import org.mifosng.platform.api.data.LoanTransactionDatajpw;
import org.mifosng.platform.api.data.MoneyData;
import org.mifosng.platform.api.data.NewLoanData;

public interface LoanReadPlatformService {

	LoanBasicDetailsData retrieveLoanAccountDetails(Long loanId);

	Collection<LoanRepaymentPeriodDatajpw> retrieveRepaymentSchedule(Long loanId);

	Collection<LoanTransactionDatajpw> retrieveLoanPayments(Long loanId);

	LoanAccountSummaryData retrieveSummary(MoneyData principal,
			Collection<LoanRepaymentPeriodDatajpw> repaymentSchedule,
			Collection<LoanTransactionDatajpw> loanRepayments);

	LoanPermissionData retrieveLoanPermissions(
			LoanBasicDetailsData loanBasicDetails, boolean isWaiverAllowed);

	NewLoanData retrieveClientAndProductDetails(Long clientId, Long productId);

	LoanTransactionData retrieveNewLoanRepaymentDetails(Long loanId);

	LoanTransactionData retrieveNewLoanWaiverDetails(Long loanId);

	LoanTransactionData retrieveLoanTransactionDetails(Long loanId,
			Long transactionId);

}