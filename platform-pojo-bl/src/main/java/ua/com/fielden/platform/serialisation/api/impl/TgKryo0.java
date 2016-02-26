package ua.com.fielden.platform.serialisation.api.impl;

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
import ua.com.fielden.platform.dao.QueryExecutionModel;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeManager.AnalysisDomainTreeManagerSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.AnalysisDomainTreeRepresentation.AnalysisDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeManager.LifecycleDomainTreeManagerSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.LifecycleDomainTreeRepresentation.LifecycleDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.MultipleDecDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.MultipleDecDomainTreeManager.MultipleDecDomainTreeManagerSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.MultipleDecDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.MultipleDecDomainTreeRepresentation.MultipleDecDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeManager.PivotDomainTreeManagerSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.PivotDomainTreeRepresentation.PivotDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeManager;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeManager.SentinelDomainTreeManagerSerialiser;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.analyses.impl.SentinelDomainTreeRepresentation.SentinelDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager0;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager0.AddToCriteriaTickManager0;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager0.AddToCriteriaTickManager0.AddToCriteriaTickManager0Serialiser;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManager0.CentreDomainTreeManager0Serialiser;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer0;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeManagerAndEnhancer0.CentreDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.CentreDomainTreeRepresentation.CentreDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManager0;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManager0.AddToCriteriaTickManagerForLocator0;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManager0.AddToCriteriaTickManagerForLocator0.AddToCriteriaTickManagerForLocator0Serialiser;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManager0.LocatorDomainTreeManager0Serialiser;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer0;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeManagerAndEnhancer0.LocatorDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeRepresentation;
import ua.com.fielden.platform.domaintree.centre.impl.LocatorDomainTreeRepresentation.LocatorDomainTreeRepresentationSerialiser;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer0;
import ua.com.fielden.platform.domaintree.impl.DomainTreeEnhancer0.DomainTreeEnhancer0Serialiser;
import ua.com.fielden.platform.domaintree.impl.LocatorManager0;
import ua.com.fielden.platform.domaintree.impl.LocatorManager0.LocatorManager0Serialiser;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager0;
import ua.com.fielden.platform.domaintree.master.impl.MasterDomainTreeManager0.MasterDomainTreeManager0Serialiser;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.meta.MetaProperty;
import ua.com.fielden.platform.entity.meta.PropertyDescriptor;
import ua.com.fielden.platform.entity.query.DynamicallyTypedQueryContainer;
import ua.com.fielden.platform.entity.query.EntityAggregates;
import ua.com.fielden.platform.entity.query.fluent.ArithmeticalOperator;
import ua.com.fielden.platform.entity.query.fluent.ComparisonOperator;
import ua.com.fielden.platform.entity.query.fluent.Functions;
import ua.com.fielden.platform.entity.query.fluent.JoinType;
import ua.com.fielden.platform.entity.query.fluent.LogicalOperator;
import ua.com.fielden.platform.entity.query.fluent.QueryTokens;
import ua.com.fielden.platform.entity.query.fluent.TokenCategory;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.entity.query.model.ExpressionModel;
import ua.com.fielden.platform.entity.query.model.OrderingModel;
import ua.com.fielden.platform.entity.query.model.PrimitiveResultQueryModel;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.equery.lifecycle.EntityPropertyLifecycle;
import ua.com.fielden.platform.equery.lifecycle.LifecycleModel;
import ua.com.fielden.platform.equery.lifecycle.LifecycleQueryContainer;
import ua.com.fielden.platform.equery.lifecycle.ValuedInterval;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.error.Warning;
import ua.com.fielden.platform.reflection.ClassesRetriever;
import ua.com.fielden.platform.reflection.PropertyTypeDeterminator;
import ua.com.fielden.platform.reflection.asm.impl.DynamicEntityClassLoader;
import ua.com.fielden.platform.report.query.generation.AnalysisResultClass;
import ua.com.fielden.platform.security.ISecurityToken;
import ua.com.fielden.platform.security.user.SecurityRoleAssociation;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.security.user.UserAndRoleAssociation;
import ua.com.fielden.platform.security.user.UserRole;
import ua.com.fielden.platform.serialisation.api.ISerialisationClassProvider;
import ua.com.fielden.platform.serialisation.api.ISerialiser0;
import ua.com.fielden.platform.serialisation.api.ISerialiserEngine;
import ua.com.fielden.platform.serialisation.api.SerialiserEngines;
import ua.com.fielden.platform.serialisation.kryo.IoHelper;
import ua.com.fielden.platform.serialisation.kryo.serialisers.ClassSerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.ColorSerializer;
import ua.com.fielden.platform.serialisation.kryo.serialisers.DateTimeSerializer;
import ua.com.fielden.platform.serialisation.kryo.serialisers.DynamicallyTypedQueryContainerSerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.EntitySerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.IntervalSerializer;
import ua.com.fielden.platform.serialisation.kryo.serialisers.LifecycleQueryContainerSerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.MoneySerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.ProperyDescriptorSerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.References;
import ua.com.fielden.platform.serialisation.kryo.serialisers.ResultSerialiser;
import ua.com.fielden.platform.serialisation.kryo.serialisers.SortKeySerialiser;
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

