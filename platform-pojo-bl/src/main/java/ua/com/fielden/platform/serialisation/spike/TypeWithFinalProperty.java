package ua.com.fielden.platform.serialisation.spike;

/**
 * A type with final property.
 * 
 * @author TG Team
 * 
 */
public class TypeWithFinalProperty {
    private final Integer intField;

    protected TypeWithFinalProperty() {
        intField = null;
    }

    protected TypeWithFinalProperty(final Integer intField) {
        this.intField = intField;
    }

    public Integer getIntField() {
        return intField;
    }

}
