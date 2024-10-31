package ua.com.fielden.platform.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RichTextHtmlTest {

    @Test
    public void inline_tags_that_apply_text_style_are_removed() {
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
    public void block_quote_markers_are_removed() {
        assertCoreText(
"Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced. That's how it goes.",
"""
 <blockquote>
 <p>Words can be like X-rays, if you use them properly-they'll go through anything. You read and you're pierced.</p>
 That's how it goes.
 </blockquote>
""");

        assertCoreText(
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

    @Test
    public void links_are_transformed_to_link_text_followed_by_parenthesised_uri_and_title() {
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

    @Test
    public void links_inside_a_word_break_the_word() {
        assertCoreText("me mo (https://example.com) ry",
                       "me<a href='https://example.com'>mo</a>ry");
    }

    @Test
    public void image_links_are_transformed_to_image_description_followed_by_parenthesised_uri() {
        assertCoreText("my lovely cat (cat.jpeg)",
                       "<img alt='my lovely cat' src='cat.jpeg' />");
        assertCoreText("(cat.jpeg)",
                       "<img src='cat.jpeg' />");
        assertCoreText("my lovely cat",
                       "<img alt='my lovely cat' />");
        // OWASP sanitizer removes useless tags, like the one below
        // assertCoreText("", "<img />");
    }

    @Test
    public void pre() {
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
        assertCoreText("one two three",
                       """
                       one
                         <PRe>
                         two
                       </prE  >
                       three
                       """);
    }

    @Test
    public void headings_are_removed() {
        assertCoreText("Introduction", "<h1> Introduction");
        assertCoreText("Introduction", " <h2 > Introduction </h2>");
        assertCoreText("Introduction to", "<h1> Introduction <h2> to");
        assertCoreText("Introduction to mathematics", " <h1> Introduction <h2> to</h2>mathematics");
        assertCoreText("Introduction to mathematics", "<h1> Introduction <h2> to </h2>mathematics");
    }

    @Test
    public void thematic_breaks_are_removed() {
        assertCoreText("one two",
                       """
                       one
                       <hr>
                       two
                       """);
        assertCoreText("one two",
                       """
                       one
                       <hr />
                       two
                       """);
        assertCoreText("one two",
                       """
                       one
                       <hr>
                       
                       <hr />
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
    public void br_tags_are_removed() {
        assertCoreText("one two three four five",
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
    public void simple_ordered_lists_are_squashed_into_a_single_line_with_list_tags_removed() {
        assertCoreText("one two",
                       """
                       <ol>
                         <li>one
                              <lI>   two
                       """);
        assertCoreText("one two",
                       """
                       <ol>
                         <li>one
                              <lI>   two
                                  </ol>
                       """);
    }

    @Test
    public void simple_unordered_lists_are_squashed_into_a_single_line_with_list_tags_removed() {
        assertCoreText("one two",
                       """
                       <ul>
                         <li>one
                              <lI>   two
                       """);
        assertCoreText("one two",
                       """
                       <ul>
                         <li>one
                              <lI>   two
                                  </ul>
                       """);
    }

    @Test
    public void list_items_with_content_are_squashed_into_a_single_line_with_list_tags_removed() {
        assertCoreText("item content more stuff",
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
    public void nested_list_items_are_squashed_into_a_single_line_with_list_tags_removed() {
        assertCoreText("item 1 subitem 1 subitem 2 item 2 subitem 3",
                       """
                       <ol>
                         <li> item 1
                           <ul>
                             <li> subitem 1
                             </li>
                               <li> subitem 2
                           </ul>
                           <li>   item 2
                           <ol> <li> subitem 3 </ol>
                       </ol>
                       """);
    }

    @Test
    public void certain_tags_within_a_word_are_removed_without_breaking_the_word() {
        assertCoreText("memory", "me<b>mo</b>ry");
        assertCoreText("memory", "me<i>mo</i>ry");
        assertCoreText("memory", "me<u>mo</u>ry");
        assertCoreText("memory", "me<code>mo</code>ry");
        assertCoreText("memory", "me<sub>mo</sub>ry");
        assertCoreText("memory", "me<s>mo</s>ry");
        assertCoreText("memory", "me<span style='color: #4CAF50 !important'>mo</span>ry");
    }

    @Test
    public void separable_tags_are_separated_from_surrounding_text() {
        assertCoreText("hello world", "hello<img />world");
        assertCoreText("first second third", "first<p>second</p>third");
        assertCoreText("first second third", "first\n<p>second</p>\nthird");
        assertCoreText("first second third", "first<p>\nsecond\n</p>third");
        assertCoreText("first second third", "first<p>\nsecond\n</p>\nthird");
        assertCoreText("first second third", "\nfirst<p>\r\nsecond\n</p>\nthird\r\n");
   }

    private static void assertCoreText(final String expected, final String input) {
        assertEquals(expected, RichText.fromHtml(input).coreText());
    }

    private static void assertCoreTextId(final String input) {
        assertCoreText(input, input);
    }

}
