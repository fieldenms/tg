package ua.com.fielden.platform.entity.functional.master;

import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.getCriteriaTitleAndDesc;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.google.inject.Inject;

import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
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
        if (masterEntityNotEmpty()) {
            final Set<PropertyWarning> propertyWarnings = masterEntity()
                    .nonProxiedProperties()
                    .filter(mp -> mp.hasWarnings())
                    .map(mp -> factory().newEntity(PropertyWarning.class, getCriteriaTitleAndDesc(masterEntity().getType(), mp.getName()).getKey(), mp.getFirstWarning().getMessage()))
                    .collect(Collectors.toCollection(TreeSet::new));
            entity.setWarnings(propertyWarnings);
        }
        
        return entity;
    }
}