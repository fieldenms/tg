package ua.com.fielden.eql;

import com.google.inject.Injector;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ua.com.fielden.platform.basic.config.Workflows;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.ioc.NewUserEmailNotifierTestIocModule;
import ua.com.fielden.platform.sample.domain.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Random;

import static ua.com.fielden.eql.BenchmarkIocModule.newBenchmarkModule;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.expr;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

/**
 * Base class for benchmarks. Contains EQL expressions of various kinds and complexity in their raw unparsed form.
 * It is up to subclasses to perform the desired transformation of those expressions via {@link #finish(QueryModel)}.
 *
 * <h3> Running benchmarks </h3>
 *
 * The following command should be used to run a benchmark based on this class, assuming the current working directory
 * is this module (platform-benchmark).
 * <pre>
 java -jar target/benchmarks.jar \
 -p propertiesFile="src/main/resources/AbstractEqlBenchmark.properties" \
 -prof gc \
 "ua.com.fielden.eql.$SPECIFIC_BENCHMARK"
 </pre>
 */
@State(Scope.Benchmark)
public abstract class AbstractEqlBenchmark {

    // required for correct initialisation of Guice modules
    @Param("") String propertiesFile;

    private Injector injector;
    private EqlRandomGenerator eqlGenerator;

    protected EqlRandomGenerator eqlRandomGenerator() {
        return eqlGenerator;
    }

