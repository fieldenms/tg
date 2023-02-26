package ua.com.fielden.platform.eql.retrieval;

import java.util.Objects;

public class ResultQueryYieldDetails implements Comparable<ResultQueryYieldDetails> {

    public final String name;
    public final Class<?> javaType;
    public final Object hibType;
    public final String column;

    public ResultQueryYieldDetails(final String name, final Class<?> javaType, final Object hibType, final String column) {
        this.name = name;
        this.javaType = javaType;
        this.hibType = hibType;
        this.column = column;
    }

    @Override
    public String toString() {
        return "\nname = " + name + "\njavaType = " + (javaType != null ? javaType.getSimpleName() : javaType) + "\nhibType = "
                + (hibType != null ? hibType.getClass().getSimpleName() : hibType) + "\ncolumn(s) = " + column;
    }

    @Override
    public int compareTo(ResultQueryYieldDetails o) {
        return name.compareTo(o.name);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + name.hashCode();
        result = prime * result + javaType.hashCode();
        result = prime * result + column.hashCode();
        result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof ResultQueryYieldDetails)) {
            return false;
        }

        final ResultQueryYieldDetails other = (ResultQueryYieldDetails) obj;

        return Objects.equals(name, other.name) &&
                Objects.equals(javaType, other.javaType) &&
                Objects.equals(hibType, other.hibType) &&
                Objects.equals(column, other.column);
    }
}