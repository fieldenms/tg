package ua.com.fielden.platform.types;

/**
 * Rich text is text which has attributes beyond those of plain text (e.g., styles such as color, boldface, italic),
 * and is expressed in some markup language (e.g., Markdown).
 * <p>
 * This representation does not commit to a fixed markup language, values can be created for arbitrary markup languages.
 * It also does not contain information about the markup language used in the formatted text.
 *
 * @param formattedText  text with markup
 * @param coreText       text without markup (its length is always less than or equal to that of formatted text)
 */
public record RichText(String formattedText, String coreText) {
}
