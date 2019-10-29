package ua.com.fielden.platform.sample.domain.compound.master.menu.actions;

import ua.com.fielden.platform.entity.AbstractFunctionalEntityForCompoundMenuItem;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.compound.TgCompoundEntity;
import ua.com.fielden.platform.utils.Pair;

/** 
 * Master entity object to model the main menu item of the compound master entity object.
 * 
 * @author TG Team
 *
 */
@KeyType(TgCompoundEntity.class)
@CompanionObject(ITgCompoundEntityMaster_OpenMain_MenuItem.class)
@EntityTitle("Tg Compound Entity Master Main Menu Item")
public class TgCompoundEntityMaster_OpenMain_MenuItem extends AbstractFunctionalEntityForCompoundMenuItem<TgCompoundEntity> {

    private static final Pair<String, String> entityTitleAndDesc = TitlesDescsGetter.getEntityTitleAndDesc(TgCompoundEntityMaster_OpenMain_MenuItem.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

}