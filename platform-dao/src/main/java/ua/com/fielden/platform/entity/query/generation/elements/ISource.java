package ua.com.fielden.platform.entity.query.generation.elements;

import java.util.List;

import ua.com.fielden.platform.entity.query.generation.elements.AbstractSource.PropResolutionInfo;

public interface ISource {
    /**
     * Represents business alias of the query source
     * 
     * @return
     */
    String getAlias();

    String getSqlAlias();

    /**
     * Indicates query source type (in case of entity type as a source it returns this entity type, in case of query as a source it returns it result type, which can be
     * real/synthetic entity type or entity aggregates type).
     * 
     * @return
     */
    Class sourceType();

    /**
     * Attempts to resolve given prop as prop of this query source. Returns null in case of attempt failure.
     * 
     * @param prop
     * @return
     */
    PropResolutionInfo containsProperty(EntProp prop);

    /**
     * Indicates whether this equery source has been generated as part of implicit properties resolution.
     * 
     * @return
     */
    boolean generated();

    /**
     * Assigns sequential sql alias for the table/subquery corresponding to the query source.
     * 
     * @param sqlAlias
     */
    void assignSqlAlias(String sqlAlias);

    /**
     * Produces sql clause for the query source.
     * 
     * @return
     */
    String sql();

    /**
     * Collects all values from given query source (assuming it is composed from query model(s))
     * 
     * @return
     */
    List<EntValue> getValues();

    void addReferencingProp(PropResolutionInfo prop);

    List<PropResolutionInfo> getReferencingProps();

    List<CompoundSource> generateMissingSources();

    void populateSourceItems(final boolean parentLeftJoinLegacy);

    void assignNullability(final boolean nullable);
}