package ua.com.fielden.platform.swing.review.factory;

import java.util.Map;
import java.util.WeakHashMap;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.swing.analysis.DetailsFrame;
import ua.com.fielden.platform.swing.categorychart.CategoryChartReview;
import ua.com.fielden.platform.swing.review.DynamicEntityReviewWithTabs;
import ua.com.fielden.platform.swing.review.IEntityMasterManager;
import ua.com.fielden.platform.ui.config.api.interaction.ILocatorConfigurationController;

/**
 * {@link IEntityReviewFactory} that can be used as super class for other {@link IEntityReviewFactory}s those creates {@link DynamicEntityReviewWithTabs}.
 * 
 * @author oleh
 * 
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public abstract class EntityReviewWithTabsFactory<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> implements IEntityReviewFactory<T, DAO, R> {

    private final Map<String, Map<Object, DetailsFrame>> detailsCache = new WeakHashMap<String, Map<Object, DetailsFrame>>();

    private final String reportName;
    private final EntityFactory entityFactory;
    private final IEntityMasterManager entityMasterFactory;
    private final ILocatorConfigurationController locatorController;

    public EntityReviewWithTabsFactory(final String reportName, final EntityFactory entityFactory, final IEntityMasterManager entityMasterFactory, final ILocatorConfigurationController locatorController) {
	this.reportName = reportName;
	this.entityFactory = entityFactory;
	this.entityMasterFactory = entityMasterFactory;
	this.locatorController = locatorController;
    }

    /**
     * Returns cache for the details frames of the {@link CategoryChartReview}.
     * 
     * @return
     */
    protected Map<String, Map<Object, DetailsFrame>> getDetailsCache() {
	return detailsCache;
    }

    /**
     * Returns the report name for this {@link IEntityReviewFactory}
     * 
     * @return
     */
    public String getReportName() {
	return reportName;
    }

    public EntityFactory getEntityFactory() {
	return entityFactory;
    }

    public IEntityMasterManager getEntityMasterFactory() {
	return entityMasterFactory;
    }

    public ILocatorConfigurationController getLocatorController() {
	return locatorController;
    }
}
