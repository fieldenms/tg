package ua.com.fielden.platform.domain.tree;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.utils.Pair;

public class DomainTreeRepresentation1 extends AbstractDomainTreeRepresentation {
    private static final long serialVersionUID = -8746883123699403533L;

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
    protected DomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final ITickRepresentation firstTick, final ITickRepresentation secondTick) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick);
    }

    public static class TickRepresentationForTest extends AbstractTickRepresentation {
	private static final long serialVersionUID = -4112761380255058134L;

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
	public DomainTreeRepresentationForTestSerialiser(final TgKryo kryo) {
	    super(kryo);
	}

	@Override
	public DomainTreeRepresentation1 read(final ByteBuffer buffer) {
	    final Set<Class<?>> rootTypes = readValue(buffer, HashSet.class);
	    final Set<Pair<Class<?>, String>> excludedProperties = readValue(buffer, HashSet.class);
	    final TickRepresentationForTest firstTick = readValue(buffer, TickRepresentationForTest.class);
	    final TickRepresentationForTest secondTick = readValue(buffer, TickRepresentationForTest.class);
	    return new DomainTreeRepresentation1(kryo(), rootTypes, excludedProperties, firstTick, secondTick);
	}
    }
}
