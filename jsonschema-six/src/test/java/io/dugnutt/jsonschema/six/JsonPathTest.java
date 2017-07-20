package io.dugnutt.jsonschema.six;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class JsonPathTest {

    @Test
    public void testParseURI_PathPartIsUrlEncodedSlash() {
        String uri = "#/this%2For%7E1that";
        JsonPath jsonPath = JsonPath.parseFromURIFragment(uri);
        assertPath("First Pass", jsonPath);
        assertPath("From parsed URI", JsonPath.parseFromURIFragment(jsonPath.toURIFragment()));
        assertPath("From parsed JsonPointer", JsonPath.parseJsonPointer(jsonPath.toJsonPointer()));

        final JsonPath child = jsonPath.child("eric~is/not/so/bad");
        String message = "Child";
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(child.toStringPath())
                    .as(message + ": Unescaped raw")
                    .hasSize(2)
                    .containsExactly("this/or/that", "eric~is/not/so/bad");
            s.assertThat(child.toURIFragment().toString())
                    .as(message + ": Correct URL Encoding")
                    .isEqualTo("#/this~1or~1that/eric~0is~1not~1so~1bad");
            s.assertThat(child.toJsonPointer())
                    .as(message + ": Correct JSON-Pointer encoding")
                    .isEqualTo("/this~1or~1that/eric~0is~1not~1so~1bad");
        });
    }

    private void assertPath(String message, JsonPath jsonPath) {
        SoftAssertions.assertSoftly(s -> {
            s.assertThat(jsonPath.toStringPath())
                    .as(message + ": Unescaped raw")
                    .hasSize(1)
                    .containsExactly("this/or/that");
            s.assertThat(jsonPath.toURIFragment().toString())
                    .as(message + ": Correct URL Encoding")
                    .isEqualTo("#/this~1or~1that");
            s.assertThat(jsonPath.toJsonPointer())
                    .as(message + ": Correct JSON-Pointer encoding")
                    .isEqualTo("/this~1or~1that");
        });
    }


}