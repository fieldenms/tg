package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.meta.*;

import java.util.Map.Entry;
import java.util.Optional;

import static java.lang.Boolean.FALSE;
import static java.lang.String.format;
import static ua.com.fielden.platform.entity.AbstractEntity.*;
import static ua.com.fielden.platform.entity.AbstractPersistentEntity.*;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.ACTIVE;
import static ua.com.fielden.platform.entity.ActivatableAbstractEntity.REF_COUNT;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetch;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.*;
import static ua.com.fielden.platform.entity.query.fluent.fetch.MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES;
import static ua.com.fielden.platform.meta.PropertyMetadataKeys.KEY_MEMBER;
import static ua.com.fielden.platform.meta.PropertyTypeMetadata.Wrapper.unwrap;
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.utils.EntityUtils.*;

/**
 * <h4> Implementation Details </h4>
 *
 * When inspecting property types for a potential entity type {@link PropertyTypeMetadata.Wrapper#unwrap(PropertyTypeMetadata)}
 * is used because of the way fetch models are constructed -- heuristically, and one case where unwrapping is needed is
 * collectional properties: fetch models know only about the collectional element type.
 */
public class EntityRetrievalModel<T extends AbstractEntity<?>> extends AbstractRetrievalModel<T> {
    private final EntityMetadata entityMetadata;
    private final EntityMetadataUtils entityMetadataUtils;
    private final PropertyMetadataUtils propMetadataUtils;

    public EntityRetrievalModel(final fetch<T> originalFetch, final IDomainMetadata domainMetadata) {
        this(originalFetch, domainMetadata, true);
    }

    EntityRetrievalModel(final fetch<T> originalFetch, final IDomainMetadata domainMetadata, final boolean topLevel) {
        super(originalFetch, domainMetadata, topLevel);
        this.entityMetadata = domainMetadata.forEntity(originalFetch.getEntityType());
        this.entityMetadataUtils = domainMetadata.entityMetadataUtils();
        this.propMetadataUtils = domainMetadata.propertyMetadataUtils();

        switch (originalFetch.getFetchCategory()) {
        case ALL_INCL_CALC:
            includeAllFirstLevelPropsInclCalc();
            break;
        case ALL:
            includeAllFirstLevelProps();
            break;
        case DEFAULT:
            includeAllFirstLevelPrimPropsAndKey();
            break;
        case KEY_AND_DESC:
            includeKeyAndDescOnly();
            break;
        case ID_AND_VERSION:
            includeIdAndVersionOnly();
            break;
        case ID_ONLY:
            includeIdOnly();
            break;
        case NONE:
            break;
        default:
            throw new IllegalStateException("Unknown fetch category [" + originalFetch.getFetchCategory() + "]");
        }

        for (final String propName : originalFetch.getExcludedProps()) {
            without(propName);
        }

        for (final String propName : originalFetch.getIncludedProps()) {
            with(propName, false);
        }

        for (final Entry<String, fetch<? extends AbstractEntity<?>>> entry : originalFetch.getIncludedPropsWithModels().entrySet()) {
            with(entry.getKey(), entry.getValue());
        }

        populateProxies();
    }

    private void populateProxies() {
        for (final PropertyMetadata pm : entityMetadata.properties()) {
            // FIXME the following condition needs to be revisited as part of EQL 3 implementation
            final String name = pm.name();
            if (!ID.equals(name) &&
                !(KEY.equals(name) && !pm.isPersistent()) &&
                !pm.type().isCollectional() &&
                !pm.isCritOnly() &&
                !name.contains(".") &&
                !containsProp(name) &&
                (entityMetadata.isSynthetic() || !pm.isPlain())) {
                getProxiedProps().add(name);
            }
        }
    }

    private void includeAllCompositeKeyMembers() {
        entityMetadataUtils.compositeKeyMembers(entityMetadata)
                .forEach(pm -> with(pm.name(), !pm.type().isEntity()));
    }

    private void includeAllUnionEntityKeyMembers() {
        entityMetadata.properties().stream()
                .filter(pm -> propMetadataUtils.isPropEntityType(pm.type(), EntityMetadata::isPersistent))
                .forEach(pm -> with(pm.name(), false));
    }

    private void includeAllFirstLevelPrimPropsAndKey() {
        entityMetadata.properties().stream()
                .filter(pm -> !pm.type().isCollectional() && !isPure(pm))
                // calculated composite are included (legacy EQL2 behaviour)
                // TODO don't treat calculated composite specially once TG applications no longer rely on this behaviour
                .filter(pm -> !pm.isCalculated() || pm.type().isComposite())
                .forEach(pm -> {
                    // if this property is a key member typed with a union entity that has property P typed with this entity
                    // (the one we are constructing fetch model for), then we need to ensure that P is not explored,
                    // otherwise fetch model construction will never terminate
                    final boolean exploreEntities = pm.type().isEntity()
                                                    && !propMetadataUtils.isPropEntityType(pm, EntityMetadata::isUnion)
                                                    && (KEY.equals(pm.name()) || pm.has(KEY_MEMBER));
                    with(pm.name(), !exploreEntities);
                });
    }

    private void includeLastUpdatedByGroupOfProperties() {
        if (AbstractPersistentEntity.class.isAssignableFrom(getEntityType())) {
            with(LAST_UPDATED_BY, true);
            addPrimProp(LAST_UPDATED_DATE);
            addPrimProp(LAST_UPDATED_TRANSACTION_GUID);
        }
    }

