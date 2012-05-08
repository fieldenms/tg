package ua.com.fielden.platform.reflection;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ua.com.fielden.platform.associations.one2many.MasterEntityWithOneToManyAssociation;
import ua.com.fielden.platform.associations.one2one.MasterEntityWithOneToOneAssociation;

/**
 * Test case for {@link Finder}'s functionality for finding <code>linkProperty</code>.
 *
 * @author TG Team
 *
 */
public class FindLinkPropertyTest {

    @Test
    public void should_have_found_link_property_in_one_to_one_association() {
	assertEquals("key", Finder.findLinkProperty(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_one_association_with_one_to_many_special_case_association() {
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToOneAssociation.class, "one2oneAssociation.one2ManyAssociation"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_collectional_association() {
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_collectional_association_of_nested_level_one_to_many_collectional_association() {
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationCollectional.one2manyAssociationCollectional"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_special_case_association() {
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase"));
    }

    @Test
    public void should_have_found_link_property_in_one_to_many_special_case_association_of_nested_level_one_to_many_collectional_association() {
	assertEquals("key1", Finder.findLinkProperty(MasterEntityWithOneToManyAssociation.class, "one2manyAssociationSpecialCase.one2manyAssociationCollectional"));
    }
}
