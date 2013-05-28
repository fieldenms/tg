package ua.com.fielden.platform.eql.s2.elements;

import java.util.List;


public interface ISource2 {
    /**
     * Represents business alias of the query source
     *
     * @return
     */
    String getAlias();

    /**
     * Indicates query source type (in case of entity type as a source it returns this entity type, in case of query as a source it returns it result type, which can be
     * real/synthetic entity type or entity aggregates type).
     *
     * @return
     */
    Class sourceType();

    List<EntValue> getValues();
}