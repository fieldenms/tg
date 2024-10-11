package ua.com.fielden.platform.types;

import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.successful;
import static ua.com.fielden.platform.utils.StreamUtils.enumerate;

/**
 * Rich text is text which has attributes beyond those of plain text (e.g., styles such as colour, boldface, italic),
 * and is expressed in some markup language (e.g., Markdown, HTML).
 * <p>
 * This representation does not commit to a fixed markup language, values can be created for arbitrary markup languages,
 * provided that there exists a corresponding static factory method in this class (e.g., {@link RichText#fromHtml(String)}.
 * It also does not contain information about the markup language used in the formatted text.
 * <p>
 * This representation is immutable.
 * <p>
 * Properties with this type are subject to the following rules:
 * <ul>
 *   <li> {@link IsProperty#length()} applies to {@link #coreText}.
 * </ul>
 * All newly created instances must go through validation, which performs sanitisation (e.g., {@linkplain RichTextSanitiser#sanitiseHtml(String)}).
 * Persistent values are considered valid, and their instances are created using type {@link Persisted}, which bypasses validation.
 */
public sealed class RichText permits RichText.Persisted {

    public static final String FORMATTED_TEXT = "formattedText";
    public static final String CORE_TEXT = "coreText";

    @IsProperty(length = Integer.MAX_VALUE)
    @MapTo
    @PersistentType("nstring")
    @Title(value = "Formatted Text", desc = "A text in HTML format, containing supported tags and CSS. This text is editable by users.")
    private final String formattedText;

    @IsProperty
    @MapTo
    @PersistentType("nstring")
    @Title(value = "Core Text", desc = "A text field with all HTML tags removed, intended for use in search functions and inline display, such as in EGI.")
    private final String coreText;

    /**
     * This constructor does not validate its arguments, thus <b>IT MUST BE KEPT PACKAGE PRIVATE</b>.
     *
     * @param formattedText text with markup
     * @param coreText      text without markup (its length is always less than or equal to that of formatted text)
     */
    // !!! KEEP THIS CONSTRUCTOR PACKAGE PRIVATE !!!
    RichText(final String formattedText, final String coreText) {
        if (formattedText == null) {
            throw new InvalidArgumentException("Argument [formattedText] must not be null.");
        }
        if (coreText == null) {
            throw new InvalidArgumentException("Argument [coreText] must not be null.");
        }
        this.formattedText = formattedText;
        this.coreText = coreText;
    }

    // NOTE: If RichText with HTML as markup is accepted completely, Markdown support can potentially be removed.
    /**
     * Creates {@link RichText} by parsing the input as Markdown and sanitising all embedded HTML.
     * Throws an exception if embedded HTML is deemed to be unsafe.
     */
    public static RichText fromMarkdown(final String input) {
        final RichText richText = RichTextSanitiser.sanitiseMarkdown(input).getInstanceOrElseThrow();
        return richText;
    }

    /**
     * Creates {@link RichText} by parsing the input as HTML and sanitising it.
     * Throws an exception if the HTML is deemed to be unsafe.
     */
    public static RichText fromHtml(final String input) {
        final RichText richText = RichTextSanitiser.sanitiseHtml(input).getInstanceOrElseThrow();
        return richText;
    }

    /**
     * Represents persisted values.
     * <b>The constructor must be used only when retrieving values from a database</b>, because it doesn't perform validation.
     */
    static final class Persisted extends RichText {
        /**
         * This constructor does not validate its arguments.
         *
         * @param formattedText text with markup
         * @param coreText      text without markup (its length is always less than or equal to that of formatted text)
         */
        Persisted(final String formattedText, final String coreText) {
            super(formattedText, coreText);
        }

        @Override
        Persisted asPersisted() {
            return this;
        }
    }

    public String formattedText() {
        return formattedText;
    }

    public String coreText() {
        return coreText;
    }

    Persisted asPersisted() {
        return new Persisted(formattedText, coreText);
    }

    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return this.equalsByText((RichText) obj);
        }
        return false;
    }

    /**
     * Polymorphic {@link #equals(Object)}, where {@link RichText} can be compared to {@link Persisted} by comparing their formatted and core text values.
     */
    public final boolean equalsByText(final RichText that) {
        return that == this ||
               Objects.equals(this.formattedText, that.formattedText) && Objects.equals(this.coreText, that.coreText);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(formattedText, coreText);
    }

    @Override
    public final String toString() {
        return "RichText[\n%s\n]".formatted(formattedText);
    }

}
