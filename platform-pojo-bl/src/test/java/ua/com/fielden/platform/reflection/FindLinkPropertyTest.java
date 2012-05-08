package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2many.incorrect.MasterEntity5;
import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;

/**
 * Test case for {@link Finder}'s functionality for finding <code>linkProperty</code> and determining association type.
 *
 * @author TG Team
 *
 */
public class FindLinkPropertyTest {

    @Test
    public void should_have_found_link_property_in_one_to_one_association() {
	assertTrue(Finder.isOne2Many_or_One2One_association(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation"));
	assertEquals("key", Finder.findLinkProperty(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_one_association_with_one_to_many_special_case_association() {
	assertTrue(Finder.isOne2Many_or_One2One_association(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation.one2ManyAssociation"));
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation.one2ManyAssociation"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_collectional_association() {
	assertTrue(Finder.isOne2Many_or_One2One_association(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional"));
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_collectional_association_of_nested_level_one_to_many_collectional_association() {
	assertTrue(Finder.isOne2Many_or_One2One_association(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional.one2manyAssociationCollectional"));
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional.one2manyAssociationCollectional"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_special_case_association() {
	assertTrue(Finder.isOne2Many_or_One2One_association(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase"));
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_special_case_association_of_nested_level_one_to_many_collectional_association() {
	assertTrue(Finder.isOne2Many_or_One2One_association(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase.one2manyAssociationCollectional"));
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase.one2manyAssociationCollectional"));
    }

    @Test
    public void self_type_association_should_not_be_one2many() {
	assertFalse(Finder.isOne2Many_or_One2One_association(MasterEntity5.class, "selfTypeAssociation"));
    }
}
