package ua.com.fielden.platform.entity;

/**
 * There are situation where an entity, especially functional or ad hoc, does not have a suitable key.
 * Often in such cases either String or DynamicEntityKey is specified, which reduced the clarity of the intended model.
 * <p> 
 * It would be good if type {@link Void} was applicable, but it does not implemented {@link Comparable} as required by the key type definition.
 * <p>
 * That is why type {@link NoKey}, which may have single instance <code>NO_KEY</code>, is introduced and should be used as a key type parameter for entities that do not really need a key per se.
 * The key value <code>NO_KEY</code> is needed to be used as the <code>key</code> property is required by definition.
 * 
 * @author TG Team
 *
 */
public enum NoKey implements Comparable<NoKey> {
    NO_KEY;
}
