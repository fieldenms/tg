package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.sample.domain.TgMeterReading;
import ua.com.fielden.platform.sample.domain.controller.ITgMeterReading;

import com.google.inject.Inject;

public class tMeterReadingRetriever extends AbstractRetriever<TgMeterReading> {

    @Inject
    public tMeterReadingRetriever(final ITgMeterReading dao) {
	super(dao);
    }

    @Override
    public String selectSql() {
	return "SELECT " + //
		"M.EQUIPNO vehicle_, " + //
		"M.LAST_READING_DATE reading_date_, " + //
		"M.LAST_READING reading_, " + //
		"M.WONO work_order_, " + //
		"F.EQUIPNO fuel_usage__vehicle_, " + //
		"F.USAGE_DATE fuel_usage__purchase_date_, " + //
		"F.FUEL_TYPE fuel_usage__fuel_type_ " + //
		"FROM METER_READING M LEFT JOIN FUEL_USAGE F ON M.FUEL_USAGE_ID = F.ID_COLUMN WHERE NOT EXISTS (SELECT * FROM ALL_WORK_ORDERS W " + //
		"WHERE W.EQUIPNO = M.EQUIPNO AND W.METER_READING = M.LAST_READING AND W.ACTUAL_START = M.LAST_READING_DATE) ORDER BY reading_date_"; //
    }

    @Override
    public String splitProperty() {
	return "vehicle";
    }
}
