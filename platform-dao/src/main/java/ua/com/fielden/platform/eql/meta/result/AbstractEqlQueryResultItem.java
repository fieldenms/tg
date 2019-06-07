package ua.com.fielden.platform.eql.meta.result;

public abstract class AbstractEqlQueryResultItem<T> {
    private final String name;
    private final Class<T> javaType;

    public AbstractEqlQueryResultItem(final String name, final Class<T> javaType) {
        this.name = name;
        this.javaType = javaType;
    }

    public String getName() {
        return name;
    }

    public Class<T> getJavaType() {
        return javaType;
    }

    @Override
    public String toString() {
        return name + ": " + javaType;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof AbstractEqlQueryResultItem)) {
            return false;
        }
        final AbstractEqlQueryResultItem other = (AbstractEqlQueryResultItem) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }
}