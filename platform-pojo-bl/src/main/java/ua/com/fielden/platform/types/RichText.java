package ua.com.fielden.platform.types;

import org.apache.commons.text.StringEscapeUtils;
import org.commonmark.node.Node;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.validation.DefaultValidatorForValueTypeWithValidation;
import ua.com.fielden.platform.error.Result;

import java.util.Objects;

import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNonNull;
import static ua.com.fielden.platform.entity.exceptions.InvalidArgumentException.requireNull;

/**
 * Rich text is text which has attributes beyond those of plain text (e.g., styles such as colour, boldface, italic),
 * and is expressed in some markup language (e.g., Markdown, HTML).
 * <p>
 * This representation does not commit to a fixed markup language, values can be created for arbitrary markup languages,
 * if there exists a corresponding static factory method in this class (e.g., {@link RichText#fromHtml(String)}.
 * It also does not contain information about the markup language used in the formatted text.
 * <p>
 * This representation is immutable and sanitisation/validation is performed upon its construction.
 * <p>
 * Properties with this type are subject to the following rules:
 * <ul>
 *   <li> {@link IsProperty#length()} applies to {@link #coreText}.
 * </ul>
 * Entity properties of this type attain validator {@link DefaultValidatorForValueTypeWithValidation}.
 * It is possible to create {@link RichText} values that contain unsafe markup, and its `validationResult` will contain the relevant information.
 * <p>
 * Core text is obtained from the formatted text upon creating a {@link RichText} value, but only if the input passes validation.
 * Otherwise, neither core text, nor formatted text are available (their accessor methods throw an exception).
 */
public sealed class RichText implements IWithValidation permits RichText.Persisted, RichText.Invalid {

    public static final String FORMATTED_TEXT = "formattedText";
    public static final String CORE_TEXT = "coreText";

    private static final Result SUCCESSFUL = Result.successful();

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

    private final Result validationResult;

    /**
     * This constructor does not validate its arguments, thus <b>IT MUST BE KEPT PACKAGE PRIVATE</b>.
     *
     * @param formattedText     text with markup
     * @param coreText          text without markup (its length is always less than or equal to that of formatted text)
     * @param validationResult  the result of validation
     */
    // !!! KEEP THIS CONSTRUCTOR PRIVATE !!!
    private RichText(final String formattedText, final String coreText, final Result validationResult) {
        if (validationResult.isSuccessful()) {
            requireNonNull(formattedText, "formattedText");
            requireNonNull(coreText, "coreText");
        }
        else {
            requireNull(formattedText, "formattedText");
            requireNull(coreText, "coreText");
        }
        this.formattedText = formattedText;
        this.coreText = coreText;
        this.validationResult = validationResult;
    }

    @Override
    public Result isValid() {
        return validationResult;
    }

    // NOTE: If RichText with HTML as markup is accepted completely, Markdown support can potentially be removed.
    /**
     * Creates {@link RichText} by parsing the input as Markdown and sanitising all embedded HTML.
     * Throws an exception if embedded HTML is deemed to be unsafe.
     */
    public static RichText fromMarkdown(final String input) {
        final Node root = Parser.builder().includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build().parse(input);
        final var validationResult = RichTextSanitiser.sanitiseMarkdown(input);
        return validationResult.isSuccessful()
                ? new RichText(input, RichTextAsMarkdownCoreTextExtractor.toCoreText(root), SUCCESSFUL)
                : new Invalid(validationResult);
    }

    /**
     * Creates {@link RichText} by parsing the input as HTML and extracting core text.
     */
    public static RichText fromHtml(final String input) {
        class $ {
            static final RichTextAsHtmlCoreTextExtractor.Extension extension = new RichTextAsHtmlCoreTextExtractor.Extension() {
                static final String TOAST_UI_CHECKED_CLASS = "checked";
                static final String TOAST_UI_TASK_ITEM_CLASS = "task-list-item";

                @Override
                public boolean isTaskItem(final Element element) {
                    return element.hasClass(TOAST_UI_TASK_ITEM_CLASS);
                }

                @Override
                public boolean isTaskItemChecked(final Element element) {
                    return element.hasClass(TOAST_UI_TASK_ITEM_CLASS) && element.hasClass(TOAST_UI_CHECKED_CLASS);
                }
            };
        }

        // Validate the input before parsing it.
        final var validationResult = RichTextSanitiser.sanitiseHtml(input);
        if (validationResult.isSuccessful()) {
            final var coreText = RichTextAsHtmlCoreTextExtractor.toCoreText(Jsoup.parse(input), $.extension);
            return new RichText(input, coreText, validationResult);
        }
        else {
            return new Invalid(validationResult);
        }
    }

    /**
     * Creates {@link RichText} from the input, treating it as plain text (HTML special characters are escaped).
     * The result should always be valid (minus implementation errors).
     */
    public static RichText fromPlainText(final String input) {
        final var escapedInput = escapeAsHtml(input);
        return fromHtml(escapedInput);
    }

    /**
     * Supports HTML 4 and HTML 5.
     *
     * @see <a href="https://www.w3.org/TR/2014/NOTE-html5-diff-20141209/#syntax">Differences between HTML 4 and 5</a>.
     */
    private static String escapeAsHtml(final String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * Represents persisted values.
     * <b>The constructor must be used only when retrieving values from a database</b>,
     * because it uses the provided core text instead of extracting it from the formatted text.
     */
    static final class Persisted extends RichText {

        /**
         * This constructor does not validate its arguments.
         *
         * @param formattedText text with markup
         * @param coreText      text without markup (its length is always less than or equal to that of formatted text)
         */
        Persisted(final String formattedText, final String coreText) {
            super(formattedText, coreText, SUCCESSFUL);
        }

        @Override
        Persisted asPersisted() {
            return this;
        }
    }

    public static final class Invalid extends RichText {

        /**
         * @param validationResult  the result of validation
         */
        Invalid(final Result validationResult) {
            super(null, null, validationResult);
        }

        @Override
        public String formattedText() {
            throw new IllegalStateException("Formatted text is not available for invalid values.");
        }

        @Override
        public String coreText() {
            throw new IllegalStateException("Core text is not available for invalid values.");
        }

        @Override
        Persisted asPersisted() {
            throw new IllegalStateException("Invalid rich text cannot be persisted.");
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public String toString() {
            return "Invalid Rich Text [%s]".formatted(isValid());
        }

    }

    /**
     * Returns formatted text if this instance is valid.
     * Otherwise, throws an exception.
     */
    public String formattedText() {
        return formattedText;
    }

    /**
     * Returns core text if this instance is valid.
     * Otherwise, throws an exception.
     */
    public String coreText() {
        return coreText;
    }

    Persisted asPersisted() {
        return new Persisted(formattedText, coreText);
    }

    /**
     * Rich text instances are equal only if both are valid and have equal formatted text and core text.
     * Invalid instances are equal only to themselves.
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof RichText that)) {
            return false;
        }

        return this.isValid().isSuccessful()
               && that.isValid().isSuccessful()
               && Objects.equals(this.formattedText, that.formattedText)
               && Objects.equals(this.coreText, that.coreText);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formattedText, coreText);
    }

    @Override
    public String toString() {
        return "RichText[%n%s%n]".formatted(formattedText);
    }

}
