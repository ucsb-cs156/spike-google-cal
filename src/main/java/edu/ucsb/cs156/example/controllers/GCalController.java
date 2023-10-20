package edu.ucsb.cs156.example.controllers;

import java.io.FileInputStream;
import java.util.Collections;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar.Events;
import com.google.api.services.calendar.Calendar;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import edu.ucsb.cs156.example.services.GoogleTokenService;
import com.google.api.client.http.javanet.NetHttpTransport;

@Tag(name = "GCalController")
@RequestMapping("/api/gcal")
@Controller
@Slf4j
public class GCalController {

    @Autowired
    GoogleTokenService googleTokenService;

    @Value("${app.gcal.calendarId:primary}")
    private String calendarId;

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    @Operation(summary = "Get events from Google Calendar", description = "Get events from Google Calendar")
    @GetMapping(value = "/events")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> getAllEvents() throws Exception {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
        Events events = service.events();
        // log.info("events={}", events);
        com.google.api.services.calendar.model.Events eventList = events.list(calendarId)
                .setTimeZone("America/Los_Angeles")
                .execute();
        log.info("eventList={}", eventList.toPrettyString());
        return new ResponseEntity<String>(eventList.toPrettyString(), HttpStatus.OK);
    }

     @Operation(summary = "Get events from Google Calendar", description = "Get events from Google Calendar")
    @GetMapping(value = "/events/bydate")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> getEventsByDate(
        @Parameter(description="Start Date", required=true)
        @RequestParam("sdate") String sdate,
        @Parameter(description="End Date", required=true)
        @RequestParam("edate") String edate
        ) throws Exception {
        GoogleCredential credential = GoogleCredential.fromStream(new FileInputStream("credentials.json"))
                .createScoped(Collections.singleton("https://www.googleapis.com/auth/calendar"));

        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME).build();
        Events events = service.events();
        // log.info("events={}", events);

        final DateTime date1 = new DateTime(sdate + "T00:00:00");
        final DateTime date2 = new DateTime(edate + "T23:59:59");

        com.google.api.services.calendar.model.Events eventList = events.list(calendarId)
                .setTimeZone("America/Los_Angeles")
                .setTimeMin(date1).setTimeMax(date2)
                .execute();
        log.info("eventList={}", eventList.toPrettyString());
        return new ResponseEntity<String>(eventList.toPrettyString(), HttpStatus.OK);
    }

}