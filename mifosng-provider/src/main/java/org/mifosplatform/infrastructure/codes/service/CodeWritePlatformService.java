package org.mifosplatform.infrastructure.codes.service;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;

public interface CodeWritePlatformService {

    Long createCode(JsonCommand command);

    EntityIdentifier updateCode(Long codeId, JsonCommand command);

    EntityIdentifier deleteCode(Long codeId, JsonCommand command);
}