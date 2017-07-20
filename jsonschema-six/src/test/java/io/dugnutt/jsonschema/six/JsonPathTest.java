package io.dugnutt.jsonschema.six;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPathTest {

    @Test
    public void testParseURI_PathPartIsUrlEncodedSlash() {
        String uri = "#/this%2Forthat";
        JsonPath jsonPath = JsonPath.parseFromURIFragment(uri);
        assertThat(jsonPath.toStringPath())
                .hasSize(1)
                .containsExactly("this/that");
    }


}