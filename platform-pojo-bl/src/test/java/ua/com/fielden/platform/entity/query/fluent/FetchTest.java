package ua.com.fielden.platform.entity.query.fluent;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

public class FetchTest {

    @Test
    public void test_immutability() {
        final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.DEFAULT) {
        };
        assertFalse("Two fetch models should not be equal", makeFetchModel.equals(makeFetchModel.without("desc")));
    }

    @Test
    public void test_duplication() {
        final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.DEFAULT) {
        };
        try {
            makeFetchModel.with("desc").without("desc");
            fail("Should have failed with duplicate exception");
        } catch (final Exception e) {
        }
    }

    @Test
    public void test_validation_of_non_existing_property() {
        final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.DEFAULT) {
        };
        try {
            makeFetchModel.with("ket");
            fail("Should have failed with non-existing property exception");
        } catch (final Exception e) {
        }
    }

}
