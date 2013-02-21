package ua.com.fielden.platform.migration;

import java.util.Map;

import ua.com.fielden.platform.sample.domain.ITgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

import com.google.inject.Inject;


public class tModelWithoutMakeRetriever extends AbstractRetriever<TgVehicleModel> {

    @Inject
    public tModelWithoutMakeRetriever(final ITgVehicleModel dao) {
	super(dao);
    }

    @Override
    public Map<String, String> resultFields() {
	return map( //
		field("key", "MODEL"), //
		field("desc", "MODEL_DESC") //
		);
    }

    @Override
    public String fromSql() {
	return "MODEL";
    }
}