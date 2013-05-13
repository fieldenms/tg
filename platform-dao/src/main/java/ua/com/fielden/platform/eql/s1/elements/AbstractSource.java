package ua.com.fielden.platform.eql.s1.elements;

import ua.com.fielden.platform.dao.DomainMetadataAnalyser;

public abstract class AbstractSource implements ISource {

    protected final boolean persistedType;

    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     */
    protected final String alias;

    /**
     * Reference to mappings generator instance - used for acquiring properties persistence infos.
     */
    private final DomainMetadataAnalyser domainMetadataAnalyser;

    public AbstractSource(final String alias, final DomainMetadataAnalyser domainMetadataAnalyser, final boolean persistedType) {
        this.alias = alias;
        this.persistedType = persistedType;
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
	if (!(obj instanceof AbstractSource)) {
	    return false;
	}
	final AbstractSource other = (AbstractSource) obj;
	if (alias == null) {
	    if (other.alias != null) {
		return false;
	    }
	} else if (!alias.equals(other.alias)) {
	    return false;
	}
	return true;
    }
}