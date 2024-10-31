package ua.com.fielden.platform.types.function;

import java.util.function.Predicate;

public interface CharPredicate extends Predicate<Character> {

    boolean test(char c);

    @Override
    default boolean test(final Character character) {
        return test(character.charValue());
    }

}
