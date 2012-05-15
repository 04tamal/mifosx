package org.mifosng.platform.loan.service;

import static org.mifosng.platform.Specifications.loansThatMatch;

import javax.sql.DataSource;

import org.joda.time.LocalDate;
import org.mifosng.data.CurrencyData;
import org.mifosng.data.DerivedLoanData;
import org.mifosng.data.LoanAccountData;
import org.mifosng.data.MoneyData;
import org.mifosng.platform.currency.domain.ApplicationCurrency;
import org.mifosng.platform.currency.domain.ApplicationCurrencyRepository;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.loan.domain.Loan;
import org.mifosng.platform.loan.domain.LoanRepository;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.mifosng.platform.user.domain.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoanReadPlatformServiceImpl implements LoanReadPlatformService {

	private final SimpleJdbcTemplate jdbcTemplate;
	private final PlatformSecurityContext context;
	private final LoanRepository loanRepository;
	private final ApplicationCurrencyRepository applicationCurrencyRepository;

	@Autowired
	public LoanReadPlatformServiceImpl(final PlatformSecurityContext context, final DataSource dataSource, 
			final LoanRepository loanRepository, final ApplicationCurrencyRepository applicationCurrencyRepository) {
		this.context = context;
		this.loanRepository = loanRepository;
		this.applicationCurrencyRepository = applicationCurrencyRepository;
		this.jdbcTemplate = new SimpleJdbcTemplate(dataSource);
	}
	
	@Override
	public LoanAccountData retrieveLoanAccountDetails(Long loanId) {

		// TODO - OPTIMISE - prefer jdbc sql approach to return only what we
		// need of loan information.
		AppUser currentUser = context.authenticatedUser();

		Loan loan = this.loanRepository.findOne(loansThatMatch(
				currentUser.getOrganisation(), loanId));

		ApplicationCurrency currency = this.applicationCurrencyRepository
				.findOneByCode(loan.getLoanRepaymentScheduleDetail()
						.getPrincipal().getCurrencyCode());

		CurrencyData currencyData = new CurrencyData(currency.getCode(),
				currency.getName(), currency.getDecimalPlaces(),
				currency.getDisplaySymbol(), currency.getNameCode());

		LoanAccountData loanData = convertToData(loan, currencyData);

		return loanData;
	}
	
	private LoanAccountData convertToData(final Loan realLoan,
			CurrencyData currencyData) {

		DerivedLoanData loanData = realLoan.deriveLoanData(currencyData);

		LocalDate expectedDisbursementDate = null;
		if (realLoan.getExpectedDisbursedOnDate() != null) {
			expectedDisbursementDate = new LocalDate(
					realLoan.getExpectedDisbursedOnDate());
		}

		Money loanPrincipal = realLoan.getLoanRepaymentScheduleDetail()
				.getPrincipal();
		MoneyData principal = MoneyData.of(currencyData,
				loanPrincipal.getAmount());

		Money loanArrearsTolerance = realLoan.getInArrearsTolerance();
		MoneyData tolerance = MoneyData.of(currencyData,
				loanArrearsTolerance.getAmount());

		Money interestRebate = realLoan.getInterestRebateOwed();
		MoneyData interestRebateOwed = MoneyData.of(currencyData,
				interestRebate.getAmount());

		boolean interestRebateOutstanding = false; // realLoan.isInterestRebateOutstanding(),

		// permissions
		boolean waiveAllowed = loanData.getSummary().isWaiveAllowed(tolerance)
				&& realLoan.isNotClosed();
		boolean undoDisbursalAllowed = realLoan.isDisbursed()
				&& realLoan.isOpenWithNoRepaymentMade();
		boolean makeRepaymentAllowed = realLoan.isDisbursed()
				&& realLoan.isNotClosed();

		LocalDate loanStatusDate = realLoan.getLoanStatusSinceDate();

		boolean rejectAllowed = realLoan.isNotApproved()
				&& realLoan.isNotDisbursed() && realLoan.isNotClosed();
		boolean withdrawnByApplicantAllowed = realLoan.isNotDisbursed()
				&& realLoan.isNotClosed();
		boolean undoApprovalAllowed = realLoan.isApproved()
				&& realLoan.isNotClosed();
		boolean disbursalAllowed = realLoan.isApproved()
				&& realLoan.isNotDisbursed() && realLoan.isNotClosed();

		return new LoanAccountData(realLoan.isClosed(), realLoan.isOpen(),
				realLoan.isOpenWithRepaymentMade(), interestRebateOutstanding,
				realLoan.isSubmittedAndPendingApproval(),
				realLoan.isWaitingForDisbursal(), undoDisbursalAllowed,
				makeRepaymentAllowed, rejectAllowed,
				withdrawnByApplicantAllowed, undoApprovalAllowed,
				disbursalAllowed, realLoan.getLoanStatusDisplayName(),
				loanStatusDate, realLoan.getId(), realLoan.getExternalId(),
				realLoan.getLoanProduct().getName(),
				realLoan.getClosedOnDate(), realLoan.getSubmittedOnDate(),
				realLoan.getApprovedOnDate(), expectedDisbursementDate,
				realLoan.getDisbursedOnDate(),
				realLoan.getExpectedMaturityDate(),
				realLoan.getExpectedFirstRepaymentOnDate(),
				realLoan.getInterestCalculatedFromDate(), principal, realLoan
						.getLoanRepaymentScheduleDetail()
						.getAnnualNominalInterestRate(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getNominalInterestRatePerPeriod(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getInterestPeriodFrequencyType().getValue(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getInterestPeriodFrequencyType().toString(), realLoan
						.getLoanRepaymentScheduleDetail().getInterestMethod()
						.getValue(), realLoan.getLoanRepaymentScheduleDetail()
						.getInterestMethod().toString(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getAmortizationMethod().getValue(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getAmortizationMethod().toString(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getNumberOfRepayments(), realLoan
						.getLoanRepaymentScheduleDetail().getRepayEvery(),
				realLoan.getLoanRepaymentScheduleDetail()
						.getRepaymentPeriodFrequencyType().getValue(), realLoan
						.getLoanRepaymentScheduleDetail()
						.getRepaymentPeriodFrequencyType().toString(),
				tolerance, loanData, waiveAllowed, interestRebateOwed);
	}
	
}