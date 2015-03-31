package ua.com.fielden.platform.web.centre.api;

import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.actionOff;
import static ua.com.fielden.platform.web.centre.api.context.impl.EntityCentreContextSelector.context;
import static ua.com.fielden.platform.web.centre.api.resultset.PropDef.mkProp;
import ua.com.fielden.platform.basic.autocompleter.FallbackValueMatcherWithCentreContext;
import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgWorkOrder;
import ua.com.fielden.platform.web.centre.api.top_level_actions.ICentreTopLevelActions;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;

/**
 * This contract is an entry point for an Entity Centre aPI -- an embedded domain specific language for constructing entity centres.
 *
 * @see <a href="https://github.com/fieldenms/tg/issues/140">Specification</a>
 *
 * @author TG Team
 *
 */
public interface IEntityCentreBuilder<T extends AbstractEntity<?>> {

    /**
     * Entity centre construction DSL entry point.
     *
     * @param type
     * @return
     */
    ICentreTopLevelActions<T> forEntity(Class<T> type);


    /**
     * This is just an example for Entity Centre DSL.
     *
     * @param args
     */
    public static void build(final IEntityCentreBuilder<TgWorkOrder> ecb) {
       ecb.forEntity(TgWorkOrder.class)
       .addTopAction(null)
       .also()
       .beginTopActionsGroup("grop description")
           .addTopAction(action(null).withContext(context().withCurrentEntity().withSelectionCrit().build()).build())
           .addTopAction(action(null).withContext(context().withSelectionCrit().withSelectedEntities().build()).build())
       .endTopActionsGroup()
       .also()
       .beginTopActionsGroup("group 2")
           .addTopAction(null)
           .addTopAction(null)
       .endTopActionsGroup()
       .addCrit("status").asMulti().autocompleter().withMatcher(MyClass.class)
       .also()
       .addCrit("intValue").asRange().integer()
       .also()
       .addCrit("intValueCritOnly").asSingle().integer()
       .also()
       .addCrit("statusCritOnly").asSingle().autocompleter()
       .setLayoutFor(Device.DESKTOP, null, ("['vertical', 'justified', 'margin:20px', "
               + "[[mr], [mr], [mr], [mr], [mr]], "
               + "[[mr], [mr], [mr], [mr], [mr]]]"))
       .setLayoutFor(Device.TABLET, null, ("['vertical', 'justified', 'margin:20px', "
               + "[[mr], [mr], [mr], [mr], [mr]], "
               + "[[mr], [mr], [mr], [mr], [mr]]]"))
       .setLayoutFor(Device.MOBILE, null, ("['vertical', 'justified', 'margin:20px', "
               + "[[mr], [mr], [mr], [mr], [mr]], "
               + "[[mr], [mr], [mr], [mr], [mr]]]"))
       .addProp("status").withAction(null)
       .also()
       .addProp("status").order(3).desc().withAction(null)
       .also()
       .addProp(mkProp("ON", "Defect ON road", "ON")).withAction(action(null).withContext(context().withCurrentEntity().withSelectionCrit().build()).build())
       .also()
       .addProp(mkProp("OF", "Defect OFF road", "OF")).withAction(actionOff().build())
       .also()
       .addProp(mkProp("IS", "In service", "IS")).withAction(null)
       .addPrimaryAction(action(null).withContext(context().withCurrentEntity().withSelectionCrit().build()).icon("name").longDesc("tooltip text").build())
       .also()
       .addSecondaryAction(null)
       .also()
       .addSecondaryAction(null)
       .setCustomPropsValueAssignmentHandler(null)
       .setRenderingCustomiser(null);


    }

    // TODO Serves for an API example purposes. Should be removed as soon as API gets implemented.
    public static class MyClass extends FallbackValueMatcherWithCentreContext<TgWorkOrder> {

        public MyClass(final IEntityDao<TgWorkOrder> dao) {
            super(dao);
            // TODO Auto-generated constructor stub
        }

    }

}
