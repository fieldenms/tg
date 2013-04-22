package ua.com.fielden.platform.domaintree.testing;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.utils.Pair;

public class DomainTreeRepresentation1 extends AbstractDomainTreeRepresentation {
    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     */
    public DomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, AbstractDomainTree.createSet(), new TickRepresentationForTest(), new TickRepresentationForTest());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected DomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final AbstractTickRepresentation firstTick, final AbstractTickRepresentation secondTick) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
    }

    public static class TickRepresentationForTest extends AbstractTickRepresentation {
	/**
	 * Used for serialisation and for normal initialisation. IMPORTANT : To use this tick it should be passed into representation constructor, which should initialise "dtr"
	 * field.
	 */
	public TickRepresentationForTest() {
	    super();
	}
    }

    /**
     * A specific Kryo serialiser for {@link DomainTreeRepresentation1}.
     *
     * @author TG Team
     *
     */
    public static class DomainTreeRepresentationForTestSerialiser extends AbstractDomainTreeRepresentationSerialiser<DomainTreeRepresentation1> {
	public DomainTreeRepresentationForTestSerialiser(final ISerialiser kryo) {
	    super(kryo);
	}

	@Override
	public DomainTreeRepresentation1 read(final ByteBuffer buffer) {
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final TickRepresentationForTest firstTick = readValue(buffer, TickRepresentationForTest.class);
	    final TickRepresentationForTest secondTick = readValue(buffer, TickRepresentationForTest.class);
	    return new DomainTreeRepresentation1(kryo(), rootTypes, excludedProperties, firstTick, secondTick);
	}
    }
}
