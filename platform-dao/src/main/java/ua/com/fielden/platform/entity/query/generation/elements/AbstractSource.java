package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hibernate.Hibernate;

import ua.com.fielden.platform.dao2.DomainPersistenceMetadata;
import ua.com.fielden.platform.dao2.PropertyPersistenceInfo;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;
import static ua.com.fielden.platform.utils.EntityUtils.isPersistedEntityType;

public abstract class AbstractSource implements ISource {
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
     * Map between source properties business names and persistence infos.
     */
    protected Map<String, PropertyPersistenceInfo> sourceItems = new HashMap<String, PropertyPersistenceInfo>();

    /**
     * Reference to mappings generator instance - used for acquiring properties persistence infos.
     */
    private final DomainPersistenceMetadata domainPersistenceMetadata;

    private boolean nullable;

    public void assignNullability(final boolean nullable) {
	this.nullable = nullable;
    }

    @Override
    public void assignSqlAlias(final String sqlAlias) {
	this.sqlAlias = sqlAlias;
    }

    public AbstractSource(final String alias, final DomainPersistenceMetadata domainPersistenceMetadata) {
	this.alias = alias;
	this.domainPersistenceMetadata = domainPersistenceMetadata;
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
	if (!prop.allExplicit()) {
	    throw new IllegalStateException("Property [" + prop.entProp + "] incorrectly resolved finally to source [" + this + "] as [" + prop + "]");
	}

	finalReferencingProps.add(prop);
	// TODO implement more transparently
	prop.entProp.setSource(this);
	prop.entProp.setSql(sourceItems.get((prop.prop.name.endsWith(".id") ? prop.prop.name.substring(0, prop.prop.name.length() - 3) : prop.prop.name)).getColumn());
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

    protected abstract Pair<PurePropInfo, PurePropInfo> lookForProp(final String dotNotatedPropName);

    protected PropResolutionInfo propAsIs(final EntProp prop) {
	final Pair<PurePropInfo, PurePropInfo> propAsIsSearchResult = lookForProp(prop.getName());
	if (propAsIsSearchResult != null/* && alias == null*/) {	// this condition will prevent usage of not-aliased properties if their source has alias
	    return new PropResolutionInfo(prop, null, propAsIsSearchResult.getValue(), propAsIsSearchResult.getKey());
	}
	return null;
    }

    protected PropResolutionInfo propAsAliased(final EntProp prop) {
	final String dealisedProp = dealiasPropName(prop.getName(), getAlias());
	if (dealisedProp == null) {
	    return null;
	} else {
	    final Pair<PurePropInfo, PurePropInfo> propAsAliasedSearchResult = lookForProp(dealisedProp);
	    if (propAsAliasedSearchResult != null) {
		return new PropResolutionInfo(prop, getAlias(), propAsAliasedSearchResult.getValue(), propAsAliasedSearchResult.getKey());
	    }
	    return null;
	}
    }

    protected PropResolutionInfo propAsImplicitId(final EntProp prop) {
	if (isPersistedEntityType(sourceType()) && prop.getName().equalsIgnoreCase(getAlias())) {
	    final PurePropInfo idProp = new PurePropInfo("id", Long.class, Hibernate.LONG, false || isNullable());
	    return new PropResolutionInfo(prop, getAlias(), idProp, idProp, true); // id property is meant here, but is it for all contexts?
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

    public static class PurePropInfo implements Comparable<PurePropInfo> {
	private String name;
	private Class type;
	private Object hibType;
	boolean nullable = false;

	public PurePropInfo(final String name, final Class type, final Object hibType, final boolean nullable) {
	    super();
	    this.name = name;
	    this.type = type;
	    this.hibType = hibType;
	    this.nullable = nullable;
	}

	@Override
	public String toString() {
	    return name + ":: " + (type != null ? type.getSimpleName() : type) + " :: " + (hibType != null ? hibType.getClass().getSimpleName() : null) + " :: " + nullable;
	}

	@Override
	public int compareTo(final PurePropInfo o) {
	    return name.compareTo(o.getName());
	}

	public String getName() {
	    return name;
	}

	public Class getType() {
	    return type;
	}

	public boolean isNullable() {
	    return nullable;
	}

	public Object getHibType() {
	    return hibType;
	}

	@Override
	public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + ((hibType == null) ? 0 : hibType.hashCode());
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
	    if (hibType == null) {
		if (other.hibType != null) {
		    return false;
		}
	    } else if (!hibType.equals(other.hibType)) {
		return false;
	    }
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

    protected SortedMap<PurePropInfo, List<EntProp>> determineGroups(final List<PropResolutionInfo> refProps) {
	final SortedMap<PurePropInfo, List<EntProp>> result = new TreeMap<PurePropInfo, List<EntProp>>();

	for (final PropResolutionInfo propResolutionInfo : refProps) {
	    if (!propResolutionInfo.allExplicit() && EntityUtils.isPersistedEntityType(propResolutionInfo.explicitProp.type)) {

		if (!result.containsKey(propResolutionInfo.explicitProp)) {
		    result.put(propResolutionInfo.explicitProp, new ArrayList<EntProp>());
		}

		result.get(propResolutionInfo.explicitProp).add(propResolutionInfo.entProp);
	    }
	}

	return result;
    }

    protected Conditions joinCondition(final String leftProp, final String rightProp) {
	//TODO provide proper hibType and holder while instantiating EntProps.
	return new Conditions(new ComparisonTest(new EntProp(leftProp, Long.class, null, null/*TEMP*/), ComparisonOperator.EQ, new EntProp(rightProp, Long.class, null, null/*TEMP*/)));
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
    public List<CompoundSource> generateMissingSources(final List<PropResolutionInfo> refProps) {
	final List<CompoundSource> result = new ArrayList<CompoundSource>();

	final SortedMap<PurePropInfo, List<EntProp>> groups = determineGroups(refProps);

	for (final Map.Entry<PurePropInfo, List<EntProp>> groupEntry : groups.entrySet()) {
	    final TypeBasedSource qrySource = new TypeBasedSource(groupEntry.getKey().type, composeAlias(groupEntry.getKey().name), true, domainPersistenceMetadata);
	    qrySource.populateSourceItems(groupEntry.getKey().nullable);
	    qrySource.assignNullability(groupEntry.getKey().nullable);
	    result.add(new CompoundSource(qrySource, joinType(groupEntry.getKey().nullable), joinCondition(qrySource.getAlias(), qrySource.getAlias() + ".id")));
	    result.addAll(qrySource.generateMissingSources(qrySource.resolvePropsInternally(groupEntry.getValue())));
	}

	return result;
    }

    public DomainPersistenceMetadata getDomainPersistenceMetadata() {
        return domainPersistenceMetadata;
    }

    /**
     * Represent data structure to hold information about potential prop resolution against some query source.
     * @author TG Team
     *
     */
    public static class PropResolutionInfo {

	private final boolean implicitId;
	/**
	 * Reference to prop being resolved within some query source
	 */
	private EntProp entProp;
	/**
	 * Part of the property dot.notation, that has been recognised as source alias. Is null if property name doesn't start with source alias prefix.
	 */
	private String aliasPart;
	/**
	 * Part of the property dot.notation, that has been recognised as purely prop (dot.notated) name (without source alias prefix).
	 */
	private PurePropInfo prop;
	/**
	 * Part of property part, that is explicitly present in given query source. For case of source-from-type it will always be just one-part property, which is equal to propPart; in case of source-from-model it can be several parts property, depending on actual yields of the source model behind.
	 */
	private PurePropInfo explicitProp;

	public PropResolutionInfo(final EntProp entProp, final String aliasPart, final PurePropInfo prop, final PurePropInfo explicitProp, final boolean implicitId) {
	    this.entProp = entProp;
	    if(entProp.getPropType() == null && prop.type != null) {
		entProp.setPropType(prop.type);
	    }
	    if(entProp.getHibType() == null && prop.hibType != null) {
		entProp.setHibType(prop.hibType);
	    }

	    entProp.setNullable(prop.isNullable());

	    this.aliasPart = aliasPart;
	    this.prop = prop;
	    this.explicitProp = explicitProp;
	    this.implicitId = implicitId;
	}

	public PropResolutionInfo(final EntProp entProp, final String aliasPart, final PurePropInfo prop, final PurePropInfo explicitProp) {
	    this(entProp, aliasPart, prop, explicitProp, false);
	}

	public boolean allExplicit() {
	    return implicitId || //
		    explicitProp.name.equals(prop.name) || //
		    (explicitProp.name + ".id").equals(prop.name);
	}

	@Override
	public String toString() {
	    return "PRI: entProp = " + entProp + "; aliasPart = " + aliasPart + "; prop = " + prop + "; explicitProp = " + explicitProp + "; implicitId = " + implicitId;
	}

	public Integer getPreferenceNumber() {
	    return implicitId ? 2000 : prop.name.length();
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
	    result = prime * result + (implicitId ? 1231 : 1237);
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
	    if (implicitId != other.implicitId) {
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

    public boolean isNullable() {
        return nullable;
    }

    public String getSqlAlias() {
        return sqlAlias;
    }
}