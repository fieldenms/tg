package ua.com.fielden.platform.entity.activatable;

import ua.com.fielden.platform.sample.domain.TgCategory;
import ua.com.fielden.platform.sample.domain.TgSubSystem;
import ua.com.fielden.platform.sample.domain.TgSystem;

public class EntityActivatabilityStandardTest extends AbstractEntityActivatabilityTestCase {

    private final Spec1<TgSystem, TgCategory> spec1 = new Spec1<>() {
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
        public CharSequence A_b3() {
            return "thirdCategory";
        }

        @Override
        public CharSequence A_b4() {
            return "secondCategory";
        }

        @Override
        public CharSequence A_a1() {
            return "system1";
        }

        @Override
        public TgSystem setB1(final TgSystem tgSystem, final TgCategory tgCategory) {
            return tgSystem.setCategory(tgCategory);
        }

        @Override
        public TgSystem setB2(final TgSystem tgSystem, final TgCategory tgCategory) {
            return tgSystem.setFirstCategory(tgCategory);
        }

        @Override
        public TgSystem setB3(final TgSystem tgSystem, final TgCategory tgCategory) {
            return tgSystem.setThirdCategory(tgCategory);
        }

        @Override
        public TgSystem setB4(final TgSystem tgSystem, final TgCategory tgCategory) {
            return tgSystem.setSecondCategory(tgCategory);
        }

        @Override
        public TgSystem setA1(final TgSystem tgSystem, final TgSystem a1) {
            return tgSystem.setSystem1(a1);
        }
    };

    private final Spec2<TgSystem, TgSubSystem> spec2 = new Spec2<>() {
        private int systemKeyCounter = 1;
        private int subSystemKeyCounter = 1;

        @Override
        public TgSystem newA() {
            return new_(TgSystem.class, "SYS%s".formatted(systemKeyCounter++));
        }

        @Override
        public TgSubSystem newB() {
            return new_(TgSubSystem.class, "SUBSYS%s".formatted(subSystemKeyCounter++));
        }

        @Override
        public Class<TgSystem> aType() {
            return TgSystem.class;
        }

        @Override
        public Class<TgSubSystem> bType() {
            return TgSubSystem.class;
        }

        @Override
        public CharSequence A_b1() {
            return "subSys1";
        }

        @Override
        public TgSystem setB1(final TgSystem tgSystem, final TgSubSystem tgSubSystem) {
            return tgSystem.setSubSys1(tgSubSystem);
        }

    };

    @SuppressWarnings("unchecked")
    @Override
    protected Spec1<TgSystem, TgCategory> spec1() {
        return spec1;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Spec2<TgSystem, TgSubSystem> spec2() {
        return spec2;
    }

}
