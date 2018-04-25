package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link ITgCategory}.
 *
 * @author Developers
 *
 */
@EntityType(TgCategory.class)
public class TgCategoryDao extends CommonEntityDao<TgCategory> implements ITgCategory {

    @Inject
    public TgCategoryDao(final IFilter filter) {
        super(filter);
    }

    @Override // overridden for convenient debugging
    @SessionRequired
    public TgCategory save(TgCategory entity) {
        return super.save(entity);
    }
    
    @Override
    public void delete(final TgCategory entity) {
        defaultDelete(entity);
    }

}