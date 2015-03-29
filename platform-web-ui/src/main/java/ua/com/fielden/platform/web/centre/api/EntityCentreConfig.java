package ua.com.fielden.platform.web.centre.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.basic.IValueMatcherWithCentreContext;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.PropDef;

/**
 *
 * Represents a final structure of an entity centre as produced by means of using Entity Centre DSL.
 *
 * @author TG Team
 *
 */
public class EntityCentreConfig {
    // TODO the actual structure still needs to be determined

    //////////////////////////////////////////
    ///////////// SELECTION CRIT /////////////
    //////////////////////////////////////////


    // TODO need to add structure suitable for capturing selection criteria configurations.
    //      Could incorporate the valueMatchers map below

    /**
     * A map between selection criteria properties and their custom value matchers.
     * If a matcher for a some criterion is not provided then a default instance of type {@link FallbackValueMatcherWithCentreContext} should be used.
     */
    private final Map<String, IValueMatcherWithCentreContext<? extends AbstractEntity<?>>> valueMatchers = new HashMap<>();


    //////////////////////////////////////////
    //////////////// RESULT SET //////////////
    //////////////////////////////////////////

    /**
     * Contains action configurations for actions that are associated with individual properties of retrieved entities,
     * which are represented in the result set.
     * This map can be empty if there is no need to provide custom actions specific for represented in the result set properties.
     * In this case, the default actions would still get associated with all not listed in this map, but added to the result set properties.
     * In order to skip the creation even of default actions, <code>no action</code> configuration needs to be provided for a property.
     * <p>
     * Custom properties that are added as instances of {@link PropDef} are represented in this map by their titles.
     * This means that titles should be unique, which is only a natural requirement and is not a restrictive constraint.
     */
    private final Map<String, EntityActionConfig> resultSetPropActions = new HashMap<>();

    /**
     * A primary entity action configuration that is associated with every retrieved and present in the result set entity.
     * It can be <code>null</code> if no primary entity action is needed.
     */
    private EntityActionConfig primaryEntityAction;

    /**
     * A list of secondary action configurations that are associated with every retrieved and present in the result set entity.
     * It can be empty if no secondary action are necessary.
     */
    private List<EntityActionConfig> secondaryEntityActions = new ArrayList<>();

}
