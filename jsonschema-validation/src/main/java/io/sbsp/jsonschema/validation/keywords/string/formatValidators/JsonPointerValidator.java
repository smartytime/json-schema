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

import io.sbsp.jsonschema.JsonPath;
import io.sbsp.jsonschema.validation.FormatValidator;

import java.util.Optional;

/**
 * Implementation of the "email" format value.
 */
public class JsonPointerValidator implements FormatValidator {

    @Override
    public String formatName() {
        return "json-pointer";
    }

    @Override
    public Optional<String> validate(final String subject) {
        try {
            JsonPath.parseJsonPointer(subject);
            return Optional.empty();
        } catch (NullPointerException e) {
            return Optional.of("invalid json-pointer. Can't be null");
        } catch (IllegalArgumentException e) {
            return Optional.of(e.getMessage());
        }
    }
}
