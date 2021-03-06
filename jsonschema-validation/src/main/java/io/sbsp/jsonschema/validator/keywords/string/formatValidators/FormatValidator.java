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
package io.sbsp.jsonschema.validator.keywords.string.formatValidators;

import io.sbsp.jsonschema.enums.FormatType;

import java.util.Optional;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * Implementations perform the validation against the "format" keyword (see JSON Schema spec section
 * 7).
 */
@FunctionalInterface
public interface FormatValidator {

    /**
     * No-operation implementation (never throws {always returns {@link Optional#empty()}).
     */
    FormatValidator NONE = subject -> Optional.empty();

    /**
     * Provides the name of this format.
     * <p>
     * Unless specified otherwise we use name to recognize string schemas using this format.
     * <p>
     * The default implementation of this method returns {@code "unnamed-format"}. It is strongly
     * recommended for implementations to give a more meaningful name by overriding this method.
     *
     * @return the format name.
     */
    default String formatName() {
        return "unnamed-format";
    }

    /**
     * Implementation-specific validation of {@code subject}. If a validation error occurs then
     * implementations should return a programmer-friendly error message as a String wrapped in an
     * Optional. If the validation succeeded then {@link Optional#empty() an empty optional} should be
     * returned.
     *
     * @param subject the string to be validated
     * @return an {@code Optional} wrapping the error message if a validation error occured, otherwise
     * {@link Optional#empty() an empty optional}.
     */
    Optional<String> validate(String subject);

    /**
     * Static factory method for {@code FormatValidator} implementations supporting the
     * {@code getFormatName}s mandated by the json schema spec.
     * <p>
     * <ul>
     * <li>date-time</li>
     * <li>email</li>
     * <li>hostname</li>
     * <li>uri</li>
     * <li>ipv4</li>
     * <li>ipv6</li>
     * </ul>
     *
     * @param format one of the 6 built-in formats.
     * @return a {@code FormatValidator} implementation handling the {@code getFormatName} format.
     */
    static FormatValidator forFormat(final FormatType format) {
        requireNonNull(format, "format cannot be null");
        String formatName = format.toString();
        switch (formatName) {

            case "date-time":
                return new DateTimeFormatValidator();
            case "time":
                return new TimeFormatValidator();
            case "date":
                return new DateFormatValidator();
            case "email":
                return new EmailFormatValidator();
            case "hostname":
                return new HostnameFormatValidator();
            case "host-name":
                return new HostnameFormatValidator();
            case "uri":
                return new URIFormatValidator();
            case "ipv4":
                return new IPV4Validator();
            case "ip-address":
                return new IPV4Validator();
            case "ipv6":
                return new IPV6Validator();
            case "json-pointer":
                return new JsonPointerValidator();
            case "uri-template":
                return new URITemplateFormatValidator();
            case "uri-reference":
                return new URIReferenceFormatValidator();
            case "uriref":
                return new URIReferenceFormatValidator();
            case "style":
                return new NoopFormatValidator("style");
            case "color":
                return new ColorFormatValidator();
            case "phone":
                return new PhoneFormatValidator();
            case "regex":
                return new RegexFormatValidator();
            case "utc-millisec":
                return new PatternBasedValidator(Pattern.compile("^[0-9]+$"), "utc-millisex");
            default:
                throw new IllegalArgumentException("unsupported format: " + formatName);
        }
    }
}
