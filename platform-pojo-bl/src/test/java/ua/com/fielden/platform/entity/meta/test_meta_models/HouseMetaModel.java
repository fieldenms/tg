package ua.com.fielden.platform.entity.meta.test_meta_models;

import ua.com.fielden.platform.entity.meta.MetaModel;

public final class HouseMetaModel extends MetaModel {
    private static final String area_ = "area";
    private static final String insurance_ = "insurance";

    public final String area;
    public final InsuranceMetaModel insurance;

    public HouseMetaModel(String context) {
        super(context);
        this.area = joinContext(area_);
        this.insurance = new InsuranceMetaModel(joinContext(insurance_));
    }

    public HouseMetaModel() {
        this("");
    }
}
