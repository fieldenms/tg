package ua.com.fielden.platform.swing.view;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * This master frame cache implementation is based on {@link WeakHashMap}, where key is the frame and the value is an entity ID. Therefore, when frame gets disposed, which happens
 * upon close, and GC runs the entry will be removed from the map.
 * 
 * @author TG Team
 * 
 */
public class WeakEntityMasterCache implements IEntityMasterCache {
    /**
     * The key in the cache is a frame instance. When frame gets disposed GC will remove the entry from the cache. The value in the cache is an entity ID, which is unique for all
     * entity types. It is used to find a corresponding master frame if present. The cache should be synchronised to correctly handle concurrent modification.
     */
    private final Map<BaseFrame, Long> cache = Collections.synchronizedMap(new WeakHashMap<BaseFrame, Long>());

    /**
     * Iterates through the cache searching for the value matching the passed in entity id. If found returns a corresponding key -- the master frame, otherwise -- null value.
     */
    @Override
    public BaseFrame get(final Long entityId) {
	if (entityId != null) {
	    for (final Map.Entry<BaseFrame, Long> entry : cache.entrySet()) {
		if (entityId.equals(entry.getValue())) {
		    final BaseFrame cachedFrame = entry.getKey();
		    if (cachedFrame != null && !cachedFrame.isDisposed()) {
			return entry.getKey();
		    }
		}
	    }
	}
	return null;
    }

    /**
     * Associates a frame with an entity ID. However, prior to that the check is made for an existing association. If it exists then a runtime exception is thrown identifying an
     * attempt to associate two different frames with the same entity.
     * <p>
     * There is a legitimate case when an attempt to make the same association can be made. Specifically, when a master was created and associated with an entity, then creation of
     * a new entity on that master is initiated (click the New button), while not finalised, user open master for the original entity again (a different instance), and then cancels
     * (clicks the Cancel button) on the original master frame. At this stage there is already a master frame, and cancellation would lead to an attempt to make another
     * association. UI model is responsible for handling this case gracefully.
     */
    @Override
    public void put(final BaseFrame frame, final Long entityId) {
	final BaseFrame cachedFrame = get(entityId);
	// let's just in case check if there is already another not disposed frame associated with this entity id
	if (cachedFrame != null && cachedFrame != frame && !cachedFrame.isDisposed()) {
	    throw new IllegalStateException("Master frame for entity with ID #" + entityId + " should not be cached more than once.");
	}
	// in all other cases associate the frame with entity id eve if this is a re-association with the same one
	cache.put(frame, entityId);
    }

    /**
     * Removes master frame associated with the provided entity ID from the cache.
     */
    @Override
    public void remove(final Long entityId) {
	cache.remove(get(entityId));
    }

    public Set<BaseFrame> all() {
	final Set<BaseFrame> set = new HashSet<BaseFrame>();
	for (final BaseFrame frame : cache.keySet()) {
	    if (frame != null && !frame.isDisposed()) {
		set.add(frame);
	    }
	}
	return set;
    }
}
