package ua.com.fielden.platform.tiny;

import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import ua.com.fielden.platform.cypher.Checksum;
import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractUnionEntity;
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
import ua.com.fielden.platform.types.Colour;
import ua.com.fielden.platform.types.Hyperlink;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.types.RichText;
import ua.com.fielden.platform.types.either.Either;
import ua.com.fielden.platform.web.annotations.AppUri;
import ua.com.fielden.platform.web.utils.EntityResourceUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchIdOnly;
import static ua.com.fielden.platform.error.Result.failuref;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.*;
import static ua.com.fielden.platform.reflection.PropertyTypeDeterminator.isCollectional;
import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getTitleAndDesc;
import static ua.com.fielden.platform.tiny.TinyHyperlink.*;
import static ua.com.fielden.platform.utils.EntityUtils.*;

@EntityType(TinyHyperlink.class)
public class TinyHyperlinkDao extends CommonEntityDao<TinyHyperlink> implements TinyHyperlinkCo {

    public static final String
            ERR_INVALID_STATE_TO_COMPUTE_HASH = "[%s] is required to compute a hash.",
            ERR_REQUIRED_PROPS_VALIDATION = "Either %s or all of [%s] must be specified.",
            ERR_INVALID_UNION_VALUE = "Invalid union value specified for property [%s]. Could not access the union's active property. Please ensure that the union value is instrumented.",
            ERR_NO_SUPPORT_FOR_COLLECTIONAL_PROPS = "Collectional properties cannot be shared with tiny hyperlinks.",
            ERR_URLS_FOR_PERSISTED_ONLY = "URLs can be created only for persisted instances of [%s].";

    private final ISerialiser serialiser;
    private final String appUri;

    @Inject
    protected TinyHyperlinkDao(final ISerialiser serialiser, final @AppUri String appUri) {
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
            final Map<? extends CharSequence, Object> modifiedProperties,
            final CentreContextHolder centreContextHolder,
            final IActionIdentifier actionIdentifier)
    {
        return save(entityType, modifiedProperties, centreContextHolder, actionIdentifier, Optional.of(fetch(TinyHyperlink.class))).asRight().value();
    }

    @Override
    @SessionRequired
    public Either<Long, TinyHyperlink> save(
            final Class<? extends AbstractEntity<?>> entityType,
            final Map<? extends CharSequence, Object> modifiedProperties,
            final CentreContextHolder centreContextHolder,
            final IActionIdentifier actionIdentifier,
            final Optional<fetch<TinyHyperlink>> maybeFetch)
    {
        // `modifiedProperties` contains conventional property values, while `modifHolder` requires a special structure used in marshalling.
        final Map<String, Object> modifHolder;
        if (modifiedProperties.isEmpty()) {
            modifHolder = Map.of();
        }
        else {
            modifHolder = new HashMap<>();
            modifiedProperties.forEach((name, value) -> {
                final var propObject = makeModifHolderPropObject(entityType, name.toString(), value);
                modifHolder.put(name.toString(), propObject);
            });
            modifHolder.put("@@touchedProps", modifiedProperties.keySet().stream().map(CharSequence::toString).toList());
        }

        // TODO #2422 Support "open new" actions for simple and compound masters.
        //      For such actions, `savingInfoHolder` should have additional levels of depth.
        //      For simple masters:
        //      1. savingInfoHolder -- entityType.
        //      2. savingInfoHolder.centreContextHolder.masterEntity -- `EntityNewAction`.
        //      For compound masters:
        //      1. savingInfoHolder -- entityType.
        //      2. savingInfoHolder.centreContextHolder.masterEntity -- compound menu item entity.
        //      3. savingInfoHolder.centreContextHolder.masterEntity.*.masterEntity -- Open*MasterAction entity.

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
            final IActionIdentifier actionIdentifier)
    {
        return save(entityType, savingInfoHolder, actionIdentifier, Optional.of(fetch(TinyHyperlink.class))).asRight().value();
    }

    @Override
    @SessionRequired
    public Either<Long, TinyHyperlink> save(
            final Class<? extends AbstractEntity<?>> entityType,
            final SavingInfoHolder savingInfoHolder,
            final IActionIdentifier actionIdentifier,
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

        final var ourSavingInfoHolder = savingInfoHolder.copy(SavingInfoHolder.class);

        // TODO #2422 For EntityNewAction and Open*MasterAction, a nested `CentreContextHolder` must be modified, not the top-level one.
        //      See `_tgOpenMasterAction` in `tg-app-template.js`.
        ourSavingInfoHolder.setCentreContextHolder(ourSavingInfoHolder.getCentreContextHolder() == null
                                                           ? co$(CentreContextHolder.class).new_()
                                                           : ourSavingInfoHolder.getCentreContextHolder().copy(CentreContextHolder.class));
        final var newCustomObject = new HashMap<>(ourSavingInfoHolder.getCentreContextHolder().getCustomObject());
        newCustomObject.put(CUSTOM_OBJECT_ACTION_IDENTIFIER, actionIdentifier.name().toString());
        ourSavingInfoHolder.getCentreContextHolder().setCustomObject(newCustomObject);

        // Not needed, as shared entity restoration always goes through a producer.
        ourSavingInfoHolder.setOriginallyProducedEntity(null);

        final var serialisedSavingInfoHolder = serialiser.serialise(ourSavingInfoHolder, SerialiserEngines.JACKSON);
        final var link = new_()
                .setEntityTypeName(baseEntityType(entityType).getCanonicalName())
                .setSavingInfoHolder(serialisedSavingInfoHolder)
                .setActionIdentifier(actionIdentifier.name().toString());
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
            throw new InvalidArgumentException(ERR_URLS_FOR_PERSISTED_ONLY.formatted(TinyHyperlink.class.getSimpleName()));
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
                throw new InvalidArgumentException(ERR_INVALID_STATE_TO_COMPUTE_HASH.formatted(TinyHyperlink.SAVING_INFO_HOLDER));
            }

