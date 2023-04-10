package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;

/**
 * Producer of {@link OpenEntityMasterHelpAction} entity to create or edit help hyperlink for entity master.
 *
 * @author TG Team
 *
 */
public class OpenEntityMasterHelpActionProducer extends DefaultEntityProducerWithContext<OpenEntityMasterHelpAction>{

    public OpenEntityMasterHelpActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, OpenEntityMasterHelpAction.class, companionFinder);
    }

}
