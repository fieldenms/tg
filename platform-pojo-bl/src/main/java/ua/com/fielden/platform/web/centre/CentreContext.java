package ua.com.fielden.platform.web.centre;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.entity_centre.review.criteria.EnhancedCentreEntityQueryCriteria;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.reflection.Reflector;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;

/// A structure that represents an execution context for functional entities.
/// Not all of its properties should or need to be populated.
/// Depending on specific needs actions may choose what parts of the context they require.
/// This helps with optimising the amount of data marshaled between between the client and server.
///
/// TODO This class needs to be renamed to `Context` as it is used for representing a context for action entity masters and entity centres.
///
/// @param <T> a type of the entity represented by an entity centre
/// @param <M> in case of an entity centre that is associated with an entity master, this is the type of the master entity
///
public final class CentreContext<T extends AbstractEntity<?>, M extends AbstractEntity<?>> {

    public static final String ERR_CANNOT_DETERMINE_CURRENT_ENTITY = "The current entity cannot be determined due to unexpected number of selected entities (%s).";

    /// An action may be applicable to zero, one or more entities that are selected in the Entity Centre.
    /// If an action is applicable to a single entity only (i.e. an action in a row for an entity)
    /// then only this one entity is present in the list of selected entities.
    /// The action configuration drives the client side logic when determining what should be included and serialised in its context.
    ///
    private final List<T> selectedEntities = new ArrayList<>();

    /// Represents selection criteria of an entity centre.
    /// Provides access to their values and meta-values.
    /// Also, it can be used for the execution of the same query as if running from an entity centre at the client side.
    ///
    private EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> selectionCrit;

    /// If an entity centre is a part of some compound master then a corresponding master entity could be provided as a context member.
    ///
    private M masterEntity;

    /// The computation function used to calculate additional information for action on entity centre or master.
    ///
    /// This is a function from some `context` or an action entity (produced from that context).
    /// The implementors, such as action entity producers, query enhancers, etc., could use any `context` to pass into the computation.
    /// However, most likely the `context` would be the one from which the computation was obtained.
    ///
    /// If some static value returns from computation (independent of any context and functional entity)
    /// then the `computation.apply(null, null)` form could be used in the implementors.
    ///
    private Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> computation = Optional.empty();

    /// The name of a property that is considered to be "chosen" in the current context.
    /// For example, this could be a property that was tapped on in EGI to invoke the associated action.
    ///
    private String chosenProperty;

    /// A bag of custom properties in the context.
    /// Usually contains some technical properties to restore the context and may contain custom properties provided by the client-side or server-side logic.
    ///
    private final Map<String, Object> customObject = new LinkedHashMap<>();

    /// A custom instance of instance-based continuation in the context to facilitate direct usage in continuation Entity Master.
    /// This is contrary to type-based continuations where `NeedMoreData` is thrown using type.
    /// Please note, that `instanceBasedContinuation` still goes through its producer (for additional API flexibility).
    ///
    private AbstractEntity<?> instanceBasedContinuation;
    public static final String INSTANCEBASEDCONTINUATION_PROPERTY_NAME = "instanceBasedContinuation";

    public T getCurrEntity() {
        if (selectedEntities.size() == 1) {
            return selectedEntities.getFirst();
        }
        throw new IllegalStateException(format(ERR_CANNOT_DETERMINE_CURRENT_ENTITY, selectedEntities.size()));
    }

    public List<AbstractEntity<?>> getSelectedEntities() {
        return Collections.unmodifiableList(selectedEntities);
    }

    @SuppressWarnings("unchecked")
	public CentreContext<T,M> setSelectedEntities(final List<T> selectedEntities) {
        this.selectedEntities.clear();
        if (selectedEntities != null) {
            for (final AbstractEntity<?> el: selectedEntities) {
                final Class<? extends AbstractEntity<?>> originalType = el.getDerivedFromType();
                final Set<String> originalTypeProperties = Finder.streamRealProperties(originalType)
                    .map(Field::getName)
                    .collect(toImmutableSet());
                final Set<String> propsToBeProxied = Finder.streamRealProperties((Class<? extends AbstractEntity<?>>) el.getClass())
                    .map(Field::getName)
                    .filter(name -> Reflector.isPropertyProxied(el, name) && originalTypeProperties.contains(name))
                    .collect(toImmutableSet());

                // let's be smart about types and try to handle the situation with generated types
                this.selectedEntities.add((T) el.copy(EntityProxyContainer.proxy(originalType, propsToBeProxied)));
            }
        }
        return this;
    }

    public EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> getSelectionCrit() {
        return selectionCrit;
    }

    public CentreContext<T,M> setSelectionCrit(final EnhancedCentreEntityQueryCriteria<T, ? extends IEntityDao<T>> selectionCrit) {
        this.selectionCrit = selectionCrit;
        return this;
    }

    public M getMasterEntity() {
        return masterEntity;
    }

    public CentreContext<T,M> setMasterEntity(final M masterEntity) {
        this.masterEntity = masterEntity;
        return this;
    }

    @Override
    public String toString() {
        return """
            Centre Context: [
                selectionCrit = %s,
                selectedEntities = %s,
                masterEntity = %s,
                computation = %s,
                chosenProperty = %s,
                customObject = %s,
                instanceBasedContinuation = %s
            ]""".formatted(selectionCrit, selectedEntities, masterEntity, computation, chosenProperty, customObject, instanceBasedContinuation);
    }

    public CentreContext<T, M> setComputation(final BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object> computation) {
        this.computation = Optional.of(computation);
        return this;
    }

    public Optional<BiFunction<AbstractFunctionalEntityWithCentreContext<?>, CentreContext<AbstractEntity<?>, AbstractEntity<?>>, Object>> getComputation() {
        return computation;
    }

    public String getChosenProperty() {
        return chosenProperty;
    }

    public CentreContext<T, M> setChosenProperty(final String chosenProperty) {
        this.chosenProperty = chosenProperty;
        return this;
    }

    public CentreContext<T, M> setCustomObject(final Map<String, Object> customObject) {
        this.customObject.clear();
        this.customObject.putAll(customObject);
        return this;
    }

    /// Bag of custom properties in the context.
    /// Usually contains some technical properties for context restoration and may contain custom properties provided by client-side application.
    ///
    public Map<String, Object> getCustomObject() {
        return unmodifiableMap(customObject);
    }

    /// Sets custom property into custom object of this [CentreContext].
    ///
    public CentreContext<T, M> setCustomProperty(final String name, final Object value) {
        customObject.put(name, value);
        return this;
    }

    /// Gets [#instanceBasedContinuation] from the context.
    ///
    public AbstractEntity<?> getInstanceBasedContinuation() {
        return instanceBasedContinuation;
    }

    /// Sets [#instanceBasedContinuation] into the context.
    ///
    public CentreContext<T, M> setInstanceBasedContinuation(AbstractEntity<?> instanceBasedContinuation) {
        this.instanceBasedContinuation = instanceBasedContinuation;
        return this;
    }

}
