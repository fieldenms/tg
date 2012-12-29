package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;

public interface ILineCalculator<T extends AbstractEntity<?>> {

    Number calculateLineValue(final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel, int categoryIndex);

    String getRelatedSeries();
}
