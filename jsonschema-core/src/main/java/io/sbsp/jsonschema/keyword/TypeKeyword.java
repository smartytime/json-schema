package io.sbsp.jsonschema.keyword;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import io.sbsp.jsonschema.enums.JsonSchemaVersion;
import io.sbsp.jsonschema.utils.JsonSchemaGenerator;
import lombok.EqualsAndHashCode;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@EqualsAndHashCode
public class TypeKeyword implements SchemaKeyword {

    private final Set<JsonSchemaType> types;
    private final Set<JsonSchemaType> disallowedTypes;

    public TypeKeyword(JsonSchemaType first, JsonSchemaType... additionalTypes) {
        checkNotNull(first, "first must not be null");
        checkNotNull(additionalTypes, "additionalTypes must not be null");
        types = ImmutableSet.copyOf(EnumSet.of(first, additionalTypes));
        if (types.isEmpty()) {
            disallowedTypes = Collections.emptySet();
        } else {
            disallowedTypes = Sets.complementOf(this.types);
        }
    }

    public TypeKeyword(Set<JsonSchemaType> requiredTypes) {
        checkNotNull(requiredTypes, "requiredTypes must not be null");
        this.types = Collections.unmodifiableSet(requiredTypes);
        if (types.isEmpty()) {
            disallowedTypes = Collections.emptySet();
        } else {
            disallowedTypes = Sets.complementOf(this.types);
        }
    }

    public Set<JsonSchemaType> getTypes() {
        return types;
    }

    public Set<JsonSchemaType> getDisallowedTypes() {
        return disallowedTypes;
    }

    public TypeKeyword withAdditionalType(JsonSchemaType another) {
        checkNotNull(another, "another must not be null");
        return new TypeKeyword(another, types.toArray(new JsonSchemaType[0]));
    }

    @Override
    public void writeJson(KeywordInfo<?> keyword, JsonSchemaGenerator generator, JsonSchemaVersion version) {
        if (types.size() == 1) {
            generator.write(Keywords.TYPE.key(), types.iterator().next().toString());
        } else {
            generator.writeKey(Keywords.TYPE.key());
            generator.writeStartArray();
            for (JsonSchemaType type : types) {
                generator.write(type.toString());
            }
            generator.writeEnd();
        }
    }

    @Override
    public String toString() {
        return types.toString();
    }
}
