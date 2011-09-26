package ua.com.fielden.platform.entity.meta;

/**
 * Provides a contract for handling After Change Events (ACE).
 * It can be used for implementing a logic that assigns meta-property properties based on the value of the entity property it is associated with.
 * For example, {@link MetaProperty} has property <code>editable</code> the value for which should be determined based on some custom logic that could use a value of the associated with this meta-property entity
 * property.
 * <p>
 * <b>IMPORTANT:</b> <i>Implementations of this contract must be immutable due to caching of handler instances for reuse on multiple entities.</i>
 *
 * @author TG Team
 *
 */
public interface IAfterChangeEventHandler {
    /**
     * Should implement ACE handling logic.
     *
     * @param property -- meta property associated with a property, which is the source of ACE
     * @param entityPropertyValue -- the current value of the property
     */
    void handle(final MetaProperty property, final Object entityPropertyValue);
}
