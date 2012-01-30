package ua.com.fielden.platform.entity.query.model.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

public abstract class AbstractEntQuerySource implements IEntQuerySource {
    private final String alias; // can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end)
    private final List<PropResolutionInfo> referencingProps = new ArrayList<PropResolutionInfo>();
    private final List<PropResolutionInfo> finalReferencingProps = new ArrayList<PropResolutionInfo>();
    private String sqlAlias;
    private Map<String, String> sourceColumns = new HashMap<String, String>();

    public void assignSqlAlias(final String sqlAlias) {
	this.sqlAlias = sqlAlias;
    }

    public AbstractEntQuerySource(final String alias) {
	this.alias = alias;
    }

    protected boolean isEntityAggregates(final Class type) {
	return EntityAggregates.class.isAssignableFrom(type);
    }

    protected boolean isSyntheticEntity(final Class type) {
	return AbstractEntity.class.isAssignableFrom(type) && IQueryModelProvider.class.isAssignableFrom(type);
    }

    public String getAlias() {
	return alias;
    }

    @Override
    public void addReferencingProp(final PropResolutionInfo prop) {
	referencingProps.add(prop);
    }

    @Override
    public void addFinalReferencingProp(final PropResolutionInfo prop) {
	finalReferencingProps.add(prop);
    }

    @Override
    public List<PropResolutionInfo> getReferencingProps() {
	return referencingProps;
    }

    /**
     * If there is alias and property is prepended with this alias, then return property with alias removed, otherwise return null
     * @param dotNotatedPropName
     * @param sourceAlias
     * @return
     */
    protected String dealiasPropName(final String dotNotatedPropName, final String sourceAlias) {
	return (sourceAlias != null && dotNotatedPropName.startsWith(sourceAlias + ".")) ? dotNotatedPropName.substring(sourceAlias.length() + 1) : null;
    }

    protected Pair<Boolean, Class> lookForPropInRealPropType(final Class parentType, final String dotNotatedPropName) {
	try {
	    return new Pair<Boolean, Class>(true, PropertyTypeDeterminator.determinePropertyType(parentType, dotNotatedPropName));
	} catch (final Exception e) {
	    return new Pair<Boolean, Class>(false, null);
	}
    }

    abstract Pair<Boolean, Class> lookForPropInEntAggregatesType(final Class parentType, final String dotNotatedPropName);

    protected Pair<Boolean, Class> lookForProp(final Class type, final String dotNotatedPropName) {
	if (EntityUtils.isPersistedEntityType(type)) {
	    return lookForPropInRealPropType(type, dotNotatedPropName);
	} else if (isEntityAggregates(type)) {
	    return lookForPropInEntAggregatesType(type, dotNotatedPropName);
	} else {
	    throw new RuntimeException("Not yet implemented the case of IQueryModelProvider");
	}
    }

    protected PropResolutionInfo propAsIs(final EntProp prop) {
	final Pair<Boolean, Class> propAsIsSearchResult = lookForProp(sourceType(), prop.getName());
	return propAsIsSearchResult.getKey() ? new PropResolutionInfo(prop, null, prop.getName(), false, propAsIsSearchResult.getValue()) : null;
    }

    protected PropResolutionInfo propAsAliased(final EntProp prop) {
	final String dealisedProp = dealiasPropName(prop.getName(), getAlias());
	if (dealisedProp == null) {
	    return null;
	} else {
		final Pair<Boolean, Class> propAsAliasedSearchResult = lookForProp(sourceType(), dealisedProp);
		return propAsAliasedSearchResult.getKey() ? new PropResolutionInfo(prop, getAlias(), dealisedProp, false, propAsAliasedSearchResult.getValue()) : null;
	}
    }

    protected PropResolutionInfo propAsImplicitId(final EntProp prop) {
	if (EntityUtils.isPersistedEntityType(sourceType())) {
	    return prop.getName().equalsIgnoreCase(getAlias()) ? new PropResolutionInfo(prop, getAlias(), null, true, Long.class) : null; // id property is meant here, but is it for all contexts?
	} else {
	    return null;
	}
    }

    @Override
    public Class propType(final String propSimpleName) {
	return PropertyTypeDeterminator.determinePropertyType(sourceType(), propSimpleName);
    }

    @Override
    public List<PropResolutionInfo> getFinalReferencingProps() {
	return finalReferencingProps;
    }

