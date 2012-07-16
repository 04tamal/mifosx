package org.mifosng.platform.api;

import java.math.BigDecimal;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.joda.time.LocalDate;
import org.mifosng.platform.api.commands.BranchMoneyTransferCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.OfficeTransactionData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/officetransactions")
@Component
@Scope("singleton")
public class OfficeTransactionsApiResource {

	@Autowired
	private OfficeWritePlatformService writePlatformService;
	
	@Autowired
	private OfficeReadPlatformService readPlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;
	
	@GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public String newOfficeTransactionDetails(@Context UriInfo uriInfo) {

		OfficeTransactionData officeTransactionData = this.readPlatformService
				.retrieveNewOfficeTransactionDetails();

		String filterName = "myFilter";
		String defaultFieldList = "transactionDate";
		String allowedFieldList = "currencyOptions,allowedOffices";
		String selectedFields = defaultFieldList + "," + allowedFieldList;
		return this.jsonFormattingService.convertRequest(officeTransactionData,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response transferMoneyFrom(final BranchMoneyTransferCommand command) {

		LocalDate transactionLocalDate = apiDataConversionService.convertFrom(command.getTransactionDate(), "transactionDate", command.getDateFormat());
		command.setTransactionLocalDate(transactionLocalDate);
		
		Locale clientLocale = this.apiDataConversionService.localeFromString(command.getLocale());

		BigDecimal transactionAmountValue = apiDataConversionService.convertFrom(command.getTransactionAmount(), "transactionAmount", clientLocale);
		command.setTransactionAmountValue(transactionAmountValue);
		
		Long id = this.writePlatformService.externalBranchMoneyTransfer(command);
		return Response.ok().entity(new EntityIdentifier(id)).build();
	}
}