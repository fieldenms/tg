package ua.com.fielden.uds.designer.zui.component.expression;

/**
 * This enumeration is used to distinguish between different type of operators. These types determine compatibility between operators and operands.
 * 
 * @author 01es
 * 
 */
public enum OperatorType {
    BOOLEAN, ARITHMETIC, ANY;

    public static boolean areCompatible(OperatorType thisType, OperatorType type) {
        return type == OperatorType.ANY || thisType == OperatorType.ANY || thisType == type;
    }
}
