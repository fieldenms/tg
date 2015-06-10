package ua.com.fielden.platform.web.test.server.sse;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

public class EventSourcingResourceFactory extends Restlet {


    @Override
    public void handle(final Request request, final Response response) {

        if (Method.GET == request.getMethod()) {
            final TimestampEventSource eventSrouce = new TimestampEventSource();


            new EventSourcingResource(getContext(), request, response) {

                @Override
                public IEventSource newEventSource(final HttpServletRequest request) {
                    return eventSrouce;
                }

            }.handle();
        }
    }

    public static class TimestampEventSource implements IEventSource {

        private IEmitter emitter;

        @Override
        public void onOpen(final IEmitter emitter) throws IOException {
            System.out.println("Connection established.");
            this.emitter = emitter;
            emitEvent("connection was established");
        }

        public void emitEvent(final String dataToSend) {
            try {
                this.emitter.data(new Date().toString());
            } catch (final IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        public void onClose() {
            System.out.println("Connection has been closed.");
        }

    }

}
