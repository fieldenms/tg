package ua.com.fielden.platform.entity_centre.review.criteria;

/**
 * This is just convenient container for dynamic properties defined by entity centre configuration object. It contains only properties needed to export data for dynamic columns into Excel
 *
 * @author TG Team
 *
 */
public class DynamicPropForExport {

    private String collectionalPropertyName;
    private String keyProp;
    private String keyPropValue;
    private String valueProp;
    private String title;

    /**
     * Returns the name of collectional property.
     *
     * @return
     */
    public String getCollectionalPropertyName() {
        return collectionalPropertyName;
    }

    public DynamicPropForExport setCollectionalPropertyName(final String collectionalPropertyName) {
        this.collectionalPropertyName = collectionalPropertyName;
        return this;
    }

    /**
     * Returns the entity property name which contains the name of group.
     *
     * @return
     */
    public String getKeyProp() {
        return keyProp;
    }

    public DynamicPropForExport setKeyProp(final String keyProp) {
        this.keyProp = keyProp;
        return this;
    }

    /**
     * Returns the name of group which is represented by this dynamic property
     *
     * @return
     */
    public String getKeyPropValue() {
        return keyPropValue;
    }

    public DynamicPropForExport setKeyPropValue(final String keyPropValue) {
        this.keyPropValue = keyPropValue;
        return this;
    }

    /**
     * Returns the entity property name that contains the value for group represented by {@link DynamicPropForExport#getKeyPropValue()}
     *
     * @return
     */
    public String getValueProp() {
        return valueProp;
    }

    public DynamicPropForExport setValueProp(final String valueProp) {
        this.valueProp = valueProp;
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

    public DynamicPropForExport setTitle(final String title) {
        this.title = title;
        return this;
    }


}
