package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

import com.google.inject.Inject;


public class tModelRetrieverWithAllMakesAssigned extends AbstractRetriever<TgVehicleModel> {

    @Inject
    public tModelRetrieverWithAllMakesAssigned(final ITgVehicleModel dao) {
	super(dao);
    }

    @Override
    public String selectSql() {
	return "SELECT MODEL key_, MODEL_DESC desc_, MAKE make_ FROM MODEL WHERE MAKE IS NOT NULL";
    }

    @Override
    public String splitProperty() {
	return "make";
    }
}
