package ua.com.fielden.platform.migration;

import java.util.Optional;
import java.util.function.Function;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A class that represents the result of processing {@code retriever} definition, including the legacy SELECT statement and the target INSERT or UPDATE statement.   
 *
 * @author TG Team
 *
 */
public class CompiledRetriever {
    public final IRetriever<? extends AbstractEntity<?>> retriever;
    public final String legacySql;
    private final TargetDataInsert tdi;
    private final TargetDataUpdate tdu;
    
    public static CompiledRetriever forInsert(final IRetriever<? extends AbstractEntity<?>> retriever, final String legacySql, final TargetDataInsert tdi) {
        return new CompiledRetriever(retriever, tdi, null, legacySql);
    }

    public static CompiledRetriever forUpdate(final IRetriever<? extends AbstractEntity<?>> retriever, final String legacySql, final TargetDataUpdate tdu) {
        return new CompiledRetriever(retriever, null, tdu, legacySql);
    }

    private CompiledRetriever(final IRetriever<? extends AbstractEntity<?>> retriever, final TargetDataInsert tdi, final TargetDataUpdate tdu, final String legacySql) {
        this.retriever = retriever;
        this.tdi = tdi;
        this.tdu = tdu;
        this.legacySql = legacySql;
    }
    
    public Optional<Long> exec(final Function<TargetDataUpdate, Optional<Long>> updater, Function<TargetDataInsert, Optional<Long>> inserter) {
        if (tdi != null) {
            return inserter.apply(tdi);
        }
        return updater.apply(tdu);
    }

}