package org.mifosng.platform.api;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.loanproduct.service.LoanProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loanproducts")
@Component
@Scope("singleton")
public class LoanProductApiResource {

	private String defaultFieldList = "principal,inArrearsTolerance,numberOfRepayments,repaymentEvery,interestRatePerPeriod,annualInterestRate,repaymentFrequencyType,interestRateFrequencyType,amortizationType,interestType,interestCalculationPeriodType";
	private String allowedFieldList = "currencyOptions,amortizationTypeOptions,interestTypeOptions,interestCalculationPeriodTypeOptions,repaymentFrequencyTypeOptions,interestRateFrequencyTypeOptions";
	private String filterName = "myFilter";

	@Autowired
	private LoanProductReadPlatformService loanProductReadPlatformService;

	@Autowired
	private LoanProductWritePlatformService loanProductWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createLoanProduct(LoanProductCommand command) {
		
		Locale clientApplicationLocale = this.apiDataConversionService.localeFromString(command.getLocale());
		
		BigDecimal principalValue = this.apiDataConversionService.convertFrom(command.getPrincipal(), "principal", clientApplicationLocale);
		BigDecimal inArrearsToleranceValue = this.apiDataConversionService.convertFrom(command.getInArrearsTolerance(), "inArrearsTolerance", clientApplicationLocale);
		BigDecimal interestRatePerPeriodValue = this.apiDataConversionService.convertFrom(command.getInterestRatePerPeriod(),"interestRatePerPeriod", clientApplicationLocale);
		
		Integer digitsAfterDecimalValue = this.apiDataConversionService.convertToInteger(command.getDigitsAfterDecimal(), "digitsAfterDecimal", clientApplicationLocale);
		Integer repaymentEveryValue = this.apiDataConversionService.convertToInteger(command.getRepaymentEvery(), "repaymentEvery", clientApplicationLocale);
		Integer numberOfRepaymentsValue = this.apiDataConversionService.convertToInteger(command.getNumberOfRepayments(), "numberOfRepayments", clientApplicationLocale);
		
		command.setPrincipalValue(principalValue);
		command.setInArrearsToleranceValue(inArrearsToleranceValue);
		command.setInterestRatePerPeriodValue(interestRatePerPeriodValue);
		command.setDigitsAfterDecimalValue(digitsAfterDecimalValue);
		command.setRepaymentEveryValue(repaymentEveryValue);
		command.setNumberOfRepaymentsValue(numberOfRepaymentsValue);

		EntityIdentifier entityIdentifier = this.loanProductWritePlatformService
				.createLoanProduct(command);

		return Response.ok().entity(entityIdentifier).build();
	}

	@GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveAllLoanProducts(@Context UriInfo uriInfo) {

		Collection<LoanProductData> products = this.loanProductReadPlatformService
				.retrieveAllLoanProducts();

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(products, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
	}

	/*
	 * see
	 * http://stackoverflow.com/questions/5250074/what-uri-can-be-used-to-request
	 * -a-default-resource
	 */
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveNewLoanProductDetails(@Context UriInfo uriInfo) {

		LoanProductData loanProduct = this.loanProductReadPlatformService
				.retrieveNewLoanProductDetails();

		String selectedFields = defaultFieldList + "," + allowedFieldList;
		return this.jsonFormattingService.convertRequest(loanProduct,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@GET
	@Path("{productId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON })
	public String retrieveLoanProductDetails(
			@PathParam("productId") final Long productId,
			@Context UriInfo uriInfo) {

		LoanProductData loanProduct = this.loanProductReadPlatformService
				.retrieveLoanProduct(productId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(loanProduct,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@PUT
	@Path("{productId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON })
	public Response updateLoanProduct(@PathParam("productId") final Long productId, final LoanProductCommand command) {

		Locale clientApplicationLocale = this.apiDataConversionService.localeFromString(command.getLocale());
		
		BigDecimal principalValue = this.apiDataConversionService.convertFrom(command.getPrincipal(), "principal", clientApplicationLocale);
		BigDecimal inArrearsToleranceValue = this.apiDataConversionService.convertFrom(command.getInArrearsTolerance(), "inArrearsTolerance", clientApplicationLocale);
		BigDecimal interestRatePerPeriodValue = this.apiDataConversionService.convertFrom(command.getInterestRatePerPeriod(),"interestRatePerPeriod", clientApplicationLocale);
		
		Integer digitsAfterDecimalValue = this.apiDataConversionService.convertToInteger(command.getDigitsAfterDecimal(), "digitsAfterDecimal", clientApplicationLocale);
		Integer repaymentEveryValue = this.apiDataConversionService.convertToInteger(command.getRepaymentEvery(), "repaymentEvery", clientApplicationLocale);
		Integer numberOfRepaymentsValue = this.apiDataConversionService.convertToInteger(command.getNumberOfRepayments(), "numberOfRepayments", clientApplicationLocale);
		
		command.setPrincipalValue(principalValue);
		command.setInArrearsToleranceValue(inArrearsToleranceValue);
		command.setInterestRatePerPeriodValue(interestRatePerPeriodValue);
		command.setDigitsAfterDecimalValue(digitsAfterDecimalValue);
		command.setRepaymentEveryValue(repaymentEveryValue);
		command.setNumberOfRepaymentsValue(numberOfRepaymentsValue);

		command.setId(productId);
		EntityIdentifier entityIdentifier = this.loanProductWritePlatformService.updateLoanProduct(command);

		return Response.ok().entity(entityIdentifier).build();
	}
}