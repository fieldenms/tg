package ua.com.fielden.eql;

import org.openjdk.jmh.annotations.*;
import ua.com.fielden.platform.entity.query.model.QueryModel;
import ua.com.fielden.platform.eql.retrieval.QueryNowValue;
import ua.com.fielden.platform.eql.stage0.QueryModelToStage1Transformer;

import java.util.Map;

import static org.openjdk.jmh.annotations.Threads.MAX;

@Fork(value = 1, jvmArgsAppend = "-Djmh.stack.lines=3")
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@Threads(MAX)
@BenchmarkMode({Mode.Throughput})
public class EqlStage0Benchmark extends AbstractEqlBenchmark {

    protected Object finish(final QueryModel<?> queryModel) {
        final var gen = new QueryModelToStage1Transformer(filter, null, new QueryNowValue(dates), Map.of());
        return gen.generateAsResultQuery(queryModel, null, null);
    }

}
