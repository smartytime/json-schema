package io.sbsp.jsonschema.validation;

import io.sbsp.jsonschema.Schema;

import javax.json.spi.JsonProvider;
import java.util.Optional;

public interface SchemaValidatorFactory {
    Optional<FormatValidator> getFormatValidator(String input);
    SchemaValidator createValidator(Schema schema);
    JsonProvider getJsonProvider();
}
