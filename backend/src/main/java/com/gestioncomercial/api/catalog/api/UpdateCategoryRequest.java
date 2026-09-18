package com.gestioncomercial.api.catalog.api;

import com.gestioncomercial.api.catalog.application.UpdateCategoryCommand;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(
        @NotBlank(message = "code must not be blank")
        @Size(max = 30, message = "code must not exceed 30 characters")
        String code,

        @NotBlank(message = "name must not be blank")
        @Size(max = 100, message = "name must not exceed 100 characters")
        String name,

        @Size(max = 250, message = "description must not exceed 250 characters")
        String description
) {

    UpdateCategoryCommand toCommand() {
        return new UpdateCategoryCommand(code, name, description);
    }
}
