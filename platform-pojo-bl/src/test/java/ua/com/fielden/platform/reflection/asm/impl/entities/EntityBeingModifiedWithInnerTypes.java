package ua.com.fielden.platform.reflection.asm.impl.entities;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.Observable;

/**
 * A test entity with two properties of the same type, which gets enhanced and one property of self-type.
 * 
 * @author TG Team
 * 
 */
@KeyType(String.class)
public class EntityBeingModifiedWithInnerTypes extends AbstractEntity<String> {
    private static final long serialVersionUID = -8773531608189695820L;

    public static class InnerClass {
        private final int integerProp;

        public InnerClass(final int integerProp) {
            this.integerProp = integerProp;
        }

        public int getIntegerProp() {
            return integerProp;
        }
    }

    public enum InnerEnum {
        ONE, TWO
    }

    @IsProperty
    private Integer integerProp;
    @IsProperty
    private InnerEnum enumProp;
    @IsProperty
    private InnerClass classProp;

    public InnerClass getClassProp() {
        return classProp;
    }

    @Observable
    public void setClassProp(final InnerClass classProp) {
        this.classProp = classProp;
    }

    public InnerEnum getEnumProp() {
        return enumProp;
    }

    @Observable
    public void setEnumProp(final InnerEnum enumProp) {
        this.enumProp = enumProp;
    }

    public Integer getIntegerProp() {
        return integerProp;
    }

    @Observable
    public void setIntegerProp(final Integer integerProp) {
        this.integerProp = integerProp;
    }
}
