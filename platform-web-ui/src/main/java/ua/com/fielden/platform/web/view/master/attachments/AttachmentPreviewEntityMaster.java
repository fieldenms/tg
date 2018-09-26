package ua.com.fielden.platform.web.view.master.attachments;

import java.util.Optional;

import ua.com.fielden.platform.attachment.AttachmentPreviewEntityAction;
import ua.com.fielden.platform.basic.IValueMatcherWithContext;
import ua.com.fielden.platform.dom.DomElement;
import ua.com.fielden.platform.dom.InnerTextElement;
import ua.com.fielden.platform.utils.ResourceLoader;
import ua.com.fielden.platform.web.centre.api.actions.EntityActionConfig;
import ua.com.fielden.platform.web.centre.api.resultset.impl.FunctionalActionKind;
import ua.com.fielden.platform.web.interfaces.IRenderable;
import ua.com.fielden.platform.web.view.master.api.IMaster;

public class AttachmentPreviewEntityMaster implements IMaster<AttachmentPreviewEntityAction> {

    private final IRenderable renderable;

    public AttachmentPreviewEntityMaster() {

        //Generating image element

        final DomElement img = new DomElement("img")
                .attr("src$", "[[_getImageUri(_currBindingEntity)]]")
                .attr("hidden$", "[[!_isImageVisisble(_currBindingEntity)]]")
                .style("width:100%", "height:100%", "object-fit:contain");
        final DomElement altImage = new DomElement("div")
                .clazz("fit", "layout horizontal center-center")
                .style("font-size: 18px", "color: #bdbdbd")
                .add(new InnerTextElement("[[_getAltImageText(_currBindingEntity)]]"));
        final DomElement container = new DomElement("div")
                .clazz("property-editors", "relative")
                .style("width:100%", "height:100%","padding:8px","box-sizing:border-box")
                .add(altImage, img);

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.html")
                .replace("<!--@imports-->", "")
                .replace("@entity_type", AttachmentPreviewEntityAction.class.getSimpleName())
                .replace("<!--@tg-entity-master-content-->", container.toString())
                .replace("//@ready-callback", generateReadyCallback())
                .replace("@prefDim", "null")
                .replace("@noUiValue", "false")
                .replace("@saveOnActivationValue", "true");

        renderable = new IRenderable() {
            @Override
            public DomElement render() {
                return new InnerTextElement(entityMasterStr);
            }
        };
    }

    private String generateReadyCallback() {
        return  "self._isNecessaryForConversion = function (propertyName) { \n"
                + "    return ['attachment'].indexOf(propertyName) >= 0; \n"
                + "}; \n"
                + "self._getImageUri = function (entity) {\n"
                + "    const newEntity = entity ? entity['@@origin'] : null;\n"
                + "    if (newEntity && newEntity.attachmentUri) {\n"
                + "        return newEntity.attachmentUri;\n"
                + "    }\n"
                + "}.bind(self);\n"
                + "self._isImageVisisble = function (entity) {\n"
                + "    const newEntity = entity ? entity['@@origin'] : null;\n"
                + "    if (newEntity && !newEntity.attachmentUri) {\n"
                + "        return false;\n"
                + "    }\n"
                + "    return true;\n"
                + "}.bind(self);\n"
                + "self._getAltImageText = function (entity) {\n"
                + "    const newEntity = entity ? entity['@@origin'] : null;\n"
                + "    if (newEntity && !newEntity.attachmentUri) {\n"
                + "        return 'Unsupported image type';\n"
                + "    }\n"
                + "    return '';\n"
                + "}.bind(self);\n";
    }

    @Override
    public Optional<Class<? extends IValueMatcherWithContext<AttachmentPreviewEntityAction, ?>>> matcherTypeFor(final String propName) {
        return Optional.empty();
    }

    @Override
    public IRenderable render() {
        return renderable;
    }

    @Override
    public EntityActionConfig actionConfig(final FunctionalActionKind actionKind, final int actionNumber) {
        throw new UnsupportedOperationException("Getting an action configuration is not supported.");
    }
}
