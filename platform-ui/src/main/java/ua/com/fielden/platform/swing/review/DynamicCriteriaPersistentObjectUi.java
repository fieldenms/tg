package ua.com.fielden.platform.swing.review;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ua.com.fielden.platform.equery.PropertyAggregationFunction;
import ua.com.fielden.platform.swing.analysis.IAnalysisReportPersistentObject;
import ua.com.fielden.platform.swing.review.persistens.DynamicCriteriaPersistentObject;

/**
 * Instances of this class should encapsulate criteria- and layout-related data that needs to be persisted.
 * 
 * @author yura
 * 
 */
public class DynamicCriteriaPersistentObjectUi extends DynamicCriteriaPersistentObject {

    private final PropertyColumnMappingsPersistentObject propertyColumnMappings;

    private final Map<String, IAnalysisReportPersistentObject> analysis = new HashMap<String, IAnalysisReportPersistentObject>();
    private final LocatorPersistentObject locatorPersistentObject;
    private final boolean autoRun;

    private final boolean useForAutocompleter;
    private final boolean searchByDesc;
    private final boolean searchByKey;

    private final Map<String, PropertyAggregationFunction> totals = new HashMap<String, PropertyAggregationFunction>();

    protected DynamicCriteriaPersistentObjectUi() {
	super(null, null, null, null, 1, true);
	this.propertyColumnMappings = null;
	this.autoRun = false;
	this.useForAutocompleter = false;
	this.searchByDesc = false;
	this.searchByKey = true;

	this.locatorPersistentObject = new LocatorPersistentObject();
    }

    public DynamicCriteriaPersistentObjectUi(final LocatorPersistentObject locatorPersistentObject, final List<String> tableHeaders, final List<String> properties, final List<String> excludeProperties, final PropertyColumnMappingsPersistentObject propertyColumnMappings, final Map<String, PropertyPersistentObject> criteriaMappings, final int columnsCount, final boolean provideSuggestions, final Map<String, IAnalysisReportPersistentObject> analysis, final boolean autoRun, final boolean useForAutocompleter, final boolean searchBydesc, final boolean searchByKey) {
	super(tableHeaders, properties, excludeProperties, criteriaMappings, columnsCount, provideSuggestions);

	this.propertyColumnMappings = propertyColumnMappings;
	this.autoRun = autoRun;
	this.useForAutocompleter = useForAutocompleter;
	this.searchByDesc = searchBydesc;
	this.searchByKey = searchByKey;

	if (analysis != null) {
	    this.analysis.putAll(analysis);
	}
	this.locatorPersistentObject = new LocatorPersistentObject(locatorPersistentObject);
    }

    public DynamicCriteriaPersistentObjectUi(final LocatorPersistentObject locatorPersistentObject, final List<String> tableHeaders, final List<String> properties, final List<String> excludeProperties, final PropertyColumnMappingsPersistentObject propertyColumnMappings, final Map<String, PropertyPersistentObject> criteriaMappings, final int columnsCount, final boolean provideSuggestions, final Map<String, IAnalysisReportPersistentObject> analysis, final Map<String, PropertyAggregationFunction> totals, final boolean autoRun) {
	this(locatorPersistentObject, tableHeaders, properties, excludeProperties, propertyColumnMappings, criteriaMappings, columnsCount, provideSuggestions, analysis, autoRun, false, false, true);

	if (totals != null) {
	    this.totals.putAll(totals);
	}
    }

    public LocatorPersistentObject getLocatorPersistentObject() {
	return new LocatorPersistentObject(locatorPersistentObject);
    }

    public Map<String, IAnalysisReportPersistentObject> getAnalysis() {
	return Collections.unmodifiableMap(analysis);
    }

    public Map<String, PropertyAggregationFunction> getTotals() {
	return Collections.unmodifiableMap(totals);
    }

