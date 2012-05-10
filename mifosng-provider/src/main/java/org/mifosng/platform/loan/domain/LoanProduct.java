package org.mifosng.platform.loan.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.StringUtils;
import org.mifosng.data.command.LoanProductCommand;
import org.mifosng.platform.currency.domain.MonetaryCurrency;
import org.mifosng.platform.currency.domain.Money;
import org.mifosng.platform.infrastructure.AbstractAuditableCustom;
import org.mifosng.platform.organisation.domain.Organisation;
import org.mifosng.platform.user.domain.AppUser;

/**
 * Loan products allow for categorisation of an organisations loans into something meaningful to them.
 * 
 * They provide a means of simplifying creation/maintenance of loans.
 * They can also allow for product comparison to take place when reporting.
 * 
 * They allow for constraints to be added at product level.
 */
@Entity
@Table(name = "portfolio_product_loan")
public class LoanProduct extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private final Organisation                organisation;

    @Column(name = "name", nullable = false)
    private String                      name;

    @Column(name = "description")
    private String                      description;

    @Embedded
    private final LoanProductRelatedDetail            loanProductRelatedDetail;

    public LoanProduct() {
        this.organisation = null;
        this.name = null;
        this.description = null;
        this.loanProductRelatedDetail = null;
    }

    public LoanProduct(final Organisation organisation, final String name, final String description, final MonetaryCurrency currency, final BigDecimal defaultPrincipal,
            final BigDecimal defaultNominalInterestRatePerPeriod, final PeriodFrequencyType interestPeriodFrequencyType, final BigDecimal defaultAnnualNominalInterestRate, 
            final InterestMethod interestMethod, final InterestCalculationPeriodMethod interestCalculationPeriodMethod, 
            final Integer repayEvery, final PeriodFrequencyType repaymentFrequencyType, final Integer defaultNumberOfInstallments, final AmortizationMethod amortizationMethod,
            final BigDecimal inArrearsTolerance,
			final boolean flexibleRepaymentSchedule,
			final boolean interestRebateAllowed) {
        this.organisation = organisation;
        this.name = name.trim();
        if (StringUtils.isNotBlank(description)) {
            this.description = description.trim();
        } else {
            this.description = null;
        }
        
        this.loanProductRelatedDetail = new LoanProductRelatedDetail(currency,
        		defaultPrincipal, defaultNominalInterestRatePerPeriod, interestPeriodFrequencyType, defaultAnnualNominalInterestRate, 
        		interestMethod, interestCalculationPeriodMethod,
				repayEvery, repaymentFrequencyType, defaultNumberOfInstallments, amortizationMethod, inArrearsTolerance, flexibleRepaymentSchedule, interestRebateAllowed);
    }

    public Organisation getOrganisation() {
        return this.organisation;
    }
    
    public MonetaryCurrency getCurrency() {
    	return this.loanProductRelatedDetail.getCurrency();
    }
    
	public Money getInArrearsTolerance() {
		return this.loanProductRelatedDetail.getInArrearsTolerance();
	}

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getRepayEvery() {
        return this.loanProductRelatedDetail.getRepayEvery();
    }

    public boolean isFlexibleRepaymentSchedule() {
        return this.loanProductRelatedDetail.isFlexibleRepaymentSchedule();
    }

	public boolean isInterestRebateAllowed() {
		return this.loanProductRelatedDetail.isInterestRebateAllowed();
	}

	public boolean identifiedBy(final String identifier) {
		return identifier.equalsIgnoreCase(this.name);
	}

	public BigDecimal getDefaultNominalInterestRatePerPeriod() {
		return this.loanProductRelatedDetail.getNominalInterestRatePerPeriod();
	}

	public PeriodFrequencyType getInterestPeriodFrequencyType() {
		return this.loanProductRelatedDetail.getInterestPeriodFrequencyType();
	}

	public BigDecimal getDefaultAnnualNominalInterestRate() {
		return this.loanProductRelatedDetail.getAnnualNominalInterestRate();
	}

	public InterestMethod getInterestMethod() {
		return this.loanProductRelatedDetail.getInterestMethod();
	}

	public PeriodFrequencyType getRepaymentPeriodFrequencyType() {
		return this.loanProductRelatedDetail.getRepaymentPeriodFrequencyType();
	}

	public Integer getDefaultNumberOfRepayments() {
		return this.loanProductRelatedDetail.getNumberOfRepayments();
	}

	public AmortizationMethod getAmortizationMethod() {
		return this.loanProductRelatedDetail.getAmortizationMethod();
	}
	
	public void update(LoanProductCommand command) {
		this.name = command.getName();
		this.description = command.getDescription();
		this.loanProductRelatedDetail.update(command);
	}
}