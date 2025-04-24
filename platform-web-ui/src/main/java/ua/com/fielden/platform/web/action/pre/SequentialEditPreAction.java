package ua.com.fielden.platform.web.action.pre;

import ua.com.fielden.platform.web.minijs.JsCode;
import ua.com.fielden.platform.web.view.master.api.actions.pre.IPreAction;

import static ua.com.fielden.platform.web.minijs.JsCode.jsCode;

/// This pre-action implementation should be used only with sequential edit action.
///
/// @author TG Team
public class SequentialEditPreAction implements IPreAction {

    /// Creates [SequentialEditPreAction].
    SequentialEditPreAction() {}

    @Override
    public JsCode build() {
        return jsCode("""
            if (!self.seqEditEntities && !self.$.egi.isEditing()) {
                if (self.$.egi.getSelectedEntities().length === 0) {
                    self.$.egi.selectAll(true);
                }
                self.seqEditEntities = self.$.egi.getSelectedEntities();
                const firstEntity = self.seqEditEntities.shift();
                action.currentEntity = () => firstEntity;
                action._oldRestoreActionState = action.restoreActionState;
                action.restoreActionState = function () {
                    action._oldRestoreActionState();
                    cancelEditing();
                }.bind(self);
                const cancelEditing = (function (data) {
                    delete this.seqEditEntities;
                    this.seqEditSuccessPostal.unsubscribe();
                    this.seqEditCancelPostal.unsubscribe();
                    const master = action._masterReferenceForTesting;
                    if (master) {
                        master.publishCloseForcibly();
                    }
                    action.currentEntity = () => null;
                    action.restoreActionState = action._oldRestoreActionState;
                }).bind(self);
                const updateCacheAndContinueSeqSaving = (function (shouldUnselect) {
                    const nextEntity = this.seqEditEntities && this.seqEditEntities.shift();
                    if (shouldUnselect !== false) {
                        self.$.egi.selectEntity(action.currentEntity(), false);
                    }
                    if (nextEntity) {
                        setEntityAndReload(nextEntity, shouldUnselect ? null : 'skipNext');
                    } else {
                        cancelEditing();
                    }
                }).bind(self);
                const setEntityAndReload = function (entity, spinnerInvoked) {
                    if (entity) {
                        action.currentEntity = () => entity;
                        const master = action._masterReferenceForTesting;
                        if (master) {
                            master.fire('tg-action-navigation-invoked', {spinner: spinnerInvoked});
                            master.savingContext = action._createContextHolderForAction();
                            master.retrieve(master.savingContext).then(function(ironRequest) {
                                if (action.modifyFunctionalEntity) {
                                    action.modifyFunctionalEntity(master._currBindingEntity, master, action);
                                }
                                master.addEventListener('binding-entity-loaded-and-focused', restoreNavigationButtonState);
                                master.save().then(function(value) {}, function (error) {
                                    fireNavigationChangeEvent(true);
                                }.bind(self));
                            }.bind(self), function (error) {
                                fireNavigationChangeEvent(true);
                            }.bind(self));
                        }
                     }
                }.bind(self),
                fireNavigationChangeEvent = function (shouldResetSpinner) {
                    const master = action._masterReferenceForTesting;
                    if (master) {
                        master.fire('tg-action-navigation-changed', {
                            shouldResetSpinner: shouldResetSpinner
                        });
                    }
                }.bind(self),
                restoreNavigationButtonState = function (e) {
                    fireNavigationChangeEvent(false);
                    const master = action._masterReferenceForTesting;
                    master.removeEventListener('binding-entity-loaded-and-focused', restoreNavigationButtonState);
                }.bind(self);
                action.continuous = true;
                action.skipNext = function() {
                    updateCacheAndContinueSeqSaving(false);
                };
                self.seqEditSuccessPostal = postal.subscribe({
                    channel: self.uuid,
                    topic: 'save.post.success',
                    callback: updateCacheAndContinueSeqSaving
                }).defer();
                self.seqEditCancelPostal = postal.subscribe({
                    channel: self.uuid,
                    topic: 'refresh.post.success',
                    callback: cancelEditing
                }).defer();
            }
        """);
    }

}
