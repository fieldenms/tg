package ua.com.fielden.platform.treemodel.rules.criteria.impl;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.criteria.ILocatorDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;
import ua.com.fielden.platform.utils.Pair;

/**
 * A domain tree representation for locators specific. A first tick means "include to locator criteria", second -- "include to locator result-set".<br>
 *
 * @author TG Team
 *
 */
public class LocatorDomainTreeRepresentation extends CriteriaDomainTreeRepresentation implements ILocatorDomainTreeRepresentation {
    private static final long serialVersionUID = -2470587042269751368L;

    /**
     * A <i>representation</i> constructor for the first time instantiation. Initialises also children references on itself.
     */
    public LocatorDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, AbstractDomainTree.createSet(), new AddToCriteriaTick(), new AddToResultSetTick());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected LocatorDomainTreeRepresentation(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final IAddToCriteriaTickRepresentation firstTick, final IAddToResultTickRepresentation secondTick) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
    }

    /**
     * A specific Kryo serialiser for {@link LocatorDomainTreeRepresentation}.
     *
     * @author TG Team
     *
     */
    public static class LocatorDomainTreeRepresentationSerialiser extends AbstractDomainTreeRepresentationSerialiser<LocatorDomainTreeRepresentation> {
	public LocatorDomainTreeRepresentationSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public LocatorDomainTreeRepresentation read(final ByteBuffer buffer) {
	    final Set<Class<?>> rootTypes = readValue(buffer, HashSet.class);
	    final Set<Pair<Class<?>, String>> excludedProperties = readValue(buffer, HashSet.class);
	    final AddToCriteriaTick firstTick = readValue(buffer, AddToCriteriaTick.class);
	    final AddToResultSetTick secondTick = readValue(buffer, AddToResultSetTick.class);
	    return new LocatorDomainTreeRepresentation(kryo(), rootTypes, excludedProperties, firstTick, secondTick);
	}
    }
}
