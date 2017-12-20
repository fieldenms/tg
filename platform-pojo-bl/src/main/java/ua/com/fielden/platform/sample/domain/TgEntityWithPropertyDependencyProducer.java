package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.EntityNewAction;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgEntityWithPropertyDependency}.
 *
 * @author TG Team
 *
 */
public class TgEntityWithPropertyDependencyProducer extends DefaultEntityProducerWithContext<TgEntityWithPropertyDependency> {

    @Inject
    public TgEntityWithPropertyDependencyProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgEntityWithPropertyDependency.class, companionFinder);
    }
    
    @Override
    protected TgEntityWithPropertyDependency provideDefaultValues(final TgEntityWithPropertyDependency entity) {
        return provideInitialValues(entity); // to be used in web unit tests
    }
    
    @Override
    protected TgEntityWithPropertyDependency provideDefaultValuesForStandardNew(final TgEntityWithPropertyDependency entity, final EntityNewAction masterEntity) {
        entity.setKey("DUMMY");
        return provideInitialValues(entity);
    }
    
    private TgEntityWithPropertyDependency provideInitialValues(final TgEntityWithPropertyDependency entity) {
        entity.setProp1("val0"); // initial value to be able to change property back, reproducing the edge-cases for property value application (#960)
        entity.setProp2(null); // remove 'value0' that was populated through prop1 definer
        return entity;
    }
}