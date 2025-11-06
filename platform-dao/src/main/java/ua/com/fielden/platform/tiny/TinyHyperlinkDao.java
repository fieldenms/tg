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
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.web.annotations.AppUri;

import java.util.Map;
import java.util.Optional;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.baseEntityType;
import static ua.com.fielden.platform.tiny.TinyHyperlink.HASH;

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

    @SessionRequired
    @Override
    protected Either<Long, TinyHyperlink> save(final TinyHyperlink tinyHyperlink, final Optional<fetch<TinyHyperlink>> maybeFetch) {
        if (!tinyHyperlink.isPersisted()) {
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
            final CentreContextHolder centreContextHolder)
    {
        // TODO Perform a transformation.
        //      `modifiedProperties` contains conventional property values, while `modifHolder` requires a special structure used in marshalling.
        final var modifHolder = modifiedProperties;
        final var savingInfoHolder = getEntityFactory().newEntity(SavingInfoHolder.class)
                .setModifHolder(modifHolder)
                .setCentreContextHolder(centreContextHolder);

        return save(entityType, savingInfoHolder);
    }

    @Override
    @SessionRequired
    public TinyHyperlink save(final Class<? extends AbstractEntity<?>> entityType, final SavingInfoHolder savingInfoHolder) {
        final var serialisedSavingInfoHolder = serialiser.serialise(savingInfoHolder, SerialiserEngines.JACKSON);
        final var link = new_()
                .setEntityTypeName(baseEntityType(entityType).getCanonicalName())
                .setSavingInfoHolder(serialisedSavingInfoHolder)
                .setUser(getUser())
                .setCreatedDate(now().toDate());
        return save(link);
    }

    @Override
    public String toURL(final TinyHyperlink tinyHyperlink) {
        if (!tinyHyperlink.isPersisted()) {
            throw new InvalidArgumentException("URLs can be created only for persisted instances of [%s].".formatted(TinyHyperlink.class.getSimpleName()));
        }

        return "%s/#/tiny/%s".formatted(appUri, tinyHyperlink.getId());
    }

    @Override
    public String hash(final TinyHyperlink tinyHyperlink) {
        if (tinyHyperlink.isPersisted()) {
            return tinyHyperlink.getHash();
        }

        if (tinyHyperlink.getSavingInfoHolder() == null || tinyHyperlink.getSavingInfoHolder().length == 0) {
            throw new InvalidArgumentException("[%s] is required to compute a hash.".formatted(TinyHyperlink.SAVING_INFO_HOLDER));
        }

        return Checksum.sha256(tinyHyperlink.getSavingInfoHolder());
    }

}
