package ua.com.fielden.platform.entity.annotation.factory;

import ua.com.fielden.platform.entity.annotation.DescTitle;

/**
 * A factory for convenient instantiation of {@link DescTitle} annotations, which mainly should be used for dynamic class generation.
 *
 * @author TG Team
 *
 */
public class DescTitleAnnotation {
    private final String value;
    private final String desc;

    public DescTitleAnnotation(final String value, final String desc) {
        this.value = value;
        this.desc = desc;
    }

    public DescTitle newInstance() {
        return new DescTitle() {

            @Override
            public Class<DescTitle> annotationType() {
                return DescTitle.class;
            }

            @Override
            public String value() {
                return value;
            }

            @Override
            public String desc() {
                return desc;
            }
        };
    }

}
