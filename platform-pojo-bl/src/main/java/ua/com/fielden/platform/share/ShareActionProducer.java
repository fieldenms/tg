package ua.com.fielden.platform.share;

import jakarta.inject.Inject;
import ua.com.fielden.platform.entity.DefaultEntityProducerWithContext;
import ua.com.fielden.platform.entity.exceptions.InvalidStateException;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.factory.ICompanionObjectFinder;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.tiny.IActionIdentifier;
import ua.com.fielden.platform.tiny.TinyHyperlink;
import ua.com.fielden.platform.tiny.TinyHyperlinkCo;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.web.centre.CentreContext;
import ua.com.fielden.platform.web.utils.EntityCentreAPI;

import java.util.Base64;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.tiny.TinyHyperlink.*;
import static ua.com.fielden.platform.utils.QrCodeUtils.*;
import static ua.com.fielden.platform.utils.QrCodeUtils.ImageFormat.PNG;

/// A producer for [ShareAction].
///
/// For new instances, this producer requires an instance of [CentreContextHolder] that was used to construct the [CentreContext].
/// Since [CentreContextHolder] is not assigned to producers by default, **it must be assigned explicitly** via [#setCentreContextHolder(CentreContextHolder)].
///
/// For persisted instances, it only requires URL of that instance (optionally, with compound master menu item suffix).
///
public class ShareActionProducer extends DefaultEntityProducerWithContext<ShareAction> {

    private CentreContextHolder centreContextHolder;
    private final EntityCentreAPI entityCentreAPI;

    @Inject
    ShareActionProducer(
            final EntityFactory factory,
            final ICompanionObjectFinder companionFinder,
            final EntityCentreAPI entityCentreAPI)
    {
        super(factory, ShareAction.class, companionFinder);
        this.entityCentreAPI = entityCentreAPI;
    }

    @Override
    protected ShareAction provideDefaultValues(final ShareAction entity) {
        if (contextNotEmpty()) {
            // Is this action invoked on a persisted entity master?
            ofNullable((String) getContext().getCustomObject().get(CUSTOM_OBJECT_SHARED_URI)).ifPresentOrElse(sharedUri -> {
                // Create and save a tiny hyperlink that points to the respective entity master.
                final TinyHyperlinkCo coTinyHyperlink = co(TinyHyperlink.class);
                final var tinyHyperlink = coTinyHyperlink.saveWithTarget(new Hyperlink(sharedUri), Optional.of(fetchIdOnly(TinyHyperlink.class).with(HASH))).asRight().value();
                final var tinyUrlHyperlink = new Hyperlink(coTinyHyperlink.toURL(tinyHyperlink));
                entity.setHyperlink(tinyUrlHyperlink)
                        .setQrCode(Base64.getEncoder().encodeToString(qrCodeImage(tinyUrlHyperlink.value, PNG, 512, 512, 24, WHITE, BLACK)));
            }, () -> {
                // This action must have been invoked on a master for a new persistent entity or an action entity.
                if (masterEntityNotEmpty()) {
                    final var masterEntity = masterEntity();
                    // Create and save a tiny hyperlink that captures the shared entity state and the context of this action.
                    if (centreContextHolder == null) {
                        throw new InvalidStateException("[centreContextHolder] must be present.");
                    }

                    final var actionIdentifierName = getContext().getCustomObject() != null ? (String) getContext().getCustomObject().get(CUSTOM_OBJECT_ACTION_IDENTIFIER) : null;
                    if (isBlank(actionIdentifierName)) {
                        throw new InvalidStateException("[%s] must be present.".formatted(CUSTOM_OBJECT_ACTION_IDENTIFIER));
                    }
                    final var savingInfoHolder = (SavingInfoHolder) centreContextHolder.getMasterEntity();

                    final TinyHyperlinkCo coTinyHyperlink = co(TinyHyperlink.class);
                    final var tinyHyperlink = coTinyHyperlink.save(masterEntity.getType(), savingInfoHolder, IActionIdentifier.of(actionIdentifierName), Optional.of(fetchIdOnly(TinyHyperlink.class).with(HASH))).asRight().value();
                    final var hyperlink = new Hyperlink(coTinyHyperlink.toURL(tinyHyperlink));
                    entity.setHyperlink(hyperlink)
                            .setQrCode(Base64.getEncoder().encodeToString(qrCodeImage(hyperlink.value, PNG, 512, 512, 12, WHITE, BLACK)));
                }
            });

            final var result = entityCentreAPI.entityCentreResult("fielden.main.menu.personnel.MiLeaveRequest", "48b767df-bf45-48b7-932a-d2892eec47e8"); // Daniel.Truong Troy.Plecas
            System.out.println("----------- API Execution (...) ------------");
            if (result.isRight()) {
                result.asRight().value().forEach(ent -> {
                    System.out.println(ent.get("details") + " " + ent.get("person") + " " + ent.get("date"));
                });
            }
            else {
                System.out.println(result.asLeft().value().getMessage());
            }
            System.out.println("----------- API Execution (end) ------------");
        }

        return super.provideDefaultValues(entity);
    }

    public ShareActionProducer setCentreContextHolder(final CentreContextHolder centreContextHolder) {
        this.centreContextHolder = centreContextHolder;
        return this;
    }

}
