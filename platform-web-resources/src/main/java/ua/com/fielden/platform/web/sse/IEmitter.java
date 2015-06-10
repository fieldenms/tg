package ua.com.fielden.platform.web.sse;

import java.io.IOException;

/**
 * {@link IEmitter} is the active half of an event source connection, and allows applications to operate on the connection by sending events, data or comments, or by closing the
 * connection.
 * <p>
 * An {@link IEmitter} instance will be created for each new event source connection.
 * <p>
 * {@link IEmitter} instances are fully thread safe and can be used from multiple threads.
 */
public interface IEmitter
{
    /**
     * Sends a named event with data to the client.
     * <p>
     * When invoked as: <code>event("foo", "bar")</code>, the client will receive the lines:
     * <pre>
     * event: foo
     * data: bar
     * </pre>
     *
     * @param name
     *            the event name
     * @param data
     *            the data to be sent
     * @throws IOException
     *             if an I/O failure occurred
     * @see #data(String)
     */
    public void event(final String name, final String data) throws IOException;

    /**
     * Sends a default event with data to the client.
     * <p>
     * When invoked as: <code>data("baz")</code>, the client will receive the line:
     * <pre>
     * data: baz
     * </pre>
     * When invoked as: <code>data("foo\r\nbar\rbaz\nbax")</code>, the client will receive the lines:
     * <pre>
     * data: foo
     * data: bar
     * data: baz
     * data: bax
     * </pre>
     *
     * @param data
     *            the data to be sent
     * @throws IOException
     *             if an I/O failure occurred
     */
    public void data(final String data) throws IOException;

    /**
     * Sends a comment to the client.
     * <p>
     * When invoked as: <code>comment("foo")</code>, the client will receive the line:
     *
     * <pre>
     * : foo
     * </pre>
     *
     * @param comment
     *            the comment to send
     * @throws IOException
     *             if an I/O failure occurred
     */
    public void comment(final String comment) throws IOException;

    /**
     * Closes this event source connection.
     */
    public void close();
}