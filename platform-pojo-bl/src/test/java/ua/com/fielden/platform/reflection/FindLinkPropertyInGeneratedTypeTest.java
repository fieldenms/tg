package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static ua.com.fielden.platform.domaintree.ICalculatedProperty.CalculatedPropertyAttribute.NO_ATTR;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;
import ua.com.fielden.platform.domaintree.IDomainTreeEnhancer;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.api.ProvidedSerialisationClassProvider;
import ua.com.fielden.platform.serialisation.kryo.TgKryo;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Test case for {@link Finder}'s functionality for finding <code>linkProperty</code> and determining association type in generated types.
 * 
 * @author TG Team
 * 
 */
public class FindLinkPropertyInGeneratedTypeTest {

    private final Module module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);

    private final ISerialiser serialiser = new TgKryo(factory, new ProvidedSerialisationClassProvider(new Class[] { MasterEntityWithOneToOneAssociation.class,
            MasterEntityWithOneToManyAssociation.class }));
    private final Set<Class<?>> rootTypes = new HashSet<Class<?>>() {
        {
            add(MasterEntityWithOneToOneAssociation.class);
            add(MasterEntityWithOneToManyAssociation.class);
        }
    };

    private IDomainTreeEnhancer dtm;
    private Class<? extends AbstractEntity<?>> typeWithOne2One;
    private Class<? extends AbstractEntity<?>> typeWithOne2Many;
    private Class<? extends AbstractEntity<?>> typeWithEnhancedOne2One;
    private Class<? extends AbstractEntity<?>> typeWithEnhancedOne2Many;

    @Before
    public void setUp() {
        dtm = new DomainTreeEnhancer(serialiser, rootTypes);

        // calc4One2One
        dtm.addCalculatedProperty(MasterEntityWithOneToOneAssociation.class, "", "2 * intProp", "Calculated property", "desc", NO_ATTR, "intProp");
        dtm.apply();
        typeWithOne2One = (Class<? extends AbstractEntity<?>>) dtm.getManagedType(MasterEntityWithOneToOneAssociation.class);

        // calc4One2Many
        dtm.addCalculatedProperty(MasterEntityWithOneToManyAssociation.class, "", "2 * moneyProp", "Calculated property", "desc", NO_ATTR, "moneyProp");
        dtm.apply();
        typeWithOne2Many = (Class<? extends AbstractEntity<?>>) dtm.getManagedType(MasterEntityWithOneToManyAssociation.class);

        // calc4EnhancedOne2One
        dtm.addCalculatedProperty(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation", "2 * intProp", "Calculated property", "desc", NO_ATTR, "intProp");
        dtm.apply();
        typeWithEnhancedOne2One = (Class<? extends AbstractEntity<?>>) dtm.getManagedType(MasterEntityWithOneToOneAssociation.class);

        // calc4EnhancedOne2Many
        dtm.addCalculatedProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional", "2 * intProp", "Calculated property", "desc", NO_ATTR, "intProp");
        dtm.apply();
        typeWithEnhancedOne2Many = (Class<? extends AbstractEntity<?>>) dtm.getManagedType(MasterEntityWithOneToManyAssociation.class);
    }

    @Test
    public void should_have_found_link_property_in_one_to_one_association() {
        assertEquals("key", Finder.findLinkProperty(typeWithOne2One, "one2oneAssociation"));
        assertTrue(Finder.isOne2Many_or_One2One_association(typeWithOne2One, "one2oneAssociation"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_association() {
        assertEquals("key1", Finder.findLinkProperty(typeWithOne2Many, "one2manyAssociationCollectional"));
        assertTrue(Finder.isOne2Many_or_One2One_association(typeWithOne2Many, "one2manyAssociationCollectional"));
    }

    @Test
    public void should_have_found_link_property_in_enhanced_one_to_one_association() {
        assertEquals("key", Finder.findLinkProperty(typeWithEnhancedOne2One, "one2oneAssociation"));
        assertTrue(Finder.isOne2Many_or_One2One_association(typeWithEnhancedOne2One, "one2oneAssociation"));
    }

    @Test
    public void should_have_found_link_property_in_enhanced_one_to_many_association() {
        assertEquals("key1", Finder.findLinkProperty(typeWithEnhancedOne2Many, "one2manyAssociationCollectional"));
        assertTrue(Finder.isOne2Many_or_One2One_association(typeWithEnhancedOne2Many, "one2manyAssociationCollectional"));
    }

}
