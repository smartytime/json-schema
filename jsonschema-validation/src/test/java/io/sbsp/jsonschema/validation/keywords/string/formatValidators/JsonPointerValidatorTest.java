package io.sbsp.jsonschema.validation.keywords.string.formatValidators;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonPointerValidatorTest {
    @Test
    public void validate_WhenInvalid_ValidationFails() throws Exception {
        Optional<String> validate = new JsonPointerValidator().validate("not/a/valid   pointer");
        assertThat(validate).isPresent()
                .hasValue("invalid json-pointer syntax.  Must either be blank or start with a /");
    }

    @Test
    public void validate_WhenNull_ValidationFails() throws Exception {

        Optional<String> validate = new JsonPointerValidator().validate(null);
        assertThat(validate).isPresent()
                .hasValue("invalid json-pointer. Can't be null");
    }

    @Test
    public void validate_WhenEmpty_ValidationPasses() throws Exception {

        Optional<String> validate = new JsonPointerValidator().validate("");
        assertThat(validate).isNotPresent();
    }

    @Test
    public void validate_WhenValid_ValidationPasses() throws Exception {

        Optional<String> validate = new JsonPointerValidator().validate("/bob/is/cool");
        assertThat(validate).isNotPresent();
    }

    @Test
    public void validate_WhenDoubleSlash_ValidationFails() throws Exception {
        Optional<String> validate = new JsonPointerValidator().validate("/bob//is/cool");
        assertThat(validate).isPresent();
    }

    @Test
    public void validate_WhenEndsInSlash_ValidationFails() throws Exception {

        Optional<String> validate = new JsonPointerValidator().validate("/bob/is/cool/");
        assertThat(validate).isPresent();
    }
}