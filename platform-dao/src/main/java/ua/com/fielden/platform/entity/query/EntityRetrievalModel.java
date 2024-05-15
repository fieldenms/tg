package ua.com.fielden.platform.entity.query;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.exceptions.EqlException;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.metadata.DomainMetadataAnalyser;
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
import static ua.com.fielden.platform.reflection.AnnotationReflector.getKeyType;
import static ua.com.fielden.platform.utils.EntityUtils.*;

public class EntityRetrievalModel<T extends AbstractEntity<?>> extends AbstractRetrievalModel<T> {
    private final EntityMetadata entityMetadata;
    private final EntityMetadataUtils entityMetadataUtils;
    private final PropertyMetadataUtils propMetadataUtils;

    public EntityRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser) {
        this(originalFetch, domainMetadataAnalyser, true);
    }

    EntityRetrievalModel(final fetch<T> originalFetch, final DomainMetadataAnalyser domainMetadataAnalyser, final boolean topLevel) {
        super(originalFetch, domainMetadataAnalyser, topLevel);
        this.entityMetadata = null; // TODO
        this.entityMetadataUtils = null; // TODO
        this.propMetadataUtils = null; // TODO

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
                !(KEY.equals(name) && !pm.nature().isPersistent()) &&
                !pm.type().isCollectional() &&
                !name.contains(".") &&
                !containsProp(name) &&
                (entityMetadata.nature().isSynthetic() || !pm.nature().isTransient())) {
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
                .filter(pm -> propMetadataUtils.isPropEntityType(pm.type(), em -> em.nature().isPersistent()))
                .forEach(pm -> with(pm.name(), false));
    }

    private void includeAllFirstLevelPrimPropsAndKey() {
        entityMetadata.properties().stream()
                .filter(pm -> !pm.nature().isCalculated() && !pm.type().isCollectional() && !isPure(pm))
                .forEach(pm -> {
                    final boolean skipEntities = !(pm.type().isEntity() && (AbstractEntity.KEY.equals(pm.name()) || pm.is(KEY_MEMBER)));
                    with(pm.name(), skipEntities);
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
                .filter(pm -> !pm.nature().isCalculated() && !pm.type().isCollectional() && !isPure(pm))
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
                if (pm.type() instanceof PropertyTypeMetadata.Entity et && !PropertyDescriptor.class.equals(et.javaType())/* && !optPm.isId()*/) {
                    if (!skipEntities) {
                        if (propMetadataUtils.isPropEntityType(pm.type(), em -> em.nature().isUnion())) {
                            with(propName, fetchAll(et.javaType()));
                        } else {
                            with(propName, fetch(et.javaType()));
                        }
                    } else if (pm.nature().isPersistent()) {
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

        if (pm.type().javaType() != fetchModel.getEntityType()) {
            throw new EqlException(format(MSG_MISMATCH_BETWEEN_PROPERTY_AND_FETCH_MODEL_TYPES, pm.type(), propName, getEntityType(), fetchModel.getEntityType()));
        }

        if (pm.type() instanceof PropertyTypeMetadata.Entity et) {
            final EntityMetadata em = domainMetadata.forEntity(et.javaType());
            em.asUnion().ifPresent(uem -> entityMetadataUtils.unionMembers(uem).stream()
                    .map(PropertyMetadata::name)
                    .map(s -> pm.name() + "." + s)
                    // is added here as primitive prop only to avoid its removal in EntQuery.adjustAccordingToFetchModel
                    .forEach(this::addPrimProp));
        }

        final EntityRetrievalModel<?> existingFetch = getRetrievalModels().get(propName);
        fetch<?> finalFetch = existingFetch != null ? existingFetch.originalFetch.unionWith(fetchModel) : fetchModel;
        addEntityPropFetchModel(propName, new EntityRetrievalModel<>(finalFetch, getDomainMetadataAnalyser(), false));
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
        return entityMetadata.nature().isPersistent() && pm.nature().isTransient();
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
