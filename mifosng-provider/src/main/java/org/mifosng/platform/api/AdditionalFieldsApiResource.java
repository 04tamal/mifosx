package org.mifosng.platform.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.mifosng.platform.InvalidSqlException;
import org.mifosng.platform.ReadExtraDataAndReportingService;
import org.mifosng.platform.api.data.AdditionalFieldsSetData;
import org.mifosng.platform.api.data.ApiParameterError;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.GenericResultsetData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.exceptions.PlatformApiDataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/additionalfields")
@Component
@Scope("singleton")
public class AdditionalFieldsApiResource {

	private final static Logger logger = LoggerFactory.getLogger(AdditionalFieldsApiResource.class);

	@Autowired
	private ReadExtraDataAndReportingService readExtraDataAndReportingService;
	
	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String datasets(@QueryParam("type") final String type, @Context final UriInfo uriInfo) {

		List<AdditionalFieldsSetData> result = this.readExtraDataAndReportingService.retrieveExtraDatasetNames(type);

		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		return this.apiDataConversionService.convertAdditionalFieldsSetDataToJson(prettyPrint, result.toArray(new AdditionalFieldsSetData[result.size()]));
	}

	@GET
	@Path("{type}/{set}/{id}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String extraData(@PathParam("type") final String type,
			@PathParam("set") final String set, @PathParam("id") final Long id,
			@Context final UriInfo uriInfo) {

		try {
			GenericResultsetData result = this.readExtraDataAndReportingService.retrieveExtraData(type, set, id);

			boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
			return this.apiDataConversionService.convertGenericResultsetDataToJson(prettyPrint, result);
		} catch (InvalidSqlException e) {
			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError(
					"extradata.invalid.sql", "The sql is invalid.", "sql",
					e.getSql());
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}

	@POST
	@Path("{type}/{set}/{id}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response saveExtraData(@PathParam("type") final String type,
			@PathParam("set") final String set, @PathParam("id") final Long id,
			final String jsonRequestBody) {

		try {

			Map<String, String> queryParams = new HashMap<String, String>();
			String pValue = "";
			String pName;
			try {
				JSONObject jsonObj = new JSONObject(jsonRequestBody);
				JSONArray jsonArr = jsonObj.names();
				if (jsonArr != null) {
					for (int i = 0; i < jsonArr.length(); i++) {
						pName = (String) jsonArr.get(i);
						pValue = jsonObj.getString(pName);
						logger.info(pName + " - " + pValue);
						queryParams.put(pName, pValue);
					}
				} else {
					throw new WebApplicationException(Response
							.status(Status.BAD_REQUEST)
							.entity("JSON body empty").build());
				}
			} catch (JSONException e) {
				throw new WebApplicationException(Response
						.status(Status.BAD_REQUEST)
						.entity("JSON body is wrong").build());
			}

			this.readExtraDataAndReportingService.updateExtraData(type, set,
					id, queryParams);

			EntityIdentifier entityIdentifier = new EntityIdentifier(
					Long.valueOf(id));

			return Response.ok().entity(entityIdentifier).build();
		} catch (InvalidSqlException e) {

			List<ApiParameterError> dataValidationErrors = new ArrayList<ApiParameterError>();
			ApiParameterError error = ApiParameterError.parameterError(
					"extradata.invalid.sql", "The sql is invalid.", "sql",
					e.getSql());
			dataValidationErrors.add(error);
			throw new PlatformApiDataValidationException(
					"validation.msg.validation.errors.exist",
					"Validation errors exist.", dataValidationErrors);
		}
	}
}