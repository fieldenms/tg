package ua.com.fielden.platform.eql.meta;


import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.eql.meta.utils.DependentCalcPropsOrder;
import ua.com.fielden.platform.sample.domain.TeVehicle;

public class EqlDomainMetadataTest extends EqlTestCase {

    @Test
    public void topological_sorting_of_directly_and_transitively_dependent_calc_props_ensures_correct_order() {
        final List<String> actOrder = DependentCalcPropsOrder.orderDependentCalcProps(querySourceInfoProvider(), qb(), querySourceInfoProvider().getModelledQuerySourceInfo(TeVehicle.class));
        final Integer replacedByTwicePosition = actOrder.indexOf("replacedByTwice");
        final Integer theSameVehiclePosition = actOrder.indexOf("theSameVehicle");
        final Integer replacedByTwiceModelPosition = actOrder.indexOf("replacedByTwiceModel");
        final Integer replacedByTwiceModelMakePosition = actOrder.indexOf("replacedByTwiceModelMake");
        // verify direct calc-props dependencies
        assertTrue("Calculated property 'replacedByTwiceModel' should precede dependent calculated property 'replacedByTwiceModelMake'!", replacedByTwiceModelPosition < replacedByTwiceModelMakePosition);
        // verify transitive calc-props dependencies
        assertTrue("Calculated property 'replacedByTwice' should precede dependent calculated property 'theSameVehicle'!", replacedByTwicePosition < theSameVehiclePosition);
    }
}