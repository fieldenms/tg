package ua.com.fielden.platform.web.view.master.attachments;

import static ua.com.fielden.platform.web.centre.EntityCentre.IMPORTS;
import static ua.com.fielden.platform.web.view.master.EntityMaster.ENTITY_TYPE;
import static ua.com.fielden.platform.web.view.master.EntityMaster.flattenedNameOf;

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
                .clazz("relative")
                .attr("id", "imageLoader")
                .attr("src$", "[[_getImageUri(_linkCheckRes, _wasConfirmed, _attachmentUri)]]")
                .attr("hidden$", "[[!_isImageVisible(_loadingError, _attachmentUri)]]")
                .style("width:100%", "height:100%", "object-fit:contain", "background-color:white;");
        final DomElement messageElement =  new DomElement("span")
                .style("font-size: 18px", "color: #BDBDBD", "margin: 24px")
                .add(new InnerTextElement("[[_getAltImageText(_linkCheckRes, _wasConfirmed)]]"));
        final DomElement downloadAction = new DomElement("paper-button")
                .style("font-size: 14px", "font-weight: 500", "color: #000000DE")
                .attr("raised", true)
                .attr("on-tap", "_downloadOrOpenAttachment")
                .attr("tooltip-text", "[[_getButtonTooltip(_linkCheckRes)]]")
                .add(new DomElement("span").add(new InnerTextElement("[[_getButtonText(_linkCheckRes)]]")));
        final DomElement altImage = new DomElement("div")
                .style("background-color: white")
                .clazz("fit", "layout vertical center-center")
                .attr("hidden$", "[[_isImageVisible(_loadingError, _currBindingEntity)]]")
                .add(messageElement, downloadAction);
        final DomElement container = new DomElement("div")
                .attr("slot", "property-editors")
                .clazz("relative")
                .style("width:100%", "height:100%","padding:8px","box-sizing:border-box")
                .add(altImage, img);

        final String entityMasterStr = ResourceLoader.getText("ua/com/fielden/platform/web/master/tg-entity-master-template.js")
                .replace(IMPORTS, "import { isSupportedLink, openLink, canOpenLinkWithoutConfirmation, confirmLinkAndThen } from '/resources/components/tg-link-opener.js'")
                .replace(ENTITY_TYPE, flattenedNameOf(AttachmentPreviewEntityAction.class))
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
        return  """
                self.$.imageLoader.addEventListener('load', () => {
                    self._loadingError = false;
                });
                self.$.imageLoader.addEventListener('error', () => {
                    self._loadingError = true;
                });
                self._isNecessaryForConversion = function (propertyName) {
                    return ['attachment', 'attachmentUri'].indexOf(propertyName) >= 0;
                };
                self._handleBindingEntityChanged = function (e) {
                    this._updateAttachmentPreviewProperties(e.detail.value);
                }.bind(self);
                self._updateAttachmentPreviewProperties = function (attachment) {
                    this._linkCheckRes = null;
                    this._wasConfirmed = true;
                    this._attachmentUri = null;
                    if (attachment && attachment.attachmentUri) {
                        this._attachmentUri = attachment.attachmentUri;
                        if (isSupportedLink(this._attachmentUri)) {
                            this._linkCheckRes = canOpenLinkWithoutConfirmation(this._attachmentUri);
                            if (this._linkCheckRes && !this._linkCheckRes.canOpenWithoutConfirmation) {
                                this._wasConfirmed = false;
                            }
                        }
                    }
                }.bind(self);
                self.addEventListener('_curr-binding-entity-changed', self._handleBindingEntityChanged.bind(self));
                self._getImageUri = function (_linkCheckRes, _wasConfirmed, _attachmentUri) {
                    if (_linkCheckRes && !_wasConfirmed) {
                        return "broken_link";
                    }
                    return _attachmentUri;
                }.bind(self);
                self._getButtonText = function (_linkCheckRes) {
                    if (_linkCheckRes) {
                        return "OPEN";
                    }
                    return "DOWNLOAD";
                };
                self._getButtonTooltip = function (_linkCheckRes) {
                    if (_linkCheckRes) {
                        return "Opens the attachment.";
                    }
                    return "Downloads the attachment.";
                };
                self._isImageVisible = function (_loadingError, _attachmentUri) {
                    return !_loadingError && _attachmentUri;
                }.bind(self);
                self._getAltImageText = function (_linkCheckRes, _wasConfirmed) {
                    if (_linkCheckRes) {
                        if (!_wasConfirmed) {
                            return "This preview isn’t from a trusted source. Please confirm that you trust it by clicking the OPEN button below.";
                        }
                        return 'Preview is not available for this link attachment. Please open it instead.';
                    }
                    return 'Preview is not available for this file. Please download it instead.';
                }.bind(self);
                self.downloadAttachment = self.mkDownloadAttachmentFunction();
                self._downloadOrOpenAttachment = function (e) {
                    if (this._linkCheckRes) {
                        if (this._wasConfirmed) {
                            if (this._loadingError) {
                                //The link does not represent an image, which caused the loadingError.
                                //Open the link instead.
                                openLink(this._attachmentUri, this._linkCheckRes.target || "_blank");
                            }
                            //Otherwise image should be visible
                        } else { //If the link is not yet trusted, confirm it first.
                            // After the link becomes trusted, opening it as an image may cause an error,
                            // which means the link does not represent an image.
                            // In that case, the link should be opened as a regular one.
                            const afterImageErrorListener = e => {
                                openLink(this._attachmentUri, this._linkCheckRes.target || "_blank");
                                removeImageListeners();
                            };
                            const afterImageLoadListener = e => {
                                removeImageListeners();
                            };
                            const removeImageListeners = () => {
                                this.$.imageLoader.removeEventListener("error", afterImageErrorListener);
                                this.$.imageLoader.removeEventListener("load", afterImageLoadListener);
                            }
                            this.$.imageLoader.addEventListener("error", afterImageErrorListener);
                            this.$.imageLoader.addEventListener("load", afterImageLoadListener);
                            confirmLinkAndThen(this._linkCheckRes, opt => {
                                // Mark the attachment as confirmed if the user accepts it.
                                // The next assignment will trigger recalculation of the image URI and, consequently, image loading.
                                // Image loading may fail, indicating that the link should be opened as a regular one in the image loading error handler.
                                this._wasConfirmed = true;
                            });
                        }
                    } else {
                        this.downloadAttachment(this._currEntity.attachment);
                    }
                }.bind(self);
                """;
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
