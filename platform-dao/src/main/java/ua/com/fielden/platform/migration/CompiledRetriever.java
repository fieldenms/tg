package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * A class that represents the result of processing {@code retriever} definition, including the legacy SELECT statement and the target INSERT or UPDATE statement.   
 *
 * @author TG Team
 */
final class CompiledRetriever {

    private final IRetriever<? extends AbstractEntity<?>> retriever;
    private final String legacySql;
    private final TargetDataInsert tdi;
    private final TargetDataUpdate tdu;
    private final EntityMd md;
    
    public static CompiledRetriever forInsert(final IRetriever<? extends AbstractEntity<?>> retriever, final String legacySql, final TargetDataInsert tdi, final EntityMd md) {
        return new CompiledRetriever(retriever, tdi, null, legacySql, md);
    }

    public static CompiledRetriever forUpdate(final IRetriever<? extends AbstractEntity<?>> retriever, final String legacySql, final TargetDataUpdate tdu, final EntityMd md) {
        return new CompiledRetriever(retriever, null, tdu, legacySql, md);
    }

    private CompiledRetriever(final IRetriever<? extends AbstractEntity<?>> retriever, final TargetDataInsert tdi, final TargetDataUpdate tdu, final String legacySql, final EntityMd md) {
        this.retriever = retriever;
        this.tdi = tdi;
        this.tdu = tdu;
        this.legacySql = legacySql;
        this.md = md;
    }
    
    public Optional<Long> exec(final Function<TargetDataUpdate, Optional<Long>> updater, Function<TargetDataInsert, Optional<Long>> inserter) {
        if (tdi != null) {
            return inserter.apply(tdi);
        }
        return updater.apply(tdu);
    }

    public Class<? extends AbstractEntity<?>> getType() {
        return retriever.type();
    }
    
    public List<PropInfo> getContainers() {
        return tdi != null ? tdi.containers() : tdu.containers();
    }

    public IRetriever<? extends AbstractEntity<?>> retriever() {
        return retriever;
    }

    public boolean isUpdater() {
        return retriever.isUpdater();
    }

    public String legacySql() {
        return legacySql;
    }

    public EntityMd entityMd() {
        return md;
    }

}