    private void includeKeyAndDescOnly() {
        if (isPersistedEntityType(getEntityType())) {
            includeIdAndVersionOnly();
        } else if (isSyntheticBasedOnPersistentEntityType(getEntityType())) {
            addPrimProp(ID);
        }

        with(KEY, true);

        if (hasDescProperty(getEntityType())) {
            addPrimProp(DESC);
        }
    }

    private void includeAllFirstLevelProps() {
        entityMetadata.properties().stream()
                .filter(pm -> !pm.type().isCollectional() && !isPure(pm))
                // calculated composite are included (legacy EQL2 behaviour)
                // TODO don't treat calculated composite specially once TG applications no longer rely on this behaviour
                .filter(pm -> !pm.isCalculated() || pm.type().isComposite())
                .forEach(pm -> with(pm.name(), false));
    }

    private void includeAllFirstLevelPropsInclCalc() {
        entityMetadata.properties().stream()
                .filter(pm -> !pm.type().isCollectional() && !isPure(pm))
                .forEach(pm -> with(pm.name(), false));
    }

    private void includeIdOnly() {
        addPrimProp(ID);
    }

    private void includeIdAndVersionOnly() {
        if (isPersistedEntityType(getEntityType())) {
            addPrimProp(ID);
            addPrimProp(VERSION);
            if (isActivatableEntityType(getEntityType())) {
                addPrimProp(ACTIVE);
                addPrimProp(REF_COUNT);
            }
            includeLastUpdatedByGroupOfProperties();
        } else if (isEntityType(getKeyType(getEntityType()))) {
            addPrimProp(ID);
        }
    }

    private void with(final String propName, final boolean skipEntities) {
        getPropMetadata(propName).ifPresentOrElse(pm -> {
            if (pm.type().isCompositeKey()) {
                includeAllCompositeKeyMembers();
            } else if (propName.equals(KEY) && isUnionEntityType(getEntityType())) {
                addPrimProp(KEY);
                includeAllUnionEntityKeyMembers();
            } else {
                final var propType = unwrap(pm.type());
                // treat PropertyDescriptor as primitive, it doesn't make sense to fetch its subproperties
                if (propType instanceof PropertyTypeMetadata.Entity et && !PropertyDescriptor.class.equals(et.javaType())/* && !optPm.isId()*/) {
                    if (!skipEntities) {
                        if (propMetadataUtils.isPropEntityType(propType, EntityMetadata::isUnion)) {
                            with(propName, fetchAll(et.javaType()));
                        } else {
                            with(propName, fetch(et.javaType()));
                        }
                    } else if (pm.isPersistent()) {
                        with(propName, fetchIdOnly(et.javaType()));
                    }
                } else {
                    getSinglePropertyOfCompositeUserType(pm).ifPresent(prop -> addPrimProp(propName + "." + prop));
                    addPrimProp(propName);
                }
            }
        }, () -> addPrimProp(propName)); // when PropertyMetadata is missing but this is considered legal -- just add it as primitive property

    }

    private void with(final String propName, final fetch<? extends AbstractEntity<?>> fetchModel) {
        final PropertyMetadata pm = domainMetadata.forProperty(getEntityType(), propName)
                .orElseThrow(() -> new EqlException("Property [%s] not found in [%s].".formatted(propName, getEntityType())));

        final var propType = unwrap(pm.type());
        if (propType.javaType() != fetchModel.getEntityType()) {
            throw new EqlException(format(MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES, pm.type(), propName, getEntityType(), fetchModel.getEntityType()));
        }

        if (propType instanceof PropertyTypeMetadata.Entity et) {
            final EntityMetadata em = domainMetadata.forEntity(et.javaType());
            em.asUnion().ifPresent(uem -> entityMetadataUtils.unionMembers(uem).stream()
                    .map(PropertyMetadata::name)
                    .map(s -> pm.name() + "." + s)
                    // is added here as primitive prop only to avoid its removal in EntQuery.adjustAccordingToFetchModel
                    .forEach(this::addPrimProp));
        }

        final EntityRetrievalModel<?> existingFetch = getRetrievalModels().get(propName);
        fetch<?> finalFetch = existingFetch != null ? existingFetch.originalFetch.unionWith(fetchModel) : fetchModel;
        addEntityPropFetchModel(propName, new EntityRetrievalModel<>(finalFetch, domainMetadata, false));
    }

    public boolean isFetchIdOnly() {
        return getPrimProps().size() == 1 && getRetrievalModels().size() == 0 && containsProp(ID);
    }

    @Override
    public boolean containsOnlyTotals() {
        return getPrimProps().stream()
                .allMatch(prop -> entityMetadata.property(prop)
                        .flatMap(PropertyMetadata::asCalculated)
                        .map(pm -> pm.data().forTotals())
                        .orElse(FALSE));
    }

    private boolean isPure(final PropertyMetadata pm) {
        return entityMetadata.isPersistent() &&
               switch (pm.nature()) {
                   case PropertyNature.Persistent $ -> false;
                   case PropertyNature.Calculated $ -> false;
                   case PropertyNature.Transient  $ -> true;
               };
    }

    private Optional<String> getSinglePropertyOfCompositeUserType(final PropertyMetadata pm) {
        if (pm.hibType() instanceof ICompositeUserTypeInstantiate hibUserType) {
            final String[] propNames = hibUserType.getPropertyNames();
            if (propNames.length == 1) {
                return Optional.of(propNames[0]);
            }
        }
        return Optional.empty();
    }

}
