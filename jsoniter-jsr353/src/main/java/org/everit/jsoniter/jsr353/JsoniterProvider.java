package org.everit.jsoniter.jsr353;

import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.spi.JsonProvider;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;

public class JsoniterProvider extends JsonProvider {

    @Override
    public JsonParser createParser(Reader reader) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonParser createParser(InputStream in) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonGenerator createGenerator(Writer writer) {

        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonReader createReader(Reader reader) {
        return new JsoniterReader(reader);
    }

    @Override
    public JsonReader createReader(InputStream in) {
        return new JsoniterReader(in);
    }

    @Override
    public JsonWriter createWriter(Writer writer) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonWriter createWriter(OutputStream out) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonWriterFactory createWriterFactory(Map<String, ?> config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public JsonObjectBuilder createObjectBuilder() {
        return null;
    }

    @Override
    public JsonArrayBuilder createArrayBuilder() {
        return null;
    }

    @Override
    public JsonBuilderFactory createBuilderFactory(Map<String, ?> config) {
        return null;
    }
}
