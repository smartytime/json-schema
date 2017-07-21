package io.dugnutt.jsonschema.six;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

public class Unescapers {

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final List<UnescapeSequence> replacements = new ArrayList<>();

        public Builder addUnescape(String matcher, String replacement) {
            checkNotNull(matcher, "matcher must not be null");
            checkNotNull(replacement, "replacement must not be null");
            replacements.add(new UnescapeSequence(matcher, replacement));
            return this;
        }

        public Unescaper build() {
            return new DefaultUnescaper(replacements);
        }
    }

    public static class UnescapeSequence {
        private final String searchString;
        private final String replacement;

        public UnescapeSequence(String searchString, String replacement) {
            checkNotNull(searchString, "searchString must not be null");
            checkNotNull(replacement, "replacement must not be null");
            this.searchString = searchString;
            this.replacement = replacement;
        }
    }

    public static class DefaultUnescaper implements Unescaper {
        private final UnescapeSequence[] unescapers;

        public DefaultUnescaper(List<UnescapeSequence> unescapers) {
            checkNotNull(unescapers, "unescapers must not be null");
            this.unescapers = unescapers.toArray(new UnescapeSequence[0]);
        }

        @Override
        public String unescape(CharSequence string) {
            if (string == null) {
                return null;
            }
            StringBuilder builder = new StringBuilder(string);
            for (UnescapeSequence unescaper : unescapers) {
                int pos = -1;
                int l = unescaper.searchString.length();
                while((pos = builder.indexOf(unescaper.searchString, pos)) > -1) {
                    builder.replace(pos, pos + l, unescaper.replacement);
                }
            }
            return builder.toString();
        }
    }
}
