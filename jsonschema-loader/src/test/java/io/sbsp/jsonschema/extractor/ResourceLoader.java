package io.sbsp.jsonschema.extractor;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.InputStream;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static javax.json.spi.JsonProvider.provider;

public class ResourceLoader {

    public static final ResourceLoader DEFAULT = new ResourceLoader("/tests/");

    private final String rootPath;

    public ResourceLoader(String rootPath) {
        this.rootPath = requireNonNull(rootPath, "rootPath cannot be null");
    }

    public JsonObject readObj(String relPath) {
        return provider().createReader(getStream(relPath)).readObject();
    }

    public JsonObjectBuilder readObjectWithBuilder(String relPath) {
        return provider().createObjectBuilder(
                provider().createReader(getStream(relPath)).readObject()
        );
    }

    public InputStream getStream(String relPath) {
        String absPath = rootPath + relPath;
        InputStream rval = getClass().getResourceAsStream(absPath);
        if (rval == null) {
            throw new IllegalArgumentException(
                    format("failed to load resource by relPath [%s].\n"
                    + "InputStream by path [%s] is null", relPath, absPath));
        }
        return rval;
    }

}
