package ua.com.fielden.platform.serialisation.impl;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.Deflater;
import java.util.zip.InflaterInputStream;

import javax.swing.RowSorter.SortKey;

import org.apache.commons.collections.map.ListOrderedMap;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import ua.com.fielden.platform.attachment.Attachment;
import ua.com.fielden.platform.attachment.EntityAttachmentAssociation;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.equery.EntityAggregates;
import ua.com.fielden.platform.equery.LogicalOperator;
import ua.com.fielden.platform.equery.Ordering;
import ua.com.fielden.platform.equery.QueryModel;
import ua.com.fielden.platform.equery.QueryParameter;
import ua.com.fielden.platform.equery.QueryTokens;
import ua.com.fielden.platform.equery.fetch;
import ua.com.fielden.platform.equery.fetchAll;
import ua.com.fielden.platform.equery.interfaces.IQueryModel;
import ua.com.fielden.platform.equery.interfaces.IQueryOrderedModel;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;
import ua.com.fielden.platform.equery.tokens.conditions.BetweenCondition;
import ua.com.fielden.platform.equery.tokens.conditions.ComparisonOperation;
import ua.com.fielden.platform.equery.tokens.conditions.ComparisonWithoutArgumentOperation;
import ua.com.fielden.platform.equery.tokens.conditions.ConditionWithoutArgument;
import ua.com.fielden.platform.equery.tokens.conditions.ExistsCondition;
import ua.com.fielden.platform.equery.tokens.conditions.GroupCondition;
import ua.com.fielden.platform.equery.tokens.conditions.ImplicitOrGroupCondition;
import ua.com.fielden.platform.equery.tokens.conditions.ImplicitOrInWithModelsCondition;
import ua.com.fielden.platform.equery.tokens.conditions.InCondition;
import ua.com.fielden.platform.equery.tokens.conditions.PropertyToPropertyCondition;
import ua.com.fielden.platform.equery.tokens.main.ConditionsGroup;
import ua.com.fielden.platform.equery.tokens.main.GroupBy;
import ua.com.fielden.platform.equery.tokens.main.JoinConditions;
import ua.com.fielden.platform.equery.tokens.main.OrderBy;
import ua.com.fielden.platform.equery.tokens.main.QuerySource;
import ua.com.fielden.platform.equery.tokens.main.Select;
import ua.com.fielden.platform.equery.tokens.properties.AbstractQueryProperty;
import ua.com.fielden.platform.equery.tokens.properties.GroupByProperty;
import ua.com.fielden.platform.equery.tokens.properties.OrderByProperty;
import ua.com.fielden.platform.equery.tokens.properties.SearchProperty;
import ua.com.fielden.platform.equery.tokens.properties.SelectCalculatedProperty;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.serialisation.api.ISerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.ClassSerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.ColorSerializer;
import ua.com.fielden.platform.serialisation.impl.serialisers.DateTimeSerializer;
import ua.com.fielden.platform.serialisation.impl.serialisers.EntitySerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.IntervalSerializer;
import ua.com.fielden.platform.serialisation.impl.serialisers.MoneySerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.ProperyDescriptorSerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.QueryModelSerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.QueryTokensSerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.References;
import ua.com.fielden.platform.serialisation.impl.serialisers.ResultSerialiser;
import ua.com.fielden.platform.serialisation.impl.serialisers.SortKeySerialiser;
import ua.com.fielden.platform.serialisation.util.IoHelper;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeManager.PivotDomainTreeManagerSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeManagerAndEnhancer.PivotDomainTreeManagerAndEnhancerSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.analyses.impl.PivotDomainTreeRepresentation.PivotDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManager.AddToCriteriaTickManager;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManager.AddToCriteriaTickManager.AddToCriteriaTickManagerSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManager.CriteriaDomainTreeManagerSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeManagerAndEnhancer.CriteriaDomainTreeManagerAndEnhancerSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.CriteriaDomainTreeRepresentation.CriteriaDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeManager.AddToCriteriaTickManagerForLocator;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeManager.AddToCriteriaTickManagerForLocator.AddToCriteriaTickManagerForLocatorSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeManager.LocatorDomainTreeManagerSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeManagerAndEnhancer;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeManagerAndEnhancer.LocatorDomainTreeManagerAndEnhancerSerialiser;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeRepresentation;
import ua.com.fielden.platform.treemodel.rules.criteria.impl.LocatorDomainTreeRepresentation.LocatorDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.treemodel.rules.impl.AbstractDomainTree;
import ua.com.fielden.platform.treemodel.rules.impl.DomainTreeEnhancer;
import ua.com.fielden.platform.treemodel.rules.impl.DomainTreeEnhancer.DomainTreeEnhancerSerialiser;
import ua.com.fielden.platform.treemodel.rules.impl.LocatorManager;
import ua.com.fielden.platform.treemodel.rules.impl.LocatorManager.LocatorManagerSerialiser;
import ua.com.fielden.platform.treemodel.rules.master.impl.MasterDomainTreeManager;
import ua.com.fielden.platform.treemodel.rules.master.impl.MasterDomainTreeManager.MasterDomainTreeManagerSerialiser;
import ua.com.fielden.platform.types.ICategorizer;
import ua.com.fielden.platform.types.Money;
import ua.com.fielden.platform.ui.config.EntityCentreConfig;
import ua.com.fielden.platform.ui.config.EntityLocatorConfig;
import ua.com.fielden.platform.ui.config.EntityMasterConfig;
import ua.com.fielden.platform.ui.config.MainMenuItem;
import ua.com.fielden.platform.ui.config.MainMenuItemInvisibility;
import ua.com.fielden.platform.utils.EntityUtils;
import ua.com.fielden.platform.utils.Pair;

