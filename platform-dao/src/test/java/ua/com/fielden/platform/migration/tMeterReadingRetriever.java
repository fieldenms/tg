package ua.com.fielden.platform.migration;

import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.sample.domain.ITgMeterReading;
import ua.com.fielden.platform.sample.domain.TgMeterReading;

import com.google.inject.Inject;

public class tMeterReadingRetriever extends AbstractRetriever<TgMeterReading> {

    @Inject
    public tMeterReadingRetriever(final ITgMeterReading dao) {
	super(dao);
    }

    @Override
    public String whereSql() {
	return "NOT EXISTS (SELECT * FROM ALL_WORK_ORDERS W WHERE W.EQUIPNO = M.EQUIPNO AND W.METER_READING = M.LAST_READING AND W.ACTUAL_START = M.LAST_READING_DATE)";
    }

    @Override
    public List<String> orderSql() {
	return list("readingDate");
    }

    @Override
    public String splitProperty() {
	return "vehicle";
    }

    @Override
    public Map<String, String> resultFields() {
	return map( //
		field("vehicle", "M.EQUIPNO"), //
		field("readingDate", "M.LAST_READING_DATE"), //
		field("reading", "M.LAST_READING"), //
		field("workOrder", "M.WONO"), //
		field("fuelUsage.vehicle", "F.EQUIPNO"), //
		field("fuelUsage.purchaseDate", "F.USAGE_DATE"), //
		field("fuelUsage.fuelType", "F.FUEL_TYPE") //
		);
    }

    @Override
    public String fromSql() {
	return "METER_READING M LEFT JOIN FUEL_USAGE F ON M.FUEL_USAGE_ID = F.ID_COLUMN";
    }
}