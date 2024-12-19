package ru.socks.inventory.dto;

import lombok.Getter;

@Getter
public enum OperatorEnum {

    MORE_THAN("moreThan"),
    LESS_THAN("lessThan"),
    EQUAL("equal");

    private final String operator;

    OperatorEnum(String operator) {
        this.operator = operator;
    }

    public static OperatorEnum fromString(String operator) {
        for (OperatorEnum op : OperatorEnum.values()) {
            if (op.getOperator().equalsIgnoreCase(operator)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Invalid operator: " + operator);
    }
}
