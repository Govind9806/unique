package com.example.uniqueAproovaResidency.common;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessRuleException {
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super("RESOURCE_NOT_FOUND", String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue), HttpStatus.NOT_FOUND);
    }
}
