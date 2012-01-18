package ua.com.fielden.platform.domaintree.testing;

import java.nio.ByteBuffer;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeManager.ChangedAction;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.IPropertyStructureChangedListener;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.utils.Pair;

public class DomainTreeRepresentation1 extends AbstractDomainTreeRepresentation {
    private static final long serialVersionUID = -8746883123699403533L;

    /**
     * A <i>representation</i> constructor for the first time instantiation.
     *
     * @param serialiser
     */
    public DomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes) {
	this(serialiser, rootTypes, AbstractDomainTree.createSet(), new TickRepresentationForTest(), new TickRepresentationForTest(), AbstractDomainTree.<ListenedArrayList>createRootsMap());
    }

    /**
     * A <i>representation</i> constructor. Initialises also children references on itself.
     */
    protected DomainTreeRepresentation1(final ISerialiser serialiser, final Set<Class<?>> rootTypes, final Set<Pair<Class<?>, String>> excludedProperties, final ITickRepresentation firstTick, final ITickRepresentation secondTick, final EnhancementRootsMap<ListenedArrayList> includedProperties) {
	super(serialiser, rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
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

	@Override
	public final void disableImmutably(final Class<?> root, final String property) {
	    super.disableImmutably(root, property);

	    fireDisablingEvent(root, property);
	}

	@Override
	public final void checkImmutably(final Class<?> root, final String property) {
	    super.checkImmutably(root, property);

	    fireDisablingEvent(root, property);
	}

	private void fireDisablingEvent(final Class<?> root, final String property) {
	    // fire DISABLED event after successful "disabled" action
	    for (final IPropertyStructureChangedListener listener : getDtr().dtm().listeners()) {
		listener.propertyStructureChanged(root, property, ChangedAction.DISABLED_FIRST_TICK); // TODO first? second?
	    }
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
	    final EnhancementLinkedRootsSet rootTypes = readValue(buffer, EnhancementLinkedRootsSet.class);
	    final EnhancementSet excludedProperties = readValue(buffer, EnhancementSet.class);
	    final TickRepresentationForTest firstTick = readValue(buffer, TickRepresentationForTest.class);
	    final TickRepresentationForTest secondTick = readValue(buffer, TickRepresentationForTest.class);
	    final EnhancementRootsMap<ListenedArrayList> includedProperties = readValue(buffer, EnhancementRootsMap.class);
	    return new DomainTreeRepresentation1(kryo(), rootTypes, excludedProperties, firstTick, secondTick, includedProperties);
	}
    }
}
