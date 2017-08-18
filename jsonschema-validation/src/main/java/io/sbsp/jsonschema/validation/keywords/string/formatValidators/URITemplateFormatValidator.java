/*
 * Copyright (C) 2017 SBSP (http://sbsp.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.sbsp.jsonschema.validation.keywords.string.formatValidators;

import com.damnhandy.uri.template.MalformedUriTemplateException;
import com.damnhandy.uri.template.UriTemplate;
import io.sbsp.jsonschema.validation.FormatValidator;

import java.util.Optional;

/**
 * Implementation of the "uri" format value.
 */
public class URITemplateFormatValidator implements FormatValidator {

    @Override
    public Optional<String> validate(final String subject) {
        try {
            UriTemplate.fromTemplate(subject);
            return Optional.empty();
        } catch (MalformedUriTemplateException e) {
            return Optional.of(String.format("[%s] is not a valid URI template", subject));
        }
    }

    @Override public String formatName() {
        return "uri-template";
    }
}
