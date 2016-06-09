package ua.com.fielden.platform.utils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Test;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgDefinersExecutorCollectionalChild;
import ua.com.fielden.platform.sample.domain.TgDefinersExecutorCompositeKeyMember;
import ua.com.fielden.platform.sample.domain.TgDefinersExecutorParent;
import ua.com.fielden.platform.test.CommonTestEntityModuleWithPropertyFactory;
import ua.com.fielden.platform.test.EntityModuleWithPropertyFactory;
import static ua.com.fielden.platform.utils.Pair.pair;
import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link DefinersExecutor}.
 * 
 * @author TG Team
 *
 */
public class DefinersExecutorTest {
    private final EntityModuleWithPropertyFactory module = new CommonTestEntityModuleWithPropertyFactory();
    private final Injector injector = new ApplicationInjectorFactory().add(module).getInjector();
    private final EntityFactory factory = injector.getInstance(EntityFactory.class);
    
    @Test
    public void definers_execute_firstly_for_key_member_entity_of_root_and_then_for_root_properties_in_order_of_their_definition() {
        final TgDefinersExecutorCompositeKeyMember grandParent = factory.newEntity(TgDefinersExecutorCompositeKeyMember.class, 1L);
        grandParent.beginInitialising();
        grandParent.setKey("grand1");
        grandParent.setPropWithHandler("PropWithHandler value");
        
        final TgDefinersExecutorParent parent = factory.newEntity(TgDefinersExecutorParent.class, 1L);
        parent.beginInitialising();
        parent.setKeyMember1(grandParent);
        parent.setKeyMember2("parent1");
        
        DefinersExecutor.execute(parent);
        
        System.out.println(grandParent.getHandledProperties());
        assertEquals(
                    Arrays.asList(
                        pair("", "propWithHandler"),
                        pair("keyMember1", "collectionWithHandler"),
                        pair("keyMember1", "propWithHandler")
                    ),
                    grandParent.getHandledProperties()
                );
    }
    

    @Test
    public void definers_execute_firstly_for_key_member_entity_of_root_and_then_for_collection_items_of_collection_property_and_then_for_collection_itself_and_for_next_property_defined_after_collection() {
        final TgDefinersExecutorCompositeKeyMember grandParent = factory.newEntity(TgDefinersExecutorCompositeKeyMember.class, 1L);
        grandParent.beginInitialising();
        grandParent.setKey("grand1");
        grandParent.setPropWithHandler("PropWithHandler value");
        
        final TgDefinersExecutorParent parent = factory.newEntity(TgDefinersExecutorParent.class, 1L);
        parent.beginInitialising();
        parent.setKeyMember1(grandParent);
        parent.setKeyMember2("parent1");

        final TgDefinersExecutorCollectionalChild child1 = factory.newEntity(TgDefinersExecutorCollectionalChild.class, 1L);
        child1.beginInitialising();
        child1.setMember1(parent);
        child1.setMember2("1");
        final TgDefinersExecutorCollectionalChild child2 = factory.newEntity(TgDefinersExecutorCollectionalChild.class, 1L);
        child2.beginInitialising();
        child2.setMember1(parent);
        child2.setMember2("2");
        
        final Set<TgDefinersExecutorCollectionalChild> collectionWithHandler = new LinkedHashSet<>();
        collectionWithHandler.add(child1);
        collectionWithHandler.add(child2);
        parent.setCollectionWithHandler(collectionWithHandler);
        
        DefinersExecutor.execute(parent);
        
        System.out.println(grandParent.getHandledProperties());
        assertEquals(
                    Arrays.asList(
                        pair("", "propWithHandler"),
                        pair("member1.keyMember1", "member2[grand1 parent1 1]"),
                        pair("member1.keyMember1", "member2[grand1 parent1 2]"),
                        pair("keyMember1", "collectionWithHandler"),
                        pair("keyMember1", "propWithHandler")
                    ),
                    grandParent.getHandledProperties()
                );
    }
    
    @Test
    public void definers_execute_firstly_for_key_member_entity_of_root_and_then_no_definers_execute_if_root_is_not_instrumented() {
        final TgDefinersExecutorCompositeKeyMember grandParent = factory.newEntity(TgDefinersExecutorCompositeKeyMember.class, 1L);
        grandParent.beginInitialising();
        grandParent.setKey("grand1");
        grandParent.setPropWithHandler("PropWithHandler value");
        
        final TgDefinersExecutorParent parent = factory.newPlainEntity(TgDefinersExecutorParent.class, 1L);
        parent.beginInitialising();
        parent.setKeyMember1(grandParent);
        parent.setKeyMember2("parent1");
        
        DefinersExecutor.execute(parent);
        
        System.out.println(grandParent.getHandledProperties());
        assertEquals(
                    Arrays.asList(
                        pair("", "propWithHandler")
                    ),
                    grandParent.getHandledProperties()
                );
    }
    
    @Test
    public void definers_execute_for_root_properties_in_order_of_their_definition_and_no_composite_member_definers_execute_if_it_is_not_instrumented() {
        final TgDefinersExecutorCompositeKeyMember grandParent = factory.newPlainEntity(TgDefinersExecutorCompositeKeyMember.class, 1L);
        grandParent.beginInitialising();
        grandParent.setKey("grand1");
        grandParent.setPropWithHandler("PropWithHandler value");
        
        final TgDefinersExecutorParent parent = factory.newEntity(TgDefinersExecutorParent.class, 1L);
        parent.beginInitialising();
        parent.setKeyMember1(grandParent);
        parent.setKeyMember2("parent1");
        
        DefinersExecutor.execute(parent);
        
        System.out.println(grandParent.getHandledProperties());
        assertEquals(
                    Arrays.asList(
                        pair("keyMember1", "collectionWithHandler"),
                        pair("keyMember1", "propWithHandler")
                    ),
                    grandParent.getHandledProperties()
                );
    }
}
