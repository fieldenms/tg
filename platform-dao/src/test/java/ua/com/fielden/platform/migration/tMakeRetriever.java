package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

import com.google.inject.Inject;


public class tMakeRetriever extends AbstractRetriever<TgVehicleMake> {

    @Inject
    public tMakeRetriever(final ITgVehicleMake dao) {
	super(dao);
    }

    @Override
    public String selectSql() {
	return "SELECT MAKE key_, MAKE_DESC desc_ FROM MAKE";
    }
}
