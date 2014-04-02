package ua.com.fielden.platform.example.swing.components.autocompleter;

/**
 * This an enumeration for autocompleter demonstration purposes.
 * 
 * @author TG Team
 * 
 */
public enum DemoEnum {
    A_VALUE1("value A one"),
    A_VALUE2("value A two"),
    A_VALUE3("value A tree"),
    B_VALUE1("value B one"),
    B_VALUE2("value B two"),
    B_VALUE3("value B tree"),
    C_VALUE1("value C one"),
    C_VALUE2("value C two"),
    C_VALUE3("value C three"),
    D_VALUE1("value D one"),
    D_VALUE2("value D two"),
    D_VALUE3("value D three");

    final String desc;

    DemoEnum(final String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return desc;
    }
}
