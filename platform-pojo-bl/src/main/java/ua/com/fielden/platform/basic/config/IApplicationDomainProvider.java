package ua.com.fielden.platform.basic.config;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that should be fulfilled in order to provide a definitive list of application specific domain entity types. This list should include both persistent and
 * non-persistent entity types. The contract requires developers to add new entity types at the end of the resultant list.
 * 
 * @author TG Team
 * 
 */
public interface IApplicationDomainProvider {
    List<Class<? extends AbstractEntity<?>>> entityTypes();
}
