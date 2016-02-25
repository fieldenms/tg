package ua.com.fielden.platform.utils;

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
    public void reconstruction_of_fetch_model_without_sub_models_should_succeed() {
        final TgDefinersExecutorCompositeKeyMember grandParent = factory.newEntity(TgDefinersExecutorCompositeKeyMember.class, 1L);
        grandParent.beginInitialising();
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
    }
    
}
