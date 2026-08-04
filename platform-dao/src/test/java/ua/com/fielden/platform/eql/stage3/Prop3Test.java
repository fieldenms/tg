package ua.com.fielden.platform.eql.stage3;

import org.junit.Test;
import ua.com.fielden.platform.eql.meta.EqlStage3TestCase;
import ua.com.fielden.platform.eql.stage3.operands.Prop3;
import ua.com.fielden.platform.sample.domain.TgVehicle;

import static org.junit.Assert.assertThrows;
import static ua.com.fielden.platform.eql.meta.PropType.STRING_PROP_TYPE;

public class Prop3Test extends EqlStage3TestCase {

    @Test
    public void a_prop_cannot_be_constructed_without_a_name() {
        assertThrows(NullPointerException.class, () -> new Prop3(null, source(TgVehicle.class, 1, 1), STRING_PROP_TYPE));
    }

    @Test
    public void a_prop_cannot_be_constructed_without_a_source() {
        assertThrows(NullPointerException.class, () -> new Prop3("key", null, STRING_PROP_TYPE));
    }

}
