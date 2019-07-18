package ua.com.fielden.platform.entity;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

@EntityType(BarCodeLocator.class)
public class BarCodeLocatorDao extends CommonEntityDao<BarCodeLocator> implements IBarCodeLocator{

    private final ICompanionObjectFinder coFinder;

    @Inject
    protected BarCodeLocatorDao(final IFilter filter, final ICompanionObjectFinder coFinder) {
        super(filter);
        this.coFinder = coFinder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public BarCodeLocator save(final BarCodeLocator entity) {
        try {
            entity.setEntity(coFinder.find((Class<AbstractEntity<?>>)Class.forName(entity.getEntityType()), true).findByKey(entity.getEntityKey()));
        } catch (final ClassNotFoundException e) {
            e.printStackTrace();
        }
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<BarCodeLocator> createFetchProvider() {
        return super.createFetchProvider().with("entityKey", "entity", "entityType");
    }
}
