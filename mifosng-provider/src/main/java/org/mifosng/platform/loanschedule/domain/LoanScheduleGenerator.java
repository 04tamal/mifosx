package org.mifosng.platform.loanschedule.domain;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.LoanScheduleNewData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;

public interface LoanScheduleGenerator {

	LoanScheduleNewData generate(ApplicationCurrency applicationCurrency, 
			LoanProductRelatedDetail loanScheduleInfo,
			Integer loanTermFrequency, 
			PeriodFrequencyType loanTermFrequencyType, 
			LocalDate disbursementDate, LocalDate firstRepaymentDate, LocalDate interestCalculatedFrom);

}