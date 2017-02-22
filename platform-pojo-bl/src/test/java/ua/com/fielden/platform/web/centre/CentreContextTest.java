package ua.com.fielden.platform.web.centre;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.factory.EntityFactory;
import ua.com.fielden.platform.entity.proxy.EntityProxyContainer;
import ua.com.fielden.platform.serialisation.jackson.entities.EntityWithOtherEntity;

/**
 * The test for {@link CentreContext}.
 * 
 * @author TG Team
 *
 */
public class CentreContextTest {

    @Test
    public void proxied_instances_remain_proxied() {
        final CentreContext<AbstractEntity<?>, AbstractEntity<?>> context = new CentreContext<>();
        
        final List<AbstractEntity<?>> originalEntities = new ArrayList<>();
        final AbstractEntity<?> originalEntity = EntityFactory.newPlainEntity(EntityProxyContainer.proxy(EntityWithOtherEntity.class, "prop"), 0L);
        originalEntities.add(originalEntity);
        
        assertEquals(1, originalEntity.proxiedPropertyNames().size());
        assertTrue(originalEntity.proxiedPropertyNames().contains("prop"));
        
        context.setSelectedEntities(originalEntities);
        
        final List<AbstractEntity<?>> entities = context.getSelectedEntities();
        final AbstractEntity<?> entity = entities.get(0);
        
        assertEquals(1, entity.proxiedPropertyNames().size());
        assertTrue(entity.proxiedPropertyNames().contains("prop"));
    }

}
