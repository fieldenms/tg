/**
 * Performs Mocha web test for valid approximation of 'targetEditingValue' to 'approximatedEditingValue'.
 * Additionally checks 'concreteAcceptedMillis' if specified.
 */
export const correctlyApproximatesFor = masterGetter => (done, targetEditingValue, approximatedEditingValue, concreteAcceptedMillis) => {
    const master = masterGetter();
    const edProperty = master.$.editor_4_dateProp;
    if (concreteAcceptedMillis) {
        edProperty.timePortionToBecomeEndOfDay = true;
    }

    master.postValidated = function (validatedEntity, bindingEntity, customObject) {
        assert.strictEqual(edProperty._editingValue, approximatedEditingValue, "_editingValue should be correct.");
        assert.strictEqual(edProperty._commValue, approximatedEditingValue, "_commValue should be correct.");
        const acceptedValue = edProperty._acceptedValue;
        assert.isNotNull(acceptedValue, "_acceptedValue should be non-empty.");
        if (concreteAcceptedMillis) {
            assert.strictEqual(acceptedValue, concreteAcceptedMillis, "_acceptedValue should correspond to 23:59:59.999 millis of the day.");
        }

        assert.strictEqual(validatedEntity.get("dateProp"), acceptedValue, "'dateProp' value should be the same as acceptedValue.");
        assert.strictEqual(bindingEntity.get("dateProp"), acceptedValue, "Binding 'dateProp' value should be the same as acceptedValue.");

        done();
    };

    master.postRetrieved = function (entity, bindingEntity, customObject) {
        assert.strictEqual(edProperty._editingValue, '', "_editingValue should be empty on start (empty string).");
        assert.strictEqual(edProperty._commValue, '', "_commValue should be empty on start (empty string).");
        assert.strictEqual(edProperty._acceptedValue, null, "_acceptedValue should be empty on start (null).");

        assert.strictEqual(entity.get("dateProp"), null, "'property' value should be empty on start (null).");
        assert.strictEqual(bindingEntity.get("dateProp"), null, "Binding 'property' value should be empty on start (null).");

        edProperty._editingValue = targetEditingValue;
        edProperty.commit();
        assert.strictEqual(edProperty._editingValue, approximatedEditingValue, "_editingValue should be already approximated.");
        
        assert.strictEqual(edProperty._editorValidationMsg, null, 'Editor should not have error.');
    };

    master.retrieve();
};

/**
 * Performs Mocha web test for invalid approximation of 'targetEditingValue'.
 * The editing value must be left unchanged.
 */
export const incorrectlyApproximatesFor = masterGetter => (done, targetEditingValue) => {
    const master = masterGetter();
    const edProperty = master.$.editor_4_dateProp;

    master.postValidated = function (validatedEntity, bindingEntity, customObject) {
        throw 'Validation should not be invoked in case of incorrect date format.';
    };

    master.postRetrieved = function (entity, bindingEntity, customObject) {
        assert.strictEqual(edProperty._editingValue, '', "_editingValue should be empty on start (empty string).");
        assert.strictEqual(edProperty._commValue, '', "_commValue should be empty on start (empty string).");
        assert.strictEqual(edProperty._acceptedValue, null, "_acceptedValue should be empty on start (null).");

        assert.strictEqual(entity.get("dateProp"), null, "'property' value should be empty on start (null).");
        assert.strictEqual(bindingEntity.get("dateProp"), null, "Binding 'property' value should be empty on start (null).");

        edProperty._editingValue = targetEditingValue;
        edProperty.commit();
        assert.strictEqual(edProperty._editingValue, targetEditingValue, "_editingValue should remain the same -- the date is incorrect.");
        
        assert.strictEqual(edProperty._editorValidationMsg, 'The entered date is incorrect.', 'Editor should have error.');
        
        done();
    };

    master.retrieve();
};