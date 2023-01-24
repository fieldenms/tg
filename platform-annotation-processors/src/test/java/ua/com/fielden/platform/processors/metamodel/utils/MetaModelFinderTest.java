package ua.com.fielden.platform.processors.metamodel.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Optional;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;

import ua.com.fielden.platform.processors.metamodel.MetaModelProcessor;
import ua.com.fielden.platform.processors.metamodel.elements.EntityElement;
import ua.com.fielden.platform.processors.test_entities.MetamodeledEntity;
import ua.com.fielden.platform.processors.test_entities.meta.MetamodeledEntityMetaModel;
import ua.com.fielden.platform.processors.test_entities.meta.MetamodeledEntityMetaModelAliased;
import ua.com.fielden.platform.processors.test_utils.ProcessingRule;

/**
 * A test case for utilities provided by {@link MetaModelFinder}.
 * <p>
 * Meta-models for test entities have to be generated prior to running these tests in order for them to be recognised by the compiler.
 * One implication of this approach is that any modifications to the processing logic should be repackaged followed by the recompilation of 
 * test entities to regenerate their meta-models.
 *
 * @author TG Team
 */
public class MetaModelFinderTest {

    @ClassRule
    public static ProcessingRule rule = new ProcessingRule(List.of(), new MetaModelProcessor());
    private static ElementFinder finder;
    private static EntityFinder entityFinder;
    private static MetaModelFinder metaModelFinder;
    
    @BeforeClass
    public static void setupOnce() {
        final Elements elements = rule.getElements();
        final Types types = rule.getTypes();
        finder = new ElementFinder(elements, types);
        entityFinder = new EntityFinder(elements, types);
        metaModelFinder = new MetaModelFinder(elements, types);
    }

    @Test
    public void isMetaModel_returns_true_if_the_type_element_represents_a_meta_model() {
        assertFalse(metaModelFinder.isMetaModel(finder.getTypeElement(String.class)));
        assertFalse(metaModelFinder.isMetaModel(finder.getTypeElement(MetamodeledEntity.class)));
        assertTrue(metaModelFinder.isMetaModel(finder.getTypeElement(MetamodeledEntityMetaModel.class)));
        assertTrue(metaModelFinder.isMetaModel(finder.getTypeElement(MetamodeledEntityMetaModelAliased.class)));
    }

}
