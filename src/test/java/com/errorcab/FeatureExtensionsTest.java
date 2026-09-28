package com.errorcab;

import com.errorcab.config.AppConfig;
import com.errorcab.controller.AdminController;
import com.errorcab.controller.AuthController;
import com.errorcab.database.DatabaseManager;
import com.errorcab.database.DatabaseSeeder;
import com.errorcab.model.Role;
import com.errorcab.model.User;
import com.errorcab.repository.UserRepository;
import com.errorcab.service.AuthenticationService;
import com.errorcab.service.SessionService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class FeatureExtensionsTest {

    @BeforeAll
    public static void setUp() {
        DatabaseManager.getInstance();
        DatabaseSeeder.seedIfEmpty();
    }

    @Test
    public void testPassengerProfileUpdateWithPhoneNumber() {
        AuthenticationService authService = new AuthenticationService();
        SessionService sessionService = new SessionService();
        AuthController authController = new AuthController(authService, sessionService);

        UserRepository userRepo = new UserRepository();
        Optional<User> passengerOpt = userRepo.findByEmail(AppConfig.DEMO_PASSENGER_EMAIL);
        assertTrue(passengerOpt.isPresent(), "Demo passenger should be present");
        User passenger = passengerOpt.get();

        String token = sessionService.createSession(passenger);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        // Test with phoneNumber and defaultAddress keys (sent by frontend profile page)
        Map<String, String> body = new HashMap<>();
        body.put("name", "Rahul Verified Passenger");
        body.put("phoneNumber", "+91 98460 11223");
        body.put("defaultAddress", "Kakkanad InfoPark Phase 1");

        ResponseEntity<?> response = authController.updateProfile(body, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> resBody = (Map<String, Object>) response.getBody();
        assertNotNull(resBody);
        assertTrue((Boolean) resBody.get("success"));
        assertEquals("Rahul Verified Passenger", resBody.get("name"));
        assertEquals("+91 98460 11223", resBody.get("phone"));
        assertEquals("+91 98460 11223", resBody.get("phoneNumber"));
        assertEquals("Kakkanad InfoPark Phase 1", resBody.get("defaultAddress"));

        // Verify DB persistence
        Optional<User> updatedDbUser = userRepo.findById(passenger.getId());
        assertTrue(updatedDbUser.isPresent());
        assertEquals("Rahul Verified Passenger", updatedDbUser.get().getName());
        assertEquals("+91 98460 11223", updatedDbUser.get().getPhone());
    }

    @Test
    public void testPassengerProfileUpdateWithPhoneKey() {
        AuthenticationService authService = new AuthenticationService();
        SessionService sessionService = new SessionService();
        AuthController authController = new AuthController(authService, sessionService);

        UserRepository userRepo = new UserRepository();
        Optional<User> passengerOpt = userRepo.findByEmail(AppConfig.DEMO_PASSENGER_EMAIL);
        assertTrue(passengerOpt.isPresent(), "Demo passenger should be present");
        User passenger = passengerOpt.get();

        String token = sessionService.createSession(passenger);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        // Test with phone and address keys
        Map<String, String> body = new HashMap<>();
        body.put("name", "Rahul P");
        body.put("phone", "+91 98460 33445");
        body.put("address", "Marine Drive, Kochi");

        ResponseEntity<?> response = authController.updateProfile(body, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify DB persistence
        Optional<User> updatedDbUser = userRepo.findById(passenger.getId());
        assertTrue(updatedDbUser.isPresent());
        assertEquals("Rahul P", updatedDbUser.get().getName());
        assertEquals("+91 98460 33445", updatedDbUser.get().getPhone());
    }

    @Test
    public void testPassengerProfileUpdatePreservesPhoneWhenPhoneOmitted() {
        AuthenticationService authService = new AuthenticationService();
        SessionService sessionService = new SessionService();
        AuthController authController = new AuthController(authService, sessionService);

        UserRepository userRepo = new UserRepository();
        Optional<User> passengerOpt = userRepo.findByEmail(AppConfig.DEMO_PASSENGER_EMAIL);
        assertTrue(passengerOpt.isPresent(), "Demo passenger should be present");
        User passenger = passengerOpt.get();
        String initialPhone = passenger.getPhone();

        String token = sessionService.createSession(passenger);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        // Send ONLY name (simulating partial update or disabled/omitted phone field)
        Map<String, String> body = new HashMap<>();
        body.put("name", "Rahul Partial Update Name");

        ResponseEntity<?> response = authController.updateProfile(body, request);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Verify that name was updated while existing phone number was preserved
        Optional<User> updatedDbUser = userRepo.findById(passenger.getId());
        assertTrue(updatedDbUser.isPresent());
        assertEquals("Rahul Partial Update Name", updatedDbUser.get().getName());
        assertEquals(initialPhone, updatedDbUser.get().getPhone());
    }

    @Test
    public void testProfileValidationRejectsEmptyName() {
        AuthenticationService authService = new AuthenticationService();
        SessionService sessionService = new SessionService();
        AuthController authController = new AuthController(authService, sessionService);

        UserRepository userRepo = new UserRepository();
        Optional<User> passengerOpt = userRepo.findByEmail(AppConfig.DEMO_PASSENGER_EMAIL);
        assertTrue(passengerOpt.isPresent(), "Demo passenger should be present");
        User passenger = passengerOpt.get();

        String token = sessionService.createSession(passenger);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        Map<String, String> body = new HashMap<>();
        body.put("name", "   ");

        ResponseEntity<?> response = authController.updateProfile(body, request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testAdminDriverRegistrationRBAC() {
        AuthenticationService authService = new AuthenticationService();
        SessionService sessionService = new SessionService();
        AdminController adminController = new AdminController(sessionService, authService);

        UserRepository userRepo = new UserRepository();

        // 1. Unauthorized request (no session token)
        MockHttpServletRequest unauthRequest = new MockHttpServletRequest();
        Map<String, String> driverPayload = new HashMap<>();
        driverPayload.put("name", "Admin Registered Driver");
        driverPayload.put("email", "admin_driver_" + System.currentTimeMillis() + "@errorcab.com");
        driverPayload.put("phone", "+91 98765 43210");
        driverPayload.put("password", "secret123");
        driverPayload.put("licenseNumber", "KL-07-" + (System.currentTimeMillis() % 100000));
        driverPayload.put("vehicleModel", "Hyundai Aura");
        driverPayload.put("plateNumber", "KL 07 CD " + (1000 + (int)(Math.random() * 9000)));
        driverPayload.put("cabType", "ECONOMY");
        driverPayload.put("color", "White");

        ResponseEntity<?> unauthRes = adminController.addDriver(driverPayload, unauthRequest);
        assertEquals(HttpStatus.UNAUTHORIZED, unauthRes.getStatusCode());

        // 2. Forbidden request (logged in as PASSENGER)
        Optional<User> passengerOpt = userRepo.findByEmail(AppConfig.DEMO_PASSENGER_EMAIL);
        assertTrue(passengerOpt.isPresent());
        String passengerToken = sessionService.createSession(passengerOpt.get());

        MockHttpServletRequest passengerRequest = new MockHttpServletRequest();
        passengerRequest.addHeader("Authorization", "Bearer " + passengerToken);

        ResponseEntity<?> forbiddenRes = adminController.addDriver(driverPayload, passengerRequest);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenRes.getStatusCode());

        // 3. Authorized request (logged in as ADMIN)
        Optional<User> adminOpt = userRepo.findByEmail(AppConfig.DEMO_ADMIN_EMAIL);
        assertTrue(adminOpt.isPresent(), "Demo admin should be present");
        String adminToken = sessionService.createSession(adminOpt.get());

        MockHttpServletRequest adminRequest = new MockHttpServletRequest();
        adminRequest.addHeader("Authorization", "Bearer " + adminToken);

        ResponseEntity<?> successRes = adminController.addDriver(driverPayload, adminRequest);
        assertEquals(HttpStatus.CREATED, successRes.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> resBody = (Map<String, Object>) successRes.getBody();
        assertNotNull(resBody);
        assertTrue((Boolean) resBody.get("success"));

        // 4. Verify the newly registered driver can log in
        User loggedInDriver = authService.login(driverPayload.get("email"), driverPayload.get("password"));
        assertNotNull(loggedInDriver);
        assertEquals(Role.DRIVER, loggedInDriver.getRole());
        assertEquals(driverPayload.get("name"), loggedInDriver.getName());
    }
}
