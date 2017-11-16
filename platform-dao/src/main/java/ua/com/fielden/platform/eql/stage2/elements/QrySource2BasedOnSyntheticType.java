package ua.com.fielden.platform.eql.stage2.elements;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

public class QrySource2BasedOnSyntheticType extends QrySource2BasedOnSubqueries {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public QrySource2BasedOnSyntheticType(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final List<EntQuery2> models) {
        super(alias, models);
        this.sourceType = sourceType;
    }

    @Override
    public Class<? extends AbstractEntity<?>> sourceType() {
        return sourceType;
    }

    @Override
    public int hashCode() {
        return super.hashCode() + 13 * sourceType.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        // TODO Auto-generated method stub
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof QrySource2BasedOnSyntheticType)) {
            return false;
        }

        return super.equals(obj) && sourceType.equals(((QrySource2BasedOnSyntheticType) obj).sourceType);
    }
}
