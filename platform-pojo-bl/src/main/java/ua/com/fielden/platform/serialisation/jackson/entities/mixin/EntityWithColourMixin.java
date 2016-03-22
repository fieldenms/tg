package ua.com.fielden.platform.serialisation.jackson.entities.mixin;

import ua.com.fielden.platform.serialisation.jackson.entities.IEntityWithColour;

/** 
 * Mixin implementation for companion object {@link IEntityWithColour}.
 * 
 * @author Developers
 *
 */
public class EntityWithColourMixin {
    
    private final IEntityWithColour companion;
    
    public EntityWithColourMixin(final IEntityWithColour companion) {
        this.companion = companion;
    }
    
}