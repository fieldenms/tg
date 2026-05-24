package ua.com.fielden.platform.sample.domain.crit_gen;

import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.EntityTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;

/// A non-synthetic entity whose title ends with ` Ext`.
///
/// Used by `CriteriaGeneratorTest` to verify that the ` Ext` suffix is *not* stripped from criteria titles for non-synthetic types —
/// the suffix is a convention of synthetic-based-on-persistent types only.
///
@KeyType(String.class)
@EntityTitle(value = "Non Synthetic Ext", desc = "Test fixture for criteria title resolution.")
public class NonSyntheticEntityWithExtTitle extends AbstractEntity<String> {
}