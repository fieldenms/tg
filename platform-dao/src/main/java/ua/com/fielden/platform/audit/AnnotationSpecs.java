package ua.com.fielden.platform.audit;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import ua.com.fielden.platform.audit.annotations.AuditFor;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.entity.annotation.*;
import ua.com.fielden.platform.utils.Pair;

final class AnnotationSpecs {

    public static AnnotationSpec auditFor(final Class<? extends AbstractEntity<?>> entityType, final int version) {
        return AnnotationSpec.builder(AuditFor.class)
                .addMember("value", "$T.class", entityType)
                .addMember("version", "$L", version)
                .build();
    }

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

    public static AnnotationSpec entityTitle(final CharSequence title, final CharSequence desc) {
        return AnnotationSpec.builder(EntityTitle.class)
                .addMember("value", "$S", title.toString())
                .addMember("desc", "$S", desc.toString())
                .build();
    }

    public static AnnotationSpec entityTitle(final Pair<? extends CharSequence, ? extends CharSequence> titleAndDesc) {
        return titleAndDesc.map(AnnotationSpecs::entityTitle);
    }

    public static AnnotationSpec entityTitle(final CharSequence title) {
        return AnnotationSpec.builder(EntityTitle.class)
                .addMember("value", "$S", title.toString())
                .build();
    }

    public static AnnotationSpec keyTitle(final CharSequence title, final CharSequence desc) {
        return AnnotationSpec.builder(KeyTitle.class)
                .addMember("value", "$S", title.toString())
                .addMember("desc", "$S", desc.toString())
                .build();
    }

    public static AnnotationSpec keyTitle(final Pair<? extends CharSequence, ? extends CharSequence> titleAndDesc) {
        return titleAndDesc.map(AnnotationSpecs::keyTitle);
    }

    public static AnnotationSpec keyTitle(final CharSequence title) {
        return AnnotationSpec.builder(KeyTitle.class)
                .addMember("value", "$S", title.toString())
                .build();
    }

    public static AnnotationSpec mapTo(final CharSequence value) {
        return AnnotationSpec.builder(MapTo.class)
                .addMember("value", "$S", value.toString())
                .build();
    }

    public static AnnotationSpec critOnly(final CritOnly.Type value) {
        return AnnotationSpec.builder(CritOnly.class)
                .addMember("value", "$T.$L", CritOnly.Type.class, value.name())
                .build();
    }

    public static AnnotationSpec keyType(final Class<?> value) {
        return keyType(ClassName.get(value));
    }

    public static AnnotationSpec keyType(final TypeName typeName) {
        return AnnotationSpec.builder(KeyType.class)
                .addMember("value", "$T.class", typeName)
                .build();
    }

    private AnnotationSpecs() {}

}
