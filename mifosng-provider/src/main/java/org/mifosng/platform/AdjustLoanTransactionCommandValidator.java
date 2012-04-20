package org.mifosng.platform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.platform.exceptions.NewDataValidationException;

public class AdjustLoanTransactionCommandValidator {

	private final AdjustLoanTransactionCommand command;

	public AdjustLoanTransactionCommandValidator(AdjustLoanTransactionCommand command) {
		this.command = command;
	}

	public void validate() {
		List<ErrorResponse> dataValidationErrors = new ArrayList<ErrorResponse>();
		
		if (command.getLoanId() == null || command.getLoanId().longValue() <= 0) {
			ErrorResponse error = new ErrorResponse("validation.msg.loan.id.is.invalid", "loanId");
			dataValidationErrors.add(error);
		}
		
		if (command.getRepaymentId() == null || command.getRepaymentId().longValue() <= 0) {
			ErrorResponse error = new ErrorResponse("validation.msg.loan.transaction.id.is.invalid", "repaymentId");
			dataValidationErrors.add(error);
		}
		
		if (command.getPaymentDate() == null) {
			ErrorResponse error = new ErrorResponse("validation.msg.loan.repayment.date.cannot.be.blank", "paymentDate");
			dataValidationErrors.add(error);
		}
		
		if (command.getPaymentAmount() == null || command.getPaymentAmount().doubleValue() < 0) {
			ErrorResponse error = new ErrorResponse("validation.msg.loan.adjustment.must.be.zero.or.greater", "paymentAmount");
			dataValidationErrors.add(error);
		}
		
		if (StringUtils.isNotBlank(command.getComment()) && command.getComment().length() > 1000) {
			ErrorResponse error = new ErrorResponse("validation.msg.note.exceeds.max.length", "comment");
			dataValidationErrors.add(error);
		}
		
		if (!dataValidationErrors.isEmpty()) {
			throw new NewDataValidationException(dataValidationErrors, "Data validation errors exist.");
		}
	}

}
