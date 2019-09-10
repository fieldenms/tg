package ua.com.fielden.platform.entity_centre.review.criteria;

public class DynamicPropForExport {

    private String collectionalPropertyName;
    private String keyProp;
    private String keyPropValue;
    private String valueProp;
    private String title;

    public String getCollectionalPropertyName() {
        return collectionalPropertyName;
    }

    public DynamicPropForExport setCollectionalPropertyName(final String collectionalPropertyName) {
        this.collectionalPropertyName = collectionalPropertyName;
        return this;
    }

    public String getKeyProp() {
        return keyProp;
    }

    public DynamicPropForExport setKeyProp(final String keyProp) {
        this.keyProp = keyProp;
        return this;
    }

    public String getKeyPropValue() {
        return keyPropValue;
    }

    public DynamicPropForExport setKeyPropValue(final String keyPropValue) {
        this.keyPropValue = keyPropValue;
        return this;
    }

    public String getValueProp() {
        return valueProp;
    }

    public DynamicPropForExport setValueProp(final String valueProp) {
        this.valueProp = valueProp;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public DynamicPropForExport setTitle(final String title) {
        this.title = title;
        return this;
    }


}
