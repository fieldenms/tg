package ua.com.fielden.platform.eql.s2.elements;

import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;

public abstract class AbstractSource2 implements ISource2 {

    public List<EntProp2> props = new ArrayList<>();

    @Override
    public String toString() {
        return sourceType().getSimpleName() + " " + hashCode();
    }


    @Override
    public void addProp(final EntProp2 prop) {
	props.add(prop);
    }

    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     */
    protected final String alias;

    /**
     * Reference to mappings generator instance - used for acquiring properties persistence infos.
     */
    private final DomainMetadataAnalyser domainMetadataAnalyser;

    public AbstractSource2(final String alias, final DomainMetadataAnalyser domainMetadataAnalyser) {
        this.alias = alias;
        this.domainMetadataAnalyser = domainMetadataAnalyser;
    }

    @Override
    public String getAlias() {
        return alias;
    }


    public DomainMetadataAnalyser getDomainMetadataAnalyser() {
        return domainMetadataAnalyser;
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((alias == null) ? 0 : alias.hashCode());
	return result;
    }

    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (!(obj instanceof AbstractSource2)) {
	    return false;
	}
	final AbstractSource2 other = (AbstractSource2) obj;
	if (alias == null) {
	    if (other.alias != null) {
		return false;
	    }
	} else if (!alias.equals(other.alias)) {
	    return false;
	}
	return true;
    }

    public List<EntProp2> props() {
        return props;
    }
}