package com.isa.transaction.frontend.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Validador JSF personalizado para la validacion del formato de identificacion del cliente.
 * Valida que la identificacion del cliente no este vacia y este dentro de las restricciones de longitud.
 */
@FacesValidator("clientIdentificationValidator")
public class ClientIdentificationValidator implements Validator<String> {

    private static final int MAX_LENGTH = 50;

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value == null || value.trim().isEmpty()) {
            // Esto es manejado por required="true" pero como una salvaguarda:
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
    }
}