import com.esotericsoftware.kryo.Context;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.SerializationException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.serialize.BigDecimalSerializer;
import com.esotericsoftware.kryo.serialize.BigIntegerSerializer;
import com.esotericsoftware.kryo.serialize.BooleanSerializer;
import com.esotericsoftware.kryo.serialize.DateSerializer;
import com.esotericsoftware.kryo.serialize.IntSerializer;
import com.esotericsoftware.kryo.serialize.LongSerializer;
import com.esotericsoftware.kryo.serialize.ReferenceFieldSerializer;
import com.esotericsoftware.kryo.serialize.StringSerializer;
import com.google.inject.Inject;

/**
 * The descendant of {@link Kryo} with TG specific logic to correctly assign serialisers and recognise descendants of {@link AbstractEntity}. This covers correct determination of
 * the underlying entity type for dynamic CGLIB proxies.
 * <p>
 * All classes have to be registered with an instance of {@link TgKryo} at the server and client sides in the same order.
 *
 * @author TG Team
 *
 */
public class TgKryo extends Kryo implements ISerialiser {

    public static final String ENTITY_REFERENCES = "entity-references";

    /** Default buffer sizes for */
    private enum BUFFER_SIZE {
	QUERY(1024), // 1Kb
	DATA(1024 * 32), // 16Kb
	INSTANCE(1024 * 16); // 16Kb

	final int size;

	BUFFER_SIZE(final int size) {
	    this.size = size;
	}
    }

    private final EntityFactory factory;
    private final ISerialisationClassProvider provider;

