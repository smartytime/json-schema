package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.JsonValueWithPath;
import io.sbsp.jsonschema.Schema;
import lombok.SneakyThrows;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.io.StringReader;

import static com.google.common.base.Preconditions.checkNotNull;
import static io.sbsp.jsonschema.JsonValueWithPath.fromJsonValue;

/**
 * Main interface for reading a schema from an input source.
 */
public interface SchemaReader {

    @SneakyThrows
    default Schema readSchema(InputStream inputStream) {
        checkNotNull(inputStream, "schema inputStream must not be null");
        try {
            final JsonObject schemaJsonObject = getProvider().createReader(inputStream).readObject();
            return readSchema(schemaJsonObject);
        } finally {
            inputStream.close();
        }
    }

    default Schema readSchema(JsonObject jsonObject) {
        checkNotNull(jsonObject, "schema jsonObject must not be null");
        return readSchema(jsonObject, new LoadingReport());
    }

    default Schema readSchema(JsonObject jsonObject, LoadingReport loadingReport) {
        checkNotNull(jsonObject, "schema jsonObject must not be null");
        final JsonValueWithPath jsonDocument = fromJsonValue(jsonObject);
        final LoadingReport report = new LoadingReport();
        return getLoader().schemaBuilder(jsonDocument, report).build();
    }

    default Schema readSchema(String inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        final StringReader jsonReader = new StringReader(inputJson);
        final JsonObject jsonSchema = getProvider().createReader(jsonReader).readObject();
        return readSchema(jsonSchema);
    }

    /**
     * The {@link JsonProvider} to use for loading json documents
     */
    JsonProvider getProvider();

    /**
     * The schemaLoader to use for the lower-level loading operations.
     */
    SchemaLoader getLoader();
}
