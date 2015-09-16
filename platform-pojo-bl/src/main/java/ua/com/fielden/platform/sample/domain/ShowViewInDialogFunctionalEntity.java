package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/**
 * The functional entity, that shows any custom view in dialog.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle(value = "Key", desc = "Some key description")
@CompanionObject(IShowViewInDialogFunctionalEntity.class)
public class ShowViewInDialogFunctionalEntity extends AbstractFunctionalEntityWithCentreContext<String> {

    private static final long serialVersionUID = 3556013090749591966L;

}