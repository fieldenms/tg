package ua.com.fielden.platform.swing.review.details;

import org.apache.commons.lang.StringUtils;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * The abstract class for analysis' details data.
 *
 * @author TG Team
 *
 */
public abstract class AbstractAnalysisDetailsData<T extends AbstractEntity<?>> {

    /**
     * The entity type of the centre based details.
     */
    public final Class<T> root;
    /**
     * Represents the report name.
     */
    public final String name;
    /**
     * Represents the report's analysis name
     */
    public final String analysisName;

    /**
     * Creates new {@link AbstractAnalysisDetailsData} with report name and it's analysis name to which this details data belongs to.
     * Also specifies the details' entity type.
     *
     * @param root
     * @param name
     * @param analysisName
     */
    public AbstractAnalysisDetailsData(//
	    final Class<T> root, //
	    final String name, //
	    final String analysisName){
	this.root = root;
	this.name = name;
	this.analysisName = analysisName;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null || obj.getClass() != getClass()) {
	    return false;
	}
	final AbstractAnalysisDetailsData<T> anotherData = (AbstractAnalysisDetailsData<T>) obj;
	if ((root == null && root != anotherData.root) || (root != null && !root.equals(anotherData.root))) {
	    return false;
	}
	if ((name == null && name != anotherData.name) || (name != null && !name.equals(anotherData.name))) {
	    return false;
	}
	if ((analysisName == null && analysisName != anotherData.analysisName) //
		|| (analysisName != null && !analysisName.equals(anotherData.analysisName))) {
	    return false;
	}
	return true;
    }

    @Override
    public int hashCode() {
	int result = 17;
	result = 31 * result + (root != null ? root.hashCode() : 0);
	result = 31 * result + (name != null ? name.hashCode() : 0);
	result = 31 * result + (analysisName != null ? analysisName.hashCode() : 0);
	return result;
    }

    @Override
    public String toString() {
	return (root == null ? "" : (root.getSimpleName() + " ")) +
		(StringUtils.isEmpty(name) ? "" : (name + " ")) +
		(StringUtils.isEmpty(analysisName) ? "" : (analysisName + " "));
    }


    /**
     * Returns the title of details frame.
     *
     * @return
     */
    public abstract String getFrameTitle();
}