    private final Serializer moneySerialiser;
    private final Serializer resultSerialiser;
    private final Serializer bigDecimalSerialiser;
    private final Serializer intSerialiser;
    private final Serializer longSerialiser;
    private final Serializer stringSerialiser;
    private final Serializer booleanSerialiser;
    private final Serializer dateSerialiser;
    private final Serializer pdSerialiser;
    private final Map<Class<AbstractEntity>, Serializer> entitySerialisers = Collections.synchronizedMap(new HashMap<Class<AbstractEntity>, Serializer>(600));
    private final Serializer querySerialiser;
    private final Serializer classSerialiser;
    private final Serializer queryTokensSerialiser;
    private final Serializer dateTimeSerialiser;
    private final Serializer bigIntegerSerialiser;
    private final Serializer intervalSerializer;
    private final Serializer colorSerializer;
    private final Serializer sortKeySerialiser;
    // "domain trees" serialisers
    private final Serializer locatorManagerSerialiser;
    private final Serializer domainTreeEnhancerSerialiser;
    private final Serializer criteriaDomainTreeRepresentationSerialiser;
    private final Serializer locatorDomainTreeRepresentationSerialiser;
    //    private final Serializer analysisDomainTreeRepresentationSerialiser;
    private final Serializer pivotDomainTreeRepresentationSerialiser;
    private final Serializer locatorDomainTreeManagerSerialiser;
    private final Serializer criteriaDomainTreeManagerSerialiser;
    private final Serializer masterDomainTreeManagerSerialiser;
    private final Serializer addToCriteriaTickManagerSerialiser;
    private final Serializer addToCriteriaTickManagerForLocatorSerialiser;
    //    private final Serializer analysisDomainTreeManagerSerialiser;
    private final Serializer pivotDomainTreeManagerSerialiser;
    private final Serializer locatorDomainTreeManagerAndEnhancerSerialiser;
    private final Serializer criteriaDomainTreeManagerAndEnhancerSerialiser;
    //    private final Serializer domainTreeManagerAndEnhancerSerialiser;
    //    private final Serializer analysisDomainTreeManagerAndEnhancerSerialiser;
    private final Serializer pivotDomainTreeManagerAndEnhancerSerialiser;

