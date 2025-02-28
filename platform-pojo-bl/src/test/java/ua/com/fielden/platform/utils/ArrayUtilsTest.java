package ua.com.fielden.platform.utils;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArrayUtilsTest {

    @Test
    public void prepend_produces_array_with_one_extra_element_as_the_first_element() {
        final String[] array = new String[] {"b", "c"};
        final String[] newArray = ArrayUtils.prepend("a", array);
        assertThat(newArray.length).isEqualTo(array.length + 1);
        assertThat(newArray[0]).isEqualTo("a");

        final String[] emptyArray = new String[] {};
        final String[] newArray1 = ArrayUtils.prepend("a", emptyArray);
        assertThat(newArray1.length).isEqualTo(emptyArray.length + 1);
        assertThat(newArray1[0]).isEqualTo("a");
    }
}
