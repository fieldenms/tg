package ua.com.fielden.platform.migration;

import java.util.SortedMap;

import ua.com.fielden.platform.sample.domain.ITgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;

import com.google.inject.Inject;

public class tMakeRetriever extends AbstractRetriever<TgVehicleMake> {

    @Inject
    public tMakeRetriever(final ITgVehicleMake dao) {
        super(dao);
    }

    @Override
    public SortedMap<String, String> resultFields() {
        return map( //
        field("key", "MAKE"), //
                field("desc", "MAKE_DESC") //
        );
    }

    @Override
    public String fromSql() {
        return "MAKE";
    }
}