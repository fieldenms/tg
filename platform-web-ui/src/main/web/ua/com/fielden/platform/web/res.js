/**
 * Initialisation block. It has all children web components already initialised.
 */
attached: function () {
    console.timeEnd("ready-to-attached");
    console.warn("attached-to-attached-async");
    console.time("attached-to-attached-async");
    var self = this;
    self.async(function () {
        console.warn("tg-MiDriverImportRow-centre: attached async");
        console.timeEnd("attached-to-attached-async");

        var oldPostRetrieved = self.postRetrieved;
        self.postRetrieved = function (entity, bindingEntity, customObject) {
            oldPostRetrieved(entity, bindingEntity, customObject);
            console.log("postRetrieved");

            self.activeFilter = bindingEntity.get('driverImportRow_errorMarker') || '';
            self.openFileSuccess = (function (event) {
                var response = event.target.response;
                var sc = this.$.selection_criteria;
                var datasetEntity = sc._serialiser().deserialise(JSON.parse(response)).instance;
                var value = [datasetEntity.get('key')];

                sc.setEditorValue4PropertyFromConcreteValue('driverImportRow_dataset', value);
                sc.setEditorValue4PropertyFromConcreteValue('driverImportRow_errorMarker', '');
                this.activeFilter = '';
                return this.run();
            }).bind(self);
            self.filterEntities = (function (event) {
                var filterAttr = event.currentTarget.getAttribute('filter-attr');
                self.async(function () {
                    self.$.selection_criteria.setEditorValue4PropertyFromConcreteValue('driverImportRow_errorMarker', filterAttr);
                    self.activeFilter = filterAttr;
                    return self.run();
                }, 200);
            }).bind(self);
            self.isFilterActive = (function (activeFilter, filter) {
                return activeFilter === filter;
            }).bind(self);

        }.bind(self);

        self._handleScroll = self._handleScroll || (function (e) {
            this.fire("scroll-container", e.target);
        }).bind(self);

        // TODO smth. like this should be generated here:
        self.topLevelActions = [
            {
                preAction: function (action) {
                    console.log('preAction: Import');
                    action.modifyFunctionalEntity = (function (bindingEntity) {
                        action.modifyValue4Property('actionType', bindingEntity, 'IMPORT');
                    });
                    return true;
                },
                postActionSuccess: function (functionalEntity) {
                    console.log('postActionSuccess: Import', functionalEntity);
                    return self.run();
                },
                attrs: {
                    entityType: 'ua.com.fielden.import_util.action.ImportUtilityAction',
                    currentState: 'EDIT',
                    centreUuid: self.uuid,
                },
                postActionError: function (functionalEntity) {
                    console.log('postActionError: Import', functionalEntity);
                }
}

                    ];
        // TODO do we need to notify paths?
        // TODO do we need to notify paths?
        self.secondaryActions = [
            {
                preAction: function (action) {
                    console.log('preAction: Show Errors');
                    return true;
                },
                postActionSuccess: function (functionalEntity) {
                    console.log('postActionSuccess: Show Errors', functionalEntity);
                },
                attrs: {
                    entityType: 'ua.com.fielden.import_util.action.ImportUtilityErrorDetails',
                    currentState: 'EDIT',
                    centreUuid: self.uuid,
                    prefDim: {
                        'width': function () {
                            return 500
                        },
                        'height': function () {
                            return 200
                        },
                        'widthUnit': 'px',
                        'heightUnit': 'px'
                    },
                },
                postActionError: function (functionalEntity) {
                    console.log('postActionError: Show Errors', functionalEntity);
                }
            }
        ];

        self.insertionPointActions = [];
        self.primaryAction = [
            {
                preAction: function (action) {
                    console.log('preAction: Edit row entity');
                    return true;
                },
                postActionSuccess: function (functionalEntity) {
                    console.log('postActionSuccess: Edit row entity', functionalEntity);
                },
                attrs: {
                    entityType: 'ua.com.fielden.platform.sample.domain.MasterInDialogInvocationFunctionalEntity',
                    currentState: 'EDIT',
                    centreUuid: self.uuid,
                },
                postActionError: function (functionalEntity) {
                    console.log('postActionError: Edit row entity', functionalEntity);
                }
            }
        ];
        self.propActions = [];

    }, 1);
},