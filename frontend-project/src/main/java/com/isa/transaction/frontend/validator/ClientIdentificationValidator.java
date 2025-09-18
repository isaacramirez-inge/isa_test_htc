package com.isa.transaction.frontend.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Custom JSF validator for client identification format validation.
 * Validates that client identification is not empty and within length constraints.
 */
@FacesValidator("clientIdentificationValidator")
public class ClientIdentificationValidator implements Validator<String> {

    private static final int MAX_LENGTH = 50;

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value == null || value.trim().isEmpty()) {
            // This is handled by required="true" but as a safeguard:
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Validación",
                "La identificación del cliente no puede estar vacía."
            );
            throw new ValidatorException(message);
        }

        String trimmedValue = value.trim();

        if (trimmedValue.length() > MAX_LENGTH) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Longitud",
                "La identificación del cliente no puede exceder los " + MAX_LENGTH + " caracteres."
            );
            throw new ValidatorException(message);
        }
        
        // Example of a pattern validation that could be added:
        // if (!trimmedValue.matches("^[a-zA-Z0-9-]*$")) {
        //     FacesMessage message = new FacesMessage(
        //         FacesMessage.SEVERITY_ERROR,
        //         "Error de Formato",
        //         "La identificación del cliente solo puede contener letras, números y guiones."
        //     );
        //     throw new ValidatorException(message);
        // }
    }
}
