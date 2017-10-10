package ua.com.fielden.platform.entity;

/**
 * There are situation where an entity, especially functional or ad hoc, does not have a suitable key.
 * Often in such cases either String or DynamicEntityKey is specified, which reduced the clarity of the intended model.
 * <p> 
 * It would be good if type {@link Void} was applicable, but it does not implemented {@link Comparable} as required by the key type definition.
 * <p>
 * That is why type {@link NoKey}, which may have no instances, is introduced and should be used as a key type parameter for entities that do not really need a key per se. 
 * 
 * @author TG Team
 *
 */
public final class NoKey implements Comparable<Void> {

    private NoKey() {}
    
    @Override
    public int compareTo(final Void novalue) {
        return 0;
    }

}
