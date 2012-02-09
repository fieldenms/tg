package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.equery.IQueryModelProvider;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyPartOfKey;
import static ua.com.fielden.platform.utils.EntityUtils.isPropertyRequired;

public abstract class AbstractEntQuerySource implements IEntQuerySource {
    /**
     * Business name for query source. Can be also dot.notated, but should stick to property alias naming rules (e.g. no dots in beginning/end).
     */
    protected final String alias;
    /**
     * List of props implicitly associated with given source (e.g. dot.notation is supported)
     */
    private final List<PropResolutionInfo> referencingProps = new ArrayList<PropResolutionInfo>();

    /**
     * List of props explicitly associated with given source (e.g. each prop should have corresponding physically achievable item (sql column) within given source).
     */
    private final List<PropResolutionInfo> finalReferencingProps = new ArrayList<PropResolutionInfo>();
    /**
     * Sql alias for query source table/query
     */
    protected String sqlAlias;
    /**
     * Map between business name and sql column name.
     */
    protected Map<String, String> sourceColumns = new HashMap<String, String>();

    @Override
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

    @Override
    public String getAlias() {
	return alias;
    }

    @Override
    public void addReferencingProp(final PropResolutionInfo prop) {
	referencingProps.add(prop);
    }

    @Override
    public void addFinalReferencingProp(final PropResolutionInfo prop) {
	if (!prop.isImplicitId() && !prop.allExplicit()) {
	    throw new IllegalStateException("Property [" + prop.entProp + "] incorrectly resolved finally to source [" + this + "] as [" + prop + "]");
	}

	finalReferencingProps.add(prop);
	prop.entProp.setSql(sqlAlias + "." + sourceColumns.get(prop.prop.name));
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

    abstract Pair<PurePropInfo, Class> lookForProp(final String dotNotatedPropName);

    protected PropResolutionInfo propAsIs(final EntProp prop) {
	final Pair<PurePropInfo, Class> propAsIsSearchResult = lookForProp(prop.getName());
	if (propAsIsSearchResult != null/* && alias == null*/) {	// this condition will prevent usage of not-aliased properties if their source has alias
	    final String explicitPart = prop.getName().equals(propAsIsSearchResult.getKey().name + ".id") ? prop.getName() : propAsIsSearchResult.getKey().name;
	    return new PropResolutionInfo(prop, null, new PurePropInfo(prop.getName(), propAsIsSearchResult.getValue()), new PurePropInfo(explicitPart, propAsIsSearchResult.getKey().type));
	}
	return null;
    }

    protected PropResolutionInfo propAsAliased(final EntProp prop) {
	final String dealisedProp = dealiasPropName(prop.getName(), getAlias());
	if (dealisedProp == null) {
	    return null;
	} else {
	    final Pair<PurePropInfo, Class> propAsAliasedSearchResult = lookForProp(dealisedProp);
	    if (propAsAliasedSearchResult != null) {
		final String explicitPart = dealisedProp.equals(propAsAliasedSearchResult.getKey().name + ".id") ? dealisedProp : propAsAliasedSearchResult.getKey().name;
		return new PropResolutionInfo(prop, getAlias(), new PurePropInfo(dealisedProp, propAsAliasedSearchResult.getValue()), new PurePropInfo(explicitPart, propAsAliasedSearchResult.getKey().type));
	    }
	    return null;
	}
    }

    protected PropResolutionInfo propAsImplicitId(final EntProp prop) {
	if (isPersistedEntityType(sourceType())) {
	    return prop.getName().equalsIgnoreCase(getAlias()) ? new PropResolutionInfo(prop, getAlias(), new PurePropInfo(null, Long.class), new PurePropInfo("", null)) : null; // id property is meant here, but is it for all contexts?
	} else {
	    return null;
	}
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

	if (propAsIs == null && propAsAliased == null && propAsImplicitId == null) {
	    return null;
	} else if (propAsIs != null && propAsAliased == null) {
	    return propAsIs;
	} else if (propAsAliased != null) {
	    return propAsAliased;
	} else if (propAsImplicitId != null) {
	    return propAsImplicitId;
	} else {
	    throw new RuntimeException("Unforeseen branch!");
	}
    }

    protected String composeAlias(final String propAlias) {
	return alias == null ? propAlias : alias + "." + propAlias;
    }

    public static class PurePropInfo {
	String name;
	Class type;
	boolean nullable = false;

	public PurePropInfo(final String name, final Class type) {
	    super();
	    this.name = name;
	    this.type = type;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((name == null) ? 0 : name.hashCode());
	    result = prime * result + (nullable ? 1231 : 1237);
	    result = prime * result + ((type == null) ? 0 : type.hashCode());
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
	    if (!(obj instanceof PurePropInfo)) {
		return false;
	    }
	    final PurePropInfo other = (PurePropInfo) obj;
	    if (name == null) {
		if (other.name != null) {
		    return false;
		}
	    } else if (!name.equals(other.name)) {
		return false;
	    }
	    if (nullable != other.nullable) {
		return false;
	    }
	    if (type == null) {
		if (other.type != null) {
		    return false;
		}
	    } else if (!type.equals(other.type)) {
		return false;
	    }
	    return true;
	}


    }

    protected Map<PurePropInfo, List<EntProp>> determineGroups(final List<PropResolutionInfo> refProps) {
	final Map<PurePropInfo, List<EntProp>> result = new HashMap<PurePropInfo, List<EntProp>>();

	for (final PropResolutionInfo propResolutionInfo : refProps) {
	    if (!propResolutionInfo.allExplicit() && !propResolutionInfo.isImplicitId() && EntityUtils.isPersistedEntityType(propResolutionInfo.explicitProp.type)) {

		if (!result.containsKey(propResolutionInfo.explicitProp)) {
		    result.put(propResolutionInfo.explicitProp, new ArrayList<EntProp>());
		}

		result.get(propResolutionInfo.explicitProp).add(propResolutionInfo.entProp);
	    }
	}

	return result;
    }

    protected ConditionsModel joinCondition(final String leftProp, final String rightProp) {
	return new ConditionsModel(new ComparisonTestModel(new EntProp(leftProp, Long.class, null/*TEMP*/), ComparisonOperator.EQ, new EntProp(rightProp, Long.class, null/*TEMP*/)));
    }

    protected JoinType joinType(final boolean leftJoin) {
	return leftJoin ? JoinType.LJ : JoinType.IJ;
    }

    protected List<PropResolutionInfo> resolvePropsInternally(final List<EntProp> props) {
	final List<PropResolutionInfo> result = new ArrayList<PropResolutionInfo>();
	for (final EntProp prop : props) {
	    result.add(containsProperty(prop));
	}
	return result;
    }

    @Override
    public List<EntQueryCompoundSourceModel> generateMissingSources(final boolean parentLeftJoinLegacy, final List<PropResolutionInfo> refProps) {
	final List<EntQueryCompoundSourceModel> result = new ArrayList<EntQueryCompoundSourceModel>();
	final Map<PurePropInfo, List<EntProp>> groups = determineGroups(refProps);

	for (final Map.Entry<PurePropInfo, List<EntProp>> groupEntry : groups.entrySet()) {
	    final EntQuerySourceFromEntityType qrySource = new EntQuerySourceFromEntityType(groupEntry.getKey().type, composeAlias(groupEntry.getKey().name), true);

	    /*TEMP*/final boolean propLeftJoin = parentLeftJoinLegacy || //
			!isRequired(groupEntry.getKey().name);


	    result.add(new EntQueryCompoundSourceModel(qrySource, joinType(propLeftJoin), joinCondition(qrySource.getAlias(), qrySource.getAlias() + ".id")));
	    result.addAll(qrySource.generateMissingSources(propLeftJoin, qrySource.resolvePropsInternally(groupEntry.getValue())));
	}

	return result;
    }

    protected boolean isRequired(final String propName) {
	return isPropertyPartOfKey(sourceType(), propName) || isPropertyRequired(sourceType(), propName);
    }

    /**
     * Represent data structure to hold information about potential prop resolution against some query source.
     * @author TG Team
     *
     */
    public static class PropResolutionInfo {
	/**
	 * Reference to prop being resolved within some query source
	 */
	private EntProp entProp;
	/**
	 * Part of the property dot.notation, that has been recognised as source alias. Is null if property name doesn't start with source alias prefix.
	 */
	private String aliasPart;

	private PurePropInfo prop;

	private PurePropInfo explicitProp;

	/**
	 * Part of the property dot.notation, that has been recognised as purely prop (dot.notated) name (without source alias prefix).
	 */
	/**
	 * Actual java type of the property.
	 */
	/**
	 * Part of property part, that is explicitly present in given query source. For case of source-from-type it will always be just one-part property, which is equal to propPart; in case of source-from-model it can be several parts property, depending on actual yields of the source model behind.
	 */
	/**
	 * Java type of the explicit property
	 */

	public PropResolutionInfo(final EntProp entProp, final String aliasPart, final PurePropInfo prop, final PurePropInfo explicitProp) {
	    this.entProp = entProp;
	    if(entProp.getPropType() == null && prop.type != null) {
		entProp.setPropType(prop.type);
	    }
	    this.aliasPart = aliasPart;
	    this.prop = prop;
	    this.explicitProp = explicitProp;
	}

	public boolean isImplicitId() {
	    return prop.name == null && aliasPart != null;
	}

	public boolean allExplicit() {
	    return explicitProp.name.equals(prop.name) || !isPersistedEntityType(explicitProp.type);
	}

	@Override
	public String toString() {
	    return "\n\nPropResolutionInfo:\n aliasPart = " + aliasPart + "\n prop = " + prop + "\n explicitProp = " + explicitProp;
	}

	public Integer getPreferenceNumber() {
	    return isImplicitId() ? 2000 : prop.name.length();
	}

	public String getAliasPart() {
	    return aliasPart;
	}

	public EntProp getEntProp() {
	    return entProp;
	}

	public PurePropInfo getProp() {
	    return prop;
	}

	public PurePropInfo getExplicitProp() {
	    return explicitProp;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((aliasPart == null) ? 0 : aliasPart.hashCode());
	    result = prime * result + ((entProp == null) ? 0 : entProp.hashCode());
	    result = prime * result + ((explicitProp == null) ? 0 : explicitProp.hashCode());
	    result = prime * result + ((prop == null) ? 0 : prop.hashCode());
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
	    if (explicitProp == null) {
		if (other.explicitProp != null) {
		    return false;
		}
	    } else if (!explicitProp.equals(other.explicitProp)) {
		return false;
	    }
	    if (prop == null) {
		if (other.prop != null) {
		    return false;
		}
	    } else if (!prop.equals(other.prop)) {
		return false;
	    }
	    return true;
	}
  }
}