package com.gestioncomercial.api.catalog.application;

public record UpdateCategoryCommand(String code, String name, String description) {
}
