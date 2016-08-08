package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

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
            final String warnings = getMasterEntity().warnings().stream().map(w -> w.getMessage()).reduce("\n", (String a, String b) -> a + b + "\n");
            entity.setAllWarnings(warnings);
        }
        
        return entity;
    }
}