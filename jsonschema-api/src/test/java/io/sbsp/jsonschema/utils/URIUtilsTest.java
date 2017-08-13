package io.sbsp.jsonschema.utils;

import io.sbsp.jsonschema.enums.JsonSchemaType;
import org.junit.Test;

import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.spi.JsonProvider;
import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

public class URIUtilsTest {
    @Test
    public void withoutFragment_WithFragment_FragmentRemoved() throws Exception {
        final URI uriToTest = URI.create("http://www.coolsite.com/items?foo=bob#/some/pointer");
        assertThat(URIUtils.withoutFragment(uriToTest))
                .isEqualTo(URI.create("http://www.coolsite.com/items?foo=bob"));
    }

    @Test
    public void withoutFragment_EmptyFragment() throws Exception {
        final URI uriToTest = URI.create("http://www.coolsite.com/items?foo=bob#");
        assertThat(URIUtils.withoutFragment(uriToTest))
                .isEqualTo(URI.create("http://www.coolsite.com/items?foo=bob"));
    }

    @Test
    public void withoutFragment_WhenNoFragment_ThenReturnsSame() throws Exception {
        final URI uriToTest = URI.create("http://www.coolsite.com/items?foo=bob");
        assertThat(URIUtils.withoutFragment(uriToTest))
                .isSameAs(uriToTest);
    }

    @Test
    public void withoutFragment_WhenBlankURI_Blank() throws Exception {
        final URI uriToTest = URI.create("");
        assertThat(URIUtils.withoutFragment(uriToTest))
                .isSameAs(uriToTest);
    }

    @Test
    public void withoutFragment_WhenOnlyFragment_ReturnBlank() throws Exception {
        final URI uriToTest = URI.create("#");
        assertThat(URIUtils.withoutFragment(uriToTest))
                .isEqualTo(URI.create(""));
    }

    @Test
    public void isFragment_WhenFullHttpURL_ReturnsFalse() {
        final URI uriToTest = URI.create("http://www.coolsite.com/items?foo=bob#/some/pointer");
        assertThat(URIUtils.isFragmentOnly(uriToTest)).isFalse();
    }

    @Test
    public void isFragment_WhenBlank_ReturnsFalse() {
        final URI uriToTest = URI.create("");
        assertThat(URIUtils.isFragmentOnly(uriToTest)).isFalse();
    }

    @Test
    public void isFragment_WhenEmptyFragment_ReturnsTrue() {
        final URI uriToTest = URI.create("#");
        assertThat(URIUtils.isFragmentOnly(uriToTest)).isTrue();
    }

    @Test
    public void isFragment_WhenNonPointer_ReturnsTrue() {
        final URI uriToTest = URI.create("#identifier");
        assertThat(URIUtils.isFragmentOnly(uriToTest)).isTrue();
    }

    @Test
    public void isFragment_WhenQueryAndFragment_ReturnsFalse() {
        final URI uriToTest = URI.create("?foo=true#path");
        assertThat(URIUtils.isFragmentOnly(uriToTest)).isFalse();
    }

    @Test
    public void isJsonPointerFragment_WhenQueryAndFragment_ReturnsFalse() {
        final URI uriToTest = URI.create("?foo=true#/path/to");
        assertThat(URIUtils.isJsonPointer(uriToTest)).isFalse();
    }

    @Test
    public void isJsonPointerFragment_WhenEmpty_ReturnsTrue() {
        final URI uriToTest = URI.create("#");
        assertThat(URIUtils.isJsonPointer(uriToTest)).isTrue();
    }

    @Test
    public void isJsonPointerFragment_WhenForwardSlash_ReturnsTrue() {
        final URI uriToTest = URI.create("#/");
        assertThat(URIUtils.isJsonPointer(uriToTest)).isTrue();
    }

    @Test
    public void generateAbsoluteURI() {
        final JsonProvider provider = JsonProvider.provider();
        final JsonObject object = provider.createObjectBuilder()
                .add("bob", "jones")
                .add("age", 34)
                .add("sub", provider.createArrayBuilder()
                        .add(3).add(6).add(67))
                .build();

        final URI uri = URIUtils.generateUniqueURI(object);
        final URI resolve = uri.resolve("#/foofy");
        assertThat(resolve.toString()).isEqualTo(uri.toString() + "#/foofy");
    }

    @Test
    public void generateUniqueURI_ForSameRootObject_ReturnsSameURI() {
        final JsonProvider provider = JsonProvider.provider();
        final JsonObject object = provider.createObjectBuilder()
                .add("bob", "jones")
                .add("age", 34)
                .add("sub", provider.createArrayBuilder()
                        .add(3).add(6).add(67))
                .build();

        final URI uri = URIUtils.generateUniqueURI(object);
        final JsonObject fromString = JsonUtils.readJsonObject(object.toString());
        assertThat(uri).isEqualTo(URIUtils.generateUniqueURI(fromString));
    }
}