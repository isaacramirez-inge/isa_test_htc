package com.isa.transaction.frontend.validator;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.math.BigDecimal;

/**
 * Custom JSF validator for transaction amount validation
 * Validates transaction amounts within business rules limits
 */
@FacesValidator("amountValidator")
public class AmountValidator implements Validator<BigDecimal> {
    
    private static final BigDecimal MIN_AMOUNT = new BigDecimal("-10000.00");
    private static final BigDecimal MAX_AMOUNT = new BigDecimal("10000.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO;
    
    @Override
    public void validate(FacesContext context, UIComponent component, BigDecimal value) throws ValidatorException {
        if (value == null) {
            return; // Let required validation handle null values
        }
        
        // Check if amount is within valid range
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
        
        // Check if amount is zero (business rule - zero amounts not allowed)
        if (value.compareTo(ZERO) == 0) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Valor",
                "El monto de la transacción no puede ser cero"
            );
            throw new ValidatorException(message);
        }
        
        // Check decimal places (should not exceed 2)
        if (value.scale() > 2) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_ERROR,
                "Error de Formato",
                "El monto no puede tener más de 2 decimales"
            );
            throw new ValidatorException(message);
        }
        
        // Additional business rules can be added here
        // For example, check for suspicious amounts or patterns
        validateBusinessRules(value);
    }
    
    /**
     * Additional business rule validations
     */
    private void validateBusinessRules(BigDecimal amount) throws ValidatorException {
        // Example: Warn for large single transactions (could be suspicious)
        BigDecimal largeTransactionThreshold = new BigDecimal("5000.00");
        if (amount.abs().compareTo(largeTransactionThreshold) > 0) {
            // This is just a warning, not an error, so we could log it but not block
            // For demonstration, we'll allow it but could add additional checks
            // LOGGER.info("Large transaction amount: " + amount);
        }
        
        // Example: Block specific patterns (like amounts ending in .99)
        // This is just for demonstration - in real world this might not make sense
        String amountStr = amount.toPlainString();
        if (amountStr.endsWith(".99") && amount.abs().compareTo(new BigDecimal("100")) > 0) {
            FacesMessage message = new FacesMessage(
                FacesMessage.SEVERITY_WARN,
                "Advertencia",
                "Montos que terminan en .99 por encima de 100 pueden requerir aprobación adicional"
            );
            // This is just a warning, not throwing exception
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }
}