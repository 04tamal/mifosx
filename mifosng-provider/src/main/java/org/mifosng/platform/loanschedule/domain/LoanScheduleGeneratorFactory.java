package org.mifosng.platform.loanschedule.domain;

import org.mifosng.platform.loan.domain.InterestMethod;

public interface LoanScheduleGeneratorFactory {

    LoanScheduleGenerator create(InterestMethod interestMethod);

}
