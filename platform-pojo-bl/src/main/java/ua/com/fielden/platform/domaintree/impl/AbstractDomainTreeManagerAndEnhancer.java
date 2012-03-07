package ua.com.fielden.platform.domaintree.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.ITickManagerWithMutability;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.IncludedAndCheckedPropertiesSynchronisationListener;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

/**
 * Domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeManagerAndEnhancer implements IDomainTreeManagerAndEnhancer {
    private static final long serialVersionUID = 7838262757425383240L;
    private final AbstractDomainTreeManager base;
    private final IDomainTreeEnhancer enhancer;
    private transient final IDomainTreeEnhancer enhancerWithPropertiesPopulation;

    private transient final DomainTreeRepresentationAndEnhancer dtr;
    private transient final TickManagerAndEnhancer firstTick;
    private transient final TickManagerAndEnhancer secondTick;

    protected IDomainTreeManager base() {
	return base;
    }

    protected IDomainTreeEnhancer enhancer() {
        return enhancer;
    }

    protected TickManagerAndEnhancer createFirstTick(final TickManager base) {
	return new TickManagerAndEnhancer(base);
    }

    protected TickManagerAndEnhancer createSecondTick(final TickManager base) {
	return new TickManagerAndEnhancer(base);
    }

    protected DomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new DomainTreeRepresentationAndEnhancer(base);
    }

    /**
     * The {@link DomainTreeEnhancer} wrapper that reflects the changes in manager.
     *
     * @author TG Team
     *
     */
    private static class DomainTreeEnhancerWithPropertiesPopulation implements IDomainTreeEnhancer {
	private final DomainTreeEnhancer baseEnhancer;
	private final DomainTreeRepresentationAndEnhancer dtr;

	/**
	 * A {@link DomainTreeEnhancerWithPropertiesPopulation} constructor which requires a base implementations of {@link DomainTreeEnhancer} and {@link AbstractDomainTreeRepresentation}.
	 *
	 * @param baseEnhancer
	 * @param dtr
	 */
	protected DomainTreeEnhancerWithPropertiesPopulation(final DomainTreeEnhancer baseEnhancer, final DomainTreeRepresentationAndEnhancer dtr) {
	    this.baseEnhancer = baseEnhancer;
	    this.dtr = dtr;
	}

	/**
	 * Finds a first index of property that does not start with "path".
	 *
	 * @param pathIndex
	 * @param path
	 * @param props
	 * @return
	 */
	private int nextBranchIndex(final int pathIndex, final String path, final List<String> props) {
	    int i = pathIndex + 1;
	    final String prefix = "".equals(path) ? "" : (path + ".");
	    while (i < props.size() && props.get(i).startsWith(prefix)) {
		i++;
	    }
	    return i;
	}

	/**
	 * Migrate <code>calcProps</code> from map form to a set form.
	 *
	 * @param oldCalculatedProperties
	 * @return
	 */
	private Set<Pair<Class<?>, String>> migrateToSet(final Map<Class<?>, List<ICalculatedProperty>> oldCalculatedProperties) {
	    final Set<Pair<Class<?>, String>> set = new HashSet<Pair<Class<?>, String>>();
	    for (final Entry<Class<?>, List<ICalculatedProperty>> entry : oldCalculatedProperties.entrySet()) {
		for (final ICalculatedProperty prop : entry.getValue()) {
		    set.add(new Pair<Class<?>, String>(entry.getKey(), prop.pathAndName()));
		}
	    }
	    return set;
	}

	@Override
	public void apply() {
	    final Map<Class<?>, List<ICalculatedProperty>> oldCalculatedProperties = DomainTreeEnhancer.extractAll(baseEnhancer.getOriginalAndEnhancedRootTypes(), baseEnhancer, baseEnhancer.getFactory()); // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    // TODO this or baseEnhancer?!!!
	    baseEnhancer.apply();
	    final Map<Class<?>, List<ICalculatedProperty>> newCalculatedProperties = new HashMap<Class<?>, List<ICalculatedProperty>>(baseEnhancer.calculatedProperties());

	    final Set<Pair<Class<?>, String>> was = migrateToSet(oldCalculatedProperties);
	    final Set<Pair<Class<?>, String>> is = migrateToSet(newCalculatedProperties);

	    final Set<Pair<Class<?>, String>> wasUnionIs = new HashSet<Pair<Class<?>,String>>(was);
	    wasUnionIs.addAll(is);

	    // form a set of completely new calculated properties:
	    final Set<Pair<Class<?>, String>> neew = new HashSet<Pair<Class<?>,String>>(wasUnionIs);
	    neew.removeAll(was);
	    // add new calc properties to included properties list
	    for (final Pair<Class<?>, String> rootAndProp : neew) {
		final Class<?> root = /*baseEnhancer.getManagedType(*/ rootAndProp.getKey()/* ) */;
		final String newProperty = rootAndProp.getValue();
		final List<String> inclProps = dtr.includedProperties(root);
		if (!dtr.isExcludedImmutably(root, newProperty)) {
		    // the property is not excluded 1) by contract 2) was not excluded manually
		    // this is a new property. "includedProperties" should be updated (the new property added).
		    final String parent = PropertyTypeDeterminator.isDotNotation(newProperty) ? PropertyTypeDeterminator.penultAndLast(newProperty).getKey() : "";
		    // ! important ! the parent should be warmed up before adding anything to it!
		    dtr.warmUp(root, parent);

		    final int pathIndex = inclProps.indexOf(parent);
		    // add the property on the place of the last parent child (just before next branch of properties)
		    final int nextBranchIndex = nextBranchIndex(pathIndex, parent, inclProps);
		    if (nextBranchIndex > 0 && !EntityUtils.equalsEx(inclProps.get(nextBranchIndex - 1), newProperty)) { // edge-case : when warming up a NEW calc property WILL BE restored from enhanced type, and it should not be added twice
			if (nextBranchIndex < inclProps.size()) {
			    dtr.includedPropertiesMutable(root).add(nextBranchIndex, newProperty);
			} else {
			    dtr.includedPropertiesMutable(root).add(newProperty);
			}
		    }
		}
	    }

	    // form a set of removed calculated properties:
	    final Set<Pair<Class<?>, String>> removed = new HashSet<Pair<Class<?>,String>>(wasUnionIs);
	    removed.removeAll(is);
	    // remove obsolete calc properties from included properties list
	    for (final Pair<Class<?>, String> rootAndProp : removed) {
		final Class<?> root = rootAndProp.getKey();
		final String removedProperty = rootAndProp.getValue();
		// this is a removed property. "includedProperties" should be updated (the removed property should be removed in incl properties).
		dtr.includedPropertiesMutable(root).remove(removedProperty);
	    }

	    // TODO update retained calc properties in included properties list? Will their places be changed? Will excludement logic be changed after that?
	    // TODO update retained calc properties in included properties list? Will their places be changed? Will excludement logic be changed after that?
	    // TODO update retained calc properties in included properties list? Will their places be changed? Will excludement logic be changed after that?
	    // TODO update retained calc properties in included properties list? Will their places be changed? Will excludement logic be changed after that?
	    // form a set of retained calculated properties:
	    // final Set<Pair<Class<?>, String>> retained = new HashSet<Pair<Class<?>,String>>(is);
	    // retained.retainAll(was);
	    // for (final Pair<Class<?>, String> rootAndProp : retained) {
	    // }
	}

	@Override
	public void discard() {
	    baseEnhancer.discard();
	}

	@Override
	public Class<?> getManagedType(final Class<?> type) {
	    return baseEnhancer.getManagedType(type);
	}

	@Override
	public void addCalculatedProperty(final ICalculatedProperty calculatedProperty) {
	    baseEnhancer.addCalculatedProperty(calculatedProperty);
	}

	@Override
	public void addCalculatedProperty(final Class<?> root, final String contextPath, final String contextualExpression, final String title, final String desc, final CalculatedPropertyAttribute attribute, final String originationProperty) {
	    baseEnhancer.addCalculatedProperty(root, contextPath, contextualExpression, title, desc, attribute, originationProperty);
	}

	@Override
	public void removeCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
	    baseEnhancer.removeCalculatedProperty(rootType, calculatedPropertyName);
	}

	@Override
	public ICalculatedProperty getCalculatedProperty(final Class<?> rootType, final String calculatedPropertyName) {
	    return baseEnhancer.getCalculatedProperty(rootType, calculatedPropertyName);
	}

	@Override
	public ICalculatedProperty validateCalculatedPropertyKey(final ICalculatedProperty calculatedProperty, final String pathAndName) {
	    return baseEnhancer.validateCalculatedPropertyKey(calculatedProperty, pathAndName);
	}
    }

    /**
     * A <i>manager with enhancer</i> constructor.
     */
    protected AbstractDomainTreeManagerAndEnhancer(final AbstractDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	this.base = base;
	this.enhancer = enhancer;

	dtr = createRepresentation((AbstractDomainTreeRepresentation) base.getRepresentation());
	firstTick = createFirstTick((TickManager) base.getFirstTick());
	secondTick = createSecondTick((TickManager) base.getSecondTick());
	enhancerWithPropertiesPopulation = new DomainTreeEnhancerWithPropertiesPopulation((DomainTreeEnhancer) this.enhancer, dtr);

	final IPropertyStructureChangedListener oldListener = this.base.listener();
	final IPropertyStructureChangedListener newListener = new IncludedAndCheckedPropertiesSynchronisationListener(this.firstTick, this.secondTick);
	this.base.removePropertyStructureChangedListener(oldListener);
	this.addPropertyStructureChangedListener(newListener);
    }


    @Override
    public boolean addPropertyStructureChangedListener(final IPropertyStructureChangedListener listener) {
	return base.addPropertyStructureChangedListener(listener);
    }

    @Override
    public boolean removePropertyStructureChangedListener(final IPropertyStructureChangedListener listener) {
	return base.removePropertyStructureChangedListener(listener);
    }

    @Override
    public IDomainTreeEnhancer getEnhancer() {
	return enhancerWithPropertiesPopulation;
    }

    @Override
    public ITickManager getFirstTick() {
        return firstTick;
    }

    @Override
    public ITickManager getSecondTick() {
        return secondTick;
    }

    /**
     * A <i>tick manager with enhancer</i>.
     *
     * @author TG Team
     *
     */
    protected class TickManagerAndEnhancer extends TickManager implements ITickManagerWithMutability {
	private static final long serialVersionUID = 6961101639080080892L;
	private final TickManager base;

	protected TickManager base() {
	    return base;
	}

	protected TickManagerAndEnhancer(final TickManager base) {
	    this.base = base;
	}

	@Override
	public List<String> checkedPropertiesMutable(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base.checkedPropertiesMutable(enhancerWithPropertiesPopulation.getManagedType(root));
	}

	@Override
	protected boolean isCheckedMutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base.isCheckedMutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
	}

	@Override
	public boolean isCheckedNaturally(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base.isCheckedNaturally(enhancerWithPropertiesPopulation.getManagedType(root), property);
	}

	@Override
	public boolean isChecked(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base.isChecked(enhancerWithPropertiesPopulation.getManagedType(root), property);
	}

	@Override
	public void check(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    base.check(enhancerWithPropertiesPopulation.getManagedType(root), property, check);
	}

	@Override
	public List<String> checkedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base.checkedProperties(enhancerWithPropertiesPopulation.getManagedType(root));
	}

	@Override
	public void swap(final Class<?> root, final String property1, final String property2) {
	    // inject an enhanced type into method implementation
	    base.swap(enhancerWithPropertiesPopulation.getManagedType(root), property1, property2);
	}

	@Override
	public void move(final Class<?> root, final String what, final String beforeWhat) {
	    // inject an enhanced type into method implementation
	    base.move(enhancerWithPropertiesPopulation.getManagedType(root), what, beforeWhat);
	}

	@Override
	public void moveToTheEnd(final Class<?> root, final String what) {
	    // inject an enhanced type into method implementation
	    base.moveToTheEnd(enhancerWithPropertiesPopulation.getManagedType(root), what);
	}

	protected IDomainTreeEnhancer enhancer() {
	    return enhancerWithPropertiesPopulation;
	}
    }

    @Override
    public IDomainTreeRepresentation getRepresentation() {
	return dtr;
    }

    /**
     * A <i>representation with enhancer</i>.
     *
     * @author TG Team
     *
     */
    protected class DomainTreeRepresentationAndEnhancer implements IDomainTreeRepresentation {
	private static final long serialVersionUID = 7828017670201120912L;
	private final AbstractDomainTreeRepresentation base;
	private final ITickRepresentation firstTick;
	private final ITickRepresentation secondTick;

	/**
	 * A <i>representation with enhancer</i> constructor.
	 */
	protected DomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    this.base = base;

	    firstTick = createFirstTick(base.getFirstTick());
	    secondTick = createSecondTick(base.getSecondTick());
	}

	/**
	 * Getter of mutable "included properties" cache for internal purposes.
	 * <p>
	 * Please note that you can only mutate this list with methods {@link List#add(Object)} and {@link List#remove(Object)} to correctly reflect the changes on depending
	 * objects. (e.g. UI tree models, checked properties etc.)
	 *
	 * @param root
	 * @return
	 */
	protected List<String> includedPropertiesMutable(final Class<?> root) {
	    return this.base.includedPropertiesMutable(enhancerWithPropertiesPopulation.getManagedType(root));
	}

	protected ITickRepresentation createFirstTick(final ITickRepresentation base) {
	    return new TickRepresentationAndEnhancer(base);
	}

	protected ITickRepresentation createSecondTick(final ITickRepresentation base) {
	    return new TickRepresentationAndEnhancer(base);
	}

	/**
	 * A <i>representation tick with enhancer</i>.
	 *
	 * @author TG Team
	 *
	 */
	protected class TickRepresentationAndEnhancer implements ITickRepresentation {
	    private static final long serialVersionUID = -4811228680416493535L;
	    private final ITickRepresentation base;

	    protected TickRepresentationAndEnhancer(final ITickRepresentation base) {
		this.base = base;
	    }

	    protected ITickRepresentation base() {
		return base;
	    }

	    @Override
	    public boolean isDisabledImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base.isDisabledImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
	    }

	    @Override
	    public void disableImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		base.disableImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
	    }

	    @Override
	    public boolean isCheckedImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base.isCheckedImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
	    }

	    @Override
	    public void checkImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		base.checkImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
	    }
	}

	@Override
	public Set<Function> availableFunctions(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base.availableFunctions(enhancerWithPropertiesPopulation.getManagedType(root), property);
	}

	@Override
	public ITickRepresentation getFirstTick() {
	    return firstTick;
	}

	@Override
	public ITickRepresentation getSecondTick() {
	    return secondTick;
	}

	@Override
	public boolean isExcludedImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base.isExcludedImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
	}

	@Override
	public void excludeImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base.excludeImmutably(enhancerWithPropertiesPopulation.getManagedType(root), property);
	}

	@Override
	public List<String> includedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base.includedProperties(enhancerWithPropertiesPopulation.getManagedType(root));
	}

	@Override
	public void warmUp(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base.warmUp(enhancerWithPropertiesPopulation.getManagedType(root), property);
	}

	@Override
	public Set<Class<?>> rootTypes() {
	    return base.rootTypes();
	}

	protected IDomainTreeEnhancer enhancer() {
	    return enhancerWithPropertiesPopulation;
	}
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((base == null) ? 0 : base.hashCode());
	result = prime * result + ((enhancer == null) ? 0 : enhancer.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	final AbstractDomainTreeManagerAndEnhancer other = (AbstractDomainTreeManagerAndEnhancer) obj;
	if (base == null) {
	    if (other.base != null)
		return false;
	} else if (!base.equals(other.base))
	    return false;
	if (enhancer == null) {
	    if (other.enhancer != null)
		return false;
	} else if (!enhancer.equals(other.enhancer))
	    return false;
	return true;
    }
}
