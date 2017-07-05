package ua.com.fielden.platform.web.centre;

import java.util.LinkedHashSet;
import java.util.Set;
import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer for {@link CentreConfigUpdaterDefaultAction}.
 * 
 * @author TG Team
 *
 */
public class CentreConfigUpdaterDefaultActionProducer extends DefaultEntityProducerWithContext<CentreConfigUpdaterDefaultAction> {

    @Inject
    public CentreConfigUpdaterDefaultActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, CentreConfigUpdaterDefaultAction.class, companionFinder);
    }
    
    @Override
    protected CentreConfigUpdaterDefaultAction provideDefaultValues(final CentreConfigUpdaterDefaultAction entity) {
        if (getMasterEntity() != null) {
            // TODO continue implementing
            final Set<String> defaultVisibleProperties = new LinkedHashSet<>();
            defaultVisibleProperties.add("this"); // TODO remove this stub
            defaultVisibleProperties.add("desc"); // TODO remove this stub
            entity.setDefaultVisibleProperties(defaultVisibleProperties);
        }
        return entity;
    }
}