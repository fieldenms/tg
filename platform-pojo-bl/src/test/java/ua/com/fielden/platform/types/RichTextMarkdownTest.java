package ua.com.fielden.platform.types;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @see <a href="https://spec.commonmark.org/">CommonMark Spec</a>
 */
public class RichTextMarkdownTest {

    @Test
    public void boldface_is_removed() {
        assertCoreText("the big bang",
                       "the **big** bang");
    }

    @Test
    public void italics_are_removed() {
        assertCoreText("the big bang",
                       "the *big* bang");
    }

    @Test
    public void code_span_backticks_are_removed() {
        assertCoreText("the big bang",
                       "the `big` bang");
    }

    @Test
    public void block_quote_markers_are_removed() {
        assertCoreText("the big bang",
                       "> the big bang");
        assertCoreText("the big bang",
                       ">the big bang");
        assertCoreText("the big bang",
                       ">  the big bang");
        assertCoreText("one two three",
                       """
                       > one
                       >two
                       >  three
                       """);
        assertCoreText("one two three",
                       """
                       > one
                       two
                       >  three
                       """);
    }

    @Test
    public void links_are_transformed_to_link_text_followed_by_parenthesised_uri_and_title() {
        assertCoreText("hackernews (https://news.ycombinator.com/)",
                       "[hackernews](https://news.ycombinator.com/)");
        assertCoreText("hackernews (https://news.ycombinator.com/ news)",
                       "[hackernews](https://news.ycombinator.com/ 'news')");
        assertCoreText("hackernews (https://news.ycombinator.com/ news)",
                       "[hackernews](https://news.ycombinator.com/ (news))");
        assertCoreText("hackernews (https://news.ycombinator.com/ news)",
                       "[hackernews](https://news.ycombinator.com/ \"news\")");
        assertCoreText("hackernews (https://news.ycombinator.com/)",
                       "[*hackernews*](https://news.ycombinator.com/)");
        assertCoreText("(https://news.ycombinator.com/)",
                       "[](https://news.ycombinator.com/)");
        assertCoreText("hackernews",
                       "[hackernews]()");
        assertCoreText("", "[]()");
    }

    @Test
    public void image_links_are_transformed_to_image_description_followed_by_parenthesised_uri() {
        assertCoreText("my lovely cat (cat.jpeg)",
                       "![my lovely cat](cat.jpeg)");
//        assertCoreText("my lovely cat (cat.jpeg)",
//                       "![my *lovely* cat](cat.jpeg)");
        assertCoreText("(cat.jpeg)",
                       "![](cat.jpeg)");
        assertCoreText("my lovely cat",
                       "![my lovely cat]()");
        assertCoreText("", "![]()");
    }

    @Test
    public void fenced_code_blocks_are_transformed_by_removing_fence_characters() {
        assertCoreText("hello",
                       """
                       ```
                       hello
                       ```
                       """);
        assertCoreText("hello",
                       """
                       `````
                       hello
                       `````
                       """);
        assertCoreText("hello",
                       """
                       ~~~
                       hello
                       ~~~
                       """);
        assertCoreText("hello",
                       """
                       ~~~~~~
                       hello
                       ~~~~~~
                       """);
        assertCoreText("",
                       """
                       ```
                       ```
                       """);
        assertCoreText("",
                       """
                       ```
                       
                       ```
                       """);
        assertCoreText("hello",
                       """
                         ```
                         hello
                       ```
                       """);
        assertCoreText("System.out.println(\"hello world\")",
                       """
                       ```java
                       System.out.println("hello world")
                       ```
                       """);
        assertCoreText("System.out.println(\"hello world\")",
                       """
                       ~~~java
                       System.out.println("hello world")
                       ~~~
                       """);
        assertCoreText("hello *world* <script>alert(1)</script>",
                       """
                       ```
                       hello *world*
                       <script>alert(1)</script>
                       ```
                       """);
        assertCoreText("one two three",
                       """
                       one
                         ```
                         two
                       ```
                       three
                       """);
    }

