package ua.com.fielden.platform.example.google.calendar;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.TimeZone;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Event.Reminders;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;

public class GoogleCalendarSpike {

    private static final String APPLICATION_NAME = "TG-Platform";

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY = new JacksonFactory();

    /** Directory to store user credentials. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"), ".tg-calendar-spike");

    /**
     * Global instance of the {@link DataStoreFactory}. The best practice is to make it a single globally shared instance across your application.
     */
    private static FileDataStoreFactory dataStoreFactory;

    /** Global instance of the HTTP transport. */
    private static HttpTransport httpTransport;

    private static com.google.api.services.calendar.Calendar client;

    public static void main(final String[] args) {
        try {
            // initialize the transport
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();

            // initialize the data store factory
            dataStoreFactory = new FileDataStoreFactory(DATA_STORE_DIR);

            // authorization
            final Credential credential = authorise();

            // set up global Calendar instance
            client = new com.google.api.services.calendar.Calendar.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();

            final Calendar calendar = addCalendar();
            addEvent(calendar);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static Calendar addCalendar() throws IOException {
        System.out.println("Add Calendar");
        final Calendar entry = new Calendar();
        entry.setSummary("Calendar for Testing 1");
        final Calendar result = client.calendars().insert(entry).execute();
        System.out.println(result);
        return result;
    }

    private static void addEvent(final Calendar calendar) throws IOException {
        System.out.println("Add Event");
        final Event event = newEvent();
        final Event result = client.events().insert(calendar.getId(), event).execute();
        System.out.println(result.getCreator().getEmail());
        System.out.println(result);
    }

    private static Event newEvent() {
        final Event event = new Event();
        event.setSummary("New Event");
        final Date startDate = new Date();
        final Date endDate = new Date(startDate.getTime() + 3600000);
        final DateTime start = new DateTime(startDate, TimeZone.getTimeZone("UTC"));
        event.setStart(new EventDateTime().setDateTime(start));
        final DateTime end = new DateTime(endDate, TimeZone.getTimeZone("UTC"));
        event.setEnd(new EventDateTime().setDateTime(end));
        final Reminders remMap = new Reminders();
        final EventReminder rem = new EventReminder();
        rem.setMethod("email");
        rem.setMinutes(Integer.valueOf(10));
        remMap.setUseDefault(Boolean.FALSE);
        remMap.setOverrides(Arrays.asList(rem));
        event.setAttendees(Arrays.asList(new EventAttendee().setEmail("oleh@fielden.com.au")));
        event.setReminders(remMap);
        return event;
    }

    private static Credential authorise() throws Exception {
        // load client secrets
        final GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(GoogleCalendarSpike.class.getResourceAsStream("/client_secret.json")));
        if (clientSecrets.getDetails().getClientId().startsWith("Enter") || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
            System.out.println("Enter Client ID and Secret from https://code.google.com/apis/console/?api=calendar "
                    + "into platform-launcher/src/main/resources/client_secrets.json");
            System.exit(1);
        }
        // set up authorization code flow
        final GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, Collections.singleton(CalendarScopes.CALENDAR)).setDataStoreFactory(dataStoreFactory).build();
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    }
}
