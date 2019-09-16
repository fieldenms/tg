package ua.com.fielden.platform.entity_centre.review.criteria;

/**
 * This is just convenient container for dynamic properties defined by entity centre configuration object. It contains only properties needed to export data for dynamic columns into Excel
 *
 * @author TG Team
 *
 */
public class DynamicColumnForExport {

    private String collectionalPropertyName;
    private String groupProp;
    private String groupPropValue;
    private String displayProp;
    private String title;

    /**
     * Returns the name of collectional property.
     *
     * @return
     */
    public String getCollectionalPropertyName() {
        return collectionalPropertyName;
    }

    public DynamicColumnForExport setCollectionalPropertyName(final String collectionalPropertyName) {
        this.collectionalPropertyName = collectionalPropertyName;
        return this;
    }

    /**
     * Returns the entity property name which contains the name of group.
     *
     * @return
     */
    public String getGroupProp() {
        return groupProp;
    }

    public DynamicColumnForExport setGroupProp(final String groupProp) {
        this.groupProp = groupProp;
        return this;
    }

    /**
     * Returns the name of group which is represented by this dynamic property
     *
     * @return
     */
    public String getGroupPropValue() {
        return groupPropValue;
    }

    public DynamicColumnForExport setGroupPropValue(final String groupPropValue) {
        this.groupPropValue = groupPropValue;
        return this;
    }

    /**
     * Returns the entity property name that contains the value for group represented by {@link DynamicColumnForExport#getGroupPropValue()}
     *
     * @return
     */
    public String getDisplayProp() {
        return displayProp;
    }

    public DynamicColumnForExport setDisplayProp(final String displayProp) {
        this.displayProp = displayProp;
        return this;
    }

    /**
     * Returns the title of group that will be displayed as column name in exported file.
     *
     * @return
     */
    public String getTitle() {
        return title;
    }

    public DynamicColumnForExport setTitle(final String title) {
        this.title = title;
        return this;
    }


}
