package ua.com.fielden.platform.swing.review.report.analysis.multipledec;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.report.analysis.chart.ICategoryAnalysisDataProvider;

/**
 * A contract that allows one to customise multiple dec analysis view.
 * 
 * @author TG Team
 * 
 * @param <T>
 */
public interface IDecModelProvider<T extends AbstractEntity<?>> {

    /**
     * Returns the multiple dec model for specified {@link ICategoryAnalysisDataProvider} instance
     * 
     * @param chartModel
     * @return
     */
    NDecPanelModel<T> getMultipleDecModel(List<String> orderedProperties, ICategoryAnalysisDataProvider<Comparable<?>, Number, List<T>> chartModel);
}
