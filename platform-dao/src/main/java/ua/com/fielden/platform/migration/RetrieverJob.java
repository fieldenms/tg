package ua.com.fielden.platform.migration;

import ua.com.fielden.platform.entity.AbstractEntity;

public class RetrieverJob {
    public final IRetriever<? extends AbstractEntity<?>> retriever;
    public final TargetDataInsert tdi;
    public final TargetDataUpdate tdu;
    public final String legacySql;
    
    public RetrieverJob(IRetriever<? extends AbstractEntity<?>> retriever, TargetDataInsert tdi, TargetDataUpdate tdu, String legacySql) {
        this.retriever = retriever;
        this.tdi = tdi;
        this.tdu = tdu;
        this.legacySql = legacySql;
    }    
}