    @Inject
    public TgKryo(final EntityFactory factory, final ISerialisationClassProvider provider) {
	this.factory = factory;
	this.provider = provider;

	moneySerialiser = new MoneySerialiser(this);
	resultSerialiser = new ResultSerialiser(this);
	bigDecimalSerialiser = new BigDecimalSerializer();
	bigIntegerSerialiser = new BigIntegerSerializer();
	intSerialiser = new IntSerializer();
	longSerialiser = new LongSerializer();
	stringSerialiser = new StringSerializer();
	booleanSerialiser = new BooleanSerializer();
	dateSerialiser = new DateSerializer();
	dateTimeSerialiser = new DateTimeSerializer();
	pdSerialiser = new ProperyDescriptorSerialiser(factory);
	querySerialiser = new QueryModelSerialiser(this);
	classSerialiser = new ClassSerialiser(this);
	queryTokensSerialiser = new QueryTokensSerialiser(this);
	intervalSerializer = new IntervalSerializer();
	colorSerializer = new ColorSerializer();
	sortKeySerialiser = new SortKeySerialiser(this);
	// "domain trees" serialisers
	locatorManagerSerialiser = new LocatorManagerSerialiser(this);
	domainTreeEnhancerSerialiser = new DomainTreeEnhancerSerialiser(this);
	criteriaDomainTreeRepresentationSerialiser = new CriteriaDomainTreeRepresentationSerialiser(this);
	//	analysisDomainTreeRepresentationSerialiser = new AnalysisDomainTreeRepresentationSerialiser(this);
	pivotDomainTreeRepresentationSerialiser = new PivotDomainTreeRepresentationSerialiser(this);
	locatorDomainTreeManagerSerialiser = new LocatorDomainTreeManagerSerialiser(this);
	criteriaDomainTreeManagerSerialiser = new CriteriaDomainTreeManagerSerialiser(this);
	masterDomainTreeManagerSerialiser = new MasterDomainTreeManagerSerialiser(this);
	locatorDomainTreeRepresentationSerialiser = new LocatorDomainTreeRepresentationSerialiser(this);
	addToCriteriaTickManagerForLocatorSerialiser = new AddToCriteriaTickManagerForLocatorSerialiser(this);
	addToCriteriaTickManagerSerialiser = new AddToCriteriaTickManagerSerialiser(this);
	//	analysisDomainTreeManagerSerialiser = new AnalysisDomainTreeManagerSerialiser(this);
	pivotDomainTreeManagerSerialiser = new PivotDomainTreeManagerSerialiser(this);
	locatorDomainTreeManagerAndEnhancerSerialiser = new LocatorDomainTreeManagerAndEnhancerSerialiser(this);
	criteriaDomainTreeManagerAndEnhancerSerialiser = new CriteriaDomainTreeManagerAndEnhancerSerialiser(this);
	//	domainTreeManagerAndEnhancerSerialiser = new DomainTreeManagerAndEnhancerSerialiser(this);
	//	analysisDomainTreeManagerAndEnhancerSerialiser = new AnalysisDomainTreeManagerAndEnhancerSerialiser(this);
	pivotDomainTreeManagerAndEnhancerSerialiser = new PivotDomainTreeManagerAndEnhancerSerialiser(this);

	// the following order of class registration is important
	register(String[].class);
	register(byte[].class);
	register(Date.class);
	register(DateTime.class);
	register(BigDecimal.class);
	register(BigInteger.class);
	register(Currency.class);
	register(ArrayList.class);
	register(List.class);
	register(HashMap.class);
	register(HashSet.class);
	register(TreeSet.class);
	register(Set.class);
	register(Money.class);
	register(Result.class);
	register(Warning.class);
	register(Pair.class);
	register(Color.class);
	// default platform entities
	register(PropertyDescriptor.class);
	register(User.class);
	register(UserAndRoleAssociation.class);
	register(UserRole.class);
	register(SecurityRoleAssociation.class);
	register(ISecurityToken.class);
	register(Attachment.class);
	register(EntityAttachmentAssociation.class);
	register(EntityAggregates.class);
	// life cycle classes
	register(ICategorizer.class);
	register(LifecycleModel.class);
	register(EntityPropertyLifecycle.class);
	register(ValuedInterval.class);
	register(Interval.class);
	// entity query classes
	register(fetch.class);
	register(fetchAll.class);
	register(IQueryOrderedModel.class);
	register(IQueryModel.class);
	register(QueryModel.class);
	register(QueryTokens.class);
	register(AbstractQueryProperty.EntityProperty.class);
	register(AbstractQueryProperty.ExpressionString.class);
	register(Select.class);
	register(SelectCalculatedProperty.class);
	register(ConditionsGroup.class);
	register(GroupCondition.class);
	register(SearchProperty.class);
	register(LogicalOperator.class);
	register(ComparisonOperation.class);
	register(GroupBy.class);
	register(GroupByProperty.class);
	register(OrderBy.class);
	register(OrderByProperty.class);
	register(Ordering.class);
	register(JoinConditions.class);
	register(QuerySource.class);
	register(Class.class);
	register(BetweenCondition.class);
	register(ConditionWithoutArgument.class);
	register(ComparisonWithoutArgumentOperation.class);
	register(ImplicitOrGroupCondition.class);
	register(QueryParameter.class);
	register(ImplicitOrInWithModelsCondition.class);
	register(InCondition.class);
	register(PropertyToPropertyCondition.class);
	register(ExistsCondition.class);
	// "domain tree" serialisers
	registerDomainTreeTypes();
	// register menu and configuration related
	register(MainMenuItem.class);
	register(MainMenuItemInvisibility.class);
	register(EntityCentreConfig.class);
	register(EntityMasterConfig.class);
	register(EntityLocatorConfig.class);

	// register classes provided by the provider
	for (final Class<?> type : provider.classes()) {
	    try {
		register(type);
	    } catch (final IllegalArgumentException e) {
		e.printStackTrace();
		throw new IllegalArgumentException("The type [" + type + "] can not be registered. Cause = [" + e.getMessage() + "]");
	    }
	}
    }

    /**
     * Registers all necessary "domain tree" types, including inner classes, enums etc.
     */
    protected void registerDomainTreeTypes() {
	// should be registered before other types, that reference on LocatorManager type
	register(LocatorManager.class);
	for (final Class<?> type : typesForRegistration("../platform-pojo-bl/target/classes", "ua.com.fielden.platform.treemodel.rules", AbstractDomainTree.DOMAIN_TREE_TYPES)) {
	    register(type);
	}
    }

