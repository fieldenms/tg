package ua.com.fielden.platform.domaintree.centre.impl;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.ILocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager.IAbstractAnalysisDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToCriteriaTickManager;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager.AddToResultTickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManager.TickManager;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.AbstractTickRepresentation;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.serialisation.impl.serialisers.TgSimpleSerializer;
import ua.com.fielden.platform.utils.Pair;
import ua.com.fielden.snappy.DateRangePrefixEnum;
import ua.com.fielden.snappy.MnemonicEnum;

/**
 * Criteria (entity-centre) domain tree manager with "power" of managing domain with calculated properties. The calculated properties can be managed exactly as simple properties.<br>
 *
 * @author TG Team
 *
 */
public class CentreDomainTreeManagerAndEnhancer extends AbstractDomainTreeManagerAndEnhancer implements ICentreDomainTreeManagerAndEnhancer {
    private static final long serialVersionUID = 8558129093648869501L;

    /**
     * A <i>manager with enhancer</i> constructor for the first time instantiation.
     */
    public CentreDomainTreeManagerAndEnhancer(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(new CentreDomainTreeManager(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)), new DomainTreeEnhancer(serialiser, AbstractDomainTree.validateRootTypes(rootTypes)));
    }

    /**
     * A <i>manager with enhancer</i> constructor.
     */
    protected CentreDomainTreeManagerAndEnhancer(final CentreDomainTreeManager base, final IDomainTreeEnhancer enhancer) {
	super(base, enhancer);
    }

    @Override
    protected AddToCriteriaTickManagerAndEnhancer createFirstTick(final TickManager base) {
	return new AddToCriteriaTickManagerAndEnhancer(base);
    }

    @Override
    protected AddToResultTickManagerAndEnhancer createSecondTick(final TickManager base) {
	return new AddToResultTickManagerAndEnhancer(base);
    }

    @Override
    protected DomainTreeRepresentationAndEnhancer createRepresentation(final AbstractDomainTreeRepresentation base) {
	return new CentreDomainTreeRepresentationAndEnhancer(base);
    }

    @Override
    protected ICentreDomainTreeManager base() {
	return (ICentreDomainTreeManager) super.base();
    }

    @Override
    public IAddToCriteriaTickManager getFirstTick() {
	return (IAddToCriteriaTickManager) super.getFirstTick();
    }

    @Override
    public IAddToResultTickManager getSecondTick() {
	return (IAddToResultTickManager) super.getSecondTick();
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    /* TODO reduce visibility */
    public class AddToCriteriaTickManagerAndEnhancer extends TickManagerAndEnhancer implements IAddToCriteriaTickManager {

	private AddToCriteriaTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	public /* TODO reduce visibility */ AddToCriteriaTickManager base() {
	    return (AddToCriteriaTickManager) super.base();
	}

	@Override
	public ILocatorDomainTreeManagerAndEnhancer produceLocatorManagerByDefault(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().produceLocatorManagerByDefault(enhancer().getManagedType(root), property);
	}

	@Override
	public void initLocatorManagerByDefault(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().initLocatorManagerByDefault(enhancer().getManagedType(root), property);
	}

	@Override
	public void resetLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().resetLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public void discardLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().discardLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public void acceptLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().acceptLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public void saveLocatorManagerGlobally(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().saveLocatorManagerGlobally(enhancer().getManagedType(root), property);
	}

	@Override
	public ILocatorDomainTreeManagerAndEnhancer getLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public void freezeLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().freezeLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean isChangedLocatorManager(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().isChangedLocatorManager(enhancer().getManagedType(root), property);
	}

	@Override
	public List<Pair<Class<?>, String>> locatorKeys() {
	    // inject an enhanced type into method implementation
	    return base().locatorKeys();
	}

	@Override
	public int getColumnsNumber() {
	    // inject an enhanced type into method implementation
	    return base().getColumnsNumber();
	}

	@Override
	public IAddToCriteriaTickManager setColumnsNumber(final int columnsNumber) {
	    // inject an enhanced type into method implementation
	    base().setColumnsNumber(columnsNumber);
	    return this;
	}

	@Override
	public Object getValue(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getValue(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean isValueEmpty(final Class<?> root, final String property) {
	    return base().isValueEmpty(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setValue(final Class<?> root, final String property, final Object value) {
	    // inject an enhanced type into method implementation
	    base().setValue(enhancer().getManagedType(root), property, value);
	    return this;
	}

	@Override
	public Object getValue2(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getValue2(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean is2ValueEmpty(final Class<?> root, final String property) {
	    return base().is2ValueEmpty(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setValue2(final Class<?> root, final String property, final Object value2) {
	    // inject an enhanced type into method implementation
	    base().setValue2(enhancer().getManagedType(root), property, value2);
	    return this;
	}

	@Override
	public boolean addPropertyValueListener(final IPropertyValueListener listener) {
	    return base().addPropertyValueListener(listener);
	}

	@Override
	public boolean removePropertyValueListener(final IPropertyValueListener listener) {
	    return base().removePropertyValueListener(listener);
	}

	@Override
	public boolean addPropertyValue2Listener(final IPropertyValueListener listener) {
	    return base().addPropertyValue2Listener(listener);
	}

	@Override
	public boolean removePropertyValue2Listener(final IPropertyValueListener listener) {
	    return base().removePropertyValue2Listener(listener);
	}


	@Override
	public Boolean getExclusive(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getExclusive(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setExclusive(final Class<?> root, final String property, final Boolean exclusive) {
	    // inject an enhanced type into method implementation
	    base().setExclusive(enhancer().getManagedType(root), property, exclusive);
	    return this;
	}

	@Override
	public Boolean getExclusive2(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getExclusive2(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setExclusive2(final Class<?> root, final String property, final Boolean exclusive2) {
	    // inject an enhanced type into method implementation
	    base().setExclusive2(enhancer().getManagedType(root), property, exclusive2);
	    return this;
	}

	@Override
	public DateRangePrefixEnum getDatePrefix(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getDatePrefix(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setDatePrefix(final Class<?> root, final String property, final DateRangePrefixEnum datePrefix) {
	    // inject an enhanced type into method implementation
	    base().setDatePrefix(enhancer().getManagedType(root), property, datePrefix);
	    return this;
	}

	@Override
	public MnemonicEnum getDateMnemonic(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getDateMnemonic(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setDateMnemonic(final Class<?> root, final String property, final MnemonicEnum dateMnemonic) {
	    // inject an enhanced type into method implementation
	    base().setDateMnemonic(enhancer().getManagedType(root), property, dateMnemonic);
	    return this;
	}

	@Override
	public Boolean getAndBefore(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getAndBefore(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setAndBefore(final Class<?> root, final String property, final Boolean andBefore) {
	    // inject an enhanced type into method implementation
	    base().setAndBefore(enhancer().getManagedType(root), property, andBefore);
	    return this;
	}

	@Override
	public Boolean getOrNull(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getOrNull(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setOrNull(final Class<?> root, final String property, final Boolean orNull) {
	    // inject an enhanced type into method implementation
	    base().setOrNull(enhancer().getManagedType(root), property, orNull);
	    return this;
	}

	@Override
	public Boolean getNot(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getNot(enhancer().getManagedType(root), property);
	}

	@Override
	public IAddToCriteriaTickManager setNot(final Class<?> root, final String property, final Boolean not) {
	    // inject an enhanced type into method implementation
	    base().setNot(enhancer().getManagedType(root), property, not);
	    return this;
	}

	@Override
	public Set<Class<?>> rootTypes() {
	    return base().rootTypes();
	}

    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected class AddToResultTickManagerAndEnhancer extends TickManagerAndEnhancer implements IAddToResultTickManager {
	private static final long serialVersionUID = 192045576594016450L;

	private AddToResultTickManagerAndEnhancer(final TickManager base) {
	    super(base);
	}

	@Override
	protected AddToResultTickManager base() {
	    return (AddToResultTickManager) super.base();
	}

	@Override
	public List<Pair<String, Ordering>> orderedProperties(final Class<?> root) {
	    // inject an enhanced type into method implementation
	    return base().orderedProperties(enhancer().getManagedType(root));
	}

	@Override
	public void toggleOrdering(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    base().toggleOrdering(enhancer().getManagedType(root), property);
	}

	@Override
	public boolean addPropertyOrderingListener(final IPropertyOrderingListener listener) {
	    // TODO Auto-generated method stub
	    throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
	}

	@Override
	public boolean removePropertyOrderingListener(final IPropertyOrderingListener listener) {
	    // TODO Auto-generated method stub
	    throw new UnsupportedOperationException("IPropertyOrderingListener is currently unsupported for CentreDomainTreeManager's second tick.");
	}

	@Override
	public int getWidth(final Class<?> root, final String property) {
	    // inject an enhanced type into method implementation
	    return base().getWidth(enhancer().getManagedType(root), property);
	}

	@Override
	public void setWidth(final Class<?> root, final String property, final int width) {
	    // inject an enhanced type into method implementation
	    base().setWidth(enhancer().getManagedType(root), property, width);
	}
    }

    @Override
    public ICentreDomainTreeRepresentation getRepresentation() {
	return (ICentreDomainTreeRepresentation) super.getRepresentation();
    }

    /**
     * Overridden to take into account calculated properties.
     *
     * @author TG Team
     *
     */
    protected class CentreDomainTreeRepresentationAndEnhancer extends DomainTreeRepresentationAndEnhancer implements ICentreDomainTreeRepresentation {
	private static final long serialVersionUID = -5345869657944629725L;

	protected CentreDomainTreeRepresentationAndEnhancer(final AbstractDomainTreeRepresentation base) {
	    super(base);
	}

	@Override
	protected IAddToCriteriaTickRepresentation createFirstTick(final AbstractTickRepresentation base) {
	    return new AddToCriteriaTickRepresentationAndEnhancer(base);
	}

	@Override
	protected IAddToResultTickRepresentation createSecondTick(final AbstractTickRepresentation base) {
	    return new AddToResultTickRepresentationAndEnhancer(base);
	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	protected class AddToCriteriaTickRepresentationAndEnhancer extends TickRepresentationAndEnhancer implements IAddToCriteriaTickRepresentation {

	    protected AddToCriteriaTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAddToCriteriaTickRepresentation base() {
		return (IAddToCriteriaTickRepresentation) super.base();
	    }

	    @Override
	    public Object getValueByDefault(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().getValueByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public Object getEmptyValueFor(final Class<?> root, final String property) {
		return base().getEmptyValueFor(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public IAddToCriteriaTickRepresentation setValueByDefault(final Class<?> root, final String property, final Object value) {
		// inject an enhanced type into method implementation
		base().setValueByDefault(enhancer().getManagedType(root), property, value);
		return this;
	    }

	    @Override
	    public Object getValue2ByDefault(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().getValue2ByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public Object get2EmptyValueFor(final Class<?> root, final String property) {
		return base().get2EmptyValueFor(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public IAddToCriteriaTickRepresentation setValue2ByDefault(final Class<?> root, final String property, final Object value2) {
		// inject an enhanced type into method implementation
		base().setValue2ByDefault(enhancer().getManagedType(root), property, value2);
		return this;
	    }

	}

	/**
	 * Overridden to take into account calculated properties.
	 *
	 * @author TG Team
	 *
	 */
	protected class AddToResultTickRepresentationAndEnhancer extends TickRepresentationAndEnhancer implements IAddToResultTickRepresentation {
	    private static final long serialVersionUID = -6145540404981386675L;

	    protected AddToResultTickRepresentationAndEnhancer(final AbstractTickRepresentation base) {
		super(base);
	    }

	    @Override
	    protected IAddToResultTickRepresentation base() {
		return (IAddToResultTickRepresentation) super.base();
	    }

	    @Override
	    public boolean isOrderingDisabledImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().isOrderingDisabledImmutably(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public void disableOrderingImmutably(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		base().disableOrderingImmutably(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public List<Pair<String, Ordering>> orderedPropertiesByDefault(final Class<?> root) {
		// inject an enhanced type into method implementation
		return base().orderedPropertiesByDefault(enhancer().getManagedType(root));
	    }

	    @Override
	    public void setOrderedPropertiesByDefault(final Class<?> root, final List<Pair<String, Ordering>> orderedPropertiesByDefault) {
		// inject an enhanced type into method implementation
		base().setOrderedPropertiesByDefault(enhancer().getManagedType(root), orderedPropertiesByDefault);
	    }

	    @Override
	    public int getWidthByDefault(final Class<?> root, final String property) {
		// inject an enhanced type into method implementation
		return base().getWidthByDefault(enhancer().getManagedType(root), property);
	    }

	    @Override
	    public void setWidthByDefault(final Class<?> root, final String property, final int width) {
		// inject an enhanced type into method implementation
		base().setWidthByDefault(enhancer().getManagedType(root), property, width);
	    }
	}

	@Override
	public IAddToCriteriaTickRepresentation getFirstTick() {
	    return (IAddToCriteriaTickRepresentation) super.getFirstTick();
	}

	@Override
	public IAddToResultTickRepresentation getSecondTick() {
	    return (IAddToResultTickRepresentation) super.getSecondTick();
	}
    }

    @Override
    public boolean isRunAutomatically() {
	return base().isRunAutomatically();
    }

    @Override
    public ICentreDomainTreeManager setRunAutomatically(final boolean runAutomatically) {
	base().setRunAutomatically(runAutomatically);
	return this;
    }

    @Override
    public void initAnalysisManagerByDefault(final String name, final AnalysisType analysisType) {
	base().initAnalysisManagerByDefault(name, analysisType);
    }

    @Override
    public void discardAnalysisManager(final String name) {
	base().discardAnalysisManager(name);
    }

    @Override
    public void acceptAnalysisManager(final String name) {
	base().acceptAnalysisManager(name);
    }

    @Override
    public boolean isChangedAnalysisManager(final String name) {
	return base().isChangedAnalysisManager(name);
    }

    @Override
    public void removeAnalysisManager(final String name) {
	base().removeAnalysisManager(name);
    }

    @Override
    public boolean addAnalysisListener(final IAnalysisListener listener) {
	return base().addAnalysisListener(listener);
    }

    @Override
    public boolean removeAnalysisListener(final IAnalysisListener listener) {
	return base().removeAnalysisListener(listener);
    }

    @Override
    public IAbstractAnalysisDomainTreeManagerAndEnhancer getAnalysisManager(final String name) {
	return base().getAnalysisManager(name);
    }

    @Override
    public void freezeAnalysisManager(final String name) {
	base().freezeAnalysisManager(name);
    }

    @Override
    public List<String> analysisKeys() {
	return base().analysisKeys();
    }

    /**
     * A specific Kryo serialiser for {@link CentreDomainTreeManagerAndEnhancer}.
     *
     * @author TG Team
     *
     */
    public static class CentreDomainTreeManagerAndEnhancerSerialiser extends TgSimpleSerializer<CentreDomainTreeManagerAndEnhancer> {
	public CentreDomainTreeManagerAndEnhancerSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public CentreDomainTreeManagerAndEnhancer read(final ByteBuffer buffer) {
	    final CentreDomainTreeManager base = readValue(buffer, CentreDomainTreeManager.class);
	    final DomainTreeEnhancer enhancer = readValue(buffer, DomainTreeEnhancer.class);
	    return new CentreDomainTreeManagerAndEnhancer(base, enhancer);
	}

	@Override
	public void write(final ByteBuffer buffer, final CentreDomainTreeManagerAndEnhancer manager) {
	    writeValue(buffer, manager.base());
	    writeValue(buffer, manager.enhancer());
	}
    }
}
