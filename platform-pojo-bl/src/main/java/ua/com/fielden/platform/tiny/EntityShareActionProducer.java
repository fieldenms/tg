package ua.com.fielden.platform.tiny;

import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.web.centre.CentreContext;

/// A producer for [EntityShareAction].
///
/// This producer requires an instance of [CentreContextHolder] that was used to construct the [CentreContext].
/// Since [CentreContextHolder] is not assigned to producers by default, **it must be assigned explicitly** via [#setCentreContextHolder(CentreContextHolder)].
///
public class EntityShareActionProducer extends DefaultEntityProducerWithContext<EntityShareAction> {

    private CentreContextHolder centreContextHolder;

    @Inject
    EntityShareActionProducer(final EntityFactory factory, final ICompanionObjectFinder companionFinder) {
        super(factory, EntityShareAction.class, companionFinder);
    }

    @Override
    protected EntityShareAction provideDefaultValues(final EntityShareAction entity) {
        if (masterEntityNotEmpty()) {
            final var masterEntity = masterEntity();
            if (masterEntity.isPersisted()) {
                throw new InvalidStateException("Entity share action is not applicable to persisted entities.");
            }

            if (centreContextHolder == null) {
                throw new InvalidStateException("[centreContextHolder] must be present.");
            }
            final var savingInfoHolder = (SavingInfoHolder) centreContextHolder.getMasterEntity();

            final TinyHyperlinkCo coTinyHyperlink = co(TinyHyperlink.class);
            // TODO Specify a minimal fetch model for refetching after save.
            final var tinyHyperlink = coTinyHyperlink.save(masterEntity.getType(), savingInfoHolder);
            entity.setHyperlink(new Hyperlink(coTinyHyperlink.toURL(tinyHyperlink)));
        }

        return super.provideDefaultValues(entity);
    }

    public EntityShareActionProducer setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }

}
