package ua.com.fielden.platform.entity.meta.test_meta_models;

import ua.com.fielden.platform.entity.meta.MetaModel;

public final class InsuranceMetaModel extends MetaModel {
    private static final String cost_ = "cost";
    private static final String expirationDate_ = "expirationDate";
    
    public final String cost;
    public final String expirationDate;

    public InsuranceMetaModel(String context) {
        super(context);
        this.cost = joinContext(cost_);
        this.expirationDate = joinContext(expirationDate_);
    }

    public InsuranceMetaModel() {
        this("");
    }
}
