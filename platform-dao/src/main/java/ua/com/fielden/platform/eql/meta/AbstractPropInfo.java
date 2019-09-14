package ua.com.fielden.platform.eql.meta;

import java.util.Objects;

/**
 * A structure that captures a query source yield-able property resolution related info within a query source of type <code>PARENT</code>. 
 * 
 * @author TG Team
 *
 */
public abstract class AbstractPropInfo<T> implements IResolvable<T> {
    private final String name;

    /**
     * Principal constructor.
     * 
     * @param name - property yield alias or property name.
     */
    public AbstractPropInfo(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof AbstractPropInfo)) {
            return false;
        }

        final AbstractPropInfo other = (AbstractPropInfo) obj;
        
        return Objects.equals(name, other.name);
    }
}