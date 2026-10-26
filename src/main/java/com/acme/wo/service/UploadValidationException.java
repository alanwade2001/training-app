package com.acme.wo.service;

import com.acme.wo.model.ValidationError;

import java.util.List;

public class UploadValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public UploadValidationException(
            List<ValidationError> errors) {

        super("The uploaded YAML is invalid");
        this.errors = errors;
    }

    public List<ValidationError> errors() {
        return errors;
    }
}