    @Override
    public PropResolutionInfo containsProperty(final EntProp prop) {
	// what are the cases/scenarios for one source and one property
	// 1) AsIs
	// 2) AsAliased
	// 3) AsImplicitId
	// possible combinations are
	// o. none
	// a. 1) Vehicle.class as v, prop "model"
	// b. 2) Vehicle.class as v, prop "v.model"
	// c. 3) Vehicle.class as v, prop "v"
	// d. 1) and 2) OrgUnit4.class as parent, prop "parent.parent"  => take 2) - its more explicit
	// e. 1) and 3) OrgUnit4.class as parent, prop "parent"		=> take 1) - favor real prop over implicit id
	// the result formula: if (1 and not 2) then 1
	//			else if (2) then 2
	//			else if (3) then 3
	//			else exception

	final PropResolutionInfo propAsIs = propAsIs(prop);
	final PropResolutionInfo propAsAliased = propAsAliased(prop);
	final PropResolutionInfo propAsImplicitId = propAsImplicitId(prop);
//	System.out.println("propAsIs: " + propAsIs);
//	System.out.println("propAsAliased: " + propAsAliased);
//	System.out.println("propAsImplicitId: " + propAsImplicitId);

	if (propAsIs == null && propAsAliased == null && propAsImplicitId == null) {
//	    System.out.println("prop [" + prop.getName() + "] not found within type " + sourceType().getSimpleName());
	    return null;
	} else if (propAsIs != null && propAsAliased == null) {
//	    System.out.println("prop [" + prop.getName() + "] found within type " + sourceType().getSimpleName() + " :AsIs: " + propAsIs);
	    return propAsIs;
	} else if (propAsAliased != null) {
//	    System.out.println("prop [" + prop.getName() + "] found within type " + sourceType().getSimpleName() + " :AsAliased: " + propAsAliased);
	    return propAsAliased;
	} else if (propAsImplicitId != null) {
//	    System.out.println("prop [" + prop.getName() + "] found within type " + sourceType().getSimpleName() + " :ImpId: " + propAsImplicitId);
	    return propAsImplicitId;
	} else {
	    throw new RuntimeException("Unforeseen branch!");
	}
    }

    /**
     * Represent data structure to hold information about potential prop resolution against some query source.
     * @author TG Team
     *
     */
    public static class PropResolutionInfo {
	private EntProp entProp;
	private String aliasPart;
	private String propPart;
	private boolean implicitId;
	private Class propType;

	@Override
	public String toString() {
	    return "PropResolutionInfo: aliasPart = " + aliasPart + " : propPart = " + propPart + " : impId = " + implicitId + " : type = " + (propType != null ? propType.getSimpleName() : null);
	}

	public PropResolutionInfo(final EntProp entProp, final String aliasPart, final String propPart, final boolean implicitId, final Class propType) {
	    this.entProp = entProp;
	    if(entProp.getPropType() == null && propType != null) {
		entProp.setPropType(propType);
	    }
	    this.aliasPart = aliasPart;
	    this.propPart = propPart;
	    this.implicitId = implicitId;
	    this.propType = propType;
	}

	public String getAliasPart() {
	    return aliasPart;
	}

	public String getPropPart() {
	    return propPart;
	}

	public boolean isImplicitId() {
	    return implicitId;
	}

	public Class getPropType() {
	    return propType;
	}

	public Integer getPreferenceNumber() {
	    return implicitId ? 2000 : propPart.length();
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((aliasPart == null) ? 0 : aliasPart.hashCode());
	    result = prime * result + ((entProp == null) ? 0 : entProp.hashCode());
	    result = prime * result + (implicitId ? 1231 : 1237);
	    result = prime * result + ((propPart == null) ? 0 : propPart.hashCode());
	    result = prime * result + ((propType == null) ? 0 : propType.hashCode());
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
	    if (!(obj instanceof PropResolutionInfo)) {
		return false;
	    }
	    final PropResolutionInfo other = (PropResolutionInfo) obj;
	    if (aliasPart == null) {
		if (other.aliasPart != null) {
		    return false;
		}
	    } else if (!aliasPart.equals(other.aliasPart)) {
		return false;
	    }
	    if (entProp == null) {
		if (other.entProp != null) {
		    return false;
		}
	    } else if (!entProp.equals(other.entProp)) {
		return false;
	    }
	    if (implicitId != other.implicitId) {
		return false;
	    }
	    if (propPart == null) {
		if (other.propPart != null) {
		    return false;
		}
	    } else if (!propPart.equals(other.propPart)) {
		return false;
	    }
	    if (propType == null) {
		if (other.propType != null) {
		    return false;
		}
	    } else if (!propType.equals(other.propType)) {
		return false;
	    }
	    return true;
	}

	public EntProp getEntProp() {
	    return entProp;
	}
    }
}