    /**
     * Determines whether this {@link DynamicCriteriaPersistentObjectUi} is different from the given one.
     * 
     * @param obj
     * @return
     */
    @Override
    public boolean isChanged(final Object obj) {
	final boolean result = super.isChanged(obj);
	if (result) {
	    return result;
	}
	if (this == obj) {
	    return false;
	}
	if (!(obj instanceof DynamicCriteriaPersistentObjectUi)) {
	    return true;
	}
	final DynamicCriteriaPersistentObjectUi pObj = (DynamicCriteriaPersistentObjectUi) obj;
	if ((getPropertyColumnMappings() == null && getPropertyColumnMappings() != pObj.getPropertyColumnMappings())
		|| (getPropertyColumnMappings() != null && getPropertyColumnMappings().isChanged(pObj.getPropertyColumnMappings()))) {
	    return true;
	}
	if ((getLocatorPersistentObject() == null && getLocatorPersistentObject() != pObj.getLocatorPersistentObject())
		|| (getLocatorPersistentObject() != null && getLocatorPersistentObject().isChanged(pObj.getLocatorPersistentObject()))) {
	    return true;
	}
	if (!isAnalysisEqual(pObj.getAnalysis())) {
	    return true;
	}
	if (!getTotals().equals(pObj.getTotals())) {
	    return true;
	}
	if (isAutoRun() != pObj.isAutoRun()) {
	    return true;
	}
	if (isUseForAutocompleter() != pObj.isUseForAutocompleter()) {
	    return true;
	}
	if (isSearchByKey() != pObj.isSearchByKey()) {
	    return true;
	}
	if (isSearchByDesc() != pObj.isSearchByDesc()) {
	    return true;
	}
	return false;
    }

    /**
     * Determines whether map of {@link AnalysisPersistentObject}s is different from the specified one.
     * 
     * @param anotherAnalysis
     * @return
     */
    public boolean isAnalysisEqual(final Map<String, IAnalysisReportPersistentObject> anotherAnalysis) {
	if (anotherAnalysis == analysis)
	    return true;

	if (anotherAnalysis.size() != analysis.size())
	    return false;

	try {
	    final Iterator<Entry<String, IAnalysisReportPersistentObject>> i = analysis.entrySet().iterator();
	    while (i.hasNext()) {
		final Entry<String, IAnalysisReportPersistentObject> e = i.next();
		final String key = e.getKey();
		final IAnalysisReportPersistentObject value = e.getValue();
		if (value == null) {
		    if (!(anotherAnalysis.get(key) == null && anotherAnalysis.containsKey(key)))
			return false;
		} else {
		    if (!value.isIdentical(anotherAnalysis.get(key)))
			return false;
		}
	    }
	} catch (final ClassCastException unused) {
	    return false;
	} catch (final NullPointerException unused) {
	    return false;
	}

	return true;
    }

    public PropertyColumnMappingsPersistentObject getPropertyColumnMappings() {
	return propertyColumnMappings;
    }

    public boolean isAutoRun() {
	return autoRun;
    }

    public boolean isUseForAutocompleter() {
	return useForAutocompleter;
    }

    public boolean isSearchByDesc() {
	return searchByDesc;
    }

    public boolean isSearchByKey() {
	return searchByKey;
    }

    @Override
    public void updatePersistentObjectFrom(final DynamicCriteriaPersistentObject obj) {
	if (isChanged(obj)) {
	    return;
	}
	if (obj instanceof DynamicCriteriaPersistentObjectUi) {
	    final DynamicCriteriaPersistentObjectUi uiObj = (DynamicCriteriaPersistentObjectUi) obj;
	    final Map<String, IAnalysisReportPersistentObject> givenAnalysisReports = uiObj.getAnalysis();
	    for (final String key : analysis.keySet()) {
		final IAnalysisReportPersistentObject analysisPersistent = analysis.get(key);
		final IAnalysisReportPersistentObject givenAnalysis = givenAnalysisReports.get(key);
		analysisPersistent.updateFromAnalysis(givenAnalysis);
	    }
	}
    }

}
