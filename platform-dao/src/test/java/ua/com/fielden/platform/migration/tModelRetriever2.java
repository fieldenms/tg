package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleModel2;

import com.google.inject.Inject;


public class tModelRetriever2 extends AbstractRetriever2<TgVehicleModel> {

    @Inject
    public tModelRetriever2(final ITgVehicleModel2 dao) {
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
