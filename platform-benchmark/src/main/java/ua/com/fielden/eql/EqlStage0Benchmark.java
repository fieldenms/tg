package ua.com.fielden.eql;

import com.google.inject.Injector;
import org.openjdk.jmh.annotations.*;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;
import ua.com.fielden.platform.utils.IDates;

import java.util.Map;
import java.util.Optional;

import static org.openjdk.jmh.annotations.Threads.MAX;

@Fork(value = 1, jvmArgsAppend = "-Djmh.stack.lines=3")
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Threads(MAX)
@BenchmarkMode({Mode.Throughput})
public class EqlStage0Benchmark extends AbstractEqlBenchmark {

    private IFilter filter;
    private IDates dates;

    @Override
    protected void afterSetup(final Injector injector) {
        this.filter = injector.getInstance(IFilter.class);
        this.dates = injector.getInstance(IDates.class);
    }

    protected Object finish(final QueryModel<?> queryModel) {
        final var gen = new QueryModelToStage1Transformer(filter, Optional.empty(), new QueryNowValue(dates), Map.of());
        return gen.generateAsResultQuery(queryModel, null, null);
    }

}
