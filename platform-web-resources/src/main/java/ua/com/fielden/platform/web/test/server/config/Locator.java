package ua.com.fielden.platform.web.test.server.config;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.AbstractFunctionalEntityWithCentreContext;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder3;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.ILayout.Device;
import ua.com.fielden.platform.web.layout.api.impl.LayoutComposer;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.IMaster;
import ua.com.fielden.platform.web.view.master.api.actions.MasterActions;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.impl.SimpleMasterBuilder;
import ua.com.fielden.platform.web.view.master.api.widgets.IAutocompleterConfig;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

/**
 * The factory class that creates entity locator action and master.
 *
 * @author TG Air Team
 *
 * @param <T>
 */
public class Locator<T extends AbstractFunctionalEntityWithCentreContext<?>> {

    private final Class<AbstractEntity<?>> entityMasterType;
    private final Class<T> locatorEntityType;
    private final String propertyName;
    private final Supplier<Optional<EntityActionConfig>> openMasterActionSupplier;
    public final IMaster<T> masterConfig;
    
    Locator(final Class<AbstractEntity<?>> entityMasterType,
                    final Class<T> locatorEntityType, 
                    final String propertyName,
                    final Optional<Class<? extends IValueMatcherWithContext<T, ?>>> matcherType,
                    final Supplier<Optional<EntityActionConfig>> openMasterActionSupplier) {
        this.entityMasterType = entityMasterType;
        this.openMasterActionSupplier = openMasterActionSupplier;
        this.locatorEntityType = locatorEntityType;
        this.propertyName = propertyName;
        final String layout = LayoutComposer.mkVarGridForMasterFitWidth(1);
        
        final IAutocompleterConfig<T> beginWith = new SimpleMasterBuilder<T>().forEntity(locatorEntityType).addProp(propertyName).asAutocompleter();
        masterConfig = matcherType.map(beginWith::withMatcher).orElse(beginWith)
                .also()
                .addAction(MasterActions.REFRESH).shortDesc("Cancel").longDesc("Cancel action")
                .addAction(MasterActions.SAVE).shortDesc("Open").longDesc("Open master")
                .setActionBarLayoutFor(Device.DESKTOP, Optional.empty(), LayoutComposer.mkActionLayoutForMaster())
                .setLayoutFor(Device.DESKTOP, Optional.empty(), layout)
                .setLayoutFor(Device.TABLET, Optional.empty(), layout)
                .setLayoutFor(Device.MOBILE, Optional.empty(), layout)
                .done();
    }

    public IEntityActionBuilder3<AbstractEntity<?>> withContext(final CentreContextConfig context) {
        return action(locatorEntityType)
                .withContext(context)
                .postActionSuccess(createActionPostAction());
    }

    private IPostAction createActionPostAction() {
        return () -> openMasterActionSupplier.get()
                    .map(Locator::generateFunctionalAction)
                    .map(Locator.this::generateCodeWithCustomAction)
                    .orElse(generateCodeWithDefaultAction(entityMasterType));
    }

    private static FunctionalActionElement generateFunctionalAction(final EntityActionConfig actionConfig) {
        return new FunctionalActionElement(actionConfig, 0, FunctionalActionKind.TOP_LEVEL);
    }

    private JsCode generateCodeWithCustomAction(final FunctionalActionElement actionElement) {
        return new JsCode(generateCustomAction(actionElement) + generateActionInvoker());
    }

    private JsCode generateCodeWithDefaultAction(final Class<AbstractEntity<?>> entityMasterType) {
        return new JsCode(generateDefaultAction(entityMasterType) + generateActionInvoker());
    }

    private String generateActionInvoker() {
        return "action.openMasterAction.currentEntity = functionalEntity.get('" + propertyName + "');\n"
                + "action.openMasterAction._run();\n";
    }

    private static String generateDefaultAction(final Class<? extends AbstractEntity<?>> entityMasterType) {
        final String entityTitle = TitlesDescsGetter.getEntityTitleAndDesc(entityMasterType).getKey();
        return "if (!action.openMasterAction) {\n"
             + "    action.openMasterAction = document.createElement('tg-ui-action');\n"
             + "    action.openMasterAction.shortDesc = '" + format("Edit %s", entityTitle) + "';\n"
             + "    action.openMasterAction.longDesc = '" + format("Edit %s", entityTitle) + "';\n"
             + "    action.openMasterAction.componentUri = '/master_ui/ua.com.fielden.platform.entity.EntityEditAction';\n"
             + "    action.openMasterAction.elementName = 'tg-EntityEditAction-master';\n"
             + "    action.openMasterAction.showDialog = self._showDialog;\n"
             + "    action.openMasterAction.createContextHolder = self._createContextHolder;\n"
             + "    action.openMasterAction.attrs = {\n"
             + "        entityType: 'ua.com.fielden.platform.entity.EntityEditAction',\n"
             + "        currentState: 'EDIT',\n"
             + "        centreUuid: self.uuid"
             + "    };\n"
             + "    action.openMasterAction.requireSelectionCriteria = 'false';\n"
             + "    action.openMasterAction.requireSelectedEntities = 'ONE';\n"
             + "    action.openMasterAction.requireMasterEntity = 'false';\n"
             + "}\n";
    }

    private static String generateCustomAction(final FunctionalActionElement actionElement) {
        final DomElement element = actionElement.render();
        return "if (!action.openMasterAction) {\n"
                + "    action.openMasterAction = document.createElement('tg-ui-action');\n"
                + "    action.openMasterAction.shortDesc = '" + element.getAttr("short-desc").value + "';\n"
                + "    action.openMasterAction.longDesc = '" + element.getAttr("long-desc").value + "';\n"
                + "    action.openMasterAction.componentUri = '" + element.getAttr("component-uri").value + "';\n"
                + "    action.openMasterAction.elementName = '" + element.getAttr("element-name").value + "';\n"
                + "    action.openMasterAction.showDialog = self._showDialog;\n"
                + "    action.openMasterAction.createContextHolder = self._createContextHolder;\n"
                + "    action.openMasterAction.attrs = {\n"
                + "        entityType: '" + actionElement.conf().functionalEntity.orElseThrow(() -> new EntityMasterConfigurationException("Functional entity is missing.")).getName() + "',\n"
                + "        currentState: 'EDIT',\n"
                + "        centreUuid: self.uuid"
                + actionElement.conf().prefDimForView
                    .map(prefDim -> format(",%n        prefDim: {'width': function() {return %s}, 'height': function() {return %s}, 'widthUnit': '%s', 'heightUnit': '%s'}%n", prefDim.width, prefDim.height, prefDim.widthUnit.value, prefDim.heightUnit.value))
                    .orElse("\n")
                + "    };\n"
                + "    action.openMasterAction.requireSelectionCriteria = 'false';\n"
                + "    action.openMasterAction.requireSelectedEntities = 'ONE';\n"
                + "    action.openMasterAction.requireMasterEntity = 'false';\n"
                + "}\n";
    }

}
