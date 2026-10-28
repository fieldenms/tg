package ua.com.fielden.platform.utils;

/// This interface simplifies implementation of [CharSequence] by enums.
///
/// The char sequence becomes the enum member name -- [Enum#name()].
///
public interface CharSequenceEnum extends CharSequence {

    @Override
    default int length() {
        return ((Enum<?>) this).name().length();
    }

    @Override
    default char charAt(final int index) {
        return ((Enum<?>) this).name().charAt(index);
    }

    @Override
    default CharSequence subSequence(final int start, final int end) {
        return ((Enum<?>) this).name().subSequence(start, end);
    }

}
