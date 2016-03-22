package ua.com.fielden.platform.web.sse._experimentation;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;

public class _EventSourcingResourceFactory extends Restlet {


    @Override
    public void handle(final Request request, final Response response) {

        if (Method.GET == request.getMethod()) {
            final TimestampEventSource eventSrouce = new TimestampEventSource();


            new _EventSourcingResource(getContext(), request, response) {

                @Override
                public _EventSource newEventSource(final HttpServletRequest request) {
                    return eventSrouce;
                }

            }.handle();
        }
    }

    public static class TimestampEventSource implements _EventSource {

        private Emitter emitter;

        @Override
        public void onOpen(final Emitter emitter) throws IOException {
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
