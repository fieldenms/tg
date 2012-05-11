package ua.com.fielden.platform.example.dynamiccriteria;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTree;
import ua.com.fielden.platform.domaintree.testing.EntityWithoutKeyType;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleNestedEntity;
import ua.com.fielden.platform.serialisation.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.google.inject.Inject;

public class EntityCentreExampleSerialisationClassProvider extends DefaultSerialisationClassProvider {

    @Inject
    public EntityCentreExampleSerialisationClassProvider(final IApplicationSettings settings) throws Exception {
	super(settings);
	types.addAll(TgKryo.typesForRegistration("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.domaintree", AbstractDomainTree.DOMAIN_TREE_TYPES));
	types.remove(EntityWithoutKeyType.class);
	types.add(SimpleECEEntity.class);
	types.add(SimpleNestedEntity.class);
	types.add(SimpleCompositeEntity.class);
    }

}
