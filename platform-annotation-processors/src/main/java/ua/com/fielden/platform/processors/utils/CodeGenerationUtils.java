package ua.com.fielden.platform.processors.utils;

import com.squareup.javapoet.AnnotationSpec;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;

import javax.annotation.processing.Generated;

public final class CodeGenerationUtils {

    private CodeGenerationUtils() {}

    /**
     * Builds a {@link javax.annotation.processing.Generated} annotation.
     */
    public static AnnotationSpec buildAnnotationGenerated(final String generator, final String date, final String comments) {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", generator)
                .addMember("date", "$S", date)
                .addMember("comments", "$S", comments)
                .build();
    }

    /**
     * Builds a {@link javax.annotation.processing.Generated} annotation.
     */
    public static AnnotationSpec buildAnnotationGenerated(final String generator, final String date) {
        return AnnotationSpec.builder(Generated.class)
                .addMember("value", "$S", generator)
                .addMember("date", "$S", date)
                .build();
    }

    /**
     * Builds a {@link CompositeKeyMember} annotation.
     *
     * @param order  the value for {@link CompositeKeyMember#value()}
     */
    public static AnnotationSpec buildAtCompositeKeyMember(final int order) {
        return AnnotationSpec.builder(CompositeKeyMember.class).addMember("value", "$L", order).build();
    }

}