    @Benchmark
    public void simple_query(final Blackhole blackhole) {
        final var model = select(TgPerson.class).where()
                .prop("id").eq().val(1)
                .model();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void many_yielded_subqueries(final Blackhole blackhole) {
        final var nullPersonModel = select(TgPerson.class).where().val(1).eq().val(0).model();

        final var subquery1 = select(TgVehicle.class).where()
                .val(10).eq().extProp("qty")
                .and()
                .prop("replacedBy.replacedBy").eq().extProp("vehicle")
                .yield().sumOf().prop("price")
                .modelAsPrimitive();
        final var subquery2 = select(TgVehicle.class).
                where().val(10).eq().extProp("qty").
                and().
                prop("replacedBy.replacedBy").eq().extProp("vehicle").
                yield().sumOf().prop("price").modelAsPrimitive();
        final var subquery3 = select(TgVehicle.class).
                where().val(10).eq().extProp("qty").
                and().
                prop("replacedBy.replacedBy").eq().extProp("vehicle").
                yield().sumOf().prop("replacedBy.price").modelAsPrimitive();
        final var subquery4 = select(TgVehicle.class).
                where().val(10).eq().extProp("qty").
                and().
                prop("replacedBy.replacedBy").eq().extProp("vehicle").
                yield().sumOf().prop("purchasePrice").modelAsPrimitive();
        final var subquery5 = select(TgVehicle.class).
                where().val(10).eq().extProp("qty").
                and().
                prop("replacedBy.replacedBy").eq().extProp("vehicle").
                yield().sumOf().prop("replacedBy.purchasePrice").modelAsPrimitive();
        final var subquery6 = select(TgVehicle.class).
                where().val(10).eq().extProp("qty").
                and().
                prop("replacedBy.replacedBy").eq().extProp("vehicle").
                yield().maxOf().prop("lastMeterReading").modelAsPrimitive();

        final var model = select(TgVehicleFuelUsage.class).
                yieldAll().
                yield().prop("id").as("id").
                yield().prop("vehicle").as("vehicle").
                yield().model(nullPersonModel).as("person").
                yield().val(0).as("qty").
                yield().prop("fuelType").as("fuelType").
                yield().model(subquery1).as("value1").
                yield().model(subquery2).as("value2").
                yield().model(subquery3).as("value3").
                yield().model(subquery4).as("value4").
                yield().model(subquery5).as("value5").
                yield().model(subquery6).as("value6").
                modelAsAggregate();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void caseWhen_01(final Blackhole blackhole) {
        final var model = select(TgVehicle.class).where().
                prop("initDate").le().now().
                yield().
                    caseWhen().prop("price").lt().val(0).
                    then().param("p1").
                    when().prop("price").eq().prop("purchasePrice").
                    then().caseWhen().
                        prop("replacedBy.purchasePrice").gt().prop("price").
                        then().val(1).
                        endAsInt().
                    when().exists(select(TgVehicle.class).where().
                            anyOfProps("price", "purchasePrice").gt().val(100).
                            model()).
                    then().val(2).
                    when().yearOf().prop("initDate").le().val(2010).and().monthOf().prop("initDate").eq().val(9).
                    then().val(3).
                    end().
                as("value").modelAsAggregate();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void conditions_02(final Blackhole blackhole) {
        final var model = select(TgVehicle.class).where()
                .prop("id").eq().val(1)
                .and()
                .prop("price").gt().val(10)
                .or()
                .prop("price").eq().val(5)
                .model();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void nested_conditions(final Blackhole blackhole) {
        final var model = select(TgVehicle.class).where()
                .prop("id").eq().val(1)
                .and()
                .begin()
                    .prop("price").gt().val(10)
                    .or()
                    .begin()
                        .allOfProps("price", "purchasePrice", "station").isNotNull()
                        .and()
                        .prop("price").notIn().values(0, 1, 2)
                    .end()
                .end()
                .or()
                .anyOfProps("price", "purchasePrice").gt().allOfProps("replacedBy.price", "replacedBy.purchasePrice")
                .or()
                .notBegin()
                    .allOfValues("a", "b", "c").like().anyOfProps("model", "replacedBy.model")
                    .and()
                    .begin()
                        .notExistsAllOf(select(TgVehicle.class).where().prop("id").gt().val(1).model())
                        .or()
                        .beginExpr().val(30).mult().prop("id").endExpr().eq().prop("price")
                    .end()
                .end()
                .and()
                .ifNull().prop("model").then().param("p1").isNotNull()
                .model();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void large_query_with_joins(final Blackhole blackhole) {
        final var newReorderQuantityExpr = expr()
                // 1
                .caseWhen().prop("orderMethod.value").eq().val(1).then()
                .beginExpr()
                .ifNull().prop("maximumQuantity").then().val(0).sub().ifNull().prop("qa").then().val(0)
                .endExpr()
                // 2
                .when().prop("orderMethod.value").eq().val(2).then()
                .beginExpr()
                .ifNull().prop("reorderPoint").then().val(0).add().ifNull().prop("reorderQuantity").then().val(0).add().ifNull().prop("safetyStockQuantity").then().val(0).add().ifNull().prop("emergencyStockQuantity").then().val(0).sub().prop("qa")
                .endExpr()
                // 3
                .when().prop("orderMethod.value").eq().val(3).then()
                .caseWhen().prop("qoo").lt().prop("qr").then()
                .beginExpr()
                .ifNull().prop("qr").then().val(0).sub().ifNull().prop("qoh").then().val(0)
                .endExpr()
                .otherwise().val(0).end()
                // 4
                .when().prop("orderMethod.value").eq().val(4).then()
                .beginExpr()
                .ifNull().prop("minimumQuantity").then().val(0)
                .add()
                .ifNull().prop("reorderQuantity").then().val(0)
                .sub()
                .ifNull().prop("qa").then().val(0)
                .endExpr()
                .end().model();

        final var supplierPartNoModel = select(TgVehicle.class).where()
                .prop("inventory").eq().extProp("id").and()
                .prop("rating").eq().val(1)
                .yield().prop("supplierPartNo")
                .modelAsPrimitive();

        final var qtyAvailableExpr = expr()
                .ifNull().prop("c1.quantityOnHand").then().val(0).add()
                .ifNull().prop("c2.quantityOnOrder").then().val(0).add()
                .ifNull().prop("c4.quantityInTransit").then().val(0).sub()
                .ifNull().prop("c3.quantityReserved").then().val(0)
                .model();

        final var inv = select(select(TgVehicle.class).where()
                .prop("active").eq().val(true).and()
                .prop("obsolete").eq().val(false).model()).as("inv")
                .leftJoin(select(TgPerson.class).
                        groupBy().prop("inventory").
                        yield().prop("inventory").as("inventory").
                        yield().sumOf().prop("quantityOnHand").as("quantityOnHand").modelAsAggregate()).as("c1").on().prop("inv.id").eq().prop("c1.inventory")
                .leftJoin(select(TgAuthor.class).where().prop("status.key").notIn().values("a", "b", "c").
                        groupBy().prop("inventory").
                        yield().prop("inventory").as("inventory").
                        yield().sumOf().round().beginExpr().prop("quantityOrdered").mult().prop("conversionFactor").endExpr().to(0).as("quantityOnOrder").modelAsAggregate()).as("c2").on().prop("inv.id").eq().prop("c2.inventory")
                .leftJoin(select(TgBogie.class).where().
                        prop("workActivity.active").eq().val(true).and().prop("quantityRequired").gt().prop("quantityExpended").
                        groupBy().prop("inventoryPart").
                        yield().prop("inventoryPart").as("inventoryPart").
                        yield().minOf().prop("dateRequired").as("dateRequired").
                        yield().sumOf().beginExpr().prop("quantityRequired").sub().prop("quantityExpended").endExpr().as("quantityReserved").modelAsAggregate()).as("c3").on().prop("inv.inventoryPart").eq().prop("c3.inventoryPart")
                .leftJoin(select(TgOriginator.class).where().prop("dateReceived").isNull().
                        groupBy().prop("inventoryBinFrom.inventory").
                        yield().prop("inventoryBinFrom.inventory").as("inventory").
                        yield().sumOf().prop("quantityTransferred").as("quantityInTransit").modelAsAggregate()).as("c4").on().prop("inv.id").eq().prop("c4.inventory")
                .where()
                .prop("inv.orderMethod.value").eq().val(1).and().expr(qtyAvailableExpr).le().prop("inv.minimumQuantity")
                .or()
                .prop("inv.orderMethod.value").eq().val(2).and().expr(qtyAvailableExpr).le().prop("inv.reorderPoint")
                .or()
                .prop("inv.orderMethod.value").eq().val(3).and().expr(qtyAvailableExpr).lt().val(0)
                .or()
                .prop("inv.orderMethod.value").eq().val(4).and().expr(qtyAvailableExpr).le().prop("inv.minimumQuantity")
                .yield().prop("id").as("invid")
                .yield().expr(qtyAvailableExpr).as("qa")
                .yield().ifNull().prop("c1.quantityOnHand").then().val(0).as("qoh")
                .yield().ifNull().prop("c2.quantityOnOrder").then().val(0).as("qoo")
                .yield().ifNull().prop("c3.quantityReserved").then().val(0).as("qr")
                .yield().ifNull().prop("c4.quantityInTransit").then().val(0).as("qit")
                .yield().prop("c3.dateRequired").as("requiredDeliveryDate")
                .modelAsAggregate();

        final var model = select(TgVehicle.class).join(inv).on().prop("id").eq().prop("invid")
                .yieldAll()
                .yield().model(supplierPartNoModel).as("supplierPartNo")
                .yield().caseWhen().expr(newReorderQuantityExpr).lt().val(0).then().val(0).otherwise().expr(newReorderQuantityExpr).end().as("newReorderQuantity")
                .yield().caseWhen().prop("orderMethod.value").eq().val(3).then().prop("requiredDeliveryDate").end().as("requiredDeliveryDate")
                .modelAsAggregate();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void multiple_group_bys(final Blackhole blackhole) {
        final var model = select(TgPerson.class).where()
                .prop("costCentreInt").isNotNull().and().prop("costCentreInt").gt().val(9999)
                .groupBy().prop("costCentre")
                .groupBy().prop("costCentreDesc")
                .groupBy().prop("pex")
                .groupBy().prop("entity")
                .groupBy().prop("employeeNo")
                .yield().prop("costCentre").as("costCentre")
                .yield().prop("costCentreDesc").as("costCentreDesc")
                .yield().prop("pex").as("pex")
                .yield().prop("entity").as("entity")
                .yield().prop("employeeNo").as("employeeNo")
                .modelAsAggregate();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void generated_conditions_x10(final Blackhole blackhole) {
        final var model = eqlGenerator.conditions(
                        select(TgPerson.class).where().val(1).isNotNull(),
                        10)
                .model();

        blackhole.consume(finish(model));
    }

    @Benchmark
    public void generated_conditions_x20(final Blackhole blackhole) {
        final var model = eqlGenerator.conditions(
                        select(TgPerson.class).where().val(1).isNotNull(),
                        20)
                .model();

        blackhole.consume(finish(model));
    }

    @Setup(Level.Trial)
    public void setup() throws IOException {
        eqlGenerator = new EqlRandomGenerator(new Random(9375679861L));

        if (!Files.isReadable(Path.of(propertiesFile))) {
            throw new IllegalStateException("Can't read file: %s".formatted(propertiesFile));
        }

        final var properties = new Properties();
        try (final var in = new FileInputStream(propertiesFile)) {
            properties.load(in);
        }

        injector = new ApplicationInjectorFactory(Workflows.development)
                .add(newBenchmarkModule(properties))
                .add(new NewUserEmailNotifierTestIocModule())
                .getInjector();

        afterSetup(injector);
    }

    protected void afterSetup(final Injector injector) {}

    private <T> T getInstance(final Class<T> type) {
        return injector.getInstance(type);
    }

    /**
     * Performs the last action in a benchmark method.
     */
    protected abstract Object finish(final QueryModel<?> queryModel);

}
