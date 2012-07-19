package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.FundCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.fund.service.FundReadPlatformService;
import org.mifosng.platform.fund.service.FundWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/funds")
@Component
@Scope("singleton")
public class FundsApiResource {

	@Autowired
	private FundReadPlatformService readPlatformService;

	@Autowired
	private FundWritePlatformService writePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveFunds(@Context final UriInfo uriInfo) {

		MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
		String commaSerperatedParameters = "";
		if (queryParams.getFirst("fields") != null) {
			commaSerperatedParameters = queryParams.getFirst("fields");
		}
		
		boolean prettyPrint = false;
		if (queryParams.getFirst("pretty") != null) {
			String prettyPrintValue = queryParams.getFirst("pretty");
			prettyPrint = "true".equalsIgnoreCase(prettyPrintValue);
		} 
		
		Set<String> responseParameters = new HashSet<String>(Arrays.asList(commaSerperatedParameters.split("\\s*,\\s*")));
		
		Collection<FundData> funds = this.readPlatformService.retrieveAllFunds();
		
		return this.apiDataConversionService.covertFundDataToJson(prettyPrint, responseParameters, funds.toArray(new FundData[funds.size()]));
	}

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createFund(final String jsonRequestBody) {
		
		FundCommand command = this.apiDataConversionService.convertJsonToFundCommand(null, jsonRequestBody);
		
		Long fundId = this.writePlatformService.createFund(command);

		return Response.ok().entity(new EntityIdentifier(fundId)).build();
	}

	@GET
	@Path("{fundId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retreiveOffice(@PathParam("fundId") final Long fundId, @Context UriInfo uriInfo) {

		FundData fund = this.readPlatformService.retrieveFund(fundId);

		return this.apiDataConversionService.covertFundDataToJson(fund);
	}

	@PUT
	@Path("{fundId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateFund(@PathParam("fundId") final Long fundId, final String jsonRequestBody) {

		FundCommand command = this.apiDataConversionService.convertJsonToFundCommand(fundId, jsonRequestBody);
		
		Long entityId = this.writePlatformService.updateFund(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
}