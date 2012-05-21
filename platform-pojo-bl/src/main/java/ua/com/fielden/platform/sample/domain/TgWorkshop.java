package ua.com.fielden.platform.sample.domain;

import org.junit.Ignore;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.entity.validation.annotation.DefaultController;
import ua.com.fielden.platform.sample.domain.controller.ITgWorkshop;

@KeyType(String.class)
@MapEntityTo
@DescTitle("Description")
@Ignore
@DefaultController(ITgWorkshop.class)
public class TgWorkshop extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor for (@link EntityFactory}.
     */
    protected TgWorkshop() {
    }
}
