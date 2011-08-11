package ua.com.fielden.platform.domain.tree;

import java.util.List;

import ua.com.fielden.platform.domain.tree.DomainTreeManager1.DomainTreeManagerForTestSerialiser;
import ua.com.fielden.platform.domain.tree.DomainTreeManagerAndEnhancer1.DomainTreeManagerAndEnhancerForTestSerialiser;
import ua.com.fielden.platform.domain.tree.DomainTreeRepresentation1.DomainTreeRepresentationForTestSerialiser;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.serialisation.impl.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;

import com.esotericsoftware.kryo.Serializer;

public class TgKryo1 extends TgKryo {

    public TgKryo1(final EntityFactory factory, final ISerialisationClassProvider provider) {
	super(factory, provider);
    }

    @Override
    protected void registerDomainTreeTypes() {
        super.registerDomainTreeTypes();

	final List<Class<?>> types = TgKryo.typesForRegistration("../platform-pojo-bl/target/test-classes", "ua.com.fielden.platform.treemodel.rules", AbstractDomainTree.DOMAIN_TREE_TYPES);
	types.addAll(TgKryo.typesForRegistration("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.domain.tree", AbstractDomainTree.DOMAIN_TREE_TYPES));
	types.remove(EntityWithoutKeyType.class);

	for (final Class<?> type : types) {
	    register(type);
	}
    }

    @Override
    public Serializer newSerializer(final Class type) {
	if (DomainTreeManager1.class.isAssignableFrom(type)) {
	    return new DomainTreeManagerForTestSerialiser(this);
	} else if (DomainTreeManagerAndEnhancer1.class.isAssignableFrom(type)) {
	    return new DomainTreeManagerAndEnhancerForTestSerialiser(this);
	} else if (DomainTreeRepresentation1.class.isAssignableFrom(type)) {
	    return new DomainTreeRepresentationForTestSerialiser(this);
	} else {
	    return super.newSerializer(type);
	}
    }

}