    /**
     * Returns all types (including inner classes, enumeration values etc.) to be registered for all <code>baseTypes</code> in specified [path; package].
     *
     * @param path
     * @param packageName
     * @param baseTypes
     * @return
     */
    public static List<Class<?>> typesForRegistration(final String path, final String packageName, final List<Class<?>> baseTypes) {
	final List<Class<?>> types = new ArrayList<Class<?>>();
	for (final Class<?> type : baseTypes) {
	    types.add(type);
	    if (!EntityUtils.isEnum(type)) {
		try {
		    types.addAll(ClassesRetriever.getAllClassesInPackageDerivedFrom(path, packageName, type));
		} catch (final Exception e) {
		    e.printStackTrace();
		    throw new IllegalStateException("Retrieval of the [" + type.getSimpleName() + "] descendants from [" + path + "; " + packageName + "] has been failed.");
		}
	    }
	}
	final List<Class<?>> theTypes = new ArrayList<Class<?>>();
	for (final Class<?> type : types) {
	    if (EntityUtils.isEnum(type)) {
		final List<Class<?>> enumTypes = EntityUtils.extractTypes((Class<Enum>)type);
		for (final Class klass : enumTypes) {
		    theTypes.add(klass);
		}
	    } else if (!AbstractEntity.class.equals(type)) {
		theTypes.add(type);
	    }
	}
	return theTypes;
    }

    @Override
    public final RegisteredClass getRegisteredClass(final Class type) {
	if (AbstractEntity.class.isAssignableFrom(type)) {
	    return super.getRegisteredClass(PropertyTypeDeterminator.stripIfNeeded(type));
	} else if (Set.class.isAssignableFrom(type) && type.isInterface()) { //
	    return super.getRegisteredClass(HashSet.class);
	} else if (List.class.isAssignableFrom(type) && type.isInterface()) {
	    return super.getRegisteredClass(ArrayList.class);
	} else if (Map.class.isAssignableFrom(type) && !ListOrderedMap.class.isAssignableFrom(type) && type.isInterface()) {
	    return super.getRegisteredClass(HashMap.class);
	} else if (Date.class.isAssignableFrom(type)) {
	    return super.getRegisteredClass(Date.class);
	}
	return super.getRegisteredClass(type);
    }

