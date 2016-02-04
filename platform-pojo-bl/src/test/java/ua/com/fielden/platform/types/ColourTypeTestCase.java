package ua.com.fielden.platform.types;

import static org.junit.Assert.*;
import groovy.ui.Console;

import org.junit.Test;

import ua.com.fielden.platform.types.Colour;

public class ColourTypeTestCase {
	@Test
	public void color_hexCode_creation_test() {
		final Colour color = new Colour("bbb");
		assertEquals("#BBB", color.getColourValue());
	}
	
	@Test
	public void colour_hexCode_must_have_correct_length() {
		try {
			new Colour("a");
			fail();
		} catch (final Exception e) {
		}
		try {
			new Colour("");
			fail();
		} catch (final Exception e) {
		}
		try {
			new Colour("a2");
			fail();
		} catch (final Exception e) {
		}
		try {
			new Colour("aaaa");
			fail();
		} catch (final Exception e) {
		}
		try {
			new Colour("aaaaa");
			fail();
		} catch (final Exception e) {
		}
		try {
			new Colour(null);
			fail();
		} catch (final Exception e) {
		}

	}

	@Test
	public void color_hexCode_must_be_build_by_correct_sumbols() {
		try {
			new Colour("ggg");
			fail();
		} catch (final Exception e) {

		}
		try {
			new Colour("ffj");
			fail();
		} catch (final Exception e) {

		}
		try {
			new Colour("gba999");
			fail();
		} catch (final Exception e) {

		}
		try {
			new Colour("12345v");
			fail();
		} catch (final Exception e) {

		}

	}

}
