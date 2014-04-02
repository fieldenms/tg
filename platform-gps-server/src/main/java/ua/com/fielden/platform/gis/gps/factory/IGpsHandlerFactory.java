package ua.com.fielden.platform.gis.gps.factory;

import org.jboss.netty.channel.ChannelUpstreamHandler;

/**
 * A factory for creating server handler to process GPS messages.
 * 
 * @author TG Team
 * 
 */
public interface IGpsHandlerFactory {
    ChannelUpstreamHandler create();
}
