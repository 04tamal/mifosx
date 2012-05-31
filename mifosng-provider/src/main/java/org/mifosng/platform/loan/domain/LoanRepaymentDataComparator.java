package org.mifosng.platform.loan.domain;

import java.util.Comparator;

import org.mifosng.platform.api.data.LoanRepaymentData;


public class LoanRepaymentDataComparator implements Comparator<LoanRepaymentData> {

    @Override
    public int compare(final LoanRepaymentData o1, final LoanRepaymentData o2) {
		return o2.getDate().compareTo(o1.getDate());
    }

}
