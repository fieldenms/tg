package ua.com.fielden.platform.entity.query.model.elements;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.reflection.Finder;
import ua.com.fielden.platform.utils.Pair;

abstract class AbstractEntQuerySource implements IEntQuerySource {
    private final String alias; // can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end
    private final List<EntProp> referencingProps = new ArrayList<EntProp>();

    protected boolean isRealEntity(final Class type) {
	return AbstractEntity.class.isAssignableFrom(type) && !EntityAggregates.class.isAssignableFrom(type) && !IQueryModelProvider.class.isAssignableFrom(type);
    }

    protected boolean isEntityAggregates(final Class type) {
	return EntityAggregates.class.isAssignableFrom(type);
    }

    protected boolean isSyntheticEntity(final Class type) {
	return AbstractEntity.class.isAssignableFrom(type) && IQueryModelProvider.class.isAssignableFrom(type);
    }

    public AbstractEntQuerySource(final String alias) {
	this.alias = alias;
    }

    public String getAlias() {
	return alias;
    }

    @Override
    public void addReferencingProp(final EntProp prop) {
	referencingProps.add(prop);
    }

    @Override
    public List<EntProp> getReferencingProps() {
	return referencingProps;
    }

    /**
     * If it looks like that given property name uses given source alias, then remove it.
     *
     * @param dotNotatedPropName
     * @param sourceAlias
     * @return
     */
    protected String dealiasPropName(final String dotNotatedPropName, final String sourceAlias) {
	return (sourceAlias == null || !dotNotatedPropName.startsWith(sourceAlias + ".")) ? dotNotatedPropName : dotNotatedPropName.substring(sourceAlias.length() + 1);
    }

    protected Pair<String, String> splitPropByFirstDot(final String dotNotatedPropName) {
	final int firstDotIndex = dotNotatedPropName.indexOf(".");
	if (firstDotIndex != -1) {
	    return new Pair<String, String>(dotNotatedPropName.substring(0, firstDotIndex), dotNotatedPropName.substring(firstDotIndex + 1));
	} else {
	    return new Pair<String, String>(dotNotatedPropName, null);
	}
    }

    protected Pair<Boolean, Class> lookForPropInRealPropType(final Class parentType, final String dotNotatedPropName) {
	try {
	    final Field field = Finder.findFieldByName(parentType, dotNotatedPropName);
	    return new Pair<Boolean, Class>(true, field.getType());
	} catch (final Exception e) {
	    return new Pair<Boolean, Class>(false, null);
	}
    }

    abstract Pair<Boolean, Class> lookForPropInEntAggregatesType(final Class parentType, final String dotNotatedPropName);

    protected Pair<Boolean, Class> lookForProp(final Class type, final String dotNotatedPropName) {
	if (isRealEntity(type)) {
	    return lookForPropInRealPropType(type, dotNotatedPropName);
	} else if (isEntityAggregates(type)) {
	    return lookForPropInEntAggregatesType(type, dotNotatedPropName);
	} else {
	    throw new RuntimeException("Not yet implemented the case of IQueryModelProvider");
	}
    }

    protected PropResolutionInfo propAsIs(final String propName) {
	final Pair<Boolean, Class> propAsIsSearchResult = lookForProp(getType(), propName);
	return propAsIsSearchResult.getKey() ? new PropResolutionInfo(null, propName, false, propAsIsSearchResult.getValue()) : null;
    }

    protected PropResolutionInfo propAsAliased(final String propName) {
	final String dealisedProp = dealiasPropName(propName, getAlias());
	final Pair<Boolean, Class> propAsAliasedSearchResult = lookForProp(getType(), dealisedProp);
	return propAsAliasedSearchResult.getKey() ? new PropResolutionInfo(getAlias(), dealisedProp, false, propAsAliasedSearchResult.getValue()) : null;
    }

    protected PropResolutionInfo propAsImplicitId(final String propName) {
	if (isRealEntity(getType())) {
	    return propName.equalsIgnoreCase(getAlias()) ? new PropResolutionInfo(getAlias(), null, true, Long.class) : null; // id property is meant here, but is it for all contexts?
	} else {
	    return null;
	}
    }

    @Override
    public Pair<Boolean, PropResolutionInfo> containsProperty(final EntProp prop) {
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

	final PropResolutionInfo propAsIs = propAsIs(prop.getName());
	final PropResolutionInfo propAsAliased = propAsAliased(prop.getName());
	final PropResolutionInfo propAsImplicitId = propAsImplicitId(prop.getName());

	if (propAsIs == null && propAsAliased == null && propAsImplicitId == null) {
	    return new Pair<Boolean, PropResolutionInfo>(false, null);
	} else if (propAsIs != null && propAsAliased == null) {
	    return new Pair<Boolean, PropResolutionInfo>(true, propAsIs);
	} else if (propAsAliased != null) {
	    return new Pair<Boolean, PropResolutionInfo>(true, propAsAliased);
	} else if (propAsImplicitId != null) {
	    return new Pair<Boolean, PropResolutionInfo>(true, propAsImplicitId);
	} else {
	    throw new RuntimeException("Unforeseen branch!");
	}
    }

    class PropResolutionInfo {
	String aliasPart;
	String propPart;
	boolean implicitId;
	Class propType;

	public PropResolutionInfo(final String aliasPart, final String propPart, final boolean implicitId, final Class propType) {
	    this.aliasPart = aliasPart;
	    this.propPart = propPart;
	    this.implicitId = implicitId;
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
    }
}