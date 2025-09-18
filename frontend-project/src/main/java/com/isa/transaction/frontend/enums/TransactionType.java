package com.isa.transaction.frontend.enums;

/**
 * Enum for transaction types (Credit/Debit)
 * Used in the transaction form to select operation type
 */
public enum TransactionType {
    
    CREDIT("CREDIT", "Crédito (Depósito)", "Agregar dinero a la cuenta", "pi pi-plus-circle", "success"),
    DEBIT("DEBIT", "Débito (Retiro)", "Retirar dinero de la cuenta", "pi pi-minus-circle", "danger");
    
    private final String code;
    private final String displayName;
    private final String description;
    private final String icon;
    private final String styleClass;
    
    TransactionType(String code, String displayName, String description, String icon, String styleClass) {
        this.code = code;
        this.displayName = displayName;
        this.description = description;
        this.icon = icon;
        this.styleClass = styleClass;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIcon() {
        return icon;
    }
    
    public String getStyleClass() {
        return styleClass;
    }
    
    /**
     * Get enum by code
     */
    public static TransactionType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        for (TransactionType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type code: " + code);
    }
    
    /**
     * Check if transaction type requires positive amount
     */
    public boolean requiresPositiveAmount() {
        return this == CREDIT;
    }
    
    /**
     * Check if transaction type requires negative amount  
     */
    public boolean requiresNegativeAmount() {
        return this == DEBIT;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}