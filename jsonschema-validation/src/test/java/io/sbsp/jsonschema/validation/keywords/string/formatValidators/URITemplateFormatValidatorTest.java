package io.sbsp.jsonschema.validation.keywords.string.formatValidators;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class URITemplateFormatValidatorTest {

    @Test
    public void testValidUriTemplate() {
        String template = "/{foo:1}{/foo,thing*}{?query,test2}";
        Optional<String> validate = new URITemplateFormatValidator().validate(template);
        assertThat(validate).isNotPresent();
    }

    @Test
    public void testInvalidUriTemplate() {
        String template = "/{foo::1}{/foo,thing*}{?query,test2}";
        Optional<String> validate = new URITemplateFormatValidator().validate(template);
        assertThat(validate).isPresent();
    }

}