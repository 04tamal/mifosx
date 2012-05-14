package org.mifosng.platform.api.user;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mifosng.data.AppUserData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ErrorResponseList;
import org.mifosng.data.UserList;
import org.mifosng.data.command.ChangePasswordCommand;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.ReadPlatformService;
import org.mifosng.platform.WritePlatformService;
import org.mifosng.platform.exceptions.ApplicationDomainRuleException;
import org.mifosng.platform.exceptions.NewDataValidationException;
import org.mifosng.platform.exceptions.UnAuthenticatedUserException;
import org.mifosng.platform.user.domain.AppUser;
import org.mifosng.platform.user.domain.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Path("/v1/users")
@Component
@Scope("singleton")
public class UserApiResource {
	
	private final static Logger logger = LoggerFactory.getLogger(UserApiResource.class);

    @Autowired
	private ReadPlatformService readPlatformService;

	@Autowired
	private WritePlatformService writePlatformService;

	@Autowired
	private AppUserRepository appUserRepository;
	
	private void hardcodeUserIntoSecurityContext() {
		AppUser currentUser = this.appUserRepository.findOne(Long.valueOf(1));
    	
    	Authentication auth = new UsernamePasswordAuthenticationToken(currentUser, currentUser, currentUser.getAuthorities());
		SecurityContext context = SecurityContextHolder.getContext();
		context.setAuthentication(auth);
	}
	
    @GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveUsers() {
    	
    	hardcodeUserIntoSecurityContext();
    	
		Collection<AppUserData> users = this.readPlatformService.retrieveAllUsers();

		return Response.ok().entity(new UserList(users)).build();
	}
    
	@GET
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveUser(@PathParam("userId") final Long userId) {
		
		hardcodeUserIntoSecurityContext();
		
		AppUserData user = this.readPlatformService.retrieveUser(userId);
    	
		return Response.ok().entity(user).build();
	}
    
    @GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response newUserDetails() {
		
    	hardcodeUserIntoSecurityContext();
    	AppUserData newUser = this.readPlatformService.retrieveNewUserDetails();
    	
		return Response.ok().entity(newUser).build();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response createUser(@Context HttpServletRequest request) {

//		try {
			String selectedItems = request.getParameter("selectedItems");
			final UserCommand command = new UserCommand();
//			command.setSelectedItems(Arrays.asList(a));
			
			hardcodeUserIntoSecurityContext();
			Long userId = this.writePlatformService.createUser(command);
			return Response.ok().entity(new EntityIdentifier(userId)).build();
//		} catch (UsernameAlreadyExistsException e) {
//			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();
//			ErrorResponse err = new ErrorResponse("validation.msg.username.already.exists.in.organisation", "username", command.getUsername());
//			allErrors.add(err);
//
//			throw new WebApplicationException(Response
//					.status(Status.BAD_REQUEST)
//					.entity(new ErrorResponseList(allErrors)).build());
//		} catch (PlatformEmailSendException e) {
//			List<ErrorResponse> allErrors = new ArrayList<ErrorResponse>();
//			ErrorResponse err = new ErrorResponse("error.msg.user.email.invalid","email", "Email is invalid.");
//			allErrors.add(err);
//			throw new WebApplicationException(Response
//					.status(Status.BAD_REQUEST)
//					.entity(new ErrorResponseList(allErrors)).build());
//		}
	}
	
	@DELETE
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response deleteUser(@PathParam("userId") final Long userId) {

		try {
			this.writePlatformService.deleteUser(userId);

			return Response.ok().build();
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
	
	@PUT
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateUser(@PathParam("userId") final Long userId, UserCommand command) {
		
		hardcodeUserIntoSecurityContext();
		
//		UserCommand command = new UserCommand();
//		command.setSelectedItems(new ArrayList<String>(commandEile.getSelectedItems()));
		command.setId(userId);
		
		this.writePlatformService.updateUser(command);
    	
		return Response.ok().entity(new EntityIdentifier(userId)).build();
	}
	
	@GET
	@Path("user/current")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveCurrentUser() {

		try {
			hardcodeUserIntoSecurityContext();
			
			AppUserData user = this.readPlatformService.retrieveCurrentUser();
	    	
			return Response.ok().entity(user).build();
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
	@Path("user/current")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateCurrentUser(UserCommand command) {

		try {
			hardcodeUserIntoSecurityContext();
			
			Long userId = this.writePlatformService.updateCurrentUser(command);

			return Response.ok().entity(new EntityIdentifier(userId)).build();
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
		} catch (DataIntegrityViolationException e) {
			ErrorResponse errorResponse = new ErrorResponse("error.msg.username.already.exists.in.organisation", "username", command.getUsername());
			ErrorResponseList list = new ErrorResponseList(Arrays.asList(errorResponse));
			throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(list).build());
		}
	}
	
	@POST
	@Path("user/current/password")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Response updateCurrentUserPassword(ChangePasswordCommand command) {

		try {
			hardcodeUserIntoSecurityContext();
			
			Long userId = this.writePlatformService.updateCurrentUserPassword(command);

			return Response.ok().entity(new EntityIdentifier(userId)).build();
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
}