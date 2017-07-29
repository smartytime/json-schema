package io.dugnutt.jsonschema.validator;

import io.dugnutt.jsonschema.six.JsonSchemaKeyword;
import io.dugnutt.jsonschema.six.JsonValueWithLocation;
import io.dugnutt.jsonschema.six.Schema;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static io.dugnutt.jsonschema.validator.ValidationError.collectErrors;

public class ValidationReport {

    private final List<ValidationError> errors = new ArrayList<>();
    private boolean foundError;

    public List<ValidationError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public void addError(ValidationError validationError) {
        errors.add(validationError);
        foundError = true;
    }

    public boolean addReport(Schema schema, JsonValueWithLocation subject, JsonSchemaKeyword keyword, String message, ValidationReport report) {
        final List<ValidationError> errors = report.getErrors();
        if (errors.size() > 0) {
            addError(ValidationError.validationBuilder()
                    .schemaLocation(schema.getPointerFragmentURI())
                    .violatedSchema(schema)
                    .causingExceptions(errors)
                    .keyword(keyword)
                    .message(message)
                    .code("validation.keyword." + keyword.key())
                    .pointerToViolation(subject.getPath())
                    .build());
            return false;
        }
        return true;
    }

    public boolean addReport(Schema schema, JsonValueWithLocation subject, ValidationReport report) {
        Optional<ValidationError> error = collectErrors(schema, subject.getPath(), report.getErrors());
        error.ifPresent(this::addError);
        return !error.isPresent();
    }

    public ValidationReport createChildReport() {
        return new ValidationReport();
    }

    public boolean isValid() {
        return !foundError;
    }

    public String toString() {
        StringWriter string = new StringWriter();
        writeTo(string);
        return string.toString();
    }

    public void writeTo(OutputStream writer) {
        writeTo(new OutputStreamWriter(writer));
    }

    public void writeTo(Writer writer) {
        PrintWriter printer = new PrintWriter(writer);
        if (errors.size() > 0) {
            printer.println("###############################################");
            printer.println("####              ERRORS                   ####");
            printer.println("###############################################");
        }
        errors.forEach(e -> this.toStringErrors(e, printer));
    }

    private void toStringErrors(ValidationError error, PrintWriter printer) {
        if (error.getCauses().size() > 0) {
            error.getCauses().forEach(e -> toStringErrors(e, printer));
        }
        printer.println(error.getPointerToViolation());
        String keywordValue = error.getKeyword() == null ? "Unknown" : error.getKeyword().key();
        printer.println("\tKeyword: " + keywordValue);
        printer.println("\tMessage: " + error.getMessage());
        printer.println("\tSchema : " + error.getSchemaLocation());
        printer.println("");
    }
}
