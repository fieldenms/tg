package ua.com.fielden.platform.migration;

import java.util.SortedMap;

import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

import com.google.inject.Inject;

public class tModelRetrieverWithAllMakesAssigned extends AbstractRetriever<TgVehicleModel> {

    @Inject
    public tModelRetrieverWithAllMakesAssigned(final ITgVehicleModel dao) {
        super(dao);
    }

    @Override
    public SortedMap<String, String> resultFields() {
        return map( //
        field("key", "MODEL"), //
                field("desc", "MODEL_DESC"), //
                field("make", "MAKE") //
        );
    }

    @Override
    public String fromSql() {
        return "MODEL";
    }

    @Override
    public String whereSql() {
        return "MAKE IS NOT NULL";
    }
}