package ua.com.fielden.platform.sample.domain.crit_gen;

import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.sample.domain.TgReVehicleModel;
import ua.com.fielden.platform.sample.domain.TgVehicleModel;

/// A synthetic-based-on-persistent entity whose title ends with ` Ext`.
///
/// It inherits its query model (`model_`) from [TgReVehicleModel], whose base type
/// [TgVehicleModel] is persistent — hence this type is recognised as synthetic-based-on-persistent.
/// Used by `CriteriaGeneratorTest` to verify that the ` Ext` suffix is stripped from criteria titles for such types.
///
@EntityTitle(value = "Synthetic Vehicle Model Ext", desc = "Test fixture for criteria title resolution.")
public class SyntheticBasedOnPersistentEntityWithExtTitle extends TgReVehicleModel {
}