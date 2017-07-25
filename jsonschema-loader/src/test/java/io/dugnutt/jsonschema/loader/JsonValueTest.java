package io.dugnutt.jsonschema.loader;

import junitparams.JUnitParamsRunner;
import org.junit.runner.RunWith;

/**
 * @author erosb
 */
@RunWith(JUnitParamsRunner.class)
public class JsonValueTest {

    // static final LoadingState emptyLs = new LoadingState(SchemaLoader.stringKeywordsBuilder()
    //         .rootSchemaJson(JsonUtils.blankObject())
    //         .schemaJson(JsonUtils.blankObject()));
    //
    // public static final JsonValue INT = JsonValue.of(3, emptyLs);
    //
    // public static final JsonValue OBJ = new JsonObject(emptyMap(), emptyLs);
    //
    // public static final JsonValue FLS = JsonValue.of(false, emptyLs);
    //
    // public static final JsonValue TRU = JsonValue.of(true, emptyLs);
    //
    // public static final JsonValue STR = JsonValue.of("string", emptyLs);
    //
    // public static final JsonValue ARR = JsonValue.of(asList(true, 42), emptyLs);
    //
    // @Rule
    // public ExpectedException exc = ExpectedException.none();
    //
    // @Test
    // public void multiplexFailureForNullValue() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type is one of Boolean or String, found: null");
    //     JsonValue.of(null, emptyLs).canBe(String.class, s -> {
    //     })
    //             .or(Boolean.class, b -> {
    //             })
    //             .requireAny();
    // }
    //
    // @Test
    // public void multiplexerFailure() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type is one of Boolean or String, found: Integer");
    //     INT.canBe(String.class, str -> {
    //     })
    //             .or(Boolean.class, bool -> {
    //             })
    //             .requireAny();
    // }
    //
    // @Test
    // public void multiplexerWithObject() {
    //     Consumer<JsonObject> objConsumer = mockConsumer();
    //     OBJ.canBe(JsonObject.class, objConsumer).requireAny();
    //     verify(objConsumer).accept(OBJ.requireObject());
    // }
    //
    // @Test
    // public void multiplexerWithPrimitives() {
    //     Consumer<String> consumer = mockConsumer();
    //     STR.canBe(String.class, consumer)
    //             .or(Boolean.class, bool -> {
    //             })
    //             .requireAny();
    //     verify(consumer).accept(STR.requireString());
    // }
    //
    // @Test
    // public void requireArrayFailure() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type: JsonArray, found: JsonObject");
    //     OBJ.requireArray();
    // }
    //
    // @Test
    // public void requireArrayWithMapping() {
    //     Integer actual = ARR.requireArray(arr -> arr.length());
    //     assertEquals(2, actual.intValue());
    // }
    //
    // @Test
    // public void requireBooleanFailure() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type: Boolean, found: String");
    //     STR.requireBoolean();
    // }
    //
    // @Test
    // public void requireBooleanSuccess() {
    //     assertTrue(TRU.requireBoolean());
    // }
    //
    // @Test
    // public void requireBooleanWithMapper() {
    //     assertTrue(FLS.requireBoolean(bool -> !bool));
    // }
    //
    // @Test
    // public void requireIntegerFailure() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type: Integer, found: JsonArray");
    //     ARR.requireInteger();
    // }
    //
    // @Test
    // public void requireIntegerSuccess() {
    //     assertEquals(3, INT.requireInteger().intValue());
    // }
    //
    // @Test
    // public void requireIntegerWithMapper() {
    //     assertEquals(4, INT.requireInteger(i -> i.intValue() + 1).intValue());
    // }
    //
    // @Test
    // public void requireNumberFailure() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type: Number, found: JsonObject");
    //     OBJ.requireNumber();
    // }
    //
    // @Test
    // public void requireNumberSuccess() {
    //     assertEquals(3.14, JsonValue.of(3.14, emptyLs).requireNumber());
    // }
    //
    // @Test
    // public void requireNumberWithMapping() {
    //     assertEquals(Integer.valueOf(3), JsonValue.of(3.14, emptyLs).requireNumber(d -> Integer.valueOf(d.intValue())));
    // }
    //
    // @Test
    // public void requireObjectFailure() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type: JsonObject, found: String");
    //     STR.requireObject();
    // }
    //
    // @Test
    // public void requireObjectSuccess() {
    //     JsonObject actual = OBJ.requireObject();
    //     assertSame(OBJ, actual);
    // }
    //
    // @Test
    // public void requireObjectWithMapping() {
    //     String actual = OBJ.requireObject(obj -> "hello");
    //     assertEquals("hello", actual);
    // }
    //
    // @Test
    // public void requireStringFailure() {
    //     exc.expect(SchemaException.class);
    //     exc.expectMessage("#: expected type: String, found: Boolean");
    //     TRU.requireString();
    // }
    //
    // @Test
    // public void requireStringSuccess() {
    //     assertEquals("string", STR.requireString());
    // }
    //
    // @Test
    // public void requireStringWithMapper() {
    //     Integer actual = JsonValue.of("42", emptyLs).requireString(e -> Integer.valueOf(e));
    //     assertEquals(Integer.valueOf(42), actual);
    // }
    //
    // @Test
    // @Parameters(method = "providerTestFactory")
    // public void testFactory(Object raw, Class<?> expectedRetType) {
    //     assertThat(JsonValue.of(raw, emptyLs), is(instanceOf(expectedRetType)));
    // }
    //
    // @Test
    // public void testMultiplexer() {
    //     Consumer<JsonArray> arrConsumer = mockConsumer();
    //     Consumer<JsonObject> objConsumer = mockConsumer();
    //     ARR.canBe(JsonArray.class, arrConsumer)
    //             .or(JsonObject.class, objConsumer)
    //             .requireAny();
    //     verify(arrConsumer).accept(ARR.requireArray());
    //     verify(objConsumer, never()).accept(any());
    // }
    //
    // private Object[] par(Object raw, Class<?> expectedRetType) {
    //     return new Object[] {raw, expectedRetType};
    // }
    //
    // private Object[] providerTestFactory() {
    //     return new Object[] {
    //             par(null, JsonValue.class),
    //             par(emptyMap(), JsonObject.class),
    //             par(emptyList(), JsonArray.class),
    //             par(new JsonObject(), JsonObject.class),
    //             par(new JSONArray(), JsonArray.class)
    //     };
    // }
}
