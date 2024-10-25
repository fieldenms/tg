package ua.com.fielden.platform.audit;

import com.squareup.javapoet.AnnotationSpec;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.CompositeKeyMember;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.utils.Pair;

final class AnnotationSpecs {

    public static AnnotationSpec auditFor(final Class<? extends AbstractEntity<?>> entityType) {
        return AnnotationSpec.builder(AuditFor.class)
                .addMember("value", "$T.class", entityType)
                .build();
    }

    public static AnnotationSpec compositeKeyMember(final int value) {
        return AnnotationSpec.builder(CompositeKeyMember.class)
                .addMember("value", "$L", value)
                .build();
    }

    public static AnnotationSpec title(final CharSequence title, final CharSequence desc) {
        return AnnotationSpec.builder(Title.class)
                .addMember("value", "$S", title.toString())
                .addMember("desc", "$S", desc.toString())
                .build();
    }

    public static AnnotationSpec title(final Pair<? extends CharSequence, ? extends CharSequence> titleAndDesc) {
        return titleAndDesc.map(AnnotationSpecs::title);
    }

    public static AnnotationSpec title(final CharSequence title) {
        return AnnotationSpec.builder(Title.class)
                .addMember("value", "$S", title.toString())
                .build();
    }

    private AnnotationSpecs() {}

}
