package ua.com.fielden.platform.swing.review.wizard;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.swing.review.DynamicCriteriaModelBuilder;
import ua.com.fielden.platform.swing.review.DynamicCriteriaPersistentObjectUi;
import ua.com.fielden.platform.swing.review.DynamicEntityQueryCriteria;
import ua.com.fielden.platform.treemodel.LocatorTreeModel;

/**
 * Model for {@link LocatorWizard}.
 * 
 * @author TG Team
 * 
 */
public class LocatorWizardModel<T extends AbstractEntity, DAO extends IEntityDao<T>, R extends AbstractEntity> extends AbstractWizardModel<T, DAO, R> {

    private boolean isUseForAutocompleter = false;
    private boolean searchByDesc = false;
    private boolean searchByKey = true;

    public LocatorWizardModel(final DynamicEntityQueryCriteria<T, DAO> dynamicCriteria, final Class<R> resultantEntityClass, final DynamicCriteriaPersistentObjectUi persistentObject, final DynamicCriteriaModelBuilder<T, DAO, R> modelBuilder) {
	super(dynamicCriteria, resultantEntityClass, persistentObject, modelBuilder);
	if (persistentObject != null) {
	    isUseForAutocompleter = persistentObject.isUseForAutocompleter();
	    searchByDesc = persistentObject.isSearchByDesc();
	    searchByKey = persistentObject.isSearchByKey();
	} else {
	    setAutoRun(true);
	}
    }

    @Override
    public LocatorTreeModel createTreeModel() {
	return new LocatorTreeModel(getEntityClass(), getPropertyFilter());
    }

    public void setUseForAutocompleter(final boolean isUseForAutocompleter) {
	this.isUseForAutocompleter = isUseForAutocompleter;
    }

    public boolean isUseForAutocompleter() {
	return isUseForAutocompleter;
    }

    public boolean isSearchByDesc() {
	return searchByDesc;
    }

    public void setSearchByDesc(final boolean searchByDesc) {
	this.searchByDesc = searchByDesc;
    }

    public boolean isSearchByKey() {
	return searchByKey;
    }

    public void setSearchByKey(final boolean searchByKey) {
	this.searchByKey = searchByKey;
    }

    @Override
    public DynamicCriteriaPersistentObjectUi createPersistentObject() {
	final DynamicCriteriaPersistentObjectUi previousObjectUi = super.createPersistentObject();
	return new DynamicCriteriaPersistentObjectUi(previousObjectUi.getLocatorPersistentObject(), previousObjectUi.getTableHeaders(), previousObjectUi.getPersistentProperties(), previousObjectUi.getExcludeProperties(), previousObjectUi.getPropertyColumnMappings(), previousObjectUi.getCriteriaMappings(), previousObjectUi.getColumnsCount(), previousObjectUi.isProvideSuggestions(), previousObjectUi.getAnalysis(), isAutoRun(), isUseForAutocompleter(), isSearchByDesc(), isSearchByKey());
    }
}
