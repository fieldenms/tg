package ua.com.fielden.platform.eql.s1.elements;

public enum EntPropStage {
    UNRESOLVED, // can't be resolved at this query level
    UNPROCESSED, // before any attempts of resolving
    PRELIMINARY_RESOLVED , // is in principle resolvable within given query explicit sources
    FINALLY_RESOLVED, // resolved against explicit or implicitly generated sources and needs no generation of additional query sources
    EXTERNAL; // declared as master query property and should not be attempted to be resolved at this query level
}
