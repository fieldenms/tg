package ua.com.fielden.platform.eql.stage2.elements;

import static ua.com.fielden.platform.utils.CollectionUtil.listOf;

import ua.com.fielden.platform.entity.AbstractEntity;

public class QrySource2BasedOnPersistentTypeWithCalcProps extends QrySource2BasedOnSubqueries {
    private final Class<? extends AbstractEntity<?>> sourceType;

    public QrySource2BasedOnPersistentTypeWithCalcProps(final Class<? extends AbstractEntity<?>> sourceType, final String alias, final EntQuery2 model) {
        super(alias, listOf(model));
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

        if (!(obj instanceof QrySource2BasedOnPersistentTypeWithCalcProps)) {
            return false;
        }

        return super.equals(obj) && sourceType.equals(((QrySource2BasedOnPersistentTypeWithCalcProps) obj).sourceType);
    }
}
