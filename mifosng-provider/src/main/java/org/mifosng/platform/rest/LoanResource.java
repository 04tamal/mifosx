package org.mifosng.platform.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.LoanRepaymentData;
import org.mifosng.data.command.AdjustLoanTransactionCommand;
import org.mifosng.data.command.LoanStateTransitionCommand;
import org.mifosng.data.command.LoanTransactionCommand;
import org.mifosng.data.command.UndoLoanDisbursalCommand;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.ReadPlatformServiceImpl;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Path("/protected/loan")
@Component
@Scope("singleton")
public class LoanResource {

	private final static Logger logger = LoggerFactory.getLogger(ReadPlatformServiceImpl.class);
	
	@Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private WritePlatformService writePlatformService;

	@POST
	@Path("disburse")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response disburseLoanApplication(final LoanStateTransitionCommand command) {

		try {
			EntityIdentifier identifier = this.writePlatformService.disburseLoan(command);

			return Response.ok().entity(identifier).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@POST
	@Path("undodisbursal")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response undoLoanDisbursal(final UndoLoanDisbursalCommand command) {

		try {
			EntityIdentifier identifier = this.writePlatformService.undloLoanDisbursal(command);

			return Response.ok().entity(identifier).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("{loanId}/repayment")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveNewRepaymentDetails(@PathParam("loanId") final Long loanId) {

		try {
			LoanRepaymentData loanRepaymentData = this.readPlatformService.retrieveNewLoanRepaymentDetails(loanId);

			return Response.ok().entity(loanRepaymentData).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@GET
	@Path("{loanId}/repayment/{repaymentId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveRepaymentDetails(@PathParam("loanId") final Long loanId, @PathParam("repaymentId") final Long repaymentId) {

		try {
			LoanRepaymentData loanRepaymentData = this.readPlatformService.retrieveLoanRepaymentDetails(loanId, repaymentId);

			return Response.ok().entity(loanRepaymentData).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}

	@POST
	@Path("repayment")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response makeLoanRepayment(final LoanTransactionCommand command) {

		try {
			EntityIdentifier identifier = this.writePlatformService.makeLoanRepayment(command);

			return Response.ok().entity(identifier).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();
			ErrorResponse err = new ErrorResponse("unknown.error", "error",
					e.getMessage());
			allErrors.add(err);

			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity(new ErrorResponseList(allErrors)).build());
		}
	}
	
	@POST
	@Path("repayment/adjust")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response adjustLoanTransaction(final AdjustLoanTransactionCommand command) {

		try {
			EntityIdentifier identifier = this.writePlatformService.adjustLoanTransaction(command);

			return Response.ok().entity(identifier).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();
			ErrorResponse err = new ErrorResponse("unknown.error", "error",
					e.getMessage());
			allErrors.add(err);

			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity(new ErrorResponseList(allErrors)).build());
		}
	}
	
	@GET
	@Path("{loanId}/waive")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response retrieveNewWaiverDetails(@PathParam("loanId") final Long loanId) {

		try {
			LoanRepaymentData loanRepaymentData = this.readPlatformService.retrieveNewLoanWaiverDetails(loanId);

			return Response.ok().entity(loanRepaymentData).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		}
	}
	
	@POST
	@Path("waive")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response waiveLoanAmount(final LoanTransactionCommand command) {
		
		try {
			EntityIdentifier identifier = this.writePlatformService.waiveLoanAmount(command);

			return Response.ok().entity(identifier).build();
		} catch (UnAuthenticatedUserException e) {
			throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).build());
		} catch (AccessDeniedException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.no.permission", "id");
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		} catch (ApplicationDomainRuleException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getErrors())).build());
		} catch (NewDataValidationException e) {
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(new ErrorResponseList(e.getValidationErrors())).build());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);

			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();
			ErrorResponse err = new ErrorResponse("unknown.error", "error",
					e.getMessage());
			allErrors.add(err);

			throw new WebApplicationException(Response
					.status(Status.BAD_REQUEST)
					.entity(new ErrorResponseList(allErrors)).build());
		}
	}
}