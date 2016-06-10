package ua.com.fielden.platform.ui.menu.sample;

import org.apache.log4j.Logger;

import ua.com.fielden.platform.sample.domain.TgDeletionTestEntity;
import ua.com.fielden.platform.swing.review.annotations.EntityType;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

@EntityType(TgDeletionTestEntity.class)
public class MiDeletionTestEntity extends MiWithConfigurationSupport<TgDeletionTestEntity> {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(TgDeletionTestEntity.class);

    private static final String caption = "Entity for deletion test case";
    private static final String description = "<html>" + "<h3>Entity for deletion test case</h3>"
            + //
            "A facility to query Entity for deletion test case information.</html>";
}
