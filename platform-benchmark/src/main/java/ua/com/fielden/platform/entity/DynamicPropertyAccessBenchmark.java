package ua.com.fielden.platform.entity;

import com.google.inject.Injector;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ua.com.fielden.benchmark.EntityModuleWithPropertyFactoryForBenchmarking;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.*;

import java.util.List;
import java.util.Map;

import static org.openjdk.jmh.annotations.Threads.MAX;
import static ua.com.fielden.platform.utils.MiscUtilities.mkProperties;

@Fork(value = 3, jvmArgsAppend = "-Djmh.stack.lines=3")
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Threads(MAX)
public class DynamicPropertyAccessBenchmark {

    private static final Injector injector = new ApplicationInjectorFactory()
            .add(new EntityModuleWithPropertyFactoryForBenchmarking(mkProperties(Map.of("dynamicPropertyAccess.caching", "enabled"))))
            .getInjector();
    private static final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @State(Scope.Thread)
    public static class BenchmarkState {
        public TgWorkOrder workOrder;
        public TgVehicle vehicle;
        public TgVehicleModel model;
        public TgVehicleMake make;
        public TgUnion union;
        public TgUnionType1 union1;
        public TgUnionCommonType unionCommon;

        @Setup(Level.Trial)
        public void setUp() {
            workOrder = factory.newByKey(TgWorkOrder.class, "WO1");
            vehicle = factory.newByKey(TgVehicle.class, "VEH1");
            workOrder.setVehicle(vehicle);
            model = factory.newByKey(TgVehicleModel.class, "MODEL1");
            vehicle.setModel(model);
            make = factory.newByKey(TgVehicleMake.class, "MAKE1");
            model.setMake(make);
            unionCommon = factory.newByKey(TgUnionCommonType.class, "UC");
            union1 = factory.newByKey(TgUnionType1.class, "U1").setCommon(unionCommon);
            union = factory.newEntity(TgUnion.class).setUnion1(union1);

            injector.getInstance(DynamicPropertyAccess.class)
                    .index(List.of(TgUnion.class, TgWorkOrder.class, TgVehicle.class,
                                   TgVehicleModel.class, TgVehicleMake.class));
        }
    }

    @Benchmark
    @Measurement(batchSize = 100_000)
    public void getLevel1(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.workOrder.get("vehicle"));
    }

    @Benchmark
    @Measurement(batchSize = 100_000)
    public void getLevel2(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.workOrder.get("vehicle.model"));
    }

    @Benchmark
    @Measurement(batchSize = 100_000)
    public void getLevel3(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.workOrder.get("vehicle.model.make"));
    }

    @Benchmark
    @Measurement(batchSize = 100_000)
    public void getLevel4(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.workOrder.get("vehicle.model.make.key"));
    }

    @Benchmark
    @Measurement(batchSize = 100_000)
    public void setLevel1(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.workOrder.set("vehicle", state.vehicle));
    }

    @Benchmark
    @Measurement(batchSize = 10_000)
    public void getLevel1_union_member(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.union.get("union1"));
    }

    @Benchmark
    @Measurement(batchSize = 10_000)
    public void getLevel1_union_common_property(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.union.get("common"));
    }

    @Benchmark
    @Measurement(batchSize = 10_000)
    public void setLevel1_union_member(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.union.set("union1", state.union1));
    }

    @Benchmark
    @Measurement(batchSize = 10_000)
    public void setLevel1_union_common_property(final Blackhole blackhole, final BenchmarkState state) {
        blackhole.consume(state.union.set("common", state.unionCommon));
    }

    /**
     * Main method for testing purposes - to verify if the static state of this class is initialised correctly.
     */
    public static void main(String[] args) {
        injector.getInstance(DynamicPropertyAccess.class);
        System.out.println(DynamicPropertyAccessBenchmark.class.getTypeName() + " works!");
    }

}