/**
 * WARNING: this is an OLD version!
 *
 * @author TG Team
 *
 */
@Deprecated
class TgKryo0 extends Kryo implements ISerialiserEngine {
    public static final String ENTITY_REFERENCES = "entity-references";

    /** Default buffer sizes for */
    private enum BUFFER_SIZE {
        QUERY(1024), // 1Kb
        DATA(1024 * 32), // 32Kb
        INSTANCE(1024 * 16); // 16Kb

        final int size;

        BUFFER_SIZE(final int size) {
            this.size = size;
        }
    }

    private final Serialiser0 serialiser;
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
    private final Serializer classSerialiser;
    private final Serializer dateTimeSerialiser;
    private final Serializer bigIntegerSerialiser;
    private final Serializer intervalSerializer;
    private final Serializer colorSerializer;
    private final Serializer sortKeySerialiser;
    // "domain trees" serialisers
    private final Serializer locatorManager0Serialiser;
    private final Serializer domainTreeEnhancer0Serialiser;
    private final Serializer centreDomainTreeRepresentationSerialiser;
    private final Serializer locatorDomainTreeRepresentationSerialiser;
    private final Serializer pivotDomainTreeRepresentationSerialiser;
    private final Serializer analysisDomainTreeRepresentationSerialiser;
    private final Serializer sentinelDomainTreeRepresentationSerialiser;
    private final Serializer lifecycleDomainTreeRepresentationSerialiser;
    private final Serializer multipleDecDomainTreeRepresentationSerialiser;
    private final Serializer locatorDomainTreeManager0Serialiser;
    private final Serializer centreDomainTreeManager0Serialiser;
    private final Serializer masterDomainTreeManager0Serialiser;
    private final Serializer addToCriteriaTickManager0Serialiser;
    private final Serializer addToCriteriaTickManagerForLocator0Serialiser;
    private final Serializer pivotDomainTreeManagerSerialiser;
    private final Serializer analysisDomainTreeManagerSerialiser;
    private final Serializer sentinelDomainTreeManagerSerialiser;
    private final Serializer lifecycleDomainTreeManagerSerialiser;
    private final Serializer multipleDecDomainTreeManagerSerialiser;
    private final Serializer locatorDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser;
    private final Serializer centreDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser;
    private final Serializer dynamicallyTypedQueryContainerSerialiser;
    private final Serializer lifecycleQueryContainerSerialiser;

