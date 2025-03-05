const prop = 'hyperlinkProp';

const _checkInitialValues = (edProperty, entity, bindingEntity) => {
    assert.strictEqual(edProperty._editingValue, '', '_editingValue should be empty on start (empty string).');
    assert.strictEqual(edProperty._commValue, '', '_commValue should be empty on start (empty string).');
    assert.strictEqual(edProperty._acceptedValue, null, '_acceptedValue should be empty on start (null).');

    assert.strictEqual(entity.get(prop), null, 'Value should be empty on start (null).');
    assert.strictEqual(bindingEntity.get(prop), null, 'Binding value should be empty on start (null).');
};

const _apply = (edProperty, targetEditingValue) => {
    edProperty._editingValue = targetEditingValue;
    edProperty.commit();
};

/**
 * Performs Mocha web test for valid approximation of 'targetEditingValue' to 'approximatedEditingValue' with validation cycle.
 */
export const correctlyApproximatesFor = masterGetter => (done, targetEditingValue, approximatedEditingValue, concreteAcceptedValue) => {
    const master = masterGetter();
    const edProperty = master.$[`editor_4_${prop}`];

    master.postValidated = function (validatedEntity, bindingEntity, customObject) {
        assert.strictEqual(edProperty._editingValue, approximatedEditingValue);
        assert.strictEqual(edProperty._commValue, approximatedEditingValue);
        const acceptedValue = edProperty._acceptedValue;
        assert.isNotNull(acceptedValue, '_acceptedValue should be non-empty.');
        assert.deepEqual(acceptedValue, concreteAcceptedValue);
        assert.deepEqual(validatedEntity.get(prop), acceptedValue, 'Value should be the same as acceptedValue.');
        assert.deepEqual(bindingEntity.get(prop), acceptedValue, 'Binding value should be the same as acceptedValue.');
        done();
    };

    master.postRetrieved = function (entity, bindingEntity, customObject) {
        _checkInitialValues(edProperty, entity, bindingEntity);

        _apply(edProperty, targetEditingValue);

        assert.strictEqual(edProperty._editingValue, approximatedEditingValue, '_editingValue should be already approximated.');
        assert.strictEqual(edProperty._editorValidationMsg, null, 'Editor should not have error.');
    };

    master.retrieve();
};

/**
 * Performs Mocha web test for valid approximation of 'targetEditingValue' to 'approximatedEditingValue' with no validation cycle (value unchanged after approximation).
 */
export const correctlyApproximatesNoValidationFor = masterGetter => (done, targetEditingValue, approximatedEditingValue) => {
    const master = masterGetter();
    const edProperty = master.$[`editor_4_${prop}`];

    master.postValidated = function (validatedEntity, bindingEntity, customObject) {
        throw 'Validation should not be invoked in case of same value.';
    };

    master.postRetrieved = function (entity, bindingEntity, customObject) {
        _checkInitialValues(edProperty, entity, bindingEntity);

        _apply(edProperty, targetEditingValue);

        assert.strictEqual(edProperty._editingValue, approximatedEditingValue, '_editingValue should be already approximated.');
        assert.strictEqual(edProperty._editorValidationMsg, null, 'Editor should not have error.');
        done();
    };

    master.retrieve();
};

/**
 * Performs Mocha web test for valid approximation of 'targetEditingValue' to 'approximatedEditingValue' with validation cycle resulting in exception (e.g. during server-side Hyperlink creation in EntityResourceUtils.convert).
 */
export const correctlyApproximatesErroneousValidationFor = masterGetter => (done, targetEditingValue, approximatedEditingValue, concreteAcceptedValue, realValue, error) => {
    const master = masterGetter();
    const edProperty = master.$[`editor_4_${prop}`];

    master.async(function () { // need to place this on async to facilitate VALIDATE tg-action postActionError proper overriding
        master._postValidatedDefaultError = function (errorResult) {
            assert.strictEqual(errorResult.message, error);
            assert.strictEqual(edProperty._editingValue, approximatedEditingValue);
            assert.strictEqual(edProperty._commValue, approximatedEditingValue);
            const acceptedValue = edProperty._acceptedValue;
            assert.isNotNull(acceptedValue, '_acceptedValue should be non-empty.');
            assert.deepEqual(acceptedValue, concreteAcceptedValue);
            assert.deepEqual(master._currEntity.get(prop), realValue);
            assert.deepEqual(master._currBindingEntity.get(prop), acceptedValue, 'Binding value should be the same as acceptedValue.');
            done();
        };
    });

    master.postRetrieved = function (entity, bindingEntity, customObject) {
        _checkInitialValues(edProperty, entity, bindingEntity);

        _apply(edProperty, targetEditingValue);

        assert.strictEqual(edProperty._editingValue, approximatedEditingValue, '_editingValue should be already approximated.');
        assert.strictEqual(edProperty._editorValidationMsg, null, 'Editor should not have error.');
    };

    master.retrieve();
};

/**
 * Performs Mocha web test for invalid approximation of 'targetEditingValue'.
 * The editing value must be left unchanged.
 */
export const incorrectlyApproximatesFor = masterGetter => (done, targetEditingValue, concreteErrorMessage) => {
    const master = masterGetter();
    const edProperty = master.$[`editor_4_${prop}`];

    master.postValidated = function (validatedEntity, bindingEntity, customObject) {
        throw 'Validation should not be invoked in case of incorrect value.';
    };

    master.postRetrieved = function (entity, bindingEntity, customObject) {
        _checkInitialValues(edProperty, entity, bindingEntity);

        _apply(edProperty, targetEditingValue);

        assert.strictEqual(edProperty._editingValue, targetEditingValue, '_editingValue should remain the same -- the value is incorrect.');
        assert.strictEqual(edProperty._editorValidationMsg, concreteErrorMessage, 'Editor should have error.');
        done();
    };

    master.retrieve();
};