package ua.com.fielden.platform.web.centre;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

/**
 * A structure that represents an execution context for functional entities. Not all of its properties should or need to be populated. Depending on specific needs actions may choose
 * what parts of the context do they require. This allows for optimising the amount of data marshaled between between the client and server.
 * 
 * TODO to be renamed to Context as it also represents the context on master functional actions, not only on centre
 *
 * @author TG Team
 *
 * @param <T>
 *            -- a type of the entity represented at an entity centre
 * @param <M>
 *            -- in case of an entity centre that is associated with an entity master, this is a type of the master entity
 */
public final class CentreContext<T extends AbstractEntity<?>, M extends AbstractEntity<?>> {

    /**
     * An action may be applicable to zero, one or more entities that are selected on an entity centre. If an action is applicable only to one entity it is associated with (i.e.
     * button in a row against an entity) then only this one entity should be present in the list of selected entities. The action configuration should drive the client side logic
     * what should be serialised and provided as its context at the server side.
     */
    private final List<T> selectedEntities = new ArrayList<>();

    /**
     * Represents selection criteria of an entity centre. Provides access to their values and meta-values. Also, it can be used for execution of the same query as if running from
     * an entity centre at the client side.
     */
    private EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> selectionCrit;

    /**
     * If an entity centre is a part of some compound master then a corresponding master entity could be provided as a context member.
     */
    private M masterEntity;

    /**
     * The computation function used to calculate additional information for action on entity centre or master.
     * <p>
     * This is the function from some <code>context</code> and / or functional <code>entity</code> (produced from that context).
     * Implementors, such as functional entity producers, query enhancers etc., could use any <code>context</code> however
     * it most likely will be the context from which computation has been retrieved.
     * <p>
     * If some static value returns from computation (independent from any context and functional entity)
     * then <code>computation.apply(null, null)</code> form could be used in implementors.
     */
    private Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation = Optional.empty();

    private String chosenProperty;
    
    public T getCurrEntity() {
        if (selectedEntities.size() == 1) {
            return selectedEntities.get(0);
        }
        throw new IllegalStateException(format("The number of selected entities is %s, which is not applicable for determining a current entity.", selectedEntities.size()));
    }

    public List<AbstractEntity<?>> getSelectedEntities() {
        return Collections.unmodifiableList(selectedEntities);
    }

    @SuppressWarnings("unchecked")
	public void setSelectedEntities(final List<T> selectedEntities) {
        this.selectedEntities.clear();
        if (selectedEntities != null) {
            for (final AbstractEntity<?> el: selectedEntities) {
                final Class<? extends AbstractEntity<?>> originalType = el.getDerivedFromType();
                final List<String> originalTypeProperties = Finder.streamRealProperties(originalType)
                    .map(field -> field.getName())
                    .collect(Collectors.toList());
                final String[] propsToBeProxied = Finder.streamRealProperties(el.getClass())
                    .map(field -> field.getName())
                    .filter(name -> Reflector.isPropertyProxied(el, name) && originalTypeProperties.contains(name))
                    .collect(Collectors.toList())
                    .toArray(new String[] {});
                    
                // let's be smart about types and try to handle the situation with generated types
                this.selectedEntities.add((T) el.copy(EntityProxyContainer.proxy(originalType, propsToBeProxied)));
            }
        }
    }

    public EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> getSelectionCrit() {
        return selectionCrit;
    }

    public void setSelectionCrit(final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> selectionCrit) {
        this.selectionCrit = selectionCrit;
    }

    public M getMasterEntity() {
        return masterEntity;
    }

    public void setMasterEntity(final M masterEntity) {
        this.masterEntity = masterEntity;
    }

    @Override
    public String toString() {
        return format("Centre Context: [\n"
            + "    selectionCrit = %s,\n"
            + "    selectedEntities = %s,\n"
            + "    masterEntity=%s,\n"
            + "    computation=%s,\n"
            + "    chosenProperty=%s\n"
            + "]", selectionCrit, selectedEntities, masterEntity, computation, chosenProperty);
    }

    public void setComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        this.computation = Optional.of(computation);
    }

    public Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> getComputation() {
        return computation;
    }
    
    public String getChosenProperty() {
        return chosenProperty;
    }
    
    public void setChosenProperty(final String chosenProperty) {
        this.chosenProperty = chosenProperty;
    }
}
