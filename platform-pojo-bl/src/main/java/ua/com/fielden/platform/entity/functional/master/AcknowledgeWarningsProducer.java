package ua.com.fielden.platform.entity.functional.master;

import com.google.inject.Inject;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.meta.MetaProperty;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static ua.com.fielden.platform.criteria.generator.impl.CriteriaReflector.getCriteriaTitleAndDesc;
import static ua.com.fielden.platform.error.Result.resultMessages;

/// A producer for new instances of entity [AcknowledgeWarnings] to fill its respective property with the actual warnings.
///
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
                    .filter(MetaProperty::hasWarnings)
                    .map(mp -> factory().newEntity(PropertyWarning.class, getCriteriaTitleAndDesc(masterEntity().getType(), mp.getName()).getKey(), resultMessages(mp.getFirstWarning()).extendedMessage))
                    .collect(Collectors.toCollection(TreeSet::new));
            entity.setWarnings(propertyWarnings);
        }
        
        return entity;
    }

}
