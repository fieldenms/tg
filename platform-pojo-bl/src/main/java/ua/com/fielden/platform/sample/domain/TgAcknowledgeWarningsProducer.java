package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * A producer for new instances of entity {@link TgAcknowledgeWarnings} to fill its respective property with the actual warnings.
 *
 * @author TG Team
 *
 */
public class TgAcknowledgeWarningsProducer extends DefaultEntityProducerWithContext<TgAcknowledgeWarnings> {

    @Inject
    public TgAcknowledgeWarningsProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, TgAcknowledgeWarnings.class, companionFinder);
    }

    @Override
    public TgAcknowledgeWarnings provideDefaultValues(final TgAcknowledgeWarnings entity) {
        if (getMasterEntity() != null) {
            final String warnings = getMasterEntity().warnings().stream().map(w -> w.getMessage()).reduce("\n", (String a, String b) -> a + b + "\n");
            entity.setAllWarnings(warnings);
        }
        
        return entity;
    }
}