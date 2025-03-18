package ua.com.fielden.platform.basic.config;

import java.util.List;

import ua.com.fielden.platform.entity.AbstractEntity;

/**
 * A contract that should be fulfilled to provide a definitive list of application-specific domain entity types.
 * This list should include both persistent and non-persistent entity types.
 * The contract requires developers to add new entity types at the end of the resultant list.
 * <p>
 * Since the implementation of issue <a href="https://github.com/fieldenms/tg/issues/1914">"ApplicationDomain: verify or generate"</a>,
 * application-specific implementations of {@link IApplicationDomainProvider} are generated automatically.
 * 
 * @author TG Team
 * 
 */
public interface IApplicationDomainProvider {
    List<Class<? extends AbstractEntity<?>>> entityTypes();
}
