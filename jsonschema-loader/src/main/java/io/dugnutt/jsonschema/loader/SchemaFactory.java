package io.dugnutt.jsonschema.loader;

import io.dugnutt.jsonschema.loader.internal.DefaultSchemaClient;
import io.dugnutt.jsonschema.six.ReferenceSchema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import io.dugnutt.jsonschema.six.Schema;

import javax.json.JsonObject;
import javax.json.spi.JsonProvider;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Main entry point for getting new schemas
 */
@Getter
@AllArgsConstructor
public class SchemaFactory {

    public static final Charset UTF8 = Charset.forName("UTF-8");

    @Wither
    private final JsonProvider provider;
    @Wither
    private final SchemaClient httpClient;
    @Wither
    private final Charset charset;
    @Wither
    private final URI resolutionScope;

    private final Map<String, ReferenceSchema.Builder> referenceSchemas;

    public static SchemaFactory schemaFactory() {
        return schemaFactory(JsonProvider.provider());
    }

    public static SchemaFactory schemaFactory(JsonProvider jsonProvider) {
        return new SchemaFactory(jsonProvider, new DefaultSchemaClient(), UTF8, null, new HashMap<>());
    }

    // public static SchemaFactory schemaFactory() {
    //     return schemaFactory(new );
    // }

    public Schema load(JsonObject schemaJson) {
        checkNotNull(schemaJson, "schemaJson must not be null");
        return SchemaLoader.builder()
                .resolutionScope(resolutionScope)
                .httpClient(httpClient)
                .pointerSchemas(referenceSchemas)
                .schemaJson(schemaJson)
                .provider(JsonProvider.provider())
                .build()
                .load()
                .build();
    }

    public Schema load(InputStream inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        return load(provider.createReader(inputJson).readObject());
    }

    public Schema load(String inputJson) {
        checkNotNull(inputJson, "inputStream must not be null");
        return load(provider.createReader(new StringReader(inputJson)).readObject());
    }
}
