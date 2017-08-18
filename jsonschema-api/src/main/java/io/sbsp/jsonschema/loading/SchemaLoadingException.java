package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaException;
import lombok.Getter;

import java.net.URI;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Exception raised during the loading of a schema, if the provided document is invalid.
 */
@Getter
public class SchemaLoadingException extends SchemaException {

    private final LoadingReport report;
    private final Schema schema;

    public SchemaLoadingException(URI location, LoadingReport report, Schema built) {
        super(location, checkNotNull(report).toString());
        this.report = report;
        this.schema = built;
    }
}
