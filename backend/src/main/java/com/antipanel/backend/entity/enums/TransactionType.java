package com.antipanel.backend.entity.enums;

/**
 * Tipo de transacción de balance.
 * Mapea el ENUM de PostgreSQL: transaction_type_enum
 */
public enum TransactionType {

    /**
     * Depósito de fondos
     */
    DEPOSIT("deposit"),

    /**
     * Cargo por orden realizada
     */
    ORDER("order"),

    /**
     * Reembolso al usuario
     */
    REFUND("refund"),

    /**
     * Ajuste manual por administrador
     */
    ADJUSTMENT("adjustment");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static TransactionType fromValue(String value) {
        for (TransactionType type : TransactionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown transaction type: " + value);
    }

    /**
     * Verifica si la transacción aumenta el balance
     */
    public boolean isCredit() {
        return this == DEPOSIT || this == REFUND;
    }

    /**
     * Verifica si la transacción disminuye el balance
     */
    public boolean isDebit() {
        return this == ORDER;
    }
}
