package ua.com.fielden.platform.swing.analysis.ndec.dec;

import ua.com.fielden.platform.reportquery.ICategoryChartEntryModel;

public interface ILineCalculator {

    Number calculateLineValue(ICategoryChartEntryModel chartModel, int category);

    int getRelatedSeriesIndex();
}
