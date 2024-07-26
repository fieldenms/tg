package ua.com.fielden.platform.entity;

import com.google.inject.Injector;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ua.com.fielden.benchmark.EntityModuleWithPropertyFactoryForBenchmarking;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.ioc.ApplicationInjectorFactory;
import ua.com.fielden.platform.sample.domain.TgVehicle;
import ua.com.fielden.platform.sample.domain.TgVehicleMake;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;

import java.util.List;

import static org.openjdk.jmh.annotations.Threads.MAX;

@Fork(value = 3, jvmArgsAppend = "-Djmh.stack.lines=3")
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@Threads(MAX)
public class DynamicPropertyAccessBenchmark {

    private static final Injector injector = new ApplicationInjectorFactory()
            .add(new EntityModuleWithPropertyFactoryForBenchmarking())
            .add(DynamicPropertyAccessModule.forceCaching())
            .getInjector();
    private static final EntityFactory factory = injector.getInstance(EntityFactory.class);

    @State(Scope.Thread)
    public static class BenchmarkState {
        public TgWorkOrder workOrder;
        public TgVehicle vehicle;
        public TgVehicleModel model;
        public TgVehicleMake make;

        @Setup(Level.Trial)
        public void setUp() {
            workOrder = factory.newByKey(TgWorkOrder.class, "WO1");
            vehicle = factory.newByKey(TgVehicle.class, "VEH1");
            workOrder.setVehicle(vehicle);
            model = factory.newByKey(TgVehicleModel.class, "MODEL1");
            vehicle.setModel(model);
            make = factory.newByKey(TgVehicleMake.class, "MAKE1");
            model.setMake(make);

            injector.getInstance(DynamicPropertyAccess.class)
                    .index(List.of(TgWorkOrder.class, TgVehicle.class, TgVehicleModel.class, TgVehicleMake.class));
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

}
