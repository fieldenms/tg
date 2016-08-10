package ua.com.fielden.platform.entity.functional.master;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector;
import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link AcknowledgeWarnings} to fill its respective property with the actual warnings.
 *
 * @author TG Team
 *
 */
public class AcknowledgeWarningsProducer extends DefaultEntityProducerWithContext<AcknowledgeWarnings> {

    @Inject
    public AcknowledgeWarningsProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, AcknowledgeWarnings.class, companionFinder);
    }

    @Override
    public AcknowledgeWarnings provideDefaultValues(final AcknowledgeWarnings entity) {
        if (getMasterEntity() != null) {
            final Set<PropertyWarning> propertyWarnings = getMasterEntity()
                    .nonProxiedProperties()
                    .filter(mp -> mp.hasWarnings())
                    .map(mp -> {
                        return factory().newEntity(PropertyWarning.class, CriteriaReflector.getCriteriaTitleAndDesc(getMasterEntity().getType(), mp.getName()).getKey(), mp.getFirstWarning().getMessage());
                    })
                    .collect(Collectors.toCollection(() -> new TreeSet<PropertyWarning>()));
            entity.setWarnings(propertyWarnings);
            entity.getProperty("warnings").resetState();
        }
        
        return entity;
    }
}