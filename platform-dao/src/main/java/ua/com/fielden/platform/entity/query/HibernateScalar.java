package ua.com.fielden.platform.entity.query;

import org.hibernate.type.Type;

public class HibernateScalar implements Comparable<HibernateScalar> {
    private final String columnName;
    private final Type hibType;
    private final Integer positionInResultList;

    public HibernateScalar(final String columnName, final Type hibType, final Integer positionInResultList) {
        this.columnName = columnName;
        this.hibType = hibType;
        this.positionInResultList = positionInResultList;
    }

    public String getColumnName() {
        return columnName;
    }

    public Type getHibType() {
        return hibType;
    }

    public boolean hasHibType() {
        return hibType != null;
    }

    @Override
    public int compareTo(final HibernateScalar o) {
        return positionInResultList.compareTo(o.positionInResultList);
    }
}