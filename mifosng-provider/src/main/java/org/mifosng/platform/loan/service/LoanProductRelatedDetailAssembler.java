package org.mifosng.platform.loan.service;

import java.math.BigDecimal;

import org.mifosng.platform.api.commands.CalculateLoanScheduleCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.exceptions.LoanProductNotFoundException;
import org.mifosng.platform.loan.domain.AmortizationMethod;
import org.mifosng.platform.loan.domain.InterestCalculationPeriodMethod;
import org.mifosng.platform.loan.domain.InterestMethod;
import org.mifosng.platform.loan.domain.LoanProduct;
import org.mifosng.platform.loan.domain.LoanProductRelatedDetail;
import org.mifosng.platform.loan.domain.LoanProductRepository;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.loanschedule.domain.AprCalculator;
import org.springframework.stereotype.Service;

@Service
public class LoanProductRelatedDetailAssembler {
	
	private final LoanProductRepository loanProductRepository;
	private final AprCalculator aprCalculator;

	public LoanProductRelatedDetailAssembler(
			final LoanProductRepository loanProductRepository,
			final AprCalculator aprCalculator) {
		this.loanProductRepository = loanProductRepository;
		this.aprCalculator = aprCalculator;
	}

	public LoanProductRelatedDetail assembleFrom(final CalculateLoanScheduleCommand command) {
		
		final BigDecimal principalAmount = command.getPrincipal();
		final BigDecimal defaultNominalInterestRatePerPeriod = command.getInterestRatePerPeriod();
		final PeriodFrequencyType interestPeriodFrequencyType = PeriodFrequencyType.fromInt(command.getInterestRateFrequencyType());
		final InterestMethod interestMethod = InterestMethod.fromInt(command.getInterestType());
		final InterestCalculationPeriodMethod interestCalculationPeriodMethod = InterestCalculationPeriodMethod.fromInt(command.getInterestCalculationPeriodType());
		
		final Integer defaultNumberOfInstallments = command.getNumberOfRepayments();
		final Integer repayEvery = command.getRepaymentEvery();
		final PeriodFrequencyType repaymentFrequencyType = PeriodFrequencyType.fromInt(command.getRepaymentFrequencyType());
		
		final AmortizationMethod amortizationMethod = AmortizationMethod.fromInt(command.getAmortizationType());
		
		final BigDecimal defaultAnnualNominalInterestRate = this.aprCalculator.calculateFrom(interestPeriodFrequencyType, defaultNominalInterestRatePerPeriod);
		
		final LoanProduct loanProduct = this.loanProductRepository.findOne(command.getProductId());
		if (loanProduct == null) {
			throw new LoanProductNotFoundException(command.getProductId());
		}
		
		final MonetaryCurrency currency = loanProduct.getCurrency();
		
		// in arrerars tolerance isnt relevant when auto-calculating a loan schedule 
		final BigDecimal inArrearsTolerance = BigDecimal.ZERO;
				
		return new LoanProductRelatedDetail(currency, principalAmount,
				defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, 
				interestMethod, interestCalculationPeriodMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, 
				inArrearsTolerance);
	}
}