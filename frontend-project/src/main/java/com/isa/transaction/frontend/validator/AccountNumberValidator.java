package com.isa.transaction.frontend.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

/**
 * Validador JSF personalizado para la validacion del formato del numero de cuenta
 * Valida que el numero de cuenta tenga entre 5 y 25 caracteres
 */
@FacesValidator("accountNumberValidator")
public class AccountNumberValidator implements Validator<String> {

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 25;

    @Override
    public void validate(FacesContext context, UIComponent component, String value) throws ValidatorException {
        if (value == null || value.trim().isEmpty()) {
            return; // Dejar que la validacion de requerido maneje los valores vacios
        }

        String accountNumber = value.trim();

        if (accountNumber.length() < MIN_LENGTH) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Validación",
                "El número de cuenta debe tener al menos " + MIN_LENGTH + " caracteres"
            );
            throw new ValidatorException(message);
        }

        if (accountNumber.length() > MAX_LENGTH) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Validación",
                "El número de cuenta no puede tener más de " + MAX_LENGTH + " caracteres"
            );
            throw new ValidatorException(message);
        }
    }
}
