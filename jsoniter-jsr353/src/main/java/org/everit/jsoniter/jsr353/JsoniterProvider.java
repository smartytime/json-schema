package org.everit.jsoniter.jsr353;

import org.everit.json.BaseJsonProvider;

import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Service provider for JSON processing objects.
 * <p>
 * <p>All the methods in this class are safe for use by multiple concurrent
 * threads.
 *
 * @see ServiceLoader
 */
public class JsoniterProvider extends BaseJsonProvider {
    /**
     * Creates a JSON parser from a character stream.
     *
     * @param reader i/o reader from which JSON is to be read
     * @return a JSON parser
     */
    @Override
    public JsonParser createParser(Reader reader) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a JSON parser from the specified byte stream.
     * The character encoding of the stream is determined
     * as defined in <a href="http://tools.ietf.org/rfc/rfc7159.txt">RFC 7159
     * </a>.
     *
     * @param in i/o stream from which JSON is to be read
     * @return a JSON parser
     * @throws JsonException if encoding cannot be determined
     *                       or i/o error (IOException would be cause of JsonException)
     */
    @Override
    public JsonParser createParser(InputStream in) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a parser factory for creating {@link JsonParser} instances.
     * The factory is configured with the specified map of
     * provider specific configuration properties. Provider implementations
     * should ignore any unsupported configuration properties specified in
     * the map.
     *
     * @param config a map of provider specific properties to configure the
     *               JSON parsers. The map may be empty or null
     * @return a JSON parser factory
     */
    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a JSON generator for writing JSON text to a character stream.
     *
     * @param writer a i/o writer to which JSON is written
     * @return a JSON generator
     */
    @Override
    public JsonGenerator createGenerator(Writer writer) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a JSON generator for writing JSON text to a byte stream.
     *
     * @param out i/o stream to which JSON is written
     * @return a JSON generator
     */
    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a generator factory for creating {@link JsonGenerator} instances.
     * The factory is configured with the specified map of provider specific
     * configuration properties. Provider implementations should
     * ignore any unsupported configuration properties specified in the map.
     *
     * @param config a map of provider specific properties to configure the
     *               JSON generators. The map may be empty or null
     * @return a JSON generator factory
     */
    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a JSON reader from a character stream.
     *
     * @param reader a reader from which JSON is to be read
     * @return a JSON reader
     */
    @Override
    public JsonReader createReader(Reader reader) {
        return new JsoniterReader(reader);
    }

    /**
     * Creates a JSON reader from a byte stream. The character encoding of
     * the stream is determined as described in
     * <a href="http://tools.ietf.org/rfc/rfc7159.txt">RFC 7159</a>.
     *
     * @param in a byte stream from which JSON is to be read
     * @return a JSON reader
     */
    @Override
    public JsonReader createReader(InputStream in) {
        return new JsoniterReader(in);
    }

    /**
     * Creates a JSON writer to write a
     * JSON {@link JsonObject object} or {@link JsonArray array}
     * structure to the specified character stream.
     *
     * @param writer to which JSON object or array is written
     * @return a JSON writer
     */
    @Override
    public JsonWriter createWriter(Writer writer) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a JSON writer to write a
     * JSON {@link JsonObject object} or {@link JsonArray array}
     * structure to the specified byte stream. Characters written to
     * the stream are encoded into bytes using UTF-8 encoding.
     *
     * @param out to which JSON object or array is written
     * @return a JSON writer
     */
    @Override
    public JsonWriter createWriter(OutputStream out) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a writer factory for creating {@link JsonWriter} objects.
     * The factory is configured with the specified map of provider specific
     * configuration properties. Provider implementations should ignore any
     * unsupported configuration properties specified in the map.
     *
     * @param config a map of provider specific properties to configure the
     *               JSON writers. The map may be empty or null
     * @return a JSON writer factory
     */
    @Override
    public JsonWriterFactory createWriterFactory(Map<String, ?> config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Creates a reader factory for creating {@link JsonReader} objects.
     * The factory is configured with the specified map of provider specific
     * configuration properties. Provider implementations should ignore any
     * unsupported configuration properties specified in the map.
     *
     * @param config a map of provider specific properties to configure the
     *               JSON readers. The map may be empty or null
     * @return a JSON reader factory
     */
    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        return new JsonReaderFactory() {
            @Override
            public JsonReader createReader(Reader reader) {
                return new JsoniterReader(reader);
            }

            @Override
            public JsonReader createReader(InputStream in) {
                return new JsoniterReader(in);
            }

            @Override
            public JsonReader createReader(InputStream in, Charset charset) {
                return new JsoniterReader(in, charset);
            }

            @Override
            public Map<String, ?> getConfigInUse() {
                return Collections.emptyMap();
            }
        };
    }
}
