package com.isa.transaction.frontend.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.math.BigDecimal;

/**
 * Validador JSF personalizado para la validacion del monto de la transaccion
 * Valida los montos de las transacciones dentro de los limites de las reglas de negocio
 */
@FacesValidator("amountValidator")
public class AmountValidator implements Validator<BigDecimal> {

    private static final BigDecimal MIN_AMOUNT = new BigDecimal("-10000.00");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO;

    @Override
    public void validate(FacesContext context, UIComponent component, BigDecimal value) throws ValidatorException {
        if (value == null) {
            return; // Dejar que la validacion de requerido maneje los valores nulos
        }

        // Comprueba si el monto esta dentro del rango valido
        if (value.compareTo(MIN_AMOUNT) < 0) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Rango",
                "El monto no puede ser menor a -10,000.00"
            );
            throw new ValidatorException(message);
        }

        if (value.compareTo(MAX_AMOUNT) > 0) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Rango",
                "El monto no puede ser mayor a 10,000.00"
            );
            throw new ValidatorException(message);
        }

        // Comprueba si el monto es cero (regla de negocio - no se permiten montos cero)
        if (value.compareTo(ZERO) == 0) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Valor",
                "El monto de la transacción no puede ser cero"
            );
            throw new ValidatorException(message);
        }

        // Comprueba los decimales (no deben exceder 2)
        if (value.scale() > 2) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Formato",
                "El monto no puede tener más de 2 decimales"
            );
            throw new ValidatorException(message);
        }

        // Se pueden agregar reglas de negocio adicionales aqui
        // Por ejemplo, comprobar si hay montos o patrones sospechosos
        validateBusinessRules(value);
    }

    /**
     * Validaciones de reglas de negocio adicionales
     */
    private void validateBusinessRules(BigDecimal amount) throws ValidatorException {
        // Ejemplo: Advertir sobre transacciones unicas grandes (podrian ser sospechosas)
        BigDecimal largeTransactionThreshold = new BigDecimal("5000.00");
        if (amount.abs().compareTo(largeTransactionThreshold) > 0) {
            // Esto es solo una advertencia, no un error, por lo que podriamos registrarlo pero no bloquearlo
            // Para la demostracion, lo permitiremos pero podriamos agregar comprobaciones adicionales
        }

        // Ejemplo: Bloquear patrones especificos (como montos que terminan en .99)
        // Esto es solo para demostracion - en el mundo real esto podria no tener sentido
        String amountStr = amount.toPlainString();
        if (amountStr.endsWith(".99") && amount.abs().compareTo(new BigDecimal("100")) > 0) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_WARN,
                "Advertencia",
                "Montos que terminan en .99 por encima de 100 pueden requerir aprobación adicional"
            );
            // Esto es solo una advertencia, no se lanza una excepcion
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}
