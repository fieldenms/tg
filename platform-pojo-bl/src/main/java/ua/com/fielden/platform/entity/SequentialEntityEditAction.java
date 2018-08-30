package ua.com.fielden.platform.entity;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;

@KeyTitle("Sequentila edit")
@CompanionObject(ISequentialEntityEditAction.class)
public class SequentialEntityEditAction extends EntityEditAction {
}