            if (tinyHyperlink.getActionIdentifier() == null || tinyHyperlink.getActionIdentifier().isEmpty()) {
                throw new InvalidArgumentException(ERR_INVALID_STATE_TO_COMPUTE_HASH.formatted(TinyHyperlink.ACTION_IDENTIFIER));
            }

            return Checksum.sha256(tinyHyperlink.getActionIdentifier().getBytes(), tinyHyperlink.getSavingInfoHolder());
        }
    }

    private Result validateRequiredProperties(final TinyHyperlink tiny) {
        final var ok = (tiny.getTarget() == null && tiny.getEntityTypeName() != null && tiny.getSavingInfoHolder() != null && tiny.getActionIdentifier() != null)
                       || (tiny.getTarget() != null && tiny.getEntityTypeName() == null && tiny.getSavingInfoHolder() == null && tiny.getActionIdentifier() == null);
        return ok ? successful() : failuref(ERR_REQUIRED_PROPS_VALIDATION,
                                            getTitleAndDesc(TARGET, TinyHyperlink.class).getKey(),
                                            Stream.of(ENTITY_TYPE_NAME, SAVING_INFO_HOLDER, ACTION_IDENTIFIER)
                                                    .map(prop -> getTitleAndDesc(prop, TinyHyperlink.class).getKey())
                                                    .collect(joining(", ")));
    }

    /// Transforms a native property value into a format suitable for [CentreContextHolder#modifHolder].
    ///
    /// This method is an inverse of [EntityResourceUtils#convert] and must be aligned with it.
    ///
    private Map<Object, Object> makeModifHolderPropObject(
            final Class<? extends AbstractEntity<?>> entityType,
            final String prop,
            final Object value)
    {
        class $ {
            static Map<Object, Object> val(final Object v) {
                final var map = new HashMap<>(1);
                map.put("val", v);
                return map;
            }
        }

        if (isCollectional(entityType, prop)) {
            throw new InvalidArgumentException(ERR_NO_SUPPORT_FOR_COLLECTIONAL_PROPS);
        }

        if (value == null) {
            return $.val(null);
        }

        final Class<?> propertyType = determinePropertyType(entityType, prop);

        // Below, `origVal` is not assigned at all, since the resulting modifHolder will apply only to new entity instances.

        if (isEntityType(propertyType)) {
            final var entity = (AbstractEntity<?>) value;
            final var object = new HashMap<>();
            object.put("val", Objects.toString(entity.getKey(), null));
            object.put("valId", entity.getId());
            if (isUnionEntityType(propertyType)) {
                final var unionValue = (AbstractUnionEntity) value;
                // TODO #2466 This assertion will be subject to removal.
                if (unionValue.activePropertyName() == null) {
                    throw new InvalidArgumentException(ERR_INVALID_UNION_VALUE.formatted(prop));
                }
                object.put("activeProperty", unionValue.activePropertyName());
            }
            return object;
        }
        else if (isDate(propertyType)) {
            final Date date = (Date) value;
            return $.val(date.getTime());
        }
        else if (Money.class.isAssignableFrom(propertyType)) {
            final var money = (Money) value;
            final var object = new HashMap<>();
            object.put("amount", money.getAmount());
            object.put("currency", money.getCurrency());
            object.put("taxPercent", money.getTaxPercent());
            return $.val(object);
        }
        else if (Colour.class.isAssignableFrom(propertyType)) {
            final var colour = (Colour) value;
            return $.val(Map.of(Colour.HASHLESS_UPPERCASED_COLOUR_VALUE, colour.hashlessUppercasedColourValue));
        }
        else if (Hyperlink.class.isAssignableFrom(propertyType)) {
            final var hyperlink = (Hyperlink) value;
            return $.val(Map.of(Hyperlink.VALUE, hyperlink.value));
        }
        else if (RichText.class.isAssignableFrom(propertyType)) {
            return value instanceof RichText.Invalid invalid
                    ? $.val(Map.of(RichText.VALIDATION_RESULT, Map.of(Result.MESSAGE, invalid.isValid().getMessage())))
                    : $.val(Map.of(RichText.FORMATTED_TEXT, ((RichText) value).formattedText()));
        }
        else if (Class.class.isAssignableFrom(propertyType)) {
            final var klass = (Class<?>) value;
            return $.val(klass.getName());
        }
        // Identity function for: Map, String, Integer, boolean, BigDecimal
        else {
            return $.val(value);
        }
    }

}
