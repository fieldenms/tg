package ua.com.fielden.platform.types;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class RichTextHtmlTest {

    @Test
    public void unsafe_RichText_does_not_have_formattedText_and_coreText_and_searchText() {
        final var unsafeHtml = "<script> alert(1) </script><p>some text</p>";
        final var richText = RichText.fromHtml(unsafeHtml);
        assertThat(richText).isInstanceOf(RichText.Invalid.class);
        assertThat(richText.isValid().isSuccessful()).isEqualTo(false);
        assertThrows(IllegalStateException.class, richText::formattedText);
        assertThrows(IllegalStateException.class, richText::coreText);
    }

    @Test
    public void core_text_inline_tags_that_apply_text_style_are_removed() {
        assertCoreText("the big bang",
                       "the <b >big</B> bang");
        assertCoreText("the big bang",
                       "the <strong >big</STrOnG> bang");
        assertCoreText("the big bang",
                       "the <I>big</i  > bang");
        assertCoreText("the big bang",
                       "the <em>big</EM  > bang");
        assertCoreText("the big bang",
                       "the <u >big</U > bang");
        assertCoreText("the big bang",
                       "the <s >big</S > bang");
        assertCoreText("the big bang",
                       "the <DEL >big</deL > bang");
        assertCoreText("the big bang",
                       "the <code>big</code> bang");
        // OWASP sanitizer removes useless tags, like the one below
        // assertCoreText("the big bang",
        //                "the <span >big</SPaN > bang");
    }

    @Test
    public void search_text_inline_tags_that_apply_text_style_are_removed() {
        assertSearchText("the big bang",
                         "the <b >big</B> bang");
        assertSearchText("the big bang",
                         "the <strong >big</STrOnG> bang");
        assertSearchText("the big bang",
                         "the <I>big</i  > bang");
        assertSearchText("the big bang",
                         "the <em>big</EM  > bang");
        assertSearchText("the big bang",
                         "the <u >big</U > bang");
        assertSearchText("the big bang",
                         "the <s >big</S > bang");
        assertSearchText("the big bang",
                         "the <DEL >big</deL > bang");
        assertSearchText("the big bang",
                         "the <code>big</code> bang");
    }

    @Test
    public void core_text_paragraphs_are_on_separate_lines() {
        assertCoreText("one", "<p>one</p>");
        assertCoreText("one", "<p>one");
        assertCoreText("one\ntwo", "<p>one</p> two");
        assertCoreText("one\ntwo", "one<p> two");
        assertCoreText("one\ntwo", "one <p></p> two");
        assertCoreText("one\ntwo", "one <p></p> <p></p> two");
        assertCoreText("one\ntwo three", "<p>one</p> two three");
        assertCoreText("one\ntwo\nthree", "<p>one</p> two <p>three");
        assertCoreText("one\ntwo three", "<p>one</p> two three </p>");
        assertCoreText("one two three\nfour five", "<p>one <b>two</b> three</p> four <i>five</i> </p>");
    }

    @Test
    public void core_text_headings_are_on_separate_lines() {
        assertCoreText("one\ntwo", "<h1>one</h1> two");
        assertCoreText("one\n...\ntwo\n...", "<h1>one</h1> ... <h2> two </h2> ...");
        assertCoreText("one\n...\ntwo\n...\nthree", "<h1>one</h1> ... <h2> two </h2> ... <h3> three");
        assertCoreText("one\n...\ntwo\n...\nthree", "<h1>one</h1> ... <h2> two </h2> ... <h3> three </h3>");
    }


    @Test
    public void search_text_headings_are_removed() {
        assertSearchText("one two",
                       "<h1>one</h1> two");
        assertSearchText("one ... two ...",
                       "<h1>one</h1> ... <h2> two </h2> ...");
        assertSearchText("one ... two ... three",
                       "<h1>one</h1> ... <h2> two </h2> ... <h3> three");
        assertSearchText("one ... two ... three",
                       "<h1>one</h1> ... <h2> two </h2> ... <h3> three </h3>");

    }

    @Test
    public void core_text_br_element_forces_line_breaks() {
        assertCoreText("one\ntwo", "one<br>two");
        assertCoreText("one\n\ntwo", "one <br> <br> two");
        assertCoreText("one\n\n\ntwo", "one <p><br></p> <br> two");
        assertCoreText("one\n\n\ntwo", "one <p><br></p> <br> <p>two</p>");
        assertCoreText("one\n\n\ntwo", "<p>one</p> <p><br></p> <br> <p>two</p>");
    }

    @Test
    public void search_text_br_elements_are_removed() {
        assertSearchText("one two three four five",
                         """
                         <br>
                         <br> one two
                         <br> <br>
                         <br>
                         three <br> four
                         five <br>
                         <br>
                         """);
    }

    @Test
    public void core_text_list_items_are_on_separate_lines() {
        assertCoreText("""
                       items:
                       - first item
                       - second item""",

                       """
                       <p>items:</p>
                       <ul>
                       <li> first
                       item
                       <li> second
                       
                       item
                       </ul>
                       """);

        assertCoreText("""
                       items:
                       1. first item
                       2. second item""",

                       """
                       <p>items:</p>
                       <ol>
                       <li> first
                       item
                       <li> second
                       
                       item
                       </ol>
                       """);
    }

    @Test
    public void core_text_leading_whitespace_is_removed() {
        assertCoreText("one", "<br> <p><br></p> <p><br><p> </p>  one");
        assertCoreText("one\ntwo", "<br> <p></p> one <p> <p> </p>  two </p> </p>");
    }

    @Test
    public void core_text_trailing_whitespace_is_removed() {
        assertCoreText("one", "one<p></p> <p> <p><br></p>  <br>");
        assertCoreText("one\ntwo", "<p></p> one <p> <p> </p>  two </p> </p> <br>");
    }

    @Test
    public void core_text_trailing_whitespace_on_each_line_is_removed() {
        assertCoreText("one\ntwo", "one   <p>two");
        assertCoreText("one\ntwo", "one   <br>two");
    }
    
    @Test
    public void core_text_blockquote_markers_are_removed() {
        assertCoreText(
"""
Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced. 
That's how it goes.""",
"""
 <blockquote>
 <p>Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced.</p>
 That's how it goes.
 </blockquote>
""");

        assertCoreText(
"""
Before quote.
Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced.
That's how it goes.
After quote.""",
"""
Before quote.
<blockquote>
<p>Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced.</p>
That's how it goes.
</blockquote>
After quote.
""");
    }

    @Test
    public void search_text_blockquote_markers_are_removed() {
        assertSearchText(
"Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced. That's how it goes.",
"""
 <blockquote>
 <p>Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced.</p>
 That's how it goes.
 </blockquote>
""");

        assertSearchText(
"""
Before quote. \
Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced. \
That's how it goes. \
After quote.\
""",
"""
Before quote.
<blockquote>
<p>Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced.</p>
That's how it goes.
</blockquote>
After quote.
""");
    }

    /**
     * Link text should be followed by a parenthesised URI.
     */
    @Test
    public void core_text_links() {
        assertCoreText("hackernews (https://news.ycombinator.com/)",
                       "<a href='https://news.ycombinator.com/'>hackernews</a>");
        assertCoreText("hackernews (https://news.ycombinator.com/)",
                       "<a href='https://news.ycombinator.com/' rel=nofollow>hackernews</a>");
        assertCoreText("hackernews (https://news.ycombinator.com/)",
                       "<a href='https://news.ycombinator.com/' rel=nofollow><b>hackernews</b></a>");
        assertCoreText("(https://news.ycombinator.com/)",
                       "<a href='https://news.ycombinator.com/' rel=nofollow></a>");
        assertCoreText("hackernews",
                       "<a href=''>hackernews</a>");
        assertCoreText("",
                       "<a href=''>  </a>");
    }

    /**
     * Link text should be preserved as if it were a regular text.
     * URIs are deduped and placed at the end.
     */
    @Test
    public void search_text_links() {
        assertSearchText("the hackernews website https://news.ycombinator.com/",
                         "the <a href='https://news.ycombinator.com/'>hackernews</a> website");
        assertSearchText("the hackernews website https://news.ycombinator.com/",
                         "the <a href='https://news.ycombinator.com/' rel=nofollow>hackernews</a> website");
        assertSearchText("the hackernews website https://news.ycombinator.com/",
                         "the <a href='https://news.ycombinator.com/' rel=nofollow><b>hackernews</b></a> website");
        assertSearchText("the hackernews website https://news.ycombinator.com/",
                         "the <a href='https://news.ycombinator.com/' rel=nofollow><b>hackernews</b></a> website");
        assertSearchText("the website https://news.ycombinator.com/",
                         "the <a href='https://news.ycombinator.com/' rel=nofollow></a> website");
        assertSearchText("the website is not as good as another website https://news.ycombinator.com/ https://news.ycombinator.com/another",
                         "the <a href='https://news.ycombinator.com/' rel=nofollow>website</a> is not as good as <a href='https://news.ycombinator.com/another'>another website</a>");
        assertSearchText("the website is as good as the same website https://news.ycombinator.com/",
                         "the <a href='https://news.ycombinator.com/' rel=nofollow>website</a> is as good as the same <a href='https://news.ycombinator.com/' rel=nofollow>website</a>");
        assertSearchText("hackernews",
                         "<a href=''>hackernews</a>");
        assertSearchText("",
                         "<a href=''>  </a>");
    }

    @Test
    public void core_text_links_inside_a_word_break_the_word() {
        assertCoreText("memo (https://example.com)ry",
                       "me<a href='https://example.com'>mo</a>ry");
    }

    @Test
    public void search_text_links_inside_a_word_do_not_break_the_word() {
        assertSearchText("memory https://example.com",
                         "me<a href='https://example.com'>mo</a>ry");
    }

    /**
     * Image description (value of the 'alt' attribute) should be followed by a parenthesised URI.
     */
    @Test
    public void core_text_image_links() {
        assertCoreText("my lovely cat (cat.jpeg)",
                       "<img alt='my lovely cat' src='cat.jpeg' />");
        assertCoreText("(cat.jpeg)",
                       "<img src='cat.jpeg' />");
        assertCoreText("my lovely cat",
                       "<img alt='my lovely cat' />");
        // OWASP sanitizer removes useless tags, like the one below
        // assertCoreText("", "<img />");
    }

    /**
     * Image description (value of the 'alt' attribute) should be preserved as if it were regular text.
     * URIs are placed at the end.
     */
    @Test
    public void search_text_image_links() {
        assertSearchText("my cat is lovely cat.jpeg",
                         "my <img alt='cat' src='cat.jpeg' /> is lovely");
        assertSearchText("my is lovely cat.jpeg",
                         "my <img src='cat.jpeg' /> is lovely");
        assertSearchText("my cat is lovely",
                         "my <img alt='cat' /> is lovely");
    }

    @Test
    public void core_text_pre_blocks_are_stripped_and_their_contents_also_processed_as_HTML() {
        assertCoreText("hello",
                       """
                       <pre>
                       hello
                       </pre>
                       """);
        assertCoreText("let i = 5;",
                       """
                       <pre><code>
                       let i = 5;
                       </code></pre>
                       """);
        assertCoreText("",
                       """
                       <pre>
                       </pre>
                       """);
        assertCoreText("",
                       """
                       <pre><code>
                       
                       </code>
                       </pre>
                       """);
        assertCoreText("hello",
                       """
                           <pre>
                         hello
                       </pre>
                       """);
        assertCoreText("""
                       one
                       two
                       three""",
                       """
                       one
                         <PRe>
                         two
                       </prE  >
                       three
                       """);
        assertCoreText("one two three",
                       "<pre> one <b> two </b> three </pre>");
    }

    @Test
    public void search_text_pre_blocks_are_stripped_and_their_contents_also_processed_as_HTML() {
        assertSearchText("hello",
                       """
                       <pre>
                       hello
                       </pre>
                       """);
        assertSearchText("let i = 5;",
                       """
                       <pre><code>
                       let i = 5;
                       </code></pre>
                       """);
        assertSearchText("",
                       """
                       <pre>
                       </pre>
                       """);
        assertSearchText("",
                       """
                       <pre><code>
                       
                       </code>
                       </pre>
                       """);
        assertSearchText("hello",
                       """
                           <pre>
                         hello
                       </pre>
                       """);
        assertSearchText("one two three",
                       """
                       one
                         <PRe>
                         two
                       </prE  >
                       three
                       """);
        assertSearchText("one two three",
                       "<pre> one <b> two </b> three </pre>");
    }

    @Test
    public void core_text_thematic_break_is_replaced_by_newline() {
        assertCoreText("""
                       one
                       two""",
                       """
                       one
                       <hr>
                       two
                       """);
        assertCoreText("""
                       one
                       two""",
                       """
                       one
                       <hr />
                       two
                       """);
        assertCoreText("""
                       one

                       two""",
                       """
                       one
                       <hr>
                       
                       <hr />
                       two
                       """);
    }

    @Test
    public void search_text_thematic_breaks_are_removed() {
        assertSearchText("one two",
                       """
                       one
                       <hr>
                       two
                       """);
        assertSearchText("one two",
                       """
                       one
                       <hr />
                       two
                       """);
        assertSearchText("one two",
                       """
                       one
                       <hr>
                       
                       <hr />
                       two
                       """);
    }

    @Test
    public void core_text_newline_characters_are_removed() {
        assertCoreText("one two three four",
                       """
                       
                       one two
                       
                       three four
                       
                       """);
    }

    @Test
    public void search_text_newline_characters_are_removed() {
        assertSearchText("one two three four",
                       """
                       
                       one two
                       
                       three four
                       
                       """);
    }

    @Test
    public void core_text_ordered_list_items_are_on_separate_lines_with_list_tags_replaced_by_numbers() {
        assertCoreText("""
                       1. one
                       2. two""",
                       """
                       <ol>
                         <li>one
                              <lI>   two
                       """);
        assertCoreText("""
                       1. one
                       2. two""",
                       """
                       <ol>
                         <li>one
                              <lI>   two
                                  </ol>
                       """);
        assertCoreText("""
                       1. one
                       details of one
                       2. two details of two""",
                       """
                       <ol>
                         <li> one
                         <p> details of one

                         <li> two

                         details of two
                       </ol>
                       """);
    }

    @Test
    public void search_text_ordered_lists_are_squashed_into_a_single_line_with_list_tags_replaced_by_numbers() {
        assertSearchText("1. item content 2. more stuff",
                         """
                         <ol>
                           <li> item
                            content
                            </li>
                         <li> more
                         
                            stuff
                         """);
    }

    @Test
    public void core_text_unordered_list_items_are_on_separate_lines_with_list_tags_replaced_by_dashes() {
        assertCoreText("""
                       - one
                       - two""",
                       """
                       <ul>
                         <li>one
                              <lI>   two
                       """);
        assertCoreText("""
                       - one
                       details of one
                       - two details of two""",
                       """
                       <ul>
                         <li> one
                         <p> details of one

                         <li> two
                         
                         details of two
                       </ul>
                       """);
    }

    @Test
    public void search_text_unordered_lists_are_squashed_into_a_single_line_with_list_tags_replaced_by_dashes() {
        assertSearchText("- one with details - two with more details",
                         """
                         <ul>
                           <li>one
                           with details
                                <lI>   two

                                with <b>more details</b>

                                    </ul>
                         """);
    }

    @Test
    public void core_text_nested_lists() {
        assertCoreText("""
                       1. letters:
                       - a
                       - b
                       2. numbers:
                       1. one
                       2. two""",
                       """
                       <ol>
                         <li> letters:
                         <ul>
                           <li> a
                           <li> b
                         </ul>
                         <li> numbers:
                         <ol>
                           <li> one
                           <li> two
                         </ol>
                       </ol>
                       """);
    }

    @Test
    public void search_text_nested_lists() {
        assertSearchText("1. letters: - a - b 2. numbers: 1. one 2. two",
                       """
                       <ol>
                         <li> letters:
                         <ul>
                           <li> a
                           <li> b
                         </ul>
                         <li> numbers:
                         <ol>
                           <li> one
                           <li> two
                         </ol>
                       </ol>
                       """);
    }

    @Test
    public void core_text_certain_tags_within_a_word_are_removed_without_breaking_the_word() {
        assertCoreText("memory", "me<b>mo</b>ry");
        assertCoreText("memory", "me<i>mo</i>ry");
        assertCoreText("memory", "me<u>mo</u>ry");
        assertCoreText("memory", "me<code>mo</code>ry");
        assertCoreText("memory", "me<sub>mo</sub>ry");
        assertCoreText("memory", "me<s>mo</s>ry");
        assertCoreText("memory", "me<span style='color: #4CAF50 !important'>mo</span>ry");
    }

    @Test
    public void search_text_certain_tags_within_a_word_are_removed_without_breaking_the_word() {
        assertSearchText("memory", "me<b>mo</b>ry");
        assertSearchText("memory", "me<i>mo</i>ry");
        assertSearchText("memory", "me<u>mo</u>ry");
        assertSearchText("memory", "me<code>mo</code>ry");
        assertSearchText("memory", "me<sub>mo</sub>ry");
        assertSearchText("memory", "me<s>mo</s>ry");
        assertSearchText("memory", "me<span style='color: #4CAF50 !important'>mo</span>ry");
    }

    @Test
    public void core_text_separable_tags_are_separated_from_surrounding_text() {
        assertCoreText("hello world", "hello<img />world");
   }

    @Test
    public void search_text_separable_tags_are_separated_from_surrounding_text() {
        assertSearchText("hello world", "hello<img />world");
    }

    @Test
    public void core_text_toastUi_task_items_are_marked_using_standard_Markdown_markers_for_task_items() {
        assertCoreText("""
                       - [ ] task 1
                       - [ ] task 2
                       - [x] task 3
                       - [ ] subtask 1
                       - [x] subtask 2
                       - [x] task 4""",
                       """
                       <ul>
                       <li class="task-list-item"><p>task 1</p></li>
                       <li class="task-list-item"><p>task 2</p></li>
                       <li class="task-list-item checked"><p>task 3</p></li>
                         <ul>
                         <li class="task-list-item"><p>subtask 1</p></li>
                         <li class="task-list-item checked"><p>subtask 2</p></li>
                         </ul>
                       <li class="task-list-item checked"><p>task 4</p></li>
                       </ul>
                       """);
    }

    @Test
    public void search_text_toastUi_task_items_are_marked_using_standard_Markdown_markers_for_task_items() {
        assertSearchText("- [ ] task 1 - [ ] task 2 - [x] task 3 - [ ] subtask 1 - [x] subtask 2 - [x] task 4",
                         """
                         <ul>
                         <li class="task-list-item"><p>task 1</p></li>
                         <li class="task-list-item"><p>task 2</p></li>
                         <li class="task-list-item checked"><p>task 3</p></li>
                           <ul>
                           <li class="task-list-item"><p>subtask 1</p></li>
                           <li class="task-list-item checked"><p>subtask 2</p></li>
                           </ul>
                         <li class="task-list-item checked"><p>task 4</p></li>
                         </ul>
                         """);
    }

    @Test
    public void core_text_whitespace() {
        assertCoreText("two three four", "<b>two </b><i>three</i> four");
        assertCoreText("two three four", "<b>two </b><i> three</i> four");
        assertCoreText("two three four", "<b>two </b> <i>three</i> four");
        assertCoreText("two three four", " <b>two </b> <i>three</i> four");
        assertCoreText("two three four", "<b> two </b> <i>three</i> four");
    }

    @Test
    public void search_text_whitespace() {
        assertSearchText("two three four", "<b>two </b><i>three</i> four");
        assertSearchText("two three four", "<b>two </b><i> three</i> four");
        assertSearchText("two three four", "<b>two </b> <i>three</i> four");
        assertSearchText("two three four", " <b>two </b> <i>three</i> four");
        assertSearchText("two three four", "<b> two </b> <i>three</i> four");
    }

    @Test
    public void html_entities_are_escaped_in_RichText_from_plain_text() {
        assertPlainText("bob & alice")
                .formattedTextEquals("bob &amp; alice")
                .coreTextEquals("bob & alice");
        assertPlainText("boom!").coreTextEquals("boom!");
        assertPlainText("user@mail.host").coreTextEquals("user@mail.host");
        assertPlainText("https://domain.org").coreTextEquals("https://domain.org");
        assertPlainText("<b> one </b>")
                .formattedTextEquals("&lt;b&gt; one &lt;/b&gt;")
                .coreTextEquals("<b> one </b>");
        assertPlainText("<script> alert(1) </script>")
                .formattedTextEquals("&lt;script&gt; alert(1) &lt;/script&gt;")
                .coreTextEquals("<script> alert(1) </script>");
    }

    private static void assertCoreText(final String expected, final String input) {
        assertEquals(expected, RichText.fromHtml(input).coreText());
    }

    private static void assertCoreTextId(final String input) {
        assertCoreText(input, input);
    }

    private static void assertSearchText(final String expected, final String input) {
        assertEquals(expected, RichText.makeSearchText(RichText.fromHtml(input)));
    }

    private static RichTextAssertor assertRichText(final RichText richText) {
        return new RichTextAssertor(richText);
    }

    private static RichTextAssertor assertPlainText(final String input) {
        return new RichTextAssertor(RichText.fromPlainText(input));
    }

    private static final class RichTextAssertor {
        private final RichText richText;

        private RichTextAssertor(final RichText richText) {
            this.richText = richText;
        }

        public RichTextAssertor formattedTextEquals(final String expected) {
            assertEquals("Unexpected formatted text", expected, richText.formattedText());
            return this;
        }

        public RichTextAssertor coreTextEquals(final String expected) {
            assertEquals("Unexpected core text", expected, richText.coreText());
            return this;
        }
    }

}
