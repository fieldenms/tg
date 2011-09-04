package ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAbstractAnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.IAbstractAnalysisDomainTreeRepresentation.IAbstractAnalysisAddToAggregationTickRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.impl.EnhancementRootsMap;
import ua.com.fielden.platform.utils.Pair;

/**
 * Analyses domain tree manager. Includes support for checking (from base {@link AbstractDomainTreeManager}). <br>
 * <br>
 *
 * Includes implementation of "checking" logic, that contain: <br>
 * a) default mutable state management; <br>
 * a) manual state management; <br>
 * b) resolution of conflicts with excluded, disabled etc. properties; <br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractAnalysisDomainTreeManager extends AbstractDomainTreeManager implements IAbstractAnalysisDomainTreeManager {
    private static final long serialVersionUID = -6113130505321975328L;

    private Boolean visible;

    @Override
    public IAbstractAnalysisAddToDistributionTickManager getFirstTick() {
	return (IAbstractAnalysisAddToDistributionTickManager) super.getFirstTick();
    }

    @Override
    public IAbstractAnalysisAddToAggregationTickManager getSecondTick() {
	return (IAbstractAnalysisAddToAggregationTickManager) super.getSecondTick();
    }

    @Override
    public IAbstractAnalysisDomainTreeRepresentation getRepresentation() {
	return (IAbstractAnalysisDomainTreeRepresentation) super.getRepresentation();
    }

    /**
     * A <i>manager</i> constructor.
     *
     * @param serialiser
     * @param dtr
     * @param firstTick
     * @param secondTick
     */
    protected AbstractAnalysisDomainTreeManager(final ISerialiser serialiser, final IDomainTreeRepresentation dtr, final Boolean visible, final TickManager firstTick, final TickManager secondTick) {
	super(serialiser, dtr, firstTick, secondTick);
	this.visible = visible;
    }

    @Override
    public boolean isVisible() {
	return visible != null ? visible : true; // should be visible by default;
    }

    @Override
    public IAbstractAnalysisDomainTreeManager setVisible(final boolean visible) {
	this.visible = visible;
	return this;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + ((visible == null) ? 0 : visible.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (!super.equals(obj))
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final AbstractAnalysisDomainTreeManager other = (AbstractAnalysisDomainTreeManager) obj;
	if (visible == null) {
	    if (other.visible != null)
		return false;
	} else if (!visible.equals(other.visible))
	    return false;
	return true;
    }



    protected abstract static class AbstractAnalysisAddToDistributionTickManager extends TickManager implements IAbstractAnalysisAddToDistributionTickManager {
	private static final long serialVersionUID = 4659406246345595522L;

	private final EnhancementRootsMap<List<String>> rootsListsOfUsedProperties;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public AbstractAnalysisAddToDistributionTickManager() {
	    super();
	    rootsListsOfUsedProperties = createRootsMap();
	}

	@Override
	public boolean isUsed(final Class<?> root, final String property) {
	    illegalUncheckedProperties(this, root, property, "It's illegal to ask whether the specified property [" + property + "] is 'used' if it is not 'checked' in type [" + root.getSimpleName() + "].");
	    if (rootsListsOfUsedProperties.containsKey(root)) {
		return rootsListsOfUsedProperties.get(root).contains(property);
	    }
	    return false;
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    illegalUncheckedProperties(this, root, property, "It's illegal to ask whether the specified property [" + property + "] is 'used' if it is not 'checked' in type [" + root.getSimpleName() + "].");
	    if (!rootsListsOfUsedProperties.containsKey(root)) {
		rootsListsOfUsedProperties.put(root, new ArrayList<String>());
	    }
	    final List<String> listOfUsedProperties = rootsListsOfUsedProperties.get(root);
	    if (check && !listOfUsedProperties.contains(property)) {
		listOfUsedProperties.add(determineIndexToInsert(root,property),property);
	    } else if (!check) {
		listOfUsedProperties.remove(property);
	    }
	}

	/**
	 * Determines the index of the property to be 'used' among checked properties.
	 *
	 * @param root
	 * @param property
	 * @return
	 */
	private int determineIndexToInsert(final Class<?> root, final String property) {
	    final List<String> checkedProperties = checkedProperties(root);
	    final int indexOfChecked = checkedProperties.indexOf(property);
	    int indexOfUsed = 0;
	    for(int i = 0; i < indexOfChecked; i++){
		if(isUsed(root, checkedProperties.get(i))){
		    indexOfUsed++;
		}
	    }
	    return indexOfUsed;
	}

	@Override
	public List<String> usedProperties(final Class<?> root) {
	    return rootsListsOfUsedProperties.containsKey(root) ? rootsListsOfUsedProperties.get(root) : new ArrayList<String>();
	}
    }

    protected abstract static class AbstractAnalysisAddToAggregationTickManager extends TickManager implements IAbstractAnalysisAddToAggregationTickManager {
	private static final long serialVersionUID = -4025471910983945279L;

	private final EnhancementRootsMap<List<Pair<String, Ordering>>> rootsListsOfOrderings;
	private final EnhancementRootsMap<List<String>> rootsListsOfUsedProperties;

	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into manager constructor, which will initialise "dtr" and "tr"
	 * fields.
	 */
	public AbstractAnalysisAddToAggregationTickManager() {
	    super();
	    rootsListsOfOrderings = createRootsMap();
	    rootsListsOfUsedProperties = createRootsMap();
	}

	@Override
	protected IAbstractAnalysisAddToAggregationTickRepresentation tr() {
	    return (IAbstractAnalysisAddToAggregationTickRepresentation)super.tr();
	}

	@Override
	public boolean isUsed(final Class<?> root, final String property) {
	    illegalUncheckedProperties(this, root, property, "It's illegal to ask whether the specified property [" + property + "] is 'used' if it is not 'checked' in type [" + root.getSimpleName() + "].");
	    if (rootsListsOfUsedProperties.containsKey(root)) {
		return rootsListsOfUsedProperties.get(root).contains(property);
	    }
	    return false;
	}

	@Override
	public void use(final Class<?> root, final String property, final boolean check) {
	    illegalUncheckedProperties(this, root, property, "It's illegal to ask whether the specified property [" + property + "] is 'used' if it is not 'checked' in type [" + root.getSimpleName() + "].");
	    if (!rootsListsOfUsedProperties.containsKey(root)) {
		rootsListsOfUsedProperties.put(root, new ArrayList<String>());
	    }
	    final List<String> listOfUsedProperties = rootsListsOfUsedProperties.get(root);
	    if (check && !listOfUsedProperties.contains(property)) {
		listOfUsedProperties.add(determineIndexToInsert(root, property), property);
	    } else if (!check) {
		listOfUsedProperties.remove(property);
	    }
	}

	/**
	 * Determines the index of the property to be 'used' among checked properties.
	 *
	 * @param root
	 * @param property
	 * @return
	 */
	private int determineIndexToInsert(final Class<?> root, final String property) {
	    final List<String> checkedProperties = checkedProperties(root);
	    final int indexOfChecked = checkedProperties.indexOf(property);
	    int indexOfUsed = 0;
	    for(int i = 0; i < indexOfChecked; i++){
		if(isUsed(root, checkedProperties.get(i))){
		    indexOfUsed++;
		}
	    }
	    return indexOfUsed;
	}

	@Override
	public List<String> usedProperties(final Class<?> root) {
	    return rootsListsOfUsedProperties.containsKey(root) ? rootsListsOfUsedProperties.get(root) : new ArrayList<String>();
	}

	@Override
	public List<Pair<String, Ordering>> orderedProperties(final Class<?> root) {
	    if (rootsListsOfOrderings.containsKey(root)) {
		return rootsListsOfOrderings.get(root);
	    } else {
		return tr().orderedPropertiesByDefault(root);
	    }
	}

	@Override
	public void toggleOrdering(final Class<?> root, final String property) {
	    AbstractDomainTree.illegalUncheckedProperties(this, root, property, "Could not toggle 'ordering' for 'unchecked' property [" + property + "] in type [" + root.getSimpleName() + "].");
	    if (!rootsListsOfOrderings.containsKey(root)) {
		rootsListsOfOrderings.put(root, new ArrayList<Pair<String, Ordering>>(tr().orderedPropertiesByDefault(root)));
	    }
	    final List<Pair<String, Ordering>> list = new ArrayList<Pair<String, Ordering>>(rootsListsOfOrderings.get(root));
	    for (final Pair<String, Ordering> pair : list) {
		if (pair.getKey().equals(property)) {
		    final int index = rootsListsOfOrderings.get(root).indexOf(pair);
		    if (Ordering.ASCENDING.equals(pair.getValue())) {
			rootsListsOfOrderings.get(root).get(index).setValue(Ordering.DESCENDING);
		    } else { // Ordering.DESCENDING
			rootsListsOfOrderings.get(root).remove(index);
		    }
		    return;
		}
	    } // if the property does not have an Ordering assigned -- put a ASC ordering to it (into the end of the list)
	    rootsListsOfOrderings.get(root).add(new Pair<String, Ordering>(property, Ordering.ASCENDING));
	}
    }

    /**
     * A specific Kryo serialiser for {@link AbstractDomainTreeManager}.
     *
     * @author TG Team
     *
     */
    protected abstract static class AbstractAnalysisDomainTreeManagerSerialiser<T extends AbstractAnalysisDomainTreeManager> extends AbstractDomainTreeManagerSerialiser<T> {
	public AbstractAnalysisDomainTreeManagerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public void write(final ByteBuffer buffer, final T manager) {
	    super.write(buffer, manager);
	    writeValue(buffer, manager.visible);
	}
    }
}
