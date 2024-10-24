package ua.com.fielden.platform.eql.meta;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.eql.retrieval.AbstractEqlShortcutTest;
import ua.com.fielden.platform.sample.domain.TeVehicle;

public class UtilsTest extends AbstractEqlShortcutTest {

    @Test
    public void order_of_dependent_calculated_properties_of_vehicle_is_correct() {
        final List<String> actOrder = DependentCalcPropsOrder.orderDependentCalcProps(querySourceInfoProvider(), metadata(), qb(), querySourceInfoProvider().getModelledQuerySourceInfo(TeVehicle.class));
        final List<String> expOrder = List.of("replacedByTwice", "theSameVehicle", "replacedByTwiceModel", "replacedByTwiceModelMake");
        assertEquals(actOrder, expOrder);
    }
}
