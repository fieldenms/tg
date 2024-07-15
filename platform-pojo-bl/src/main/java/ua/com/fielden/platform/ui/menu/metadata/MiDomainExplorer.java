package ua.com.fielden.platform.ui.menu.metadata;

import ua.com.fielden.platform.domain.metadata.DomainExplorer;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing centre for domain explorer entity.
 *
 * @author TG Team
 *
 */
@EntityType(DomainExplorer.class)
public class MiDomainExplorer extends MiWithConfigurationSupport<DomainExplorer> {

}
