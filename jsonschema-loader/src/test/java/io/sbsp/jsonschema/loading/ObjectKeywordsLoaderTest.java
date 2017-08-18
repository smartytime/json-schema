package io.sbsp.jsonschema.loading;

import io.sbsp.jsonschema.Draft6Schema;
import io.sbsp.jsonschema.Schema;
import io.sbsp.jsonschema.SchemaException;
import io.sbsp.jsonschema.enums.JsonSchemaType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;
import java.util.Optional;

import static io.sbsp.jsonschema.JsonSchemaProvider.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author erosb
 */
public class ObjectKeywordsLoaderTest extends BaseLoaderTest {

    public static final Draft6Schema BOOLEAN_SCHEMA = schemaBuilder().type(JsonSchemaType.BOOLEAN)
            .build()
            .asDraft6();

    public ObjectKeywordsLoaderTest() {
        super("objecttestschemas.json");
    }

    @Rule
    public ExpectedException expExc = ExpectedException.none();

    @Test
    public void objectSchema() {
        Draft6Schema actual =  getSchemaForKey("objectSchema");
        
        final Map<String, Schema> propertySchemas = actual.getProperties();
        assertThat(propertySchemas).isNotNull();
        assertThat(propertySchemas).hasSize(2);
        final Draft6Schema boolProp = actual.getPropertySchema("boolProp");
        assertThat(boolProp).isNotNull();

        assertThat(actual.getRequiredProperties()).hasSize(2);
        assertThat(actual.getMinProperties()).isEqualTo(2);
        assertThat(actual.getMaxProperties()).isEqualTo(3);
    }

    @Test(expected = SchemaException.class)
    public void objectInvalidAdditionalProperties() {
        getSchemaForKey("objectInvalidAdditionalProperties");
    }

    @Test
    public void objectWithAdditionalPropSchema() {
        Draft6Schema actual = getSchemaForKey("objectWithAdditionalPropSchema");
        final Optional<Draft6Schema> addtlPropSchema = actual.getAdditionalPropertiesSchema();
        assertThat(addtlPropSchema)
                .isPresent()
                .hasValue(BOOLEAN_SCHEMA);
    }

    @Test
    public void objectWithPropDep() {
        Draft6Schema actual =  getSchemaForKey("objectWithPropDep");
        assertThat(actual.getPropertyDependencies().get("isIndividual"))
                .isNotNull()
                .hasSize(1);
    }

    @Test
    public void objectWithSchemaDep() {
        final Draft6Schema actual = getSchemaForKey("objectWithSchemaDep");
        assertThat(actual.getPropertySchemaDependencies()).hasSize(1);
    }

    @Test
    public void patternProperties() {
        Draft6Schema actual = getSchemaForKey("patternProperties");
        assertThat(actual).isNotNull();
        assertThat(actual.getPatternProperties()).hasSize(2);
    }

    @Test(expected = SchemaException.class)
    public void invalidDependency() {
        getSchemaForKey("invalidDependency");
    }

    @Test
    public void emptyDependencyList() {
        getSchemaForKey("emptyDependencyList");
    }
}
