package io.sbsp.jsonschema;

import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.keyword.KeywordInfo;
import io.sbsp.jsonschema.keyword.SchemaKeyword;

import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import static io.sbsp.jsonschema.utils.JsonUtils.prettyPrintGeneratorFactory;

public interface Schema {

    SchemaLocation getLocation();

    URI getId();

    URI getSchemaURI();

    String getTitle();

    String getDescription();

    JsonSchemaVersion getVersion();

    JsonGenerator toJson(final JsonGenerator writer, JsonSchemaVersion version);

    Map<KeywordInfo<?>, SchemaKeyword> getKeywords();

    default JsonGenerator toJson(final JsonGenerator writer) {
        return toJson(writer, getVersion());
    }

    default URI getAbsoluteURI() {
        return getLocation().getUniqueURI();
    }

    default URI getPointerFragmentURI() {
        return getLocation().getJsonPointerFragment();
    }

    default Schema asVersion(JsonSchemaVersion version) {
        switch (version) {
            case Draft3:
                return asDraft3();
            case Draft4:
                return asDraft4();
            case Draft5:
                return asDraft4();
            case Draft6:
                return asDraft6();
            default:
                throw new IllegalArgumentException("Unable to determine version from: " + version);
        }
    }

    Draft6Schema asDraft6();
    Draft3Schema asDraft3();
    Draft4Schema asDraft4();

    default String toString(boolean pretty, JsonSchemaVersion version) {
        final StringWriter stringWriter = new StringWriter();
        final JsonGenerator generator;
        if (pretty) {
            generator = prettyPrintGeneratorFactory().createGenerator(stringWriter);
        } else {
            generator = JsonProvider.provider().createGenerator(stringWriter);
        }
        this.toJson(generator, version);
        generator.flush();
        return stringWriter.toString();
    }
}
