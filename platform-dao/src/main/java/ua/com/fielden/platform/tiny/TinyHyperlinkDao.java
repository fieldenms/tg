package ua.com.fielden.platform.tiny;

import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.cypher.Checksum;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.fetch.FetchModelReconstructor;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.functional.centre.CentreContextHolder;
import ua.com.fielden.platform.entity.functional.centre.SavingInfoHolder;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.tiny.TinyHyperlink.*;

@EntityType(TinyHyperlink.class)
public class TinyHyperlinkDao extends CommonEntityDao<TinyHyperlink> implements TinyHyperlinkCo {

    private final ISerialiser serialiser;
    private final String appUri;

    @Inject
    TinyHyperlinkDao(final ISerialiser serialiser, final @AppUri String appUri) {
        this.serialiser = serialiser;
        this.appUri = StringUtils.stripEnd(appUri, "/");
    }

    @Override
    protected IFetchProvider<TinyHyperlink> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    @SessionRequired
    public TinyHyperlink save(final TinyHyperlink tinyHyperlink) {
        return save(tinyHyperlink, Optional.of(FetchModelReconstructor.reconstruct(tinyHyperlink))).asRight().value();
    }

    /// The definitive save method.
    ///
    @SessionRequired
    @Override
    protected Either<Long, TinyHyperlink> save(final TinyHyperlink tinyHyperlink, final Optional<fetch<TinyHyperlink>> maybeFetch) {
        if (!tinyHyperlink.isPersisted()) {
            validateRequiredProperties(tinyHyperlink).ifFailure(Result::throwRuntime);
            final var hash = hash(tinyHyperlink);
            // An instrumented instance is required for `save`.
            final var existingTinyHyperlink = co$(TinyHyperlink.class).findByKeyAndFetch(fetchIdOnly(TinyHyperlink.class).with(HASH), hash);
            if (existingTinyHyperlink != null) {
                return super.save(existingTinyHyperlink, maybeFetch);
            }
            tinyHyperlink.setHash(hash);
        }

        return super.save(tinyHyperlink, maybeFetch);
    }

    @Override
    @SessionRequired
    public TinyHyperlink save(
            final Class<? extends AbstractEntity<?>> entityType,
            final Map<String, Object> modifiedProperties,
            final CentreContextHolder centreContextHolder,
            final String actionIdentifier)
    {
        return save(entityType, modifiedProperties, centreContextHolder, actionIdentifier, Optional.of(fetch(TinyHyperlink.class))).asRight().value();
    }

    @Override
    @SessionRequired
    public Either<Long, TinyHyperlink> save(
            final Class<? extends AbstractEntity<?>> entityType,
            final Map<String, Object> modifiedProperties,
            final CentreContextHolder centreContextHolder,
            final String actionIdentifier,
            final Optional<fetch<TinyHyperlink>> maybeFetch)
    {
        // TODO Perform a transformation.
        //      `modifiedProperties` contains conventional property values, while `modifHolder` requires a special structure used in marshalling.
        final var modifHolder = modifiedProperties;
        final var savingInfoHolder = getEntityFactory().newEntity(SavingInfoHolder.class)
                .setModifHolder(modifHolder)
                .setCentreContextHolder(centreContextHolder);

        return save(entityType, savingInfoHolder, actionIdentifier, maybeFetch);
    }

    @Override
    @SessionRequired
    public TinyHyperlink save(
            final Class<? extends AbstractEntity<?>> entityType,
            final SavingInfoHolder savingInfoHolder,
            final String actionIdentifier)
    {
        return save(entityType, savingInfoHolder, actionIdentifier, Optional.of(fetch(TinyHyperlink.class))).asRight().value();
    }

