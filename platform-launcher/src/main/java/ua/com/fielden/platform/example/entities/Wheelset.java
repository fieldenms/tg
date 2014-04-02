package ua.com.fielden.platform.example.entities;

import ua.com.fielden.platform.entity.annotation.Observable;
import ua.com.fielden.platform.entity.validation.annotation.EntityExists;
import ua.com.fielden.platform.entity.validation.annotation.NotNull;

/**
 * Wheelset rotable business entity
 * 
 * @author nc
 * 
 */
public class Wheelset extends Rotable {

    private static final long serialVersionUID = 1L;

    protected Wheelset() {
    }

    public Wheelset(final String name, final String desc) {
        super(name, desc);
    }

    @Override
    public WheelsetClass getRotableClass() {
        return (WheelsetClass) super.getRotableClass();
    }

    @Override
    @Observable
    @NotNull
    @EntityExists(WheelsetClass.class)
    public Wheelset setRotableClass(final RotableClass klass) {
        super.setRotableClass(klass);
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer result = new StringBuffer();
        result.append("Name: " + getKey() + "\n");
        result.append("Desc: " + getDesc() + "\n");
        result.append(getRotableClass().toString() + "\n");
        result.append("Location: " + getLocation().getId());
        return result.toString();
    }
}
