package ua.com.fielden.platform.dao;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.ICompleted;
import ua.com.fielden.platform.entity.query.fluent.EntityQueryProgressiveInterfaces.IWhere0;

public interface IOpenCompoundMasterAction<T extends AbstractEntity<?>> extends IEntityDao<T> {

    default <K extends AbstractEntity<?>> ICompleted<K> enhnaceEmbededCentreQuery(final IWhere0<K> where, final String prop, final Object value) {
        return where.prop(prop).eq().val(value);
    }
}
