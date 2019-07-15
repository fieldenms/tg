package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.query.IFilter;

public class KeyLocatorDao<K extends AbstractEntity<?>, T extends KeyLocator<K>> extends CommonEntityDao<T> {

    private final ICompanionObjectFinder coFinder;

    protected KeyLocatorDao(final IFilter filter, final ICompanionObjectFinder coFinder) {
        super(filter);
        this.coFinder = coFinder;
    }

    @Override
    public T save(final T entity) {
        entity.setEntity(coFinder.find(entity.getEntityTypeAsClass(), true).findByKey(entity.getEntityKey()));
        return super.save(entity);
    }

}
