package ua.com.fielden.platform.web.resources.webui;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader.getOriginalType;
import static ua.com.fielden.platform.types.tuples.T2.t2;
import static ua.com.fielden.platform.web.factories.webui.ResourceFactoryUtils.getEntityMaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.types.tuples.T2;
import ua.com.fielden.platform.web.app.IWebUiConfig;
import ua.com.fielden.platform.web.centre.EntityCentre;
import ua.com.fielden.platform.web.centre.api.actions.multi.IEntityMultiActionSelector;
import ua.com.fielden.platform.web.view.master.EntityMaster;

/// Utilities for resolving runtime sub-action indices for multi-action groups across primary, secondary, and property action zones.
/// For each of the supported zones, this class produces a per-entity payload that the client uses to pick which sub-action of each group is currently active.
///
/// The payload shapes differ by zone:
/// primary (single multi-action per centre) is `List<Integer>`;
/// secondary (list of multi-actions per row) is `List<List<Integer>>`;
/// property — centre side (list of multi-action groups per column) is `List<Map<String, List<Integer>>>` — per entity, per property name, one index per group, in DSL declaration order;
/// property — master side is `Map<String, Integer>` per entity, because master DSL does not group property actions.
///
public class MultiActionUtils {
    private static final String PRIMARY_ACTION_INDICES = "primaryActionIndices";
    private static final String SECONDARY_ACTION_INDICES = "secondaryActionIndices";
    private static final String PROPERTY_ACTION_INDICES = "propertyActionIndices";

    /// Calculates the index of the active primary action for each entity in `entities`.
    ///
    static T2<String, List<Integer>> createPrimaryActionIndicesForCentre(final List<AbstractEntity<?>> entities, final EntityCentre<AbstractEntity<?>> centre) {
        return t2(PRIMARY_ACTION_INDICES, centre.createPrimaryActionSelector().map(selector -> {
            return entities.stream().map(entity -> selector.getActionFor(entity)).collect(toList());
        }).orElse(emptyList()));
    }

    /// Calculates indices of active secondary actions for each entity in `entities`.
    /// Per entity, returns one index per declared secondary multi-action.
    ///
    static T2<String, List<List<Integer>>> createSecondaryActionIndicesForCentre(final List<AbstractEntity<?>> entities, final EntityCentre<AbstractEntity<?>> centre) {
        final List<? extends IEntityMultiActionSelector> selectors = centre.createSecondaryActionSelectors(); // create all selectors before entities streaming (and reuse them for every entity)
        return t2(SECONDARY_ACTION_INDICES, entities.stream()
            .map(entity -> selectors.stream()
                .map(selector -> selector.getActionFor(entity))
                .collect(toList())
            )
            .collect(toList()));
    }

    /// Master-side helper: returns the index of the active sub-action for each property's single multi-action.
    ///
    private static <T extends AbstractEntity<?>> Map<String, Integer> getIndicesFor(final T entity, final Map<String, ? extends IEntityMultiActionSelector> selectors) {
        return selectors.entrySet().stream()
            .map(entry -> t2(entry.getKey(), entry.getValue().getActionFor(entity)))
            .collect(toMap(tt -> tt._1, tt -> tt._2));
    }

    /// Centre-side helper: for each property maps to the ordered list of selectors' chosen sub-action indices.
    /// The per-property list mirrors the column's `withAction` / `withMultiAction` chain in declaration order.
    ///
    private static <T extends AbstractEntity<?>> Map<String, List<Integer>> getCentreIndicesFor(final T entity, final Map<String, List<IEntityMultiActionSelector>> selectors) {
        return selectors.entrySet().stream()
            .map(entry -> t2(entry.getKey(), entry.getValue().stream().map(selector -> selector.getActionFor(entity)).collect(toList())))
            .collect(toMap(tt -> tt._1, tt -> tt._2));
    }

    /// Calculates per-group sub-action indices of property actions for each entity in the result set.
    /// Each column may have several multi-action groups (one per `withAction` / `withMultiAction` call); for each entity the value is a list of indices — one per group.
    ///
    static T2<String, List<Map<String, List<Integer>>>> createPropertyActionIndicesForCentre(final List<AbstractEntity<?>> entities, final EntityCentre<AbstractEntity<?>> centre) {
        final Map<String, List<IEntityMultiActionSelector>> selectors = centre.createPropertyActionSelectors(); // build once, reuse per entity
        return t2(PROPERTY_ACTION_INDICES, entities.stream().map(entity -> getCentreIndicesFor(entity, selectors)).collect(toList()));
    }

    /// Returns indices of property actions on the entity master for the specified entity.
    ///
    static <T extends AbstractEntity<?>> T2<String, Object> createPropertyActionIndicesForMaster(final T entity, final IWebUiConfig webUiConfig) {
        if (entity != null) {
            final Class<T> entityType = getOriginalType(entity.getType());
            final EntityMaster<T> master = (EntityMaster<T>) getEntityMaster(entityType, webUiConfig);
            if (master != null) {
                return t2(PROPERTY_ACTION_INDICES, getIndicesFor(entity, master.getPropertyActionSelectors()));
            }
        }
        return t2(PROPERTY_ACTION_INDICES, new HashMap<>());
    }

}