package ua.com.fielden.platform.swing.egi.models.builders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ua.com.fielden.platform.swing.egi.AbstractPropertyColumnMapping;
import ua.com.fielden.platform.swing.egi.models.mappings.PropertyColumnMappingByExpression;
import ua.com.fielden.platform.swing.egi.models.mappings.ReadonlyPropertyColumnMapping;

public class PropertyTableModelBuilderTest {

    @Test
    public void test_createReadonly_method() {
	final PropertyTableModelBuilder<DummyEntity> ptmb = new PropertyTableModelBuilder<DummyEntity>(DummyEntity.class);

	AbstractPropertyColumnMapping<DummyEntity> mapping = ptmb.createReadonly("", "", 0, "", null, null, null, null);
	assertEquals("Simple PropertyColumnMapping should be created for empty property.", mapping.getClass(), ReadonlyPropertyColumnMapping.class);

	mapping = ptmb.createReadonly("dummyEntity2.moneyField1", "", 0, "", null, null, null, null);
	assertNull("Null should be returned for non-correct property.", mapping);

	mapping = ptmb.createReadonly("dummyEntity2.getMoneyField1()", "", 0, "", null, null, null, null);
	assertNull("Null should be returned for non-correct method.", mapping);

	mapping = ptmb.createReadonly("dummyEntity2.getMoneyField()", "", 0, "", null, null, null, null);
	assertEquals("PropertyColumnMapping by expression should be created for method (defined by dot-notation).", mapping.getClass(), PropertyColumnMappingByExpression.class);

	mapping = ptmb.createReadonly("dummyEntity2.moneyField", "", 0, "", null, null, null, null);
	assertEquals("Simple PropertyColumnMapping should be created for property.", mapping.getClass(), ReadonlyPropertyColumnMapping.class);

	mapping = ptmb.createReadonly("dummyEntity2.nonProperty", "", 0, "", null, null, null, null);
	assertEquals("PropertyColumnMapping by expression should be created for non-property.", mapping.getClass(), PropertyColumnMappingByExpression.class);
    }

}
