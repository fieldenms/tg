package ua.com.fielden.platform.entity.query.fluent;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.fetch.FetchCategory;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class FetchTest {

    @Test
    public void test_immutability() {
	final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.MINIMAL){};
	assertFalse("Two fetch models should not be equal", makeFetchModel.equals(makeFetchModel.without("desc")));
    }

    @Test
    public void test_duplication() {
	final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class, FetchCategory.MINIMAL){};
	try {
	    makeFetchModel.with("desc").without("desc");
	    fail("Should have failed with duplicate exception");
	} catch (final Exception e) {
	}
    }
}