    /**
     * Creates serialisers for entity types in such a way that each seprate type has its own serialiser.
     *
     * This allows for type specific serialisation optimisation such as caching of property information etc.
     */
    @Override
    public Serializer newSerializer(final Class type) {
	if (Result.class.isAssignableFrom(type)) {
	    return resultSerialiser;
	} else if (PropertyDescriptor.class.isAssignableFrom(type)) { // PropertyDescriptor must alway be checked before any other entity
	    return pdSerialiser;
	} else if (AbstractEntity.class.isAssignableFrom(type)) {
	    final Class<AbstractEntity> entityType = (Class<AbstractEntity>) PropertyTypeDeterminator.stripIfNeeded(type);
	    Serializer ser = entitySerialisers.get(entityType);
	    if (ser == null) {
		ser = new EntitySerialiser(this, entityType, factory);
		entitySerialisers.put(entityType, ser);
	    }
	    return ser;
	} else if (Money.class.isAssignableFrom(type)) {
	    return moneySerialiser;
	} else if (String.class.isAssignableFrom(type)) {
	    return stringSerialiser;
	} else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
	    return longSerialiser;
	} else if (Date.class.isAssignableFrom(type)) {
	    return dateSerialiser;
	} else if (BigDecimal.class.isAssignableFrom(type)) {
	    return bigDecimalSerialiser;
	} else if (BigInteger.class.isAssignableFrom(type)) {
	    return bigIntegerSerialiser;
	} else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
	    return intSerialiser;
	} else if (Boolean.class.isAssignableFrom(type) || boolean.class.isAssignableFrom(type)) {
	    return booleanSerialiser;
	} else if (DateTime.class.isAssignableFrom(type)) {
	    return dateTimeSerialiser;
	} else if (IQueryOrderedModel.class.isAssignableFrom(type)) {
	    return querySerialiser;
	} else if (QueryTokens.class.isAssignableFrom(type)) {
	    return queryTokensSerialiser;
	} else if (Class.class.isAssignableFrom(type)) {
	    return classSerialiser;
	} else if (Interval.class.isAssignableFrom(type)) {
	    return intervalSerializer;
	} else if (Color.class.isAssignableFrom(type)) {
	    return colorSerializer;
	} else if (SortKey.class.isAssignableFrom(type)) {
	    return sortKeySerialiser;
	} else if (DomainTreeEnhancer.class.isAssignableFrom(type)) { // "domain tree" serialisers
	    return domainTreeEnhancerSerialiser;
	} else if (LocatorManager.class.isAssignableFrom(type)) {
	    return locatorManagerSerialiser;
	} else if (LocatorDomainTreeRepresentation.class.isAssignableFrom(type)) { // higher in hierarchy above CriteriaDomainTreeRepresentation!
	    return locatorDomainTreeRepresentationSerialiser;
	} else if (CriteriaDomainTreeRepresentation.class.isAssignableFrom(type)) {
	    return criteriaDomainTreeRepresentationSerialiser;
	    //	} else if (AnalysisDomainTreeRepresentation.class.isAssignableFrom(type)) {
	    //	    return analysisDomainTreeRepresentationSerialiser;
	} else if (PivotDomainTreeRepresentation.class.isAssignableFrom(type)) {
	    return pivotDomainTreeRepresentationSerialiser;
	} else if (LocatorDomainTreeManager.class.isAssignableFrom(type)) { // higher in hierarchy above CriteriaDomainTreeManager!
	    return locatorDomainTreeManagerSerialiser;
	} else if (CriteriaDomainTreeManager.class.isAssignableFrom(type)) {
	    return criteriaDomainTreeManagerSerialiser;
	} else if (MasterDomainTreeManager.class.isAssignableFrom(type)) {
	    return masterDomainTreeManagerSerialiser;
	} else if (AddToCriteriaTickManagerForLocator.class.isAssignableFrom(type)) {
	    return addToCriteriaTickManagerForLocatorSerialiser;
	} else if (AddToCriteriaTickManager.class.isAssignableFrom(type)) {
	    return addToCriteriaTickManagerSerialiser;
	} else if (PivotDomainTreeManager.class.isAssignableFrom(type)) {
	    return pivotDomainTreeManagerSerialiser;
	} else if (LocatorDomainTreeManagerAndEnhancer.class.isAssignableFrom(type)) { // higher in hierarchy above CriteriaDomainTreeManagerAndEnhancer!
	    return locatorDomainTreeManagerAndEnhancerSerialiser;
	} else if (CriteriaDomainTreeManagerAndEnhancer.class.isAssignableFrom(type)) {
	    return criteriaDomainTreeManagerAndEnhancerSerialiser;
	} else if (PivotDomainTreeManagerAndEnhancer.class.isAssignableFrom(type)) {
	    return pivotDomainTreeManagerAndEnhancerSerialiser;
	    //	} else if (AnalysisDomainTreeManagerAndEnhancer.class.isAssignableFrom(type)) {
	    //	    return analysisDomainTreeManagerAndEnhancerSerialiser;
	}
	return super.newSerializer(type);
    }

    @Override
    protected Serializer newDefaultSerializer(final Class type) {
	return new ReferenceFieldSerializer(this, type);
    }

    @Override
    public <T> T newInstance(final Class<T> type) {
	if (Set.class.isAssignableFrom(type) && type.isInterface()) {
	    return (T) new HashSet();
	} else if (List.class.isAssignableFrom(type) && type.isInterface()) {
	    return (T) new ArrayList();
	} else if (Map.class.isAssignableFrom(type) && type.isInterface()) {
	    return (T) new HashMap();
	}
	return super.newInstance(type);
    }

    /**
     * Writes an object and resizes the buffer in case if its limit is exceeded.
     *
     * @param writeBuffer
     * @param obj
     * @return
     */
    private ByteBuffer safeWrite(final ByteBuffer writeBuffer, final Object obj) {
	ByteBuffer buffer = writeBuffer;
	final Class<?> type = PropertyTypeDeterminator.stripIfNeeded(obj.getClass());
	boolean keepWriting = true;
	while (keepWriting) {
	    try {
		writeClass(buffer, type);
		writeObject(buffer, obj);
		keepWriting = false;
	    } catch (final SerializationException e) {
		if (e.getMessage().startsWith("Buffer limit exceeded")) {
		    buffer = ByteBuffer.allocate(buffer.capacity() * 2);
		} else {
		    throw e;
		}
	    }
	}
	return buffer;
    }

    @Override
    public byte[] serialise(final Object obj) {
	//	int size;
	//	if (obj instanceof IQueryOrderedModel) {
	//	    size = BUFFER_SIZE.QUERY.size;
	//	} else if (((obj instanceof Result) && ((Result) obj).getInstance() instanceof List) || obj instanceof Collection) {
	//	    size = BUFFER_SIZE.DATA.size;
	//	} else {
	//	    size = BUFFER_SIZE.INSTANCE.size;
	//	}

	final ByteBuffer writeBuffer = safeWrite(ByteBuffer.allocate(BUFFER_SIZE.DATA.size), obj);
	writeBuffer.flip();
	final byte[] data = new byte[writeBuffer.limit()];
	writeBuffer.get(data);
	writeBuffer.clear();

	// Compress the bytes
	final Deflater compressor = new Deflater(Deflater.BEST_COMPRESSION);
	compressor.setInput(data);
	compressor.finish();
	final ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);

	final byte[] buf = new byte[1024];
	while (!compressor.finished()) {
	    final int count = compressor.deflate(buf);
	    bos.write(buf, 0, count);
	}
	try {
	    bos.close();
	} catch (final IOException e) {
	}

	// return the compressed data
	return bos.toByteArray();
    }

    @Override
    public <T> T deserialise(final byte[] content, final Class<T> type) throws Exception {
	final ByteArrayInputStream bis = new ByteArrayInputStream(content);
	return deserialise(bis, type);
    }

    @Override
    public <T> T deserialise(final InputStream content, final Class<T> type) throws Exception {
	final InflaterInputStream in = new InflaterInputStream(content);
	final ByteBuffer readBuffer = IoHelper.readAsByteBuffer(in);
	try {
	    final Class<?> serialisedType = readClass(readBuffer).getType();
	    final T result = (T) readObject(readBuffer, serialisedType);
	    executeDefiners();
	    return result;
	} finally {
	    readBuffer.clear();
	}
    }

    /**
     * All entity instances are cached during deserialisation.
     *
     * Once serialisation is completed it is necessary to execute respective definers for all cached instances.
     *
     * Definers cannot be executed inside {@link EntitySerialiser} due to the use of cache in conjunction with sub-requests issued by some of the definers leasing to an incorrect
     * deserialisation (specifically, object identifiers in cache get mixed up with the ones from newly obtained stream of data).
     *
     */
    private void executeDefiners() {
	final Context context = getContext();
	final References references = (References) context.get(ENTITY_REFERENCES);
	if (references != null) {
	    // references is thread local variable, which gets reset if a nested deserialisation happens
	    // therefore need to make a local cache of the present in references entities
	    final List<AbstractEntity<?>> refs = new ArrayList<AbstractEntity<?>>(references.referenceCount);

	    for (int index = 2; index < 2 + references.referenceCount; index++) {
		final Object obj = references.referenceToObject.get(index);
		// interested only in instances of the enhanced AbstractEntity.
		if (obj instanceof AbstractEntity) {
		    refs.add((AbstractEntity) obj);
		}
	    }
	    // explicit reset in order to make the reason for the above snippet more explicit
	    references.reset();

	    // iterate through all locally cached entity instances and execute respective definers
	    for (final AbstractEntity<?> entity : refs) {
		entity.setInitialising(true);
		for (final Object mt : entity.getProperties().values()) {
		    final MetaProperty prop = (MetaProperty) mt;
		    if (prop != null) {
			if (!prop.isCollectional()) {
			    prop.define(prop.getOriginalValue());
			}
		    }
		}
		entity.setInitialising(false);
	    }
	}
    }

    public static void main(final String[] args) throws ClassNotFoundException {
	System.out.println(Class.forName("com.esotericsoftware.kryo.util.IntHashMap$Entry"));

	// final String path = "../platform-pojo-bl/target/classes", packageName = "ua.com.fielden.platform.treemodel.rules";
	// try {
	// 	System.out.println(ClassesRetriever.getAllClassesInPackageDerivedFrom(path, packageName, ITickManager.class));
	// } catch (final Exception e) {
	// 	e.printStackTrace();
	// 	throw new IllegalStateException("Retrieval of the [" + ITickManager.class.getSimpleName() + "] descendants from [" + path + "; " + packageName
	//	+ "] has been failed.");
	// }
    }
}
