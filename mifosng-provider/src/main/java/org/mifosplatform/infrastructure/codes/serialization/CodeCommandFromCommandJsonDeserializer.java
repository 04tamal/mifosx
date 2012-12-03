package org.mifosplatform.infrastructure.codes.serialization;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.codes.command.CodeCommand;
import org.mifosplatform.infrastructure.core.exception.InvalidJsonException;
import org.mifosplatform.infrastructure.core.serialization.FromCommandJsonDeserializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

/**
 *
 */
@Component
public final class CodeCommandFromCommandJsonDeserializer implements FromCommandJsonDeserializer<CodeCommand> {

    private final FromJsonHelper fromJsonHelper;

    @Autowired
    public CodeCommandFromCommandJsonDeserializer(final FromJsonHelper fromApiJsonHelper) {
        this.fromJsonHelper = fromApiJsonHelper;
    }

    @Override
    public CodeCommand commandFromCommandJson(final String commandAsJson) {
        return commandFromCommandJson(null, commandAsJson);
    }

    @Override
    public CodeCommand commandFromCommandJson(final Long codeId, final String commandAsJson) {
        return commandFromCommandJson(codeId, commandAsJson, false);
    }

    @Override
    public CodeCommand commandFromCommandJson(Long codeId, String commandAsJson, boolean makerCheckerApproval) {
        if (StringUtils.isBlank(commandAsJson)) { throw new InvalidJsonException(); }

        final Set<String> parametersPassedInRequest = new HashSet<String>();

        final JsonElement element = fromJsonHelper.parse(commandAsJson);
        final String name = fromJsonHelper.extractStringNamed("name", element, parametersPassedInRequest);

        return new CodeCommand(parametersPassedInRequest, makerCheckerApproval, codeId, name);
    }
}