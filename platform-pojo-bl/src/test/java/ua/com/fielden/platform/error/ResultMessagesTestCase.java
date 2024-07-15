package ua.com.fielden.platform.error;

import static org.junit.Assert.assertEquals;
import static ua.com.fielden.platform.error.Result.failure;
import static ua.com.fielden.platform.error.Result.resultMessages;
import static ua.com.fielden.platform.error.Result.warning;

import org.junit.Test;

import ua.com.fielden.platform.error.Result.ResultMessages;

/**
 * Tests for {@link Result#resultMessages(Result)} method.
 * 
 * @author TG Team
 *
 */
public class ResultMessagesTestCase {

    @Test
    public void result_or_exception_with_null_message_returns_javas_NPE_description_in_short_and_extended_messages() {
        final ResultMessages resultMessages = resultMessages(failure(new NullPointerException()));
        assertEquals("Null pointer exception", resultMessages.shortMessage);
        assertEquals("Null pointer exception", resultMessages.extendedMessage);
    }

    @Test
    public void result_or_exception_with_empty_message_returns_empty_short_and_extended_messages() {
        final ResultMessages resultMessages = resultMessages(warning(""));
        assertEquals("", resultMessages.shortMessage);
        assertEquals("", resultMessages.extendedMessage);
    }

    @Test
    public void result_or_exception_with_message_without_EXT_SEPARATOR_returns_the_same_message_in_short_and_extended_counterparts() {
        final ResultMessages resultMessages = resultMessages(warning("msg"));
        assertEquals("msg", resultMessages.shortMessage);
        assertEquals("msg", resultMessages.extendedMessage);
    }

    @Test
    public void result_or_exception_with_EXT_SEPARATOR_message_returns_empty_short_and_extended_messages() {
        final ResultMessages resultMessages = resultMessages(warning("<extended/>"));
        assertEquals("", resultMessages.shortMessage);
        assertEquals("", resultMessages.extendedMessage);
    }

    @Test
    public void result_or_exception_with_shortEXT_SEPARATOR_message_returns_short_message_in_both_short_and_extended_counterparts() {
        final ResultMessages resultMessages = resultMessages(warning("short<extended/>"));
        assertEquals("short", resultMessages.shortMessage);
        assertEquals("short", resultMessages.extendedMessage);
    }

    @Test
    public void result_or_exception_with_EXT_SEPARATORext_message_returns_ext_message_in_both_short_and_extended_counterparts() {
        final ResultMessages resultMessages = resultMessages(warning("<extended/>ext"));
        assertEquals("ext", resultMessages.shortMessage);
        assertEquals("ext", resultMessages.extendedMessage);
    }

    @Test
    public void result_or_exception_with_shortEXT_SEPARATORext_message_returns_short_and_ext_messages_in_short_and_extended_counterparts_respectively() {
        final ResultMessages resultMessages = resultMessages(warning("short<extended/>ext"));
        assertEquals("short", resultMessages.shortMessage);
        assertEquals("ext", resultMessages.extendedMessage);
    }

}