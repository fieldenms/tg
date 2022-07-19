package ua.com.fielden.platform.eql.retrieval;

import org.hibernate.type.Type;

import ua.com.fielden.platform.entity.query.IUserTypeInstantiate;

public class YieldDetails {
    public final String name;
    public final Object hibType;
    public final String column;

    public YieldDetails(final String name, final Object hibType, final String column) {
        this.name = name;
        this.hibType = hibType;
        this.column = column;
    }

    public Type getHibTypeAsType() {
        return hibType instanceof Type ? (Type) hibType : null;
    }

    public IUserTypeInstantiate getHibTypeAsUserType() {
        return hibType instanceof IUserTypeInstantiate ? (IUserTypeInstantiate) hibType : null;
    }
}