package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel;

import com.google.inject.Inject;


public class tModelRetriever extends AbstractRetriever<TgVehicleModel> {

    @Inject
    public tModelRetriever(final ITgVehicleModel dao) {
	super(dao);
    }

    @Override
    public String selectSql() {
	return "SELECT MODEL key_, MODEL_DESC desc_, MAKE make_ FROM MODEL";
    }

    @Override
    public String splitProperty() {
	return "make";
    }
}
