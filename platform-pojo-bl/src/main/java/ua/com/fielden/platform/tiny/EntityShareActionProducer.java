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
import ua.com.fielden.platform.web.interfaces.IEntityMasterUrlProvider;

import java.util.Base64;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static ua.com.fielden.platform.utils.QrCodeUtils.*;
import static ua.com.fielden.platform.utils.QrCodeUtils.ImageFormat.PNG;

/// A producer for [EntityShareAction].
///
/// This producer requires an instance of [CentreContextHolder] that was used to construct the [CentreContext].
/// Since [CentreContextHolder] is not assigned to producers by default, **it must be assigned explicitly** via [#setCentreContextHolder(CentreContextHolder)].
///
public class EntityShareActionProducer extends DefaultEntityProducerWithContext<EntityShareAction> {

    private static final String ERR_NO_MASTER = "Share action was invoked for an entity without a registered master. Entity: [%s] of type [%s].";

    private final IEntityMasterUrlProvider masterUrlProvider;

    private CentreContextHolder centreContextHolder;

    @Inject
    EntityShareActionProducer(
            final EntityFactory factory,
            final ICompanionObjectFinder companionFinder,
            final IEntityMasterUrlProvider masterUrlProvider)
    {
        super(factory, EntityShareAction.class, companionFinder);
        this.masterUrlProvider = masterUrlProvider;
    }

    @Override
    protected EntityShareAction provideDefaultValues(final EntityShareAction entity) {
        if (masterEntityNotEmpty()) {
            final var masterEntity = masterEntity();

            // Is this action invoked on a persisted entity master?
            if (masterEntity.isPersisted()) {
                // Create and save a tiny hyperlink that points to the respective entity master.

                final var masterUrl = masterUrlProvider.masterUrlFor(masterEntity)
                        .orElseThrow(() -> new InvalidStateException(format(ERR_NO_MASTER, masterEntity, masterEntity.getType().getCanonicalName())));
                final TinyHyperlinkCo coTinyHyperlink = co(TinyHyperlink.class);
                // TODO Specify a minimal fetch model for refetching after save.
                final var tinyHyperlink = coTinyHyperlink.saveWithTarget(new Hyperlink(masterUrl));
                final var tinyUrlHyperlink = new Hyperlink(coTinyHyperlink.toURL(tinyHyperlink));
                entity.setHyperlink(tinyUrlHyperlink)
                      .setQrCode(Base64.getEncoder().encodeToString(qrCodeImage(tinyUrlHyperlink.value, PNG, 512, 512, 24, WHITE, BLACK)));
            }
            // This action must have been invoked on a master for a new persistent entity or an action entity.
            else {
                // Create and save a tiny hyperlink that captures the shared entity state and the context of this action.

                if (centreContextHolder == null) {
                    throw new InvalidStateException("[centreContextHolder] must be present.");
                }

                final String actionIdentifier = getContext().getCustomObject() != null ? (String) getContext().getCustomObject().get("actionIdentifier") : null;
                if (isBlank(actionIdentifier)) {
                    throw new InvalidStateException("[actionIdentifier] must be present.");
                }
                final var savingInfoHolder = (SavingInfoHolder) centreContextHolder.getMasterEntity();

                final TinyHyperlinkCo coTinyHyperlink = co(TinyHyperlink.class);
                // TODO Specify a minimal fetch model for refetching after save.
                final var tinyHyperlink = coTinyHyperlink.save(masterEntity.getType(), savingInfoHolder, actionIdentifier);
                final var hyperlink = new Hyperlink(coTinyHyperlink.toURL(tinyHyperlink));
                entity.setHyperlink(hyperlink)
                        .setQrCode(Base64.getEncoder().encodeToString(qrCodeImage(hyperlink.value, PNG, 512, 512, 24, WHITE, BLACK)));
            }
        }

        return super.provideDefaultValues(entity);
    }

    public EntityShareActionProducer setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }

}
