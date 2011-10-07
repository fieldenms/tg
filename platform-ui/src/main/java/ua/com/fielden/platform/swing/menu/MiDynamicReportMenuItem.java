package ua.com.fielden.platform.swing.menu;

import ua.com.fielden.platform.dao.IDaoFactory;
import ua.com.fielden.platform.dao.IEntityAggregatesDao;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.matcher.IValueMatcherFactory;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.factory.IEntityReviewFactory;
import ua.com.fielden.platform.ui.config.api.interaction.ICenterConfigurationController;

import com.google.inject.Injector;

/**
 * A convenient abstraction for a menu item for entity centres.
 *
 * @author TG Team
 *
 * @param <T>
 * @param <DAO>
 * @param <R>
 */
public abstract class MiDynamicReportMenuItem<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends MiWithConfigurationSupport<T, DAO, R> {

    private static final long serialVersionUID = 8838631121425630548L;

    public MiDynamicReportMenuItem(//
    final TreeMenuWithTabs<?> treeMenu,//
    final Injector injector,//
    final ICenterConfigurationController centerController,//
    final ITreeMenuItemVisibilityProvider visibilityProvider,//
    final Class<?> menuItemClass,//
    final String caption,//
    final String description,//
    final Class<R> resultantEntityClass,//
    final Class<DAO> daoClass, final IEntityReviewFactory<T, DAO, R> entityReviewModelFactory) {
	super(treeMenu,//
	injector,//
	centerController,//
	resultantEntityClass,//
	daoClass,//
	new DynamicReportWrapper<T, DAO, R>(//
	caption,//
	description,//
	new DynamicCriteriaModelBuilder<T, DAO, R>(injector.getInstance(EntityFactory.class), injector.getInstance(IValueMatcherFactory.class), injector.getInstance(IDaoFactory.class), injector.getInstance(daoClass), injector.getInstance(IEntityAggregatesDao.class), resultantEntityClass, centerController.generateKeyForPrincipleCenter(menuItemClass), centerController, null, entityReviewModelFactory),//
	treeMenu), visibilityProvider);
	getView().setSaveAction(getView().getDynamicCriteriaModelBuilder().createSaveAction());
	getView().setSaveAsAction(createSaveAsAction(getView().getDynamicCriteriaModelBuilder()));
	getView().setPanelBuilder(createAnalysisActionPanel(this));
    }

    @Override
    protected DynamicCriteriaModelBuilder<T, DAO, R> getDynamicCriteriaModelBuilderFor(final String centerKey, final String reportName) {
	return new DynamicCriteriaModelBuilder<T, DAO, R>(getInjector().getInstance(EntityFactory.class), getInjector().getInstance(IValueMatcherFactory.class), getInjector().getInstance(IDaoFactory.class), getInjector().getInstance(getDaoClass()), getInjector().getInstance(IEntityAggregatesDao.class), getResultantEntityClass(), centerKey, getCenterController(), null, createEntityReviewFactory(reportName));
    }

}