    @Override
    @SessionRequired
    public Either<Long, TinyHyperlink> save(
            final Class<? extends AbstractEntity<?>> entityType,
            final SavingInfoHolder savingInfoHolder,
            final String actionIdentifier,
            final Optional<fetch<TinyHyperlink>> maybeFetch)
    {
        // Normally, `CentreContextHolder.customObject` stores the position of an action (e.g. `@@actionNumber`),
        // which the server uses to locate the corresponding EntityActionConfig in the Web UI configuration.
        // This works for actions invoked directly, but fails for actions shared via tiny hyperlinks:
        //
        // 1. A tiny hyperlink is saved for a top-level centre action in position 0.
        // 2. Later, the configuration changes and that action moves to position 2.
        // 3. Opening the saved hyperlink now points to the old position, resolving to a wrong or missing action.
        //
        // Since correct action lookup is essential for obtaining the right computation object,
        // we instead capture the action identifier, which uniquely locates the action configuration.
        final var centreContextHolder = savingInfoHolder.getCentreContextHolder() != null
                ? savingInfoHolder.getCentreContextHolder()
                : co$(CentreContextHolder.class).new_();
        final var newCustomObject = new HashMap<>(centreContextHolder.getCustomObject());
        newCustomObject.put("@@actionIdentifier", actionIdentifier);
        centreContextHolder.setCustomObject(newCustomObject);

        final var serialisedSavingInfoHolder = serialiser.serialise(savingInfoHolder, SerialiserEngines.JACKSON);
        final var link = new_()
                .setEntityTypeName(baseEntityType(entityType).getCanonicalName())
                .setSavingInfoHolder(serialisedSavingInfoHolder)
                .setActionIdentifier(actionIdentifier);
        return save(link, maybeFetch);
    }

    @Override
    @SessionRequired
    public TinyHyperlink saveWithTarget(final Hyperlink hyperlink) {
        return saveWithTarget(hyperlink, Optional.of(fetch(TinyHyperlink.class))).asRight().value();
    }

    @Override
    @SessionRequired
    public Either<Long, TinyHyperlink> saveWithTarget(final Hyperlink hyperlink, final Optional<fetch<TinyHyperlink>> maybeFetch) {
        final var tiny = new_().setTarget(hyperlink);
        return save(tiny, maybeFetch);
    }

    @Override
    public String toURL(final TinyHyperlink tinyHyperlink) {
        if (!tinyHyperlink.isPersisted()) {
            throw new InvalidArgumentException("URLs can be created only for persisted instances of [%s].".formatted(TinyHyperlink.class.getSimpleName()));
        }

        return "%s/#/tiny/%s".formatted(appUri, tinyHyperlink.getHash());
    }

    @Override
    public String hash(final TinyHyperlink tinyHyperlink) {
        if (tinyHyperlink.isPersisted()) {
            return tinyHyperlink.getHash();
        }

        if (tinyHyperlink.getTarget() != null) {
            return Checksum.sha256(tinyHyperlink.getTarget().value.getBytes());
        }
        else {
            if (tinyHyperlink.getSavingInfoHolder() == null || tinyHyperlink.getSavingInfoHolder().length == 0) {
                throw new InvalidArgumentException("[%s] is required to compute a hash.".formatted(TinyHyperlink.SAVING_INFO_HOLDER));
            }

            if (tinyHyperlink.getActionIdentifier() == null || tinyHyperlink.getActionIdentifier().isEmpty()) {
                throw new InvalidArgumentException("[%s] is required to compute a hash.".formatted(TinyHyperlink.ACTION_IDENTIFIER));
            }

            return Checksum.sha256(tinyHyperlink.getActionIdentifier().getBytes(), tinyHyperlink.getSavingInfoHolder());
        }
    }

    private Result validateRequiredProperties(final TinyHyperlink tiny) {
        final var ok = (tiny.getTarget() == null && tiny.getEntityTypeName() != null && tiny.getSavingInfoHolder() != null && tiny.getActionIdentifier() != null)
                       || (tiny.getTarget() != null && tiny.getEntityTypeName() == null && tiny.getSavingInfoHolder() == null && tiny.getActionIdentifier() == null);
        return ok ? successful() : failuref("Either %s or all of [%s] must be specified.",
                                            getTitleAndDesc(TARGET, TinyHyperlink.class).getKey(),
                                            Stream.of(ENTITY_TYPE_NAME, SAVING_INFO_HOLDER, ACTION_IDENTIFIER)
                                                    .map(prop -> getTitleAndDesc(prop, TinyHyperlink.class).getKey())
                                                    .collect(joining(", ")));
    }

}
