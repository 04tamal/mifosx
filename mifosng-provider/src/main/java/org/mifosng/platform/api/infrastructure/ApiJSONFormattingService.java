package org.mifosng.platform.api.infrastructure;

import javax.ws.rs.core.MultivaluedMap;

public interface ApiJSONFormattingService {

	String convertRequest(Object dataObject, String allowedFieldList,
			String selectedFields, MultivaluedMap<String, String> queryParams);

}
