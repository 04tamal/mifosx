package org.mifosplatform.portfolio.loanaccount.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface LoanWritePlatformService {

    EntityIdentifier disburseLoan(Long loanId, JsonCommand command);

    EntityIdentifier undoLoanDisbursal(Long loanId, JsonCommand command);

    EntityIdentifier makeLoanRepayment(Long loanId, JsonCommand command);

    EntityIdentifier adjustLoanTransaction(Long loanId, Long transactionId, JsonCommand command);

    EntityIdentifier waiveInterestOnLoan(Long loanId, JsonCommand command);

    EntityIdentifier writeOff(Long loanId, JsonCommand command);

    EntityIdentifier closeLoan(Long loanId, JsonCommand command);

    EntityIdentifier closeAsRescheduled(Long loanId, JsonCommand command);

    EntityIdentifier addLoanCharge(Long loanId, JsonCommand command);

    EntityIdentifier updateLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    EntityIdentifier deleteLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    EntityIdentifier waiveLoanCharge(Long loanId, Long loanChargeId, JsonCommand command);

    EntityIdentifier loanReassignment(Long loanId, JsonCommand command);

    EntityIdentifier bulkLoanReassignment(JsonCommand command);
}