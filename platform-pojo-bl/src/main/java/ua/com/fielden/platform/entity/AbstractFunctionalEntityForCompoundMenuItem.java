package ua.com.fielden.platform.entity;

/**
 * A base class for functional entities that are intended to be used on compound master as menu item.
 * 
 * @author TG Team
 *
 * @param <K>
 */
public abstract class AbstractFunctionalEntityForCompoundMenuItem<K extends Comparable<?>> extends AbstractFunctionalEntityWithCentreContext<K> {
    private static final long serialVersionUID = 1L;
}
