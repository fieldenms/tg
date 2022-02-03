package ua.com.fielden.platform.entity.meta.test_meta_models;

import ua.com.fielden.platform.entity.meta.MetaModel;

public final class VehicleMetaModel extends MetaModel {
    private static final String propertyNameThatIsErrorProneAndLong_ = "propertyNameThatIsErrorProneAndLong";
    private static final String insurance_ = "insurance";

    public final String propertyNameThatIsErrorProneAndLong;
    public final InsuranceMetaModel insurance;

    public VehicleMetaModel(String context) {
        super(context);
        this.propertyNameThatIsErrorProneAndLong = joinContext(propertyNameThatIsErrorProneAndLong_);
        this.insurance = new InsuranceMetaModel(joinContext(insurance_));
    }

    public VehicleMetaModel() {
        this("");
    }
}
