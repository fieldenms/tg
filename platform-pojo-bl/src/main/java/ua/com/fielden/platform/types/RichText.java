package ua.com.fielden.platform.types;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import ua.com.fielden.platform.entity.annotation.IsProperty;
import ua.com.fielden.platform.entity.annotation.MapTo;
import ua.com.fielden.platform.entity.annotation.PersistentType;
import ua.com.fielden.platform.entity.annotation.Title;
import ua.com.fielden.platform.entity.exceptions.InvalidArgumentException;
import ua.com.fielden.platform.entity.validation.RichTextValidator;
import ua.com.fielden.platform.error.Result;

import java.util.Objects;

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
 * Entity properties of this type attain validator {@link RichTextValidator}.
 * It is possible to create {@link RichText} values that contain unsafe markup, and its `validationResult` will contain the relevant information.
 * <p>
 * Core text is obtained from the formatted text upon creating a {@link RichText} value, but only if the input passes validation.
 * Otherwise, the value of `coreText` is empty.
 */
public sealed class RichText permits RichText.Persisted {

    public static final String ERR_FORMATTED_TEXT_MUST_NOT_BE_NULL = "Argument [formattedText] must not be null.";
    public static final String ERR_CORE_TEXT_MUST_NOT_BE_NULL = "Argument [coreText] must not be null.";

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

    private final Result validationResult;

    /**
     * This constructor does not validate its arguments, thus <b>IT MUST BE KEPT PACKAGE PRIVATE</b>.
     *
     * @param formattedText     text with markup
     * @param coreText          text without markup (its length is always less than or equal to that of formatted text)
     * @param validationResult  the result of validation
     */
    // !!! KEEP THIS CONSTRUCTOR PACKAGE PRIVATE !!!
    RichText(final String formattedText, final String coreText, final Result validationResult) {
        if (formattedText == null) {
            throw new InvalidArgumentException(ERR_FORMATTED_TEXT_MUST_NOT_BE_NULL);
        }
        if (coreText == null) {
            throw new InvalidArgumentException(ERR_CORE_TEXT_MUST_NOT_BE_NULL);
        }
        this.formattedText = formattedText;
        this.coreText = coreText;
        this.validationResult = validationResult;
    }

    public Result getValidationResult() {
        return validationResult;
    }

    // NOTE: If RichText with HTML as markup is accepted completely, Markdown support can potentially be removed.
    /**
     * Creates {@link RichText} by parsing the input as Markdown and sanitising all embedded HTML.
     * Throws an exception if embedded HTML is deemed to be unsafe.
     */
    public static RichText fromMarkdown(final String input) {
        return RichTextSanitiser.sanitiseMarkdown(input);
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
        final var coreText = validationResult.isSuccessful()
                             ? RichTextAsHtmlCoreTextExtractor.toCoreText(Jsoup.parse(input), $.extension)
                             : "";
        return new RichText(input, coreText, validationResult);
    }

    /**
     * Represents persisted values.
     * <b>The constructor must be used only when retrieving values from a database</b>,
     * because it uses the provided core text instead of extracting it from the formatted text.
     */
    static final class Persisted extends RichText {

        public static final Result SUCCESSFUL = Result.successful();

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
        return "RichText[%n%s%n]".formatted(formattedText);
    }

}
