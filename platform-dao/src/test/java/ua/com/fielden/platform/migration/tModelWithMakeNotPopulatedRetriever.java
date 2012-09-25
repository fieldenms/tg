package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

import com.google.inject.Inject;


public class tModelWithMakeNotPopulatedRetriever extends AbstractRetriever<TgVehicleModel> {

    @Inject
    public tModelWithMakeNotPopulatedRetriever(final ITgVehicleModel dao) {
	super(dao);
    }

    @Override
    public String selectSql() {
	return "SELECT MODEL key_, MODEL_DESC desc_, null make_ FROM MODEL";
    }
}
