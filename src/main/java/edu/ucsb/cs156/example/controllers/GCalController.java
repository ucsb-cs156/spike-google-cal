package edu.ucsb.cs156.example.controllers;



import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String APPLICATION_NAME = "Google Calendar API Java Quickstart";

    @Operation(summary = "Get events from Google Calendar", description = "Get events from Google Calendar")
    @GetMapping(value = "/events")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> getEvents(
           @Parameter(name="sdate") @RequestParam(value = "sdate") String sdate,
           @Parameter(name="edate") @RequestParam(value = "edate") String edate,
           @Parameter(name="q") @RequestParam(value = "q") String q,
           OAuth2AuthenticationToken authentication) {
        log.info("sdate={}, edate={}, q={}", sdate, edate, q);
        com.google.api.services.calendar.model.Events eventList;
        String message;
        try {
            String principalName = authentication.getPrincipal().getName();
            log.info("principalName={}", principalName);
            String token = googleTokenService.getAccessToken(principalName).getTokenValue();
            log.info("token={}", token);
            GoogleCredential credential = new GoogleCredential().setAccessToken(token);
            log.info("credential={}", credential);

            final DateTime date1 = new DateTime(sdate + "T00:00:00");
            final DateTime date2 = new DateTime(edate + "T23:59:59");

            NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            Calendar service = new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                    .setApplicationName(APPLICATION_NAME).build();
            Events events = service.events();
            eventList = events.list("primary").setTimeZone("America/Los_Angeles").setTimeMin(date1).setTimeMax(date2)
                    .setQ(q)
                    .execute();
            message = eventList.getItems().toString();
            System.out.println("My:" + eventList.getItems());
        } catch (Exception e) {

            message = "Exception while handling OAuth2 callback (" + e.getMessage() + ")."
                    + " Redirecting to google connection status page.";
        }

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

}