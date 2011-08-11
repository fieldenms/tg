package ua.com.fielden.platform.swing.components.bind.test;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.KeyType;

@KeyType(String.class)
@DisplayDescription
public class DemoAbstractEntity extends AbstractEntity<String> {
    private static final long serialVersionUID = 1L;
}
