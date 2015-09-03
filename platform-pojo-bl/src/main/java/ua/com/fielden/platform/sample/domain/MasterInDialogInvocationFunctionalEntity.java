package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * The functional entity, that executes invocation of corresponding entity master in dialog for centre row entity.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IMasterInDialogInvocationFunctionalEntity.class)
public class MasterInDialogInvocationFunctionalEntity extends MasterInvocationFunctionalEntity {
    private static final long serialVersionUID = 4124624171309887379L;

}