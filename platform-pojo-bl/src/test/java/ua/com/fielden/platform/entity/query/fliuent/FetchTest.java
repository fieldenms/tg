package ua.com.fielden.platform.entity.query.fliuent;

import org.junit.Test;

import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

public class FetchTest {

    @Test
    public void test() {
	//fail("Not yet implemented");
	final fetch<TgVehicleMake> makeFetchModel = new fetch<TgVehicleMake>(TgVehicleMake.class){};
	//makeFetchModel.with("nep1");
    }

}
