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

/**
 * Utilities for multi-actions.
 * 
 * @author TG Team
 *
 */
public class MultiActionUtils {
    private static final String PRIMARY_ACTION_INDICES = "primaryActionIndices";
    private static final String SECONDARY_ACTION_INDICES = "secondaryActionIndices";
    private static final String PROPERTY_ACTION_INDICES = "propertyActionIndices";

    /**
     * Calculates indices of active primary action for {@code entities}.
     *
     * @param entities
     * @return
     */
    static T2<String, List<Integer>> createPrimaryActionIndicesForCentre(final List<AbstractEntity<?>> entities, final EntityCentre<AbstractEntity<?>> centre) {
        return t2(PRIMARY_ACTION_INDICES, centre.createPrimaryActionSelector().map(selector -> {
            return entities.stream().map(entity -> selector.getActionFor(entity)).collect(toList());
        }).orElse(emptyList()));
    }

    /**
     * Calculates indices of active secondary actions for {@code entities}.
     *
     * @param entities
     * @return
     */
    static T2<String, List<List<Integer>>> createSecondaryActionIndicesForCentre(final List<AbstractEntity<?>> entities, final EntityCentre<AbstractEntity<?>> centre) {
        final List<? extends IEntityMultiActionSelector> selectors = centre.createSecondaryActionSelectors(); // create all selectors before entities streaming (and reuse them for every entity)
        return t2(SECONDARY_ACTION_INDICES, entities.stream()
            .map(entity -> selectors.stream()
                .map(selector -> selector.getActionFor(entity))
                .collect(toList())
            )
            .collect(toList()));
    }

    /**
     * Returns indices of actions for {@code entity} and list of {@code selectors}.
     * 
     * @param entity
     * @param selectors
     * @return
     */
    private static <T extends AbstractEntity<?>> Map<String, Integer> getIndicesFor(final T entity, final Map<String, ? extends IEntityMultiActionSelector> selectors) {
        return selectors.entrySet().stream()
            .map(entry -> t2(entry.getKey(), entry.getValue().getActionFor(entity)))
            .collect(toMap(tt -> tt._1, tt -> tt._2));
    }

    /**
     * Calculates indices of property actions for each entity in result set.
     *
     * @param entities - result set entities
     * @return
     */
    static T2<String, List<Map<String, Integer>>> createPropertyActionIndicesForCentre(final List<AbstractEntity<?>> entities, final EntityCentre<AbstractEntity<?>> centre) {
        return t2(PROPERTY_ACTION_INDICES, entities.stream().map(entity -> getIndicesFor(entity, centre.createPropertyActionSelectors())).collect(toList()));
    }

    /**
     * Return indices of property actions on the entity master for specified entity.
     *
     * @param <T>
     * @param entity
     * @param webUiConfig
     * @return
     */
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