package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.controller.ITgVehicleMake2;

import com.google.inject.Inject;


public class tMakeRetriever2 extends AbstractRetriever2<TgVehicleMake> {

    @Inject
    public tMakeRetriever2(final ITgVehicleMake2 dao) {
	super(dao);
    }

    @Override
    public String selectSql() {
	return "SELECT MAKE key_, MAKE_DESC desc_ FROM MAKE";
    }
}
