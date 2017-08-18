package io.sbsp.jsonschema;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;
import static javax.json.spi.JsonProvider.provider;

public class ResourceLoader {

    private final Class loadFrom;

    public ResourceLoader(Class loadFrom) {
        checkNotNull(loadFrom, "rootPath must not be null");
        this.loadFrom = loadFrom;
    }

    public ResourceLoader() {
        this(ResourceLoader.class);
    }

    public InputStream getStream(String relPath) {
        InputStream rval = loadFrom.getResourceAsStream(relPath);
        if (rval == null) {
            throw new IllegalArgumentException(format("failed to load resource [%s].", relPath));
        }
        return rval;
    }

    public JsonObject readJsonObject(String relPath) {
        return provider().createReader(getStream(relPath)).readObject();
    }

    public JsonObjectBuilder readObjectWithBuilder(String relPath) {
        return provider().createObjectBuilder(
                readJsonObject(relPath)
        );
    }

    public static ResourceLoader resourceLoader() {
        return new ResourceLoader();
    }

    public static ResourceLoader resourceLoaderForInstance(Object base) {
        return new ResourceLoader(base.getClass());
    }
}