    public TgKryo0(final EntityFactory factory, final ISerialisationClassProvider provider, final Serialiser0 serialiser) {
        setRegistrationOptional(true);
        setClassLoader(ClassLoader.getSystemClassLoader());

        this.factory = factory;
        this.provider = provider;
        this.serialiser = serialiser;
        this.serialiser.setEngine(SerialiserEngines.KRYO, this);

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
        classSerialiser = new ClassSerialiser(this);
        intervalSerializer = new IntervalSerializer();
        colorSerializer = new ColorSerializer();
        sortKeySerialiser = new SortKeySerialiser(this);
        // "domain trees" serialisers
        locatorManager0Serialiser = new LocatorManager0Serialiser(serialiser);
        domainTreeEnhancer0Serialiser = new DomainTreeEnhancer0Serialiser(serialiser);
        centreDomainTreeRepresentationSerialiser = new CentreDomainTreeRepresentationSerialiser(serialiser);
        pivotDomainTreeRepresentationSerialiser = new PivotDomainTreeRepresentationSerialiser(serialiser);
        analysisDomainTreeRepresentationSerialiser = new AnalysisDomainTreeRepresentationSerialiser(serialiser);
        sentinelDomainTreeRepresentationSerialiser = new SentinelDomainTreeRepresentationSerialiser(serialiser);
        lifecycleDomainTreeRepresentationSerialiser = new LifecycleDomainTreeRepresentationSerialiser(serialiser);
        multipleDecDomainTreeRepresentationSerialiser = new MultipleDecDomainTreeRepresentationSerialiser(serialiser);
        locatorDomainTreeManager0Serialiser = new LocatorDomainTreeManager0Serialiser(serialiser);
        centreDomainTreeManager0Serialiser = new CentreDomainTreeManager0Serialiser(serialiser);
        masterDomainTreeManager0Serialiser = new MasterDomainTreeManager0Serialiser(serialiser);
        locatorDomainTreeRepresentationSerialiser = new LocatorDomainTreeRepresentationSerialiser(serialiser);
        addToCriteriaTickManagerForLocator0Serialiser = new AddToCriteriaTickManagerForLocator0Serialiser(serialiser);
        addToCriteriaTickManager0Serialiser = new AddToCriteriaTickManager0Serialiser(serialiser);
        pivotDomainTreeManagerSerialiser = new PivotDomainTreeManagerSerialiser(serialiser);
        analysisDomainTreeManagerSerialiser = new AnalysisDomainTreeManagerSerialiser(serialiser);
        sentinelDomainTreeManagerSerialiser = new SentinelDomainTreeManagerSerialiser(serialiser);
        lifecycleDomainTreeManagerSerialiser = new LifecycleDomainTreeManagerSerialiser(serialiser);
        multipleDecDomainTreeManagerSerialiser = new MultipleDecDomainTreeManagerSerialiser(serialiser);
        locatorDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser = new LocatorDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser(serialiser);
        centreDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser = new CentreDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser(serialiser);
        dynamicallyTypedQueryContainerSerialiser = new DynamicallyTypedQueryContainerSerialiser(this);
        lifecycleQueryContainerSerialiser = new LifecycleQueryContainerSerialiser(this);

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
        register(ListOrderedMap.class);
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
        register(AnalysisResultClass.class);
        register(ICategorizer.class);
        register(LifecycleModel.class);
        register(EntityPropertyLifecycle.class);
        register(ValuedInterval.class);
        register(Interval.class);
        register(LifecycleQueryContainer.class);

        // entity query classes
        register(EntityAggregates.class);
        register(QueryModel.class);
        register(EntityResultQueryModel.class);
        register(AggregatedResultQueryModel.class);
        register(ExpressionModel.class);
        register(PrimitiveResultQueryModel.class);
        register(QueryExecutionModel.class);
        register(fetch.FetchCategory.class);
        register(fetch.class);
        register(OrderingModel.class);
        register(TokenCategory.class);
        register(QueryTokens.class);
        register(LogicalOperator.class);
        register(ComparisonOperator.class);
        register(ArithmeticalOperator.class);
        register(Functions.class);
        register(DynamicallyTypedQueryContainer.class);
        register(JoinType.class);

        register(Class.class);
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
                final List<Class<?>> enumTypes = EntityUtils.extractTypes((Class<Enum>) type);
                for (final Class<?> klass : enumTypes) {
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
        } else if (PropertyDescriptor.class.isAssignableFrom(type)) { // PropertyDescriptor must always be checked before any other entity
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
        } else if (Class.class.isAssignableFrom(type)) {
            return classSerialiser;
        } else if (Interval.class.isAssignableFrom(type)) {
            return intervalSerializer;
        } else if (Color.class.isAssignableFrom(type)) {
            return colorSerializer;
        } else if (SortKey.class.isAssignableFrom(type)) {
            return sortKeySerialiser;
        } else if (DomainTreeEnhancer0.class.isAssignableFrom(type)) { // "domain tree" serialisers
            return domainTreeEnhancer0Serialiser;
        } else if (LocatorManager0.class.isAssignableFrom(type)) {
            return locatorManager0Serialiser;
        } else if (LocatorDomainTreeRepresentation.class.isAssignableFrom(type)) { // higher in hierarchy above CriteriaDomainTreeRepresentation!
            return locatorDomainTreeRepresentationSerialiser;
        } else if (CentreDomainTreeRepresentation.class.isAssignableFrom(type)) {
            return centreDomainTreeRepresentationSerialiser;
        } else if (PivotDomainTreeRepresentation.class.isAssignableFrom(type)) {
            return pivotDomainTreeRepresentationSerialiser;
        } else if (SentinelDomainTreeRepresentation.class.isAssignableFrom(type)) {
            return sentinelDomainTreeRepresentationSerialiser;
        } else if (AnalysisDomainTreeRepresentation.class.isAssignableFrom(type)) {
            return analysisDomainTreeRepresentationSerialiser;
        } else if (LifecycleDomainTreeRepresentation.class.isAssignableFrom(type)) {
            return lifecycleDomainTreeRepresentationSerialiser;
        } else if (MultipleDecDomainTreeRepresentation.class.isAssignableFrom(type)) {
            return multipleDecDomainTreeRepresentationSerialiser;
        } else if (LocatorDomainTreeManager0.class.isAssignableFrom(type)) { // higher in hierarchy above CriteriaDomainTreeManager!
            return locatorDomainTreeManager0Serialiser;
        } else if (CentreDomainTreeManager0.class.isAssignableFrom(type)) {
            return centreDomainTreeManager0Serialiser;
        } else if (MasterDomainTreeManager0.class.isAssignableFrom(type)) {
            return masterDomainTreeManager0Serialiser;
        } else if (AddToCriteriaTickManagerForLocator0.class.isAssignableFrom(type)) {
            return addToCriteriaTickManagerForLocator0Serialiser;
        } else if (AddToCriteriaTickManager0.class.isAssignableFrom(type)) {
            return addToCriteriaTickManager0Serialiser;
        } else if (PivotDomainTreeManager.class.isAssignableFrom(type)) {
            return pivotDomainTreeManagerSerialiser;
        } else if (SentinelDomainTreeManager.class.isAssignableFrom(type)) {
            return sentinelDomainTreeManagerSerialiser;
        } else if (AnalysisDomainTreeManager.class.isAssignableFrom(type)) {
            return analysisDomainTreeManagerSerialiser;
        } else if (LifecycleDomainTreeManager.class.isAssignableFrom(type)) {
            return lifecycleDomainTreeManagerSerialiser;
        } else if (MultipleDecDomainTreeManager.class.isAssignableFrom(type)) {
            return multipleDecDomainTreeManagerSerialiser;
        } else if (LocatorDomainTreeManagerAndEnhancer0.class.isAssignableFrom(type)) { // higher in hierarchy above CentreDomainTreeManagerAndEnhancer0!
            return locatorDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser;
        } else if (CentreDomainTreeManagerAndEnhancer0.class.isAssignableFrom(type)) {
            return centreDomainTreeManagerAndEnhancer0WithTransientAnalysesSerialiser;
        } else if (DynamicallyTypedQueryContainer.class.isAssignableFrom(type)) {
            return dynamicallyTypedQueryContainerSerialiser;
        } else if (LifecycleQueryContainer.class.isAssignableFrom(type)) {
            return lifecycleQueryContainerSerialiser;
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
        } else if (type.getName().contains("java.util.Arrays$ArrayList")) {
            // using Arrays.asList produces instance of List of type Arrays$ArrayList, which do not have a default constructor;
            // thus, need to instantiate ArrayList instead.
            return (T) new ArrayList();
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
            final T result = readObject(readBuffer, /* serialisedType */type);
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

                // let's try to identify whether we are loading generated types here
                if (obj != null && DynamicEntityClassLoader.isGenerated(obj.getClass())) {
                    return;
                }

                // interested only in instances of the enhanced AbstractEntity.
                if (obj instanceof AbstractEntity) {
                    refs.add((AbstractEntity<?>) obj);
                }
            }
            // explicit reset in order to make the reason for the above snippet more explicit
            references.reset();

            // iterate through all locally cached entity instances and execute respective definers
            for (final AbstractEntity<?> entity : refs) {
                entity.beginInitialising();
                for (final MetaProperty<?> prop : entity.getProperties().values()) {
                    if (prop != null) {
                        if (!prop.isCollectional()) {
                            prop.defineForOriginalValue();
                        }
                    }
                }
                entity.endInitialising();
            }
        }
    }

    @Override
    public EntityFactory factory() {
        return factory;
    }

    protected ISerialiser0 getSerialiser() {
        return serialiser;
    }
}
