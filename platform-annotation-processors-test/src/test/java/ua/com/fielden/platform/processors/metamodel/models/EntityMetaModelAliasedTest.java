package ua.com.fielden.platform.processors.metamodel.models;

import metamodels.MetaModels;
import org.junit.Test;
import ua.com.fielden.platform.processors.metamodel.exceptions.EntityMetaModelAliasedException;
import ua.com.fielden.platform.processors.test_entities.meta.EntityWithEntityTypedAndOrdinaryPropsMetaModel;
import ua.com.fielden.platform.processors.test_entities.meta.ExampleEntityMetaModelAliased;

import static org.junit.Assert.*;

/**
 * Tests that cover meta-model aliasing capabilities.
 * <p>
 * The referenced meta-models must already be generated for this class to compile.
 * 
 * @author TG Team
 *
 */
public class EntityMetaModelAliasedTest {

    /**
     * Normally a meta-model node can be directly converted to a path that reads "this". 
     * However, if a meta-model is aliased it should convert to its alias if the target of conversion is the immediate meta-model node.
     */
    @Test
    public void aliased_meta_model_converts_to_its_alias() {
        final var alias = "m";
        final ExampleEntityMetaModelAliased aliased = MetaModels.ExampleEntity_(alias);
        assertEquals(alias, aliased.alias);
        assertEquals(alias, aliased.toPath());
        
        // without alias - "this"
        assertEquals("this", MetaModels.ExampleEntity_.toPath());
        assertEquals("this", MetaModels.ExampleEntity_.toString());
    }

    @Test
    public void traversing_aliased_meta_model_yields_path_starting_with_alias() {
        var alias = "a";
        final EntityWithEntityTypedAndOrdinaryPropsMetaModel aliased = MetaModels.EntityWithEntityTypedAndOrdinaryProps_(alias);
        assertEquals("a.entity2.prop1", aliased.entity2().prop1().toPath());
        
        // without alias
        final EntityWithEntityTypedAndOrdinaryPropsMetaModel metaModel = MetaModels.EntityWithEntityTypedAndOrdinaryProps_;
        assertEquals("entity2.prop1", metaModel.entity2().prop1().toPath());
    }

    @Test
    public void blank_aliases_are_not_permitted() {
        for (final String alias:  new String[]{"", " ", "  "} ) {
            assertThrows(EntityMetaModelAliasedException.class, () -> MetaModels.ExampleEntity_(alias) /* should throw an exception */);
        }
    }

    @Test
    public void same_type_meta_models_with_equal_aliases_are_not_cached_at_the_moment() {
        final ExampleEntityMetaModelAliased exampleAliased1 = MetaModels.ExampleEntity_("e");
        final ExampleEntityMetaModelAliased exampleAliased2 = MetaModels.ExampleEntity_("e");
        assertNotSame(exampleAliased1, exampleAliased2);
        assertEquals(exampleAliased1, exampleAliased2);
        
        final ExampleEntityMetaModelAliased exampleAliased3 = MetaModels.ExampleEntity_("ex");
        assertNotSame(exampleAliased3, exampleAliased1);
        assertNotEquals(exampleAliased3, exampleAliased1);
    }

    @Test
    public void meta_models_for_different_entities_with_the_same_alias_are_not_equal() {
        assertNotEquals(MetaModels.ExampleEntity_("m"), MetaModels.SubEntity_("m"));
    }

    @Test
    public void meta_models_for_the_same_entities_but_different_aliases_are_not_equal() {
        assertNotEquals(MetaModels.ExampleEntity_("m1"), MetaModels.ExampleEntity_("m2"));
    }

}
