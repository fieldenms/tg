package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel;

import com.google.inject.Inject;


public class tModelUpdaterWithMake extends AbstractRetriever<TgVehicleModel> {

    @Inject
    public tModelUpdaterWithMake(final ITgVehicleModel dao) {
	super(dao);
    }

    @Override
    public String selectSql() {
	return "SELECT MODEL key_, MAKE make_ FROM MODEL";
    }
}
