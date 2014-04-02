package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.categorychart.EntityWrapper;

//TODO this class should be removed later on.
class CategoryDataModel<T extends AbstractEntity<?>> {

    private final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> dataProvider;

    public CategoryDataModel(final ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> dataProvider) {
        this.dataProvider = dataProvider;
    }

    public Comparable<?> getCategory(final int index) {
        final List<String> categories = dataProvider.categoryProperties();
        if (categories.size() == 0) {
            return "category";
        }
        final Comparable<?> entity = dataProvider.getCategoryDataValue(index, categories.get(0));
        return new EntityWrapper(entity);
    }

    public int getCategoryCount() {
        return dataProvider.getCategoryDataEntryCount();
    }

    public Comparable<?> getSeries(final int index) {
        return dataProvider.aggregatedProperties().get(index);
    }

    public int getSeriesCount() {
        return dataProvider.aggregatedProperties().size();
    }

    public Number getValue(final int row, final int column) {
        final List<String> aggregations = dataProvider.aggregatedProperties();
        return dataProvider.getAggregatedDataValue(column, aggregations.get(row));
    }
}
