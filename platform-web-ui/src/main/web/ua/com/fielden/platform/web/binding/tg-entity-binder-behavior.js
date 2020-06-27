import '/resources/polymer/@polymer/polymer/polymer-legacy.js';
import {processResponseError, toastMsgForError} from '/resources/reflection/tg-ajax-utils.js';

export const TgEntityBinderBehavior = {

    properties: {
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        ////////////////////////////////////////// EXTERNAL PROPERTIES //////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These mandatory properties must be specified in attributes, when constructing descendant elements.  //
        // No default values are allowed in this case.														   //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * The id of the currently bound entity (or 'new' in case of entity without id, aka 'not yet persisted' one, or 'find_or_new' in case of one-2-one associations).
         *
         * Sets initially during tg-entity-binder (master or selection criteria) generation phase and then updates accordingly.
         */
        entityId: {
            type: String
        },

        /**
         * The state for the entity master.
         *
         * The master can be only in two states: EDIT and VIEW. The state EDIT
         * allows user to edit properties and use some actions (as it was defined in Java API with 'enabledWhen').
         *
         * The state VIEW allows user to review the entity properties and do some actions (as it was defined in
         * Java API with 'enabledWhen').
         *
         * The initial state can be VIEW or EDIT.
         */
        currentState: {
            type: String
            // reflectToAttribute: true -- why is this needed?
        },

        /**
         * Universal identifier of this element instance (used for pub / sub communication).
         *
         * It is either assigned from the outside or could be defined internally.
         * For example, entity masters have this property assigned in their ready callback, which can be replaced later with a different value if required.
         *
         * Property observer is used to report value changes in case one needs to chase undesired assignment side-effect.
         */
        uuid: {
            type: String,
            observer: "_uuidChanged"
        },

        /**
         * Custom callback that will be invoked after successfull retrieval / deserialisation and before actual promotion of the entity to the entity binder.
         *
         * This function-property should be mainly used for testing. That is why default function-property value is set -- it does just nothing.
         *
         * arguments: entity
         */
        preRetrieved: {
            type: Function,
            value: function () {
                return function (entity) {
                    return entity;
                };
            }
        },

        /**
         * Custom callback that will be invoked after successfull retrieval.
         *
         * arguments: entity, bindingEntity, customObject
         */
        postRetrieved: {
            type: Function
        },

        /**
         * Custom callback that will be invoked in case retrieval fails.
         *
         * arguments: errorResult
         */
        postRetrievedError: {
            type: Function
        },

        /**
         * Custom callback that will be invoked after successfull validation.
         *
         * arguments: validatedEntity, bindingEntity, customObject
         */
        postValidated: {
            type: Function
        },
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////////////////// INNER PROPERTIES ///////////////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix and default values specified in 'value' specificator of the property definition (or,       //
        //   alternatively, computing function needs to be specified). 									       //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        /**
         * The current fully-fledged entity, that is used to create '_currBindingEntity' and '_originalBindingEntity'.
         */
        _currEntity: {
            type: Object,
            value: null,
            notify: true
        },

        /**
         * Represents the view of original entity for binding.
         */
        _originalBindingEntity: {
            type: Object,
            value: null
        },

        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        //////////////////////////////// INNER PROPERTIES, THAT GOVERN CHILDREN /////////////////////////////////
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // These properties derive from other properties and are considered as 'private' -- need to have '_'   //
        //   prefix. 																				           //
        // Also, these properties are designed to be bound to children element properties -- it is necessary to//
        //   populate their default values in ready callback (to have these values populated in children)!     //
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        /**
         * Represents the view of current entity for binding.
         */
        _currBindingEntity: {
            type: Object,
            notify: true
        },

        /**
         * This modif holder is needed for lazy value conversion in concrete editors.
         */
        _previousModifiedPropertiesHolder: {
            type: Object
        },

        /**
         * Returns true in case where binding entity has modified properties, false otherwise.
         *
         * Please, note that this property recomputes and caches manually strictly when _currBindingEntity and _originalBindingEntity changes (_postEntityReceived method).
         *
         * Also, invalid entity value will be 'modified', even though the entities from server arrive with valid value only (+invalid validation result).
         * Client-side logic handles this gracefully in method _extractBindingView(entity, previousModifiedPropertiesHolder, prevCurrBindingEntity).
         *
         * This property can not be used in tg-selection-criteria-behavior due to the fact that it will be isModified from previous, not from original version of centre.
         * Currently _centreChanged property is used (see tg-entity-centre-behavior and tg-selection-criteria-behavior).
         */
        _bindingEntityModified: {
            type: Boolean,
            observer: '_bindingEntityModifiedChanged'
        },

        /**
         * Returns indication whether SAVE button should be enabled for the entity. Returns true in following cases:
         *
         * 1) the entity is not persistent (SAVE is enabled always);
         * 2) the entity is persistent but is not persisted yet (SAVE is enabled always);
         * 3) the entity is persistent, is persisted but and it is modified (SAVE is enabled always).
         *
         * Please, note that this property (analogously to _bindingEntityModified) recomputes and caches manually strictly when _currBindingEntity and _originalBindingEntity changes (_postEntityReceived method).
         */
        _bindingEntityNotPersistentOrNotPersistedOrModified: {
            type: Boolean,
            observer: '_bindingEntityNotPersistentOrNotPersistedOrModifiedChanged',
            notify: true
        },

        /**
         * '_createModifiedPropertiesHolder' function, that is designated for binding.
         */
        _createModifiedPropertiesHolder: {
            type: Function
        },

        /**
         * Default implementation for postValidated callback.
         * 
         * Note, that this method is not intended for overriding.
         * Use custom postValidated function for these purposes.
         */
        _postValidatedDefault: {
            type: Function
        },

        /**
         * Default implementation for unsuccessful postValidated callback.
         */
        _postValidatedDefaultError: {
            type: Function
        },

        /**
         * Default implementation for postRetrieved callback.
         */
        _postRetrievedDefault: {
            type: Function
        },

        /**
         * Default implementation for unsuccessful postRetrieved callback.
         */
        _postRetrievedDefaultError: {
            type: Function
        },

        /**
         * Default implementation for unsuccessful postSearched callback.
         */
        _postSearchedDefaultError: {
            type: Function
        },

        /**
         * This is the standard processor for 'core-response's and is applicable for all
         *   responses that contain java Result inside 'e.detail.response'.
         *
         * In case when result is not successful (i.e. has 'ex' inside) the toast message is shown
         *   and 'customErrorHandlerFor(result)' is invoked.
         *
         * In case when result is successful -- 'customHandlerFor(result.instance)' is invoked.
         *
         *
         * This is the function, that is designated for binding.
         */
        _processResponse: {
            type: Function
        },

        /**
         * This is the standard processor for 'core-response's and is applicable for all
         *   responses that contain java Result inside 'e.detail.response'.
         *
         * In case when result is not successful (i.e. has 'ex' inside) the toast message is shown
         *   and 'customErrorHandlerFor(result)' is invoked.
         *
         * In case when result is successful -- 'customHandlerFor(result.instance)' is invoked.
         *
         *
         * This is the function, that is designated for binding.
         */
        _processError: {
            type: Function
        },

        /**
         * Starts the process of entity validation (based on _currBindingEntity).
         *
         * This function is designated for binding.
         */
        validate: {
            type: Function
        },

        /**
         * The function that explicitly says that validation is skipped.
         *
         * This function is designated for binding.
         */
        doNotValidate: {
            type: Function
        },

        /**
         * Starts the process of entity retrieval (based on current entityId).
         *
         * This function is designated for binding.
         */
        retrieve: {
            type: Function
        },

        /**
         * Layout configuration (desktop) for this binder content (editors in case of 'tg-entity-master', criteria in case of 'tg-entity-centre').
         */
        _desktopLayout: {
            type: Array
        },

        /**
         * Layout configuration (tablet) for this binder content (editors in case of 'tg-entity-master', criteria in case of 'tg-entity-centre').
         */
        _tabletLayout: {
            type: Array
        },

        /**
         * Layout configuration (mobile) for this binder content (editors in case of 'tg-entity-master', criteria in case of 'tg-entity-centre').
         */
        _mobileLayout: {
            type: Array
        },

        _disablementCounter: {
            type: Number,
            value: 0
        },

        _validationPromise: {
            type: Object,
            observer: '_validationPromiseChanged'
        },

        /**
         * Current number of initiated requests as per validate() method.
         * 
         * This property counts only validation requests, not saving / running (other requests that apply values).
         * This is used to check whether returning validation results correspond to most recent validation request.
         * If not, these results should simply be ignored in favour to more fresh results.
         * 
         * There are 5 situations were several validation requests may occur:
         * 1. Two validations: close enough in time to be debounced (< 50ms), resulting in only one actual request;
         * 2. Two validations: first is started, second is started and first is aborted (in the sense of iron-ajax requests);
         * 3. Two validations: first is started, second is started, first returned and needs to be ignored due to the second request pending;
         * 4. Two validations: first is started, first returned, second is started, second returned;
         * 5. Two validations: first is started, second is started, second is returned, first is returned and needs to be ignored.
         * 
         * Counting the number of validation requests is used to ensure that only the result of the latest validation result is applied, which is most pertinent in cases 3 and 5 above.
         * This is needed to ensure consistency of the state for the entity being modified.
         *
         * There can be situations where the last (or any for that matter) validation request does not return (e.g. due to communication failure).
         * In such situations no intermediate validation results would be applied and entity would appear as if no validation was performed at all.
         * This should not be a problem per se due to validation that is performed as part of the save request.
         */
        _validationCounter: {
            type: Number,
            value: 0
        },

        /**
         * Returns true in case where there exist some properties which were edited (but not yet committed) by tg-editor-behavior.
         */
        _editedPropsExist: {
            type: Boolean,
            notify: true
        },

        /**
         * Indicates whether retrieval process has been initiated by using CTRL+R key or tapping on CANCEL/REFRESH button (or even programatically).
         * Strictly speaking it becomes 'true' when 'retrieve' function gets functioned.
         *
         * _retrievalInitiated is needed to disable master view only once during debouncing of multiple retrieval requests, which in turn will be turned into single ajax request,
         * and thus only single 'view enablement' action.
         *
         * Debouncing of retrieval actions is needed to avoid multiple concurrent retrieval requests. However it adds additional debouncing 50-second delay similarly
         * as in validation.
         */
        _retrievalInitiated: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether saving process has been initiated by using CTRL+S key or tapping on SAVE button (or even programatically).
         * Strictly speaking it becomes 'true' when 'save' function gets functioned.
         *
         * _savingInitiated is needed to disable master / selectionCriteria view only once during debouncing of multiple saving requests, which in turn will be turned into single ajax request,
         * and thus only single 'view enablement' action.
         *
         * Debouncing of saving actions is needed to avoid multiple concurrent saving requests. However it adds additional debouncing 50-second delay similarly
         * as in validation.
         */
        _savingInitiated: {
            type: Boolean,
            value: false
        },

        /**
         * Indicates whether continuation master appearance should be complemented with error toast (true) or with informational toast (false).
         */
        showContinuationsAsErrors: {
            type: Boolean,
            value: false
        }
    },

    observers: [
        '_editedPropsChanged(_currBindingEntity.@editedProps)',
        '_modificationsChanged(_bindingEntityNotPersistentOrNotPersistedOrModified, _editedPropsExist)'
    ],

    _validationPromiseChanged: function (newValidationPromise, oldValidationPromise) { },
    _bindingEntityModifiedChanged: function (new_bindingEntityModified, old_bindingEntityModified) { },
    _bindingEntityNotPersistentOrNotPersistedOrModifiedChanged: function (newValue, oldValue) { },
    _modificationsChanged: function (_bindingEntityNotPersistentOrNotPersistedOrModified, _editedPropsExist) { },

    _editedPropsChanged: function (editedProps) {
        this._editedPropsExist = typeof editedProps === 'undefined' ? false : Object.keys(editedProps).length !== 0;
        // console.debug('_bindingEntity (_editedPropsChanged) self = ', this.is, 'this.$._saveAction = ', (this.$._saveAction ? this.$._saveAction.is : 'undefined'), 'editedProps', editedProps, 'this._editedPropsExist', this._editedPropsExist);
    },

    /**
     * Nullifies previously bound to this master entity in preparation to rebind a brand new instance.
     */
    _resetState: function () {
        this._currEntity = null;
        this._currBindingEntity = null;
        this._originalBindingEntity = null;
    },

    /* Returns a function that accepts an instance of Attachment to start the download of the associated file. The passed in attachment must not be null.*/
    mkDownloadAttachmentFunction: function () {
        return attachment => {
            const openAsHyperLink = attachment.title.startsWith('https://') || attachment.title.startsWith('http://') ||
                attachment.title.startsWith('ftp://') || attachment.title.startsWith('ftps://') ||
                attachment.title.startsWith('mailto:')
            if (openAsHyperLink === true) {
                const win = window.open(attachment.title, '_blank');
                win.focus();
            } else {
                const self = this;
                const url = '/download-attachment/' + attachment.id + '/' + attachment.sha1;
                // AJAX approach to the file download
                const xhr = new XMLHttpRequest();
                xhr.open("GET", url, true);
                xhr.responseType = 'blob';
                xhr.onload = function (e) {
                    if (xhr.status === 200) {
                        console.log('File received', xhr.getResponseHeader('Content-Disposition'));
                        const bySemicolon = xhr.getResponseHeader('Content-Disposition').split(';');
                        const filename = bySemicolon.filter(part => part.includes('filename=')).map(part => part.split('=')[1])[0];
                        if (filename) {
                            // create a Blob object from the response and a URL for it
                            const blob = new Blob([xhr.response]);
                            const blobUrl = window.URL.createObjectURL(blob);
                            // the blob URL can be used for the <a> element for the user to download/open the file
                            const a = document.createElement("a");
                            a.href = blobUrl;
                            // let's perform decoding, followed by removal of leading and trailing double quotes if present
                            // browsers replace double quotes with _ automatically, which may corrupt the file extension
                            let trimmedFileName = decodeURIComponent(filename);
                            if (trimmedFileName.startsWith('"')) {
                                trimmedFileName = trimmedFileName.substring(1);
                            }
                            if (trimmedFileName.endsWith('"')) {
                                trimmedFileName = trimmedFileName.substring(0, trimmedFileName.length - 1);
                            }
                            a.download = trimmedFileName;
                            a.click();
                            // release the reference to the file by revoking the Object URL
                            window.URL.revokeObjectURL(blobUrl);
                        }
                    } else {
                        console.error('Error occurred when trying to download the attachment.', 'Error code:', xhr.status);
                        const reader = new FileReader();
                        reader.onload = function () {
                            const resultAsObj = JSON.parse(reader.result);
                            const result = self._serialiser().deserialise(resultAsObj);
                            self._openToastForError(result.message, toastMsgForError(self._reflector(), result), true);
                        }
                        reader.readAsText(xhr.response);
                    }

                }.bind(self);
                xhr.onerror = function (e) {
                    const msg = "Unknown error occured when sending request to download the attachment.";
                    console.error(msg, e);
                    self._openToastForError(msg, msg + ' Request status: ' + xhr.status, true);

                }.bind(self);
                xhr.send();
            }
        }
    },

    /**
     * Initialisation block. It has all children web components already initialised.
     */
    ready: function () {
        var self = this;

        self._bindingEntityModified = false;
        self._bindingEntityNotPersistentOrNotPersistedOrModified = false;
        self._editedPropsExist = false;
        self._resetState();

        self._createModifiedPropertiesHolder = (function () {
            var mph = this._extractModifiedPropertiesHolder(this._currBindingEntity, this._originalBindingEntity);
            return this._reset(mph);
        }).bind(self);

        self._provideExceptionOccured = (function (entity, exceptionOccured) {
            if (exceptionOccured !== null && this._reflector().isEntity(entity)) {
                entity._setExceptionOccured(exceptionOccured);
            }
        }).bind(self);

        self._processRetrieverResponse = function (e) {
            self._processResponse(e, "retrieve", function (entityAndCustomObject, exceptionOccured) {
                self._provideExceptionOccured(entityAndCustomObject[0], exceptionOccured);
                return self._postRetrievedDefault(entityAndCustomObject);
            });
        };

        self._processRetrieverError = function (e) {
            self._processError(e, "retrieve", function (errorResult) {
                return self._postRetrievedDefaultError(errorResult);
            });
        };

        self._processResponse = (function (e, name, customHandlerFor) {
            console.log("PROCESS RESPONSE");
            console.log(name, ": iron-response: status = ", e.detail.xhr.status, ", e.detail.response = ", e.detail.response);
            if (e.detail.xhr.status === 200) { // successful execution of the request
                e.detail.successful = true;
                var deserialisedResult = this._serialiser().deserialise(e.detail.response);

                if (this._reflector().isWarning(deserialisedResult)) {
                    console.warn(toastMsgForError(this._reflector(), deserialisedResult));
                    //this._openToastForError('Warning.', toastMsgForError(this._reflector(), deserialisedResult), false);
                } else {
                    // continue with normal handling of the result's instance
                    var deserialisedInstance = deserialisedResult.instance;
                    deserialisedResult.instance = null;
                    // Need to open toast message in case where the top-level result is unsuccessful -- this message will be shown BEFORE
                    //   other messages about validation errors of 'deserialisedInstance' or '... completed successfully'.
                    // Current logic of tg-toast will discard all other messages after this message, until this message dissapear.
                    if (this._reflector().isError(deserialisedResult)) {
                        console.log('deserialisedResult: ', deserialisedResult);
                        this._openToastForError(deserialisedResult.message, toastMsgForError(this._reflector(), deserialisedResult), !this._reflector().isContinuationError(deserialisedResult) || this.showContinuationsAsErrors);
                    }
                    e.detail.successful = customHandlerFor(deserialisedInstance, this._reflector().isError(deserialisedResult) ? deserialisedResult : null);
                    if (this._reflector().isError(deserialisedResult)) {
                        e.detail.successful = false;
                    }
                    console.log('CUSTOM HANDLER FOR RETURNED', e.detail.successful);
                    // in case of customHandlerFor not returning any result need to assign true to recognise event handling as successful by default
                    if (e.detail.successful !== false) {
                        e.detail.successful = true;
                        if (typeof deserialisedInstance.id !== 'undefined') {
                            e.detail.entityId = deserialisedInstance.get('id');
                            e.detail.entityPersistent = deserialisedInstance.type().isPersistent();
                            e.detail.entityContinuation = deserialisedInstance.type().isContinuation();
                        } else if (Array.isArray(deserialisedInstance) && this._reflector().isEntity(deserialisedInstance[0]) && typeof deserialisedInstance[0]['id'] !== 'undefined') {
                            e.detail.entityId = deserialisedInstance[0].get('id');
                        }
                    }
                }
            } else { // other codes
                var error = 'Request could not be dispatched.';
                console.warn(error);
                this._openToastForError(error, 'Most likely due to networking issues the request could not be dispatched to server. Please try again later.', true);
                // this is equivalent to server side error
                // threfore, this._postRetrievedDefaultError should be invoked
                this._postRetrievedDefaultError(error);
            }
        }).bind(self);

        self._processError = (function (e, name, customErrorHandlerFor) {
            processResponseError(e, this._reflector(), this._serialiser(), customErrorHandlerFor, this.toaster);
        }).bind(self);

        // calbacks, that will be bound by editor child elements:
        self.validate = (function () {
            const slf = this;
            slf._validationCounter += 1;
            console.log('validate initiated (', slf._validationCounter, ')');
            // it is extremely important to create 'holder' outside of the debouncing construction to create immutable data
            //   and pass it to debouncing function. The main reason for that is the following:
            //     'slf._currBindingEntity' instance inside the debounced function can be altered by the results
            //     of previous validations!
            const holder = slf._extractModifiedPropertiesHolder(slf._currBindingEntity, slf._originalBindingEntity);
            holder['@validationCounter'] = slf._validationCounter;
            // After the first 'validate' invocation arrives -- debouncer will wait 50 milliseconds
            //   for the next 'validate' invocation, and if it arrives -- the recent one will become as active ( and
            //   again will start waiting for 50 millis and so on).
            this.debounce('invoke-validation', function () {
                console.log('validate (', holder['@validationCounter'], ')');
                // cancel the 'invoke-validation' debouncer if there is any active one:
                this.cancelDebouncer('invoke-validation');
                // cancel previous validation before starting new one! The results of previous validation are irrelevant!
                slf._validator().abortValidationIfAny();
                // IMPORTANT: no need to check whether the _hasModified(holder) === true -- because the error recovery should happen!
                // (if the entity was not modified -- _validate(holder) will start the error recovery process)
                slf._validationPromise = slf._validateForDescendants(slf._reset(holder));
            }, 50);
        }).bind(self);

        self.doNotValidate = (function () {
            console.log("do not validate");
        }).bind(self);

        self._createRetrievalPromise = function (context) {
            this._reflector()._validateRetrievalContext(context, this.entityId);
            if (context) {
                console.debug(':MASTER:RETRIEVE1', '|type', this.entityType, '|id', this.entityId);
                console.debug(':MASTER:RETRIEVE2', '|cch or sih', context);
                const ser = this._serialiser().serialise(context);
                this._ajaxRetriever().body = JSON.stringify(ser);
            } else {
                console.debug(':MASTER:RETRIEVE', '|type', this.entityType, '|id', this.entityId);
                this._ajaxRetriever().body = null;
            }
            return this._ajaxRetriever().generateRequest().completes;
        }.bind(self);

        self.retrieve = (function (context) {
            const slf = this;
            if (!slf._retrievalInitiated) {
                slf._retrievalInitiated = true;
                slf.disableView();
            }

            return new Promise(function (resolve, reject) {
                slf.debounce('invoke-retrieval', function () {
                    // cancel the 'invoke-retrieval' debouncer if there is any active one:
                    slf.cancelDebouncer('invoke-retrieval');

                    // cancel previous validation requests except the last one -- if it exists then retrieval process will be chained on top of that last validation process,
                    // otherwise -- retrieval process will simply start immediately
                    const lastValidationPromise = slf._validator().abortValidationExceptLastOne();
                    if (lastValidationPromise !== null) {
                        console.warn("Retrieval is chained to the last validation promise...");
                        return resolve(lastValidationPromise
                            .then(function () {
                                return slf._createRetrievalPromise(context);
                            }));
                    }
                    return resolve(slf._createRetrievalPromise(context));
                }, 50);
            });
        }).bind(self);

        // calbacks, that will potentially be augmented by tg-action child elements:
        // 				retrieval:
        self._postRetrievedDefault = (function (entityAndCustomObject) {
            // console.timeEnd('actual-retrieval');
            var entity = this.preRetrieved(entityAndCustomObject[0]);
            var customObject = this._reflector().customObject(entityAndCustomObject);

            var msg = this._toastMsg("Refreshing", entity);
            this._openToast(entity, msg, !entity.isValid() || entity.isValidWithWarning(), msg, false);

            var newBindingEntity = this._postEntityReceived(entity, true);

            this._postRetrievedDefaultForDescendants(entity, newBindingEntity, customObject);
            // custom external action
            if (this.postRetrieved) {
                this.postRetrieved(entity, newBindingEntity, customObject);
            }

            this.enableView();
            this._retrievalInitiated = false;
            return true;
        }).bind(self);

        self._postRetrievedDefaultError = (function (errorResult) {
            // This function will be invoked after server-side error appear.
            // 'tg-action' will augment this function with its own '_afterExecution' logic (spinner stopping etc.).
            console.warn("SERVER ERROR: ", errorResult);

            const oldCurrBindingEntity = this._currBindingEntity;
            if (oldCurrBindingEntity == null) {
                this.fire('binding-entity-did-not-appear', errorResult);
            }

            // custom external action
            if (this.postRetrievedError) {
                this.postRetrievedError(errorResult);
            }
            this.enableView();
            this._retrievalInitiated = false;
            this.fire('binding-entity-retrieved-error', errorResult);
        }).bind(self);
        // 				validation:
        self._postValidatedDefault = (function (entityAndCustomObject) {
            const validatedEntity = entityAndCustomObject[0];
            const customObject = this._reflector().customObject(entityAndCustomObject);
            if (customObject['@validationCounter'] < this._validationCounter) { // ignore results that do not correspond to most recent validation request
                // note that _postValidatedDefault is used as customHandlerFor _processResponse;
                //  _processResponse has its own handling for error / warning Result instances;
                //  but in most cases the Result will be successful but the entity inside can be valid or not;
                //  that's why we believe that _postValidatedDefault method is sufficient for this logic to cover majority of cases;
                //  for the same reason, there is no need to ignore error results in _postValidatedDefaultError or its caller _processError.
                console.warn('Old validation results with number [' + customObject['@validationCounter'] + '] appeared and will be ignored. New results with number [' + this._validationCounter + '] are pending.');
                return;
            }
            console.log('validate received (', customObject['@validationCounter'], ')');
            if (!validatedEntity.isValid()) {
                const msg = this._toastMsg("Validation", validatedEntity);
                this._openToast(validatedEntity, msg, !validatedEntity.isValid() || validatedEntity.isValidWithWarning(), msg, false);
            }
            // in case where _continuations property exists (only in tg-entity-master) there is a need to reset continuations (they become stale after any change in initiating entity)
            if (typeof this._continuations === 'object') {
                this._continuations = {};
            }
            const newBindingEntity = this._postEntityReceived(validatedEntity, false);
            // custom external action
            if (this.postValidated) {
                this.postValidated(validatedEntity, newBindingEntity, customObject);
            }
        }).bind(self);

        self._postValidatedDefaultError = (function (errorResult) {
            // This function will be invoked after server-side error appear.
            // 'tg-action' will augment this function with its own '_afterExecution' logic (spinner stopping etc.).
            console.warn("SERVER ERROR: ", errorResult);
        }).bind(self);

        // 				searching:
        self._postSearchedDefaultError = (function (errorResult) {
            // This function will be invoked after server-side error appear.
            // 'tg-action' will augment this function with its own '_afterExecution' logic (spinner stopping etc.).
            console.warn("SERVER ERROR: ", errorResult);
        }).bind(this);

        //Toaster object Can be used in other components on binder to show toasts.
        self.toaster = {
            openToastForError : self._openToastForError.bind(self),
            openToast: self._openToast.bind(self),
            openToastWithoutEntity: self._openToastWithoutEntity.bind(self)
        };
    },

    ///////////// toast related //////////////////
    /**
     * Opens the toast with some message and with indication whether progress is started.
     */
    _openToast: function (entity, toastMsg, hasMoreInfo, moreInfo, showProgress) {
        this._toastGreeting().text = toastMsg;
        this._toastGreeting().hasMore = hasMoreInfo;
        this._toastGreeting().msgText = moreInfo;
        this._toastGreeting().showProgress = showProgress;
        this._toastGreeting().isCritical = false;
        if (hasMoreInfo) {
            if (!entity.isValid()) {
                // TODO is it still relevant? msgHeading
                // TODO is it still relevant? msgHeading
                // TODO is it still relevant? msgHeading
                this._toastGreeting().msgHeading = "Error";
                this._toastGreeting().isCritical = true;
            } else if (entity.isValidWithWarning()) {
                this._toastGreeting().msgHeading = "Warning";
            } else {
                this._toastGreeting().msgHeading = "Info";
            }
        }
        this._toastGreeting().show();
    },

    /**
     * Opens the toast with some message and with indication whether progress is started.
     */
    _openToastWithoutEntity: function (toastMsg, hasMoreInfo, moreInfo, showProgress) {
        this._toastGreeting().text = toastMsg;
        this._toastGreeting().hasMore = hasMoreInfo;
        this._toastGreeting().msgText = moreInfo;
        this._toastGreeting().showProgress = showProgress;
        this._toastGreeting().isCritical = false;
        if (hasMoreInfo) {
            this._toastGreeting().msgHeading = "Info";
        }
        this._toastGreeting().show();
    },

    /**
     * The inner function for validation to be overridden in descendants (for e.g. context-dependent validation in tg-entity-master)
     */
    _validateForDescendants: function (preparedModifHolder) {
        return this._validator().validate(preparedModifHolder);
    },

    /**
     * Opens the toast with some error message including full 'moreInfo' message.
     */
    _openToastForError: function (toastMsg, moreInfo, isCritical) {
        console.log('this._toastGreeting().isCritical = ', isCritical);
        this.fire('tg-error-happened', moreInfo);
        this._toastGreeting().isCritical = isCritical;
        this._toastGreeting().text = toastMsg;
        if (moreInfo) {
            this._toastGreeting().hasMore = true;
            this._toastGreeting().msgText = moreInfo;
        }
        this._toastGreeting().showProgress = false;
        // TODO is it still relevant? msgHeading
        // TODO is it still relevant? msgHeading
        // TODO is it still relevant? msgHeading
        this._toastGreeting().msgHeading = "Error";
        console.log('about to show ... this._toastGreeting().isCritical = ', isCritical);
        this._toastGreeting().show();
    },

    _toastMsgForErrorObject: function (errorObject) {
        var stack = errorObject.stack;
        return this._reflector().stackTraceForErrorObjectStack(stack);
    },

    _toastMsg: function (actionName, entity) {
        if (!entity.isValid()) {
            return entity.firstFailure().message;
        } else if (entity.isValidWithWarning()) {
            return entity.firstWarning().message;
        } else {
            return actionName + " completed successfully.";
        }
    },
    //////////////////////////////////////////////

    /**
     * The component for entity serialisation.
     */
    _serialiser: function () {
        throw "_serialiser: not implemented";
    },

    /**
     * The core-ajax component for entity retrieval.
     */
    _ajaxRetriever: function () {
        throw "_ajaxRetriever: not implemented";
    },

    /**
     * The tg-entity-validator component for entity validation.
     */
    _validator: function () {
        throw "_validator: not implemented";
    },

    /**
     * The reflector component.
     */
    _reflector: function () {
        throw "_reflector: not implemented";
    },

    /**
     * The toast component.
     */
    _toastGreeting: function () {
        throw "_toastGreeting: not implemented";
    },

    //////////////////////////////////////// RETRIEVAL ////////////////////////////////////////
    /**
     * This callback is intended to be invoked just before the custom 'postRetrieved' callback.
     * Override it in descendants to provide some custom behaviour.
     */
    _postRetrievedDefaultForDescendants: function (entity, newBindingEntity, customObject) { },

    //////////////////////////////////////// VALIDATION ////////////////////////////////////////
    _reset: function (modifiedPropertiesHolder) {
        delete modifiedPropertiesHolder['@modified']; // remove it not to serialise this purely technical property
        return modifiedPropertiesHolder;
    },

    _extractModifiedPropertiesHolder: function (bindingEntity, _originalBindingEntity) {
        const modPropHolder = {
            '@modified': false
        };
        const self = this;
        if (self._reflector().isEntity(bindingEntity)) {
            modPropHolder['id'] = bindingEntity.get('id');
            modPropHolder['version'] = bindingEntity['version'];
            modPropHolder['@@touchedProps'] = bindingEntity['@@touchedProps'].names.slice(); // need to perform array copy because bindingEntity['@@touchedProps'].names is mutable array (see tg-reflector.setAndRegisterPropertyTouch/tg_convertPropertyValue for more details of how it can be mutated)
            
            // function that converts arrays of entities to array of strings or otherwise return the same (or equal) value;
            // this is needed to provide modifHolder with flatten 'val' and 'origVal' arrays that do not contain fully-fledged entities but rather string representations of those;
            // this is because modifHolder deserialises as simple LinkedHashMap on server and inner values will not be deserialised as entities but rather as simple Java bean objects;
            // also, we do not support conversion of array of entities on the server side -- such properties are immutable from client-side editor perspective (see EntityResourceUtils.convert method with isEntityType+isCollectional conditions)
            const convert = value => Array.isArray(value) ? value.map(el => self._reflector().tg_convert(el)) : value;
            
            bindingEntity.traverseProperties(function (propertyName) {
                const value = convert(bindingEntity.get(propertyName));
                const originalValue = convert(_originalBindingEntity.get(propertyName));
                const valId = bindingEntity['@' + propertyName + '_id'];
                const origValId = _originalBindingEntity['@' + propertyName + '_id'];
                
                // VERY IMPORTANT: the property is considered to be 'modified'
                //                 in the case when its value does not equal to original value.
                //
                //                 The 'modified' property is marked by existence of 'val' sub-property.
                //
                //                 All modified properties will be applied on the server upon the validation prototype.
                if (!self._reflector().equalsEx(value, originalValue)) {
                    // the property is 'modified'
                    modPropHolder[propertyName] = {
                        'val': value,
                        'origVal': originalValue
                    };
                    modPropHolder['@modified'] = true;
                    if (typeof valId !== 'undefined') {
                        modPropHolder[propertyName]['valId'] = valId;
                    }
                } else {
                    // the property is 'unmodified'
                    modPropHolder[propertyName] = {
                        'origVal': originalValue
                    };
                }
                if (typeof origValId !== 'undefined') {
                    modPropHolder[propertyName]['origValId'] = origValId;
                }
            });
        }
        console.log('       _extractModifiedPropertiesHolder: modPropHolder', modPropHolder);
        return modPropHolder;
    },

    _idConvert: function (id) {
        return id === null ? "new" : ("" + id);
    },

    /**
     * Returns 'true' if the entity has been modified from original, 'false' otherwise.
     *
     * @param modifiedPropertiesHolder -- the entity with modified properties
     */
    _hasModified: function (modifiedPropertiesHolder) {
        return modifiedPropertiesHolder["@modified"];
    },

    //////////////////////////////////////// BINDING & UTILS ////////////////////////////////////////
    /**
     * Implements the default action to (re)bind freshly received entity. Entity receival is the result of actions Refresh, Validate, Save and Run.
     *
     * @param isRefreshingProcess -- value true indicates that the call happens as part of refresh lifecycle, which requires resetting the state.
     *                               In all other cases (validate, save, run) value false should be provided.
     */
    _postEntityReceived: function (entity, isRefreshingProcess) {
        var self = this;
        // in case entity is being retrieved need to reset the state, so that the master would behave as if it was created for the first time
        if (isRefreshingProcess) {
            self._resetState();
        }
        // After the entity has received, potentially its id has been updated:
        if (self._idConvert(entity.get('id')) !== self.entityId) {
            self.entityId = self._idConvert(entity.get('id'));
        }
        // extract previous version of modified properties holder, to merge it with new version of validated entity for invalida properties!
        var previousModifiedPropertiesHolder = null;
        if (self._currBindingEntity !== null) {
            previousModifiedPropertiesHolder = self._extractModifiedPropertiesHolder(self._currBindingEntity, self._originalBindingEntity);
            self._reset(previousModifiedPropertiesHolder);
        }
        const previousEntity = self._currEntity;
        // Determine whether the entity is stale in sense of stale property conflicts (errors).
        const isEntityStale = self._hasStaleConflicts(entity);
        if (isEntityStale) {
            // version of entity should be taken from previous entity to correctly restore stale entity at the client-side
            entity.version = self._extractPreviousEntityVersion(previousEntity, entity.version, self._originalBindingEntity);
        }
        // New entity should be promoted to the local cache:
        self._currEntity = entity;
        // before the next assignment -- the editors should be already prepared for "refresh cycle" (for Retrieve and Save actions)
        var oldCurrBindingEntity = self._currBindingEntity;
        self._previousModifiedPropertiesHolder = previousModifiedPropertiesHolder;
        self._currBindingEntity = self._extractBindingView(self._currEntity, previousModifiedPropertiesHolder, self._currBindingEntity);
        self._originalBindingEntity = self._extractOriginalBindingView(self._currEntity, isEntityStale ? self._originalBindingEntity : null);

        self._bindingEntityModified = self._hasModified(self._extractModifiedPropertiesHolder(self._currBindingEntity, self._originalBindingEntity));
        // console.debug('_bindingEntityModified = ', self._bindingEntityModified, ' type = ', self._currBindingEntity.type()._simpleClassName());
        self._bindingEntityNotPersistentOrNotPersistedOrModified = !self._currBindingEntity.type().isPersistent() ||
            !self._currBindingEntity.isPersisted() ||
            self._bindingEntityModified;
        // console.debug('_bindingEntityNotPersistentOrNotPersistedOrModified = ', self._bindingEntityNotPersistentOrNotPersistedOrModified, ' type = ', self._currBindingEntity.type()._simpleClassName());
        if (self._currBindingEntity != null && oldCurrBindingEntity == null) {
            self.fire('binding-entity-appeared', self._currBindingEntity);
        }
        console.log("       _postEntityReceived: _currBindingEntity + _originalBindingEntity", self._currBindingEntity, self._originalBindingEntity);
        return self._currBindingEntity;
    },

    /**
     * Returns 'true' in case where the entity has at least one stale conflict, 'false' otherwise.
     *
     * Please, note that in sense of versions the entity could be stale, but this method still returns 'false' if no stale conflicts exist,
     * and thus the entity will be treated as NOT 'stale'.
     * That means that fully resolvable staleness will not be appearing to the user -- this is the case where behind the scenes
     * the entity has been saved one or more times (by other user) but saving did not actually change the entity to the level of conflicts appearance.
     * However, warnings 'The property has been recently changed by another user.' could exist in this case.
     */
    _hasStaleConflicts: function (entity) {
        const self = this;
        var hasStaleConflicts = false;
        entity.traverseProperties(function (propertyName) {
            const validationResult = entity.prop(propertyName).validationResult();
            if (self._reflector().isError(validationResult) && (validationResult['@resultType'] === 'ua.com.fielden.platform.web.utils.PropertyConflict')) {
                hasStaleConflicts = true;
            }
        });
        return hasStaleConflicts;
    },

    /**
     * Extracts the version of previous entity for the case where new entity is stale.
     *
     * This method validates existence of previous entity / originalBindingEntity and the fact that the version has been increased.
     * These validations are required because new entity should not be stale otherwise.
     */
    _extractPreviousEntityVersion: function (previousEntity, currentEntityVersion, _originalBindingEntity) {
        if (previousEntity == null) {
            throw 'Previous version of entity does not exist, but somehow the stale entity has arrived from the server.';
        }
        if (currentEntityVersion <= previousEntity.version) {
            throw 'Previous version of entity has [' + previousEntity.version + '] version, that is not lower than the version of stale new entity (' + currentEntityVersion + ').';
        }
        if (_originalBindingEntity === null) {
            throw 'Previous version of _originalBindingEntity does not exist, but somehow the stale entity has arrived from the server.';
        }
        return previousEntity.version;
    },

    /**
     * Creates a binding view of the entity from its fully fledged representation.
     *
     * @param entity -- a fully fledged entity representation
     * @param previousModifiedPropertiesHolder -- a container holding original and current value for all entity properties used to represent invalid properties to the user;
     *                                            this container is always null for brand new entity instances that arrive from the sever for the first time (i.e. further client-server conversation re new instances should populate this container).
     */
    _extractBindingView: function (entity, previousModifiedPropertiesHolder, prevCurrBindingEntity) {
        const self = this;
        const bindingView = self._reflector().newEntity(entity.type().fullClassName());
        bindingView['id'] = entity.get('id');
        bindingView['version'] = entity['version'];
        // this property of the bindingView will hold the reference to fully-fledged entity,
        //   this entity can be used effectively to process 'dot-notated' properties (for e.g. retrieving the values)
        bindingView['@@origin'] = entity;
        // We use exactly the same object for touchedProps over long period of time up until saving (see tg-selection-criteria-behavior/tg-entity-master-behavior._postSavedDefault) -- then new object with empty arrays will be created;
        //  this single object resides in current version of currBindingEntity;
        //  mutation of this object's arrays occurs in tg-reflector.setAndRegisterPropertyTouch/tg_convertPropertyValue;
        //  we must copy these arrays (array.slice()) when using; at this stage the only place where they are used is function _extractModifiedPropertiesHolder.
        bindingView['@@touchedProps'] = prevCurrBindingEntity ? prevCurrBindingEntity['@@touchedProps'] : {
            names: [],
            values: [],
            counts: []
        };
        entity.traverseProperties(function (propertyName) {
            // value conversion of property value performs here only for specialised properties (see method '_isNecessaryForConversion');
            // conversion for other properties performs in corresponding editors (tg-editor-behavior).
            if (self._isNecessaryForConversion(propertyName)) {
                self._reflector().tg_convertPropertyValue(bindingView, propertyName, entity, previousModifiedPropertiesHolder);
            }
            // meta-state is provided for all properties, not only specialised
            if (self._reflector().isError(entity.prop(propertyName).validationResult())) {
                bindingView['@' + propertyName + '_error'] = entity.prop(propertyName).validationResult();
            } else {
                if (self._reflector().isWarning(entity.prop(propertyName).validationResult())) {
                    bindingView['@' + propertyName + '_warning'] = entity.prop(propertyName).validationResult();
                }
                bindingView['@' + propertyName + '_required'] = entity.prop(propertyName).isRequired();
            }
            // the following logic is required in both cases: property with error and without error
            if (entity.type().prop(propertyName).isUpperCase()) {
                bindingView['@' + propertyName + '_uppercase'] = true;
            }
            bindingView['@' + propertyName + '_editable'] = entity.prop(propertyName).isEditable();
        });
        return bindingView;
    },

    /**
     * Defines whether the property should be converted to binding entity representation regardless of existence of corresponding editor.
     */
    _isNecessaryForConversion: function (propertyName) {
        return ['columnParameters', // adjust column width action
            'entityType', 'importUri', 'elementName', 'entityId', // entity edit / new standard actions
            'pageCapacity', // export actions ('mime', 'fileName', 'data' props are not needed because post action success uses fully-fledged version of entity)
            'chosenIds', 'addedIds', 'removedIds', 'sortingVals', // collectional modification actions
            'visibleMenuItems', 'invisibleMenuItems', // menu visibility action (MenuSaveAction)
            'skipUi' // specialised property to control functional entity master appearance
        ].indexOf(propertyName) !== -1;
    },

    /**
     * Extracts binding view from 'entity' for its original values taking into account that entity could be 'stale'.
     *
     * In case of stale entity (previousEntity has been passed into this method), original values should be taken from the previous version of the entity to be able to mimic restoration of stale instance.
     */
    _extractOriginalBindingView: function (entity, previousOriginalBindingEntity) {
        const stale = previousOriginalBindingEntity !== null;
        const self = this;
        const originalBindingView = self._reflector().newEntityEmpty();
        
        originalBindingView['_type'] = entity['_type'];
        originalBindingView['id'] = entity.get('id');
        originalBindingView['version'] = entity['version'];
        // this property of the bindingView will hold the reference to fully-fledged entity,
        //   this entity can be used effectively to process 'dot-notated' properties (for e.g. retrieving the values)
        originalBindingView['@@origin'] = (stale === true ? self._reflector().tg_getFullEntity(previousOriginalBindingEntity) : entity);
        
        entity.traverseProperties(function (propertyName) {
            // value conversion of original property value performs here only for specialised properties (see method '_isNecessaryForConversion');
            // conversion for other properties performs in corresponding editors (tg-editor-behavior).
            if (self._isNecessaryForConversion(propertyName)) {
                self._reflector().tg_convertOriginalPropertyValue(originalBindingView, propertyName, self._reflector().tg_getFullEntity(originalBindingView));
            }
        });
        
        // console.log("       entity + originalBindingView", entity, bindingView);
        return originalBindingView;
    },

    /**
     * This uuid property observer has only an assistive role at this stage -- to track uuid changes if needed.
     * However, it has a potential to be used for subscribing to various message topics associated with a channel that has the same value as uuid.
     * In this case changes to uuid should handle unsubscribing to previously subscribed channels (i.e. oldValue).
     */
    _uuidChanged: function (newValue, oldValue) {
        if (oldValue !== undefined) {
            console.warn('Property uuid for element <', this.is, '> has changed from "', oldValue, '" to "', newValue, '".');
        }
    },

    /**
     * Sets the value of entity property ('propNameFromFuncEntityToAssign') to the property editor with propertyName 'propNameToBeAssigned'.
     */
    setEditorValue4Property: function (propNameToBeAssigned, entity, propNameFromFuncEntityToAssign) {
        const editor = this.$.masterDom.querySelector('[id=editor_4_' + propNameToBeAssigned + ']');
        editor.assignValue(entity, propNameFromFuncEntityToAssign, editor.reflector().tg_getBindingValueFromFullEntity.bind(editor.reflector()));
        editor.commit();
    },

    /**
     * Sets the value of entity property ('propNameFromFuncEntityToAssign') to the property editor with propertyName 'propNameToBeAssigned'.
     */
    setEditorValue4PropertyFromConcreteValue: function (propNameToBeAssigned, value) {
        var editor = this.$.masterDom.querySelector('[id=editor_4_' + propNameToBeAssigned + ']');
        editor.assignConcreteValue(value, editor.reflector().convert.bind(editor.reflector()));
        editor.commit();
    },

    disableView: function () {
        this._disablementCounter += 1;
        if (this._disablementCounter > 0 && this.currentState !== 'VIEW') {
            this.disableViewForDescendants();
        }
    },
    disableViewForDescendants: function () {
        this.currentState = 'VIEW';
        if (this.$.loader && this.$.loader.loadedElement) {
            this.$.loader.loadedElement.disableView();
        }
    },
    enableView: function () {
        if (this._disablementCounter > 0) {
            this._disablementCounter -= 1;
            if (this._disablementCounter === 0 && this.currentState !== 'EDIT') {
                this.enableViewForDescendants();
            }
        }
    },
    enableViewForDescendants: function () {
        this.currentState = 'EDIT';
        if (this.$.loader && this.$.loader.loadedElement) {
            this.$.loader.loadedElement.enableView();
        }
    }
};