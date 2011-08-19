package ua.com.fielden.platform.treemodel.rules.impl;

import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.treemodel.rules.Function;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeEnhancer;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeManager.IDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.IDomainTreeRepresentation;

/**
 * Domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public abstract class AbstractDomainTreeManagerAndEnhancer implements IDomainTreeManagerAndEnhancer {
    private static final long serialVersionUID = 7838262757425383240L;
    private final IDomainTreeManager base;
    private final IDomainTreeEnhancer enhancer;

    private transient final IDomainTreeRepresentation dtr;
    private transient final ITickManager firstTick;
    private transient final ITickManager secondTick;

    protected IDomainTreeManager base() {
	return base;
    }

    protected IDomainTreeEnhancer enhancer() {
        return enhancer;
    }

    protected ITickManager createFirstTick(final ITickManager base) {
	return new TickManagerAndEnhancer(base);
    }

    protected ITickManager createSecondTick(final ITickManager base) {
	return new TickManagerAndEnhancer(base);
    }

    protected IDomainTreeRepresentation createRepresentation(final IDomainTreeRepresentation base) {
	return new DomainTreeRepresentationAndEnhancer(base);
    }

    /**
     * A <i>manager with enhancer</i> constructor.
     */
    protected AbstractDomainTreeManagerAndEnhancer(final IDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	this.base = base;
	this.enhancer = enhancer;

	dtr = createRepresentation(base.getRepresentation());
	firstTick = createFirstTick(base.getFirstTick());
	secondTick = createSecondTick(base.getSecondTick());
    }

    @Override
    public IDomainTreeEnhancer getEnhancer() {
	return enhancer;
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
    protected class TickManagerAndEnhancer implements ITickManager {
	private static final long serialVersionUID = 6961101639080080892L;
	private final ITickManager base;

	protected ITickManager base() {
	    return base;
	}

	protected TickManagerAndEnhancer(final ITickManager base) {
	    this.base = base;
	}

	@Override
	public boolean isChecked(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base.isChecked(enhancer.getManagedType(root), property);
	}

	@Override
	public void check(final Class<?> root, final String property, final boolean check) {
	    // inject an enhanced type into method implementation
	    base.check(enhancer.getManagedType(root), property, check);
	}

	@Override
	public List<String> checkedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base.checkedProperties(enhancer.getManagedType(root));
	}

	@Override
	public void swap(final Class<?> root, final String property1, final String property2) {
	    // inject an enhanced type into method implementation
	    base.swap(enhancer.getManagedType(root), property1, property2);
	}

	@Override
	public void move(final Class<?> root, final String what, final String beforeWhat) {
	    // inject an enhanced type into method implementation
	    base.move(enhancer.getManagedType(root), what, beforeWhat);
	}

	@Override
	public void moveToTheEnd(final Class<?> root, final String what) {
	    // inject an enhanced type into method implementation
	    base.moveToTheEnd(enhancer.getManagedType(root), what);
	}

	protected IDomainTreeEnhancer enhancer() {
	    return enhancer;
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
	private final IDomainTreeRepresentation base;
	private final ITickRepresentation firstTick;
	private final ITickRepresentation secondTick;

	/**
	 * A <i>representation with enhancer</i> constructor.
	 */
	protected DomainTreeRepresentationAndEnhancer(final IDomainTreeRepresentation base) {
	    this.base = base;

	    firstTick = createFirstTick(base.getFirstTick());
	    secondTick = createSecondTick(base.getSecondTick());
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
		return base.isDisabledImmutably(enhancer.getManagedType(root), property);
	    }

	    @Override
	    public void disableImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		base.disableImmutably(enhancer.getManagedType(root), property);
	    }

	    @Override
	    public boolean isCheckedImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base.isCheckedImmutably(enhancer.getManagedType(root), property);
	    }

	    @Override
	    public void checkImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		base.checkImmutably(enhancer.getManagedType(root), property);
	    }
	}

	@Override
	public Set<Function> availableFunctions(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base.availableFunctions(enhancer.getManagedType(root), property);
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
	    return base.isExcludedImmutably(enhancer.getManagedType(root), property);
	}

	@Override
	public void excludeImmutably(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base.excludeImmutably(enhancer.getManagedType(root), property);
	}

	@Override
	public List<String> includedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base.includedProperties(enhancer.getManagedType(root));
	}

	@Override
	public boolean addStructureChangedListener(final IStructureChangedListener listener) {
	    return base.addStructureChangedListener(listener);
	}

	@Override
	public boolean removeStructureChangedListener(final IStructureChangedListener listener) {
	    return base.removeStructureChangedListener(listener);
	}

	@Override
	public void warmUp(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base.warmUp(enhancer.getManagedType(root), property);
	}

	@Override
	public Set<Class<?>> rootTypes() {
	    return base.rootTypes();
	}

	protected IDomainTreeEnhancer enhancer() {
	    return enhancer;
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
