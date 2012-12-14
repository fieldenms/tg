package ua.com.fielden.platform.swing.review.report.analysis.chart;

import java.util.List;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.domaintree.centre.ICentreDomainTreeManager.ICentreDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.domaintree.centre.analyses.IAbstractAnalysisDomainTreeManager;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.reflection.development.EntityDescriptor;
import ua.com.fielden.platform.utils.Pair;

/**
 * The analysis' data that is needed to create details view.
 *
 * @author TG Team
 *
 */
public class AnalysisDetailsData<T extends AbstractEntity<?>> {

    private final Class<T> root;
    private final String name;
    private final String analysisName;
    private final String frameTitle;
    private final ICentreDomainTreeManagerAndEnhancer baseCdtme;
    private final IAbstractAnalysisDomainTreeManager adtm;
    private final List<Pair<String, Object>> linkPropValuePairs;

    public AnalysisDetailsData(//
	    final Class<T> root, //
	    final String name, //
	    final String analysisName, //
	    final ICentreDomainTreeManagerAndEnhancer baseCdtme, //
	    final IAbstractAnalysisDomainTreeManager adtme, //
	    final List<Pair<String, Object>> linkPropValuePairs){
	this.root = root;
	this.baseCdtme = baseCdtme;
	this.adtm = adtme;
	this.linkPropValuePairs = linkPropValuePairs;
	this.name = name;
	this.analysisName = analysisName;
	this.frameTitle = createFrameTitle(linkPropValuePairs);
    }

    /**
     * Returns the entity type for which this detail must be created.
     *
     * @return
     */
    public Class<T> getRoot() {
	return root;
    }

    /**
     * Returns the managed type of the base centre domain tree manager and root type.
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Class<T> getManagedType(){
	return (Class<T>)getBaseCdtme().getEnhancer().getManagedType(getRoot());
    }

    /**
     * Returns the instance of {@link ICentreDomainTreeManagerAndEnhancer} on which the details will be based.
     *
     * @return
     */
    public ICentreDomainTreeManagerAndEnhancer getBaseCdtme() {
	return baseCdtme;
    }

    /**
     * Returns the list of pairs of property names and it's values for details query.
     *
     * @return
     */
    public List<Pair<String, Object>> getLinkPropValuePairs() {
	return linkPropValuePairs;
    }

    /**
     * Returns the title for details frame.
     *
     * @return
     */
    public String getFrameTitle(){
	return frameTitle;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || obj.getClass() != this.getClass()) {
	    return false;
	}
	final AnalysisDetailsData<T> anotherData = (AnalysisDetailsData<T>) obj;
	if ((getRoot() == null && getRoot() != anotherData.getRoot()) || (getRoot() != null && !getRoot().equals(anotherData.getRoot()))) {
	    return false;
	}
	if ((name == null && name != anotherData.name) || (name != null && !name.equals(name))) {
	    return false;
	}
	if ((analysisName == null && analysisName != anotherData.analysisName) //
		|| (analysisName != null && !analysisName.equals(anotherData.analysisName))) {
	    return false;
	}
	if ((getLinkPropValuePairs() == null && getLinkPropValuePairs() != anotherData.getLinkPropValuePairs()) //
		|| (getLinkPropValuePairs() != null && !getLinkPropValuePairs().equals(anotherData.getLinkPropValuePairs()))) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + (getRoot() != null ? getRoot().hashCode() : 0);
	result = 31 * result + (name != null ? name.hashCode() : 0);
	result = 31 * result + (analysisName != null ? analysisName.hashCode() : 0);
	result = 31 * result + (getLinkPropValuePairs() != null ? getLinkPropValuePairs().hashCode() : 0);
	return result;
    }

    @Override
    public String toString() {
	return (getRoot() == null ? "" : (getRoot().getSimpleName() + " ")) +
		(StringUtils.isEmpty(name) ? "" : (name + " ")) +
		(StringUtils.isEmpty(analysisName) ? "" : (analysisName + " ")) +
		(getLinkPropValuePairs() == null ? "" : getLinkPropValuePairs().toString());
    }

    /**
     * Creates the title for details frame.
     *
     * @param choosenItems
     * @return
     */
    private String createFrameTitle(final List<Pair<String, Object>> choosenItems) {
	return "Details for " + createDistributionPropertyTitle(choosenItems) + " " + createDistributionEntitiesTitle(choosenItems) + " ("
		+ (StringUtils.isEmpty(name) ? "" : (name + ": ")) + analysisName + ")";
    }

    /**
     * Creates part of the details frame title that contains only property values.
     *
     * @param choosenItems
     * @return
     */
    private String createDistributionEntitiesTitle(final List<Pair<String, Object>> choosenItems) {
	String titles = "";
	for (final Pair<String, Object> pair : choosenItems) {
	    titles += ", " + createPairString(pair.getValue());
	}
	return titles.isEmpty() ? titles : titles.substring(2);
    }

    /**
     * Creates the property value string.
     *
     * @param value
     * @return
     */
    private String createPairString(final Object value) {
	if (value instanceof AbstractEntity) {
	    return ((AbstractEntity<?>) value).getKey().toString() + " \u2012 " + ((AbstractEntity<?>) value).getDesc();
	} else if (value != null) {
	    return value.toString() + " \u2012 " + value.toString();
	} else {
	    return "UNKNOWN \u2012 UNKNOWN";
	}
    }

    /**
     * Creates the details frame title that consists of property name
     *
     * @param choosenItems
     * @return
     */
    private String createDistributionPropertyTitle(final List<Pair<String, Object>> choosenItems) {
	String name = "";
	final EntityDescriptor ed = new EntityDescriptor(getManagedType(), adtm.getFirstTick().checkedProperties(getRoot()));
	for (final Pair<String, Object> pair : choosenItems) {
	    name += '\u2192' + "(" + ed.getTitle(pair.getKey()) + ")";
	}
	return name.isEmpty() ? name : name.substring(1);
    }
}
