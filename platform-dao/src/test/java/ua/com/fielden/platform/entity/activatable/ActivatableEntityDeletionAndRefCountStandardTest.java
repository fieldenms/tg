package ua.com.fielden.platform.entity.activatable;

import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSystem;

public class ActivatableEntityDeletionAndRefCountStandardTest extends AbstractActivatableEntityDeletionAndRefCountTestCase {

    private final Spec1 spec1 = new Spec1<TgSystem, TgCategory>() {
        private int categoryKeyCounter = 1;
        private int systemKeyCounter = 1;

        @Override
        public TgSystem newA() {
            return new_(TgSystem.class, "SYS%s".formatted(systemKeyCounter++));
        }

        @Override
        public TgCategory newB() {
            return new_(TgCategory.class, "CAT%s".formatted(categoryKeyCounter++));
        }

        @Override
        public Class<TgSystem> aType() {
            return TgSystem.class;
        }

        @Override
        public Class<TgCategory> bType() {
            return TgCategory.class;
        }

        @Override
        public CharSequence A_b1() {
            return "category";
        }

        @Override
        public CharSequence A_b2() {
            return "firstCategory";
        }

        @Override
        public TgSystem setB1(final TgSystem tgSystem, final TgCategory tgCategory) {
            return tgSystem.setCategory(tgCategory);
        }

        @Override
        public TgSystem setB2(final TgSystem tgSystem, final TgCategory tgCategory) {
            return tgSystem.setFirstCategory(tgCategory);
        }
    };

    @Override
    protected Spec1 spec1() {
        return spec1;
    }

}
