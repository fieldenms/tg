package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(KeyLocator.class)
public class KeyLocatorDao extends CommonEntityDao<KeyLocator> implements IKeyLocator{

    private final ICompanionObjectFinder coFinder;

    @Inject
    protected KeyLocatorDao(final IFilter filter, final ICompanionObjectFinder coFinder) {
        super(filter);
        this.coFinder = coFinder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public KeyLocator save(final KeyLocator entity) {
        try {
            entity.setEntity(coFinder.find((Class<AbstractEntity<?>>)Class.forName(entity.getEntityType()), true).findByKey(entity.getEntityKey()));
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<KeyLocator> createFetchProvider() {
        return super.createFetchProvider().with("entityKey", "entity", "entityType");
    }
}
