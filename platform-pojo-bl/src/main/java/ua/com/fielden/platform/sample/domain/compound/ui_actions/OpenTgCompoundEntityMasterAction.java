package ua.com.fielden.platform.sample.domain.compound.ui_actions;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityToOpenCompoundMaster;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityChild;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntityDetail;
import ua.com.fielden.platform.utils.Pair;

/** 
 * Open Master Action entity object.
 * 
 * @author TG Team
 *
 */
@KeyType(TgCompoundEntity.class)
@CompanionObject(IOpenTgCompoundEntityMasterAction.class)
@EntityTitle("Tg Compound Entity Master")
public class OpenTgCompoundEntityMasterAction extends AbstractFunctionalEntityToOpenCompoundMaster<TgCompoundEntity> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(OpenTgCompoundEntityMasterAction.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

    public static final String MAIN = "Main";
    public static final String TGCOMPOUNDENTITYDETAILS = TgCompoundEntityDetail.ENTITY_TITLE + "s"; // Please adjust manually if the plural form is not standard
    public static final String TGCOMPOUNDENTITYCHILDS = TgCompoundEntityChild.ENTITY_TITLE + "s"; // Please adjust manually if the plural form is not standard
}