package ua.com.fielden.eql;

import org.openjdk.jmh.annotations.*;
import ua.com.fielden.platform.audit.AuditingIocModule;
import ua.com.fielden.platform.audit.AuditingMode;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.meta.QuerySourceInfoProvider;
import ua.com.fielden.platform.eql.retrieval.EqlQueryTransformer;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.eql.stage1.TransformationContextFromStage1To2;
import ua.com.fielden.platform.eql.stage2.IPropPathResolver;
import ua.com.fielden.platform.eql.stage2.operands.Prop2;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.meta.IDomainMetadata;
import ua.com.fielden.platform.sample.domain.*;
import ua.com.fielden.platform.utils.IDates;
import ua.com.fielden.platform.web.test.config.ApplicationDomain;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ua.com.fielden.eql.BenchmarkIocModule.benchmarkModule;
import static ua.com.fielden.eql.IPropPathResolverBenchmark.IocModule.iocModule;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.utils.MiscUtilities.mkProperties;
import static ua.com.fielden.platform.utils.MiscUtilities.propertiesUnionLeft;

/// Measures [IPropPathResolver#resolve], the property-resolution facility of stage 2 of the EQL-to-SQL transformation.
///
/// This benchmark simulates [EqlQueryTransformer#transform]: stage 0 and the stage 1-to-2 transformation of every query
/// are performed during trial setup, so that only the subsequent call to [IPropPathResolver] is measured.
/// Each query has its own benchmark method, whose sole statement resolves that query's pre-computed properties.
///
/// The corpus covers the axes along which the cost of resolution varies: the number of properties, the length of property
/// paths, the sharing of path prefixes (which implicit joins must reuse rather than recreate), header properties
/// (component- and union-typed), and the expansion of calculated properties, including transitive ones.
///
/// ## Running
///
/// Assuming the current working directory is this module (`platform-benchmark`):
///
/// ```
/// java -jar target/benchmarks.jar \
///         -p propertiesFile="src/main/resources/benchmark-application.properties" \
///         -prof gc \
///         "ua.com.fielden.eql.IPropPathResolverBenchmark"
/// ```
///
/// A single query can be benchmarked by appending its method name, e.g. `...IPropPathResolverBenchmark.deep_dot_notation`.
///
@Fork(value = 1)
@Warmup(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
// Single-threaded on purpose.
// `resolve` is handed a `QueryModelToStage1Transformer`, which carries a mutable source-ID counter that is advanced for
// every implicit join, so a single instance cannot be shared between threads.
@Threads(1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class IPropPathResolverBenchmark {

    // Required for correct initialisation of Guice modules.
    @Param("")
    public String propertiesFile;

    private IPropPathResolver propPathResolver;
    private QueryModelToStage1Transformer gen;
    private int initSourceId;

    /// Stage 2 properties of each query, pre-computed by [#beforeTrial()].
    /// A dedicated field per query keeps each measured method down to a pair of field reads.
    ///
    private Set<Prop2>
            singleProp,
            dotNotation,
            deepDotNotation,
            componentProps,
            unionProps,
            calcProps,
            calcEntityProps,
            transitiveCalcProps,
            composite;

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Benchmarks

    @Benchmark
    public IPropPathResolver.Result single_prop() {
        return propPathResolver.resolve(singleProp, gen);
    }

    @Benchmark
    public IPropPathResolver.Result dot_notation() {
        return propPathResolver.resolve(dotNotation, gen);
    }

    @Benchmark
    public IPropPathResolver.Result deep_dot_notation() {
        return propPathResolver.resolve(deepDotNotation, gen);
    }

    @Benchmark
    public IPropPathResolver.Result component_props() {
        return propPathResolver.resolve(componentProps, gen);
    }

    @Benchmark
    public IPropPathResolver.Result union_props() {
        return propPathResolver.resolve(unionProps, gen);
    }

    @Benchmark
    public IPropPathResolver.Result calc_props() {
        return propPathResolver.resolve(calcProps, gen);
    }

    @Benchmark
    public IPropPathResolver.Result calc_entity_props() {
        return propPathResolver.resolve(calcEntityProps, gen);
    }

    @Benchmark
    public IPropPathResolver.Result transitive_calc_props() {
        return propPathResolver.resolve(transitiveCalcProps, gen);
    }

    @Benchmark
    public IPropPathResolver.Result composite() {
        return propPathResolver.resolve(composite, gen);
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Setup

    @Setup(Level.Trial)
    public void beforeTrial() throws IOException {
        if (!Files.isReadable(Path.of(propertiesFile))) {
            throw new IllegalStateException("Can't read file: %s".formatted(propertiesFile));
        }
        final var properties = new Properties();
        try (final var in = new FileInputStream(propertiesFile)) {
            properties.load(in);
        }

        final var injector = new ApplicationInjectorFactory(Workflows.development)
                .add(benchmarkModule(iocModule(properties)))
                .getInjector();

        propPathResolver = injector.getInstance(IPropPathResolver.class);
        gen = new QueryModelToStage1Transformer(
                injector.getInstance(IFilter.class),
                Optional.empty(),
                new QueryNowValue(injector.getInstance(IDates.class)),
                Map.of());
        initSourceId = gen.nextSourceId() - 1;
        final var context = TransformationContextFromStage1To2.mkContext(
                injector.getInstance(QuerySourceInfoProvider.class),
                injector.getInstance(IDomainMetadata.class));

        singleProp = prepare(singlePropQuery(), context);
        dotNotation = prepare(dotNotationQuery(), context);
        deepDotNotation = prepare(deepDotNotationQuery(), context);
        componentProps = prepare(componentPropsQuery(), context);
        unionProps = prepare(unionPropsQuery(), context);
        calcProps = prepare(calcPropsQuery(), context);
        calcEntityProps = prepare(calcEntityPropsQuery(), context);
        transitiveCalcProps = prepare(transitiveCalcPropsQuery(), context);
        composite = prepare(compositeQuery(), context);
    }

    static class IocModule extends BenchmarkIocModule {

        public static IocModule iocModule(final Properties inProps) {
            final var props = propertiesUnionLeft(
                    mkProperties(Map.of(AuditingIocModule.AUDIT_MODE, AuditingMode.DISABLED.name())),
                    inProps);
            return new IocModule(props);
        }

        private IocModule(final Properties props) {
            super(props, new ApplicationDomain(), ApplicationDomain.domainTypes());
        }

        @Override
        protected void configure() {
            super.configure();
            install(new NewUserEmailNotifierTestIocModule());
        }

    }

    @Setup(Level.Invocation)
    public void beforeInvocation() {
        // Reset the source ID generator to avoid running out of IDs.
        gen._setSourceId(initSourceId);
    }

    /// Replicates [EqlQueryTransformer#transform] up to, but excluding, the call to [IPropPathResolver#resolve],
    /// and returns the properties that the call would receive.
    ///
    private Set<Prop2> prepare(final QueryModel<?> queryModel, final TransformationContextFromStage1To2 context) {
        final var props = gen.generateAsResultQuery(queryModel, null, null).transform(context).collectProps();
        // Domain metadata and query source information are generated lazily and cached.
        // Resolve once up front so that the cost of populating those caches is not attributed to a measured invocation.
        propPathResolver.resolve(props, gen);
        return props;
    }

    // ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    // : Queries

    /// The baseline: a single persistent property, one resolution, no implicit joins.
    ///
    private static QueryModel<?> singlePropQuery() {
        return select(TgVehicle.class).where()
                .prop("initDate").isNotNull()
                .model();
    }

    /// Dot-notated properties, each of which produces an implicit join.
    ///
    private static QueryModel<?> dotNotationQuery() {
        return select(TgVehicle.class).where()
                .anyOfProps("initDate", "station.name", "station.parent.name", "replacedBy.initDate").isNotNull()
                .model();
    }

    /// A chain of implicit joins along the org-unit hierarchy, with shared path prefixes that must be reused.
    ///
    private static QueryModel<?> deepDotNotationQuery() {
        return select(TgVehicle.class).where()
                .anyOfProps("station.name",
                            "station.parent.name",
                            "station.parent.parent.name",
                            "station.parent.parent.parent.name",
                            "station.parent.parent.parent.parent.key",
                            "replacedBy.station.parent.parent.name")
                .isNotNull()
                .model();
    }

    /// Component-typed (header) properties, which do not produce joins of their own but do affect path splitting.
    ///
    private static QueryModel<?> componentPropsQuery() {
        return select(TgVehicle.class).where()
                .prop("price.amount").gt().prop("purchasePrice.amount")
                .and().prop("replacedBy.price.amount").isNotNull()
                .model();
    }

    /// Union-typed (header) properties, including `location.key`, which is an implicitly calculated property.
    ///
    private static QueryModel<?> unionPropsQuery() {
        return select(TgBogie.class).where()
                .anyOfProps("location.workshop.key", "location.wagonSlot.key", "location.key").isNotNull()
                .model();
    }

    /// Calculated properties, several of which reference other calculated properties or contain sub-queries.
    ///
    private static QueryModel<?> calcPropsQuery() {
        return select(TgVehicle.class).where()
                .anyOfProps("constValueProp", "calc0", "calc2", "calc3", "calc4", "calc5", "calc6", "sumOfPrices")
                .isNotNull()
                .model();
    }

    /// Entity-typed calculated properties used as intermediate path elements.
    /// These produce implicit joins whose left-hand side is an expression rather than a column.
    ///
    private static QueryModel<?> calcEntityPropsQuery() {
        return select(TeVehicle.class).where()
                .anyOfProps("modelMakeKey7", "modelMakeKey8", "replacedByTwiceModelMake", "replacedByTwicePrice")
                .isNotNull()
                .model();
    }

    /// Calculated properties with transitive dependencies on other calculated properties.
    ///
    private static QueryModel<?> transitiveCalcPropsQuery() {
        return select(TeVehicle.class).where()
                .anyOfProps("modelKey", "modelKey2", "modelDesc", "modelMakeDesc",
                            "modelMakeKey", "modelMakeKey2", "modelMakeKey3",
                            "modelMakeKey4", "modelMakeKey5", "modelMakeKey6",
                            "stationName", "priceDiffBetweenCurrentAndReplacedByTwice")
                .isNotNull()
                .model();
    }

    /// A query that combines explicit joins, dot-notation, component properties, a calculated property and a sub-query.
    ///
    private static QueryModel<?> compositeQuery() {
        return select(TgVehicle.class).as("v")
                .join(TgVehicleModel.class).as("m").on().prop("v.model").eq().prop("m.id")
                .where()
                .prop("v.station.parent.name").isNotNull()
                .and().prop("v.price.amount").gt().prop("v.purchasePrice.amount")
                .and().prop("m.make.key").like().val("A%")
                .and().prop("v.calc0").gt().val(0)
                .and().prop("v.replacedBy.station.parent.parent.name").isNotNull()
                .and().exists(select(TgFuelUsage.class).where()
                                      .prop("vehicle").eq().extProp("v.id")
                                      .and().prop("qty").gt().val(0)
                                      .model())
                .model();
    }

}
