package ua.com.fielden.platform.entity.query.fluent;

import ua.com.fielden.platform.utils.Pair;

import java.util.HashMap;
import java.util.Map;

public sealed interface ITypeCast {

    enum AsBoolean implements ITypeCast {
        AS_BOOLEAN;
    }

    enum AsInteger implements ITypeCast {
        AS_INTEGER;
    }

    final class AsDecimal implements ITypeCast {

        private static final Map<Pair<Integer, Integer>, AsDecimal> instances = new HashMap<>();

        public final int precision;
        public final int scale;

        private AsDecimal(final int precision, final int scale) {
            this.precision = precision;
            this.scale = scale;
        }

        public static AsDecimal getInstance(final int precision, final int scale) {
            final Pair<Integer, Integer> params = new Pair<>(precision, scale);
            final AsDecimal existing = instances.get(params);
            final AsDecimal result;
            if (existing != null) {
                result = existing;
            } else {
                result = new AsDecimal(precision, scale);
                instances.put(params, result);
            }
            return result;
        }

        public int precision() {
            return precision;
        }

        public int scale() {
            return scale;
        }

    }

    final class AsString implements ITypeCast {

        private static final Map<Integer, AsString> instances = new HashMap<>();

        public final int length;

        private AsString(final int length) {
            this.length = length;
        }

        public static AsString getInstance(final int length) {
            final AsString existing = instances.get(length);
            final AsString result;
            if (existing != null) {
                result = existing;
            } else {
                result = new AsString(length);
                instances.put(length, result);
            }
            return result;
        }

        public int length() {
            return length;
        }

    }

}
