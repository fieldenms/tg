package ua.com.fielden.platform.domaintree.impl;

import java.util.List;
import java.util.Set;

import ua.com.fielden.platform.domaintree.IDomainTreeRepresentation;
import ua.com.fielden.platform.utils.Pair;

/**
 * This interface is just a wrapper for {@link IDomainTreeRepresentation} with accessor to mutable "included properties".
 * 
 * @author TG Team
 * 
 */
public interface IDomainTreeRepresentationWithMutability extends IDomainTreeRepresentation {
    /**
     * Getter of mutable "included properties" cache for internal purposes.
     * <p>
     * These properties are fully lazy. If some "root" has not been used -- it will not be loaded. This partly initialised stuff could be even persisted. After deserialisation lazy
     * mechanism can simply load missing stuff well.
     * 
     * @param root
     * @return
     */
    List<String> includedPropertiesMutable(final Class<?> root);

    /**
     * Getter of mutable "excluded properties" cache for internal purposes.
     * <p>
     * These properties are fully lazy. If some "root" has not been used -- it will not be loaded. This partly initialised stuff could be even persisted. After deserialisation lazy
     * mechanism can simply load missing stuff well.
     * 
     * @param root
     * @return
     */
    Set<Pair<Class<?>, String>> excludedPropertiesMutable();

    //    /**
    //     * TODO
    //     *
    //     * @param root
    //     * @param property
    //     * @return
    //     */
    //    boolean isExcludedNaturally(final Class<?> root, final String property);
}