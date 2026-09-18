package com.gestioncomercial.api.catalog.application;

public record CreateCategoryCommand(String code, String name, String description) {
}
