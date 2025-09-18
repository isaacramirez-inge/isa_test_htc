package com.isa.transaction.frontend.converter;

import com.isa.transaction.frontend.enums.TransactionType;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

/**
 * JSF Converter for TransactionType enum
 * Handles conversion between String and TransactionType for form binding
 */
@FacesConverter(value = "transactionTypeConverter", managed = true)
public class TransactionTypeConverter implements Converter<TransactionType> {

    @Override
    public TransactionType getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        try {
            return TransactionType.fromCode(value.trim());
        } catch (IllegalArgumentException e) {
            throw new ConverterException("Invalid transaction type: " + value, e);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, TransactionType value) {
        if (value == null) {
            return "";
        }
        
        return value.getCode();
    }
}