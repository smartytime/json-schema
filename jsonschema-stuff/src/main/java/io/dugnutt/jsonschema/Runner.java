package io.dugnutt.jsonschema;

public class Runner {
    public static void main(String[] args) {
        User.builder().mutateSchema(schema->schema.setName("Bob").build().);

    }
}