    @Test
    public void atx_headings_are_removed() {
        assertCoreText("Introduction", "# Introduction");
        assertCoreText("Introduction", "## Introduction");
        assertCoreText("Introduction", "### Introduction");
        assertCoreText("Introduction", "#### Introduction");
        assertCoreText("Introduction", "##### Introduction");
        assertCoreText("Introduction", "###### Introduction");
    }

    @Test
    public void thematic_breaks_are_removed() {
        assertCoreText("one two",
                       """
                       one
                       ---
                       two
                       """);
        assertCoreText("one two",
                       """
                       one
                       *****
                       two
                       """);
        assertCoreText("one two",
                       """
                       one
                       ____
                       two
                       """);
    }

    @Test
    public void newline_characters_are_removed() {
        assertCoreText("one two three four",
                       """
                       
                       one two
                       
                       three four
                       
                       """);
    }

    @Test
    public void simple_ordered_lists_are_squashed_into_a_single_line_with_markers_removed() {
        assertCoreText("one two",
                       """
                       1. one
                       2. two
                       """);
    }

    @Test
    public void simple_unordered_lists_are_squashed_into_a_single_line_and_list_characters_removed() {
        for (var listChar : List.of('-', '*', '+')) {
            final var input = """
            %1$s one
            %1$s two""".formatted(listChar);
            assertCoreText("one two", input);
        }
    }

    @Test
    public void list_items_with_content_are_squashed_into_a_single_line_with_markers_removed() {
        assertCoreText("item content more stuff",
                       """
                       1. item
                          content
                       2. more
                       
                          stuff
                       """);
    }

    @Test
    public void nested_list_items_are_squashed_into_a_single_line() {
        assertCoreText("item subitem",
                       """
                       1. item
                          * subitem
                       """);
        assertCoreText("item subitem another item",
                       """
                       1. item
                          * subitem
                            1. another item
                       """);
    }

    @Test
    public void inline_html_elements_are_removed() {
        assertCoreText("hello big world",
                       "hello <b>big</b> world");
        assertCoreText("hello world",
                       "<i>hello</i> world");
        assertCoreText("hello world",
                       "hello <B>world</B>");
        assertCoreText("hello world",
                       "hello <img src='cat.jpeg'/> world");
    }

    @Test
    public void html_blocks_are_removed() {
        assertCoreText("",
                       """
                       <pre>
                       blah blah blah
                       </pre>
                       """);
        assertCoreText("above below",
                       """
                       above
                       <pre>
                       blah blah blah
                       </pre>
                       below
                       """);
    }

    @Test
    public void reference_link_components_are_preserved() {
        assertCoreTextId("[bar]: /url \"title\"");
        assertCoreTextId("[bar]: /url");
        assertCoreTextId("[foo bar]: /url");
    }

    @Test
    public void contents_of_html_block_beginning_with_br_tag_is_included_in_core_text() {
        assertCoreText("first second last",
                       """
                       first
                       
                       <br>
                       second
                       last""");
        assertCoreText("first second last",
                       """
                       first
                       
                       <br  >
                       second
                       last""");
        assertCoreText("first second last",
                       """
                       first
                       
                       <br  />
                       second
                       last""");
        assertCoreText("first second last",
                       """
                       first
                       
                       </br>
                       second
                       last""");
        assertCoreText("first second last",
                       """
                       first
                       
                       </br  >
                       second
                       last""");
        assertCoreText("first second last",
                       """
                       first
                       
                          </br  >
                       second
                       last""");
        assertCoreText("first second last",
                       """
                       first
                       
                         <br  >
                       second
                       last""");
    }

    private static void assertCoreText(final String expected, final String input) {
        assertEquals(expected, RichText.fromHtml(input).coreText());
    }

    private static void assertCoreTextId(final String input) {
        assertCoreText(input, input);
    }

}
