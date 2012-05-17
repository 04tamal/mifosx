package org.mifosng.platform.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mifosng.data.AppUserData;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.UserList;
import org.mifosng.data.command.UserCommand;
import org.mifosng.platform.user.service.AppUserReadPlatformService;
import org.mifosng.platform.user.service.AppUserWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/users")
@Component
@Scope("singleton")
public class UserApiResource {
	
    @Autowired
	private AppUserReadPlatformService appUserReadPlatformService;

	@Autowired
	private AppUserWritePlatformService appUserWritePlatformService;

    @GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveUsers() {
    	
		Collection<AppUserData> users = this.appUserReadPlatformService.retrieveAllUsers();

		return Response.ok().entity(new UserList(users)).build();
	}
    
	@GET
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retrieveUser(@PathParam("userId") final Long userId) {
		
		AppUserData user = this.appUserReadPlatformService.retrieveUser(userId);
    	
		return Response.ok().entity(user).build();
	}
    
    @GET
	@Path("template")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response newUserDetails() {
		
    	AppUserData newUser = this.appUserReadPlatformService.retrieveNewUserDetails();
    	
		return Response.ok().entity(newUser).build();
	}

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response createUser(UserCommand command) {

		Long userId = this.appUserWritePlatformService.createUser(command);
		return Response.ok().entity(new EntityIdentifier(userId)).build();
	}
	
	@DELETE
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response deleteUser(@PathParam("userId") final Long userId) {

		this.appUserWritePlatformService.deleteUser(userId);

		return Response.ok().build();
	}
	
	@PUT
	@Path("{userId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateUser(@PathParam("userId") final Long userId, UserCommand command) {
		
		command.setId(userId);
		
		this.appUserWritePlatformService.updateUser(command);
    	
		return Response.ok().entity(new EntityIdentifier(userId)).build();
	}
}