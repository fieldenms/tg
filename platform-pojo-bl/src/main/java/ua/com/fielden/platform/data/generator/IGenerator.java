package ua.com.fielden.platform.data.generator;

import java.util.Map;
import java.util.Optional;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.error.Result;

/**
 * A contract to be used for implementing data generators. 
 * The main purpose of data generators that implement this contract is to be used by Entity Centres at their <code>run</code> phase to generate the data, which is then used for retrieval.
 * <p>
 * A typical example where this could be useful is some analysis, which computes the necessary data ad hoc, temporarily stores it and exposes for further interrogation via an Entity Centre.  
 * <p>
 * Another interesting use cases for it is the implementation of simple "wizards" where the source data for the resultant data, which needs to be produced by the wizard, is generated based on the specified selection criteria.
 * <p>
 * In these cases, the selection criteria values are used as parameters for a data generation algorithm.
 * <p>
 * It is expected that all concrete implementations of this contract will exists at the level of specific application DAO modules. 
 * 
 * @author TG Team
 *
 * @param <T> -- An entity type that describes the data, which needs to be generated. This information is especially useful in cases where the generated data underpins or used as one of the building blocks for some synthesized entity. 
 */
public interface IGenerator<T extends AbstractEntity<?> & WithCreatedByUser<T>> {
    public static final String FORCE_REGENERATION_KEY = "@@forceRegeneration";
    
    /**
     * Kicks in the data generation algorithm. 
     * The implementation of this method should most likely be annotate with <code>@SessionRequired</code> annotation to establish a database transaction demarcation.
     * The necessary companion objects should be injected at the constructor level.
     * Removal of previously generated for current user data should be implemented as part of this method.
     * 
     * @param type -- A class for a type of the data to be generated. 
     * @param params -- A map of parameter/value pairs that is used by the data generation algorithm. Values are wrapped into {@link Optional} to better reflect the fact that some parameters could have no value. At run-time, these parameters are to be provided by the Entity Centre runner, but they can also be conveniently passed in for unit testing purposes.
     * @return
     */
    Result gen(final Class<T> type, final Map<String, Optional<?>> params);
    
    /**
     * Returns <code>true</code> in case if regeneration of generated (and modified by user) data should occur, otherwise <code>false</code>.
     *
     * @param params - parameters of centre running action or {@link IGenerator#gen(Class, Map)} method's <code>params</code>
     * @return
     */
    public static boolean shouldForceRegeneration(final Map<String, ?> params) {
        return params.containsKey(FORCE_REGENERATION_KEY);
    }
    
}
