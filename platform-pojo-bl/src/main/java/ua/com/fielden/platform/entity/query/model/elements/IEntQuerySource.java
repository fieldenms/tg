package ua.com.fielden.platform.entity.query.model.elements;

import java.util.List;

import ua.com.fielden.platform.entity.query.model.elements.AbstractEntQuerySource.PropResolutionInfo;

public interface IEntQuerySource {
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

    /**
     * Determines Java type of the property within given query source (can be entity type or query)
     *
     * @param propSimpleName
     * @return
     */
    Class propType(String propSimpleName);

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

    void addReferencingProp(EntProp prop);

    List<EntProp> getReferencingProps();

    void addFinalReferencingProp(EntProp prop);

    List<EntProp> getFinalReferencingProps();
}