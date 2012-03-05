package ua.com.fielden.platform.example.dynamiccriteria;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ua.com.fielden.platform.basic.config.IApplicationSettings;
import ua.com.fielden.platform.domaintree.Function;
import ua.com.fielden.platform.domaintree.ICalculatedProperty;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute;
import ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyCategory;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.IDomainTreeManager;
import ua.com.fielden.platform.domaintree.IDomainTreeManager.ITickManager;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation.ITickRepresentation;
import ua.com.fielden.platform.domaintree.centre.ILocatorDomainTreeManager.SearchBy;
import ua.com.fielden.platform.domaintree.centre.IOrderingRepresentation.Ordering;
import ua.com.fielden.platform.domaintree.impl.AbstractDomainTreeRepresentation.ListenedArrayList;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer.ByteArray;
import ua.com.fielden.platform.domaintree.impl.EnhancementLinkedRootsSet;
import ua.com.fielden.platform.domaintree.impl.EnhancementPropertiesMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementRootsMap;
import ua.com.fielden.platform.domaintree.impl.EnhancementSet;
import ua.com.fielden.platform.domaintree.master.IMasterDomainTreeManager;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleCompositeEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleECEEntity;
import ua.com.fielden.platform.example.dynamiccriteria.entities.SimpleNestedEntity;
import ua.com.fielden.platform.serialisation.impl.DefaultSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.impl.TgKryo;

import com.google.inject.Inject;

public class EntityCentreExampleSerialisationClassProvider extends DefaultSerialisationClassProvider {

    public static final List<Class<?>> DOMAIN_TREE_TYPES = new ArrayList<Class<?>>() {{
	add(SearchBy.class);
	add(ListenedArrayList.class);
	add(LinkedHashMap.class); //
	add(EnhancementSet.class); //
	add(EnhancementLinkedRootsSet.class); //
	add(EnhancementRootsMap.class); //
	add(EnhancementPropertiesMap.class); //
	add(ByteArray.class); //
	add(Ordering.class); //
	add(Function.class); //
	add(CalculatedPropertyCategory.class); //
	add(CalculatedPropertyAttribute.class); //
	add(ICalculatedProperty.class); //
	add(IMasterDomainTreeManager.class); //
	add(IDomainTreeEnhancer.class); //
	add(IDomainTreeRepresentation.class); //
	add(IDomainTreeManager.class); //
	add(ITickRepresentation.class); //
	add(ITickManager.class); //
    }};

    @Inject
    public EntityCentreExampleSerialisationClassProvider(final IApplicationSettings settings) throws Exception {
	super(settings);
	types.addAll(TgKryo.typesForRegistration("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.domaintree", DOMAIN_TREE_TYPES));
	types.add(SimpleECEEntity.class);
	types.add(SimpleNestedEntity.class);
	types.add(SimpleCompositeEntity.class);
    }

}
