package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.application.CreateCategoryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "code must not be blank")
        @Size(max = 30, message = "code must not exceed 30 characters")
        String code,

        @NotBlank(message = "name must not be blank")
        @Size(max = 100, message = "name must not exceed 100 characters")
        String name,

        @Size(max = 250, message = "description must not exceed 250 characters")
        String description
) {

    CreateCategoryCommand toCommand() {
        return new CreateCategoryCommand(code, name, description);
    }
}
