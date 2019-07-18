package ua.com.fielden.platform.web.view.master.barcode;

import static java.lang.String.format;
import static ua.com.fielden.platform.web.centre.api.actions.impl.EntityActionBuilder.action;

import java.util.Optional;
import java.util.function.Supplier;

import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.BarCodeLocator;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.actions.IEntityActionBuilder3;
import ua.com.fielden.platform.web.centre.api.context.CentreContextConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionElement;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.post.IPostAction;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;
import ua.com.fielden.platform.web.view.master.exceptions.EntityMasterConfigurationException;

public class BarCodeLocatorAction <K extends AbstractEntity<?>> {

    private final Supplier<Optional<EntityActionConfig>> openMasterActionSupplier;

    BarCodeLocatorAction(final Supplier<Optional<EntityActionConfig>> openMasterActionSupplier) {
        this.openMasterActionSupplier = openMasterActionSupplier;
    }

    public IEntityActionBuilder3<AbstractEntity<?>> withContext(final CentreContextConfig context, final Class<K> entityMasterType) {
        return action(BarCodeLocator.class)
                .withContext(context)
                .preAction(createPreAction(entityMasterType))
                .postActionSuccess(createActionPostAction(entityMasterType));
    }

    private IPreAction createPreAction(final Class<K> entityMasterType) {
        return new IPreAction() {

            @Override
            public JsCode build() {
                return new JsCode(""
                        + "return new Promise(function (resolve, reject) {\n"
                        + "    const barcodeScanned = function () {\n"
                        + "        const hash = window.location.hash;\n"
                        + "        if(hash.includes('?__zx__=')) {\n"
                        + "            const ind = hash.indexOf('?__zx__=');\n"
                        + "            const barCode = hash.substring(ind + 8);\n"
                        + "            window.history.replaceState(window.history.state, '', window.location.href.substring(0, window.location.href.indexOf('?__zx__=')) + '?__scanid__=' +window.history.state.currIndex);\n"
                        + "            action.modifyFunctionalEntity = (funcEntity, master, action) => {\n"
                        + "                funcEntity.entityKey = barCode;\n"
                        + "                funcEntity.entityType = '" + entityMasterType.getName() + "';\n"
                        + "                window.removeEventListener('hashchange', barcodeScanned, false);\n"
                        + "                master.save();\n"
                        + "            };\n"
                        + "            resolve(barCode);\n"
                        + "        } else {\n"
                        + "            reject('Bar code was not scanned or scanned incorrectly');\n"
                        + "        }\n"
                        + "    };\n"
                        + "    window.addEventListener('hashchange', barcodeScanned, false);\n"
                        + "    const href = window.location.href;\n"
                        + "    if(navigator.userAgent.match(/Firefox/i)){\n"
                        + "        //Used for Firefox. If Chrome uses this, it raises the 'hashchanged' event only.\n"
                        + "        window.location.href =  ('zxing://scan/?ret=' + encodeURIComponent(href + '?__zx__={CODE}'));\n"
                        + "    } else {\n"
                        + "        //Used for Chrome. If Firefox uses this, it leaves the scan window open.\n"
                        + "        window.open('zxing://scan/?ret=' + encodeURIComponent(href + '?__zx__={CODE}'));\n"
                        +     "}\n"
                        + "});\n");
            }
        };
    }

    private IPostAction createActionPostAction(final Class<K> entityMasterType) {
        return () -> openMasterActionSupplier.get()
                    .map(BarCodeLocatorAction.this::generateFunctionalAction)
                    .map(BarCodeLocatorAction.this::generateCodeWithCustomAction)
                    .orElse(generateCodeWithDefaultAction(entityMasterType));
    }

    private FunctionalActionElement generateFunctionalAction(final EntityActionConfig actionConfig) {
        return new FunctionalActionElement(actionConfig, 0, FunctionalActionKind.TOP_LEVEL);
    }

    private JsCode generateCodeWithCustomAction(final FunctionalActionElement actionElement) {
        return new JsCode(generateCustomAction(actionElement) + generateActionInvoker());
    }

    private JsCode generateCodeWithDefaultAction(final Class<K> entityMasterType) {
        return new JsCode(generateDefaultAction(entityMasterType) + generateActionInvoker());
    }

    private String generateActionInvoker() {
        return "delete action.barcodeScanned;\n"
                + "delete action.modifyFunctionalEntity;\n"
                + "action.openMasterAction.currentEntity = functionalEntity.get('entity');\n"
                + "action.openMasterAction._run();\n";
    }

    private String generateDefaultAction(final Class<? extends AbstractEntity<?>> entityMasterType) {
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

    private String generateCustomAction(final FunctionalActionElement actionElement) {
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
