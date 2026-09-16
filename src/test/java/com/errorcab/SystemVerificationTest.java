package com.errorcab;

import com.errorcab.config.AppConfig;
import com.errorcab.database.DatabaseManager;
import com.errorcab.database.DatabaseSeeder;
import com.errorcab.model.Booking;
import com.errorcab.model.CabType;
import com.errorcab.model.Driver;
import com.errorcab.model.Passenger;
import com.errorcab.model.Payment;
import com.errorcab.model.PaymentMethod;
import com.errorcab.model.Rating;
import com.errorcab.model.RideStatus;
import com.errorcab.model.User;
import com.errorcab.service.AuthService;
import com.errorcab.service.FareService;
import com.errorcab.service.MapService;
import com.errorcab.service.PaymentService;
import com.errorcab.service.RatingService;
import com.errorcab.service.RideService;

import java.util.Optional;

/**
 * End-to-end automated verification test for ERRORCab.
 * Validates database, authentication, polymorphic fare formulas,
 * state machine transitions, payments, ratings, and cancellations.
 */
public class SystemVerificationTest {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      ERRORCab System Automated Verification      ");
        System.out.println("==================================================");

        try {
            // 1. Database & Seeding
            System.out.println("\n[Test 1] Initializing SQLite Database & Seeding...");
            DatabaseManager.getInstance();
            DatabaseSeeder.seedIfEmpty();
            System.out.println("✓ SQLite Database schema initialized and seeded.");

            // 2. Authentication
            System.out.println("\n[Test 2] Testing Demo Authentication & Roles...");
            AuthService auth = AuthService.getInstance();

            Optional<User> pUser = auth.login(AppConfig.DEMO_PASSENGER_EMAIL, AppConfig.DEMO_PASSWORD);
            assertCondition(pUser.isPresent() && pUser.get() instanceof Passenger, "Passenger login failed");
            System.out.println("✓ Passenger login OK: " + pUser.get().getName() + " (" + pUser.get().getRole() + ")");

            Optional<User> dUser = auth.login(AppConfig.DEMO_DRIVER_EMAIL, AppConfig.DEMO_PASSWORD);
            assertCondition(dUser.isPresent() && dUser.get() instanceof Driver, "Driver login failed");
            System.out.println("✓ Driver login OK: " + dUser.get().getName() + " (" + dUser.get().getRole() + ")");

            Optional<User> aUser = auth.login(AppConfig.DEMO_ADMIN_EMAIL, AppConfig.DEMO_PASSWORD);
            assertCondition(aUser.isPresent() && aUser.get().getRole().name().equals("ADMIN"), "Admin login failed");
            System.out.println("✓ Admin login OK: " + aUser.get().getName() + " (" + aUser.get().getRole() + ")");

            // 3. Polymorphic Fare Calculation
            System.out.println("\n[Test 3] Testing Polymorphic Fare Calculations...");
            FareService fareService = FareService.getInstance();
            double testDistance = 9.2; // Kakkanad to Vyttila

            double ecoFare = fareService.calculateFare(CabType.ECONOMY, testDistance);
            double premFare = fareService.calculateFare(CabType.PREMIUM, testDistance);
            double suvFare = fareService.calculateFare(CabType.SUV, testDistance);

            System.out.println("Kakkanad → Vyttila (9.2 km):");
            System.out.println("  - Economy: ₹" + (int) ecoFare + " (Expected: ₹179)");
            System.out.println("  - Premium: ₹" + (int) premFare + " (Expected: ₹264)");
            System.out.println("  - SUV:     ₹" + (int) suvFare + " (Expected: ₹330)");

            assertCondition(ecoFare == 179.0, "Economy fare mismatch: " + ecoFare);
            assertCondition(premFare == 264.0, "Premium fare mismatch: " + premFare);
            assertCondition(suvFare == 330.0, "SUV fare mismatch: " + suvFare);

            // Minimum fare test (1 km)
            double shortEco = fareService.calculateFare(CabType.ECONOMY, 1.0);
            System.out.println("Short trip (1.0 km) Economy: ₹" + (int) shortEco + " (Expected Minimum: ₹80)");
            assertCondition(shortEco == 80.0, "Minimum fare not applied: " + shortEco);
            System.out.println("✓ Polymorphic fare calculations passed perfectly.");

            // 4. Ride Lifecycle & State Machine
            System.out.println("\n[Test 4] Testing Ride Lifecycle & State Machine...");
            Passenger passenger = (Passenger) pUser.get();
            RideService rideService = RideService.getInstance();

            Booking booking = rideService.requestRide(passenger, "Kakkanad", "Vyttila", CabType.ECONOMY, 9.2, 24, 179.0);
            System.out.println("Created booking " + booking.getBookingCode() + " with status: " + booking.getStatus());
            assertCondition(booking.getStatus() == RideStatus.SEARCHING, "Initial status must be SEARCHING");

            boolean assigned = rideService.assignAvailableDriver(booking);
            assertCondition(assigned && booking.getStatus() == RideStatus.DRIVER_ASSIGNED, "Driver assignment failed");
            System.out.println("✓ Driver assigned: " + booking.getDriverName() + " (" + booking.getVehiclePlateNumber() + ")");

            rideService.advanceRideStatus(booking, RideStatus.DRIVER_ARRIVING);
            assertCondition(booking.getStatus() == RideStatus.DRIVER_ARRIVING, "Advance to DRIVER_ARRIVING failed");
            System.out.println("✓ Status advanced to: " + booking.getStatus());

            rideService.advanceRideStatus(booking, RideStatus.RIDE_STARTED);
            assertCondition(booking.getStatus() == RideStatus.RIDE_STARTED, "Advance to RIDE_STARTED failed");
            System.out.println("✓ Status advanced to: " + booking.getStatus());

            rideService.advanceRideStatus(booking, RideStatus.RIDE_COMPLETED);
            assertCondition(booking.getStatus() == RideStatus.RIDE_COMPLETED, "Advance to RIDE_COMPLETED failed");
            System.out.println("✓ Status advanced to: " + booking.getStatus());

            // Test invalid state transition
            boolean transitionFailed = false;
            try {
                rideService.advanceRideStatus(booking, RideStatus.SEARCHING);
            } catch (IllegalStateException e) {
                transitionFailed = true;
            }
            assertCondition(transitionFailed, "Terminal state transition protection failed!");
            System.out.println("✓ State machine correctly prevented invalid transition from RIDE_COMPLETED to SEARCHING.");

            // 5. Payment Processing
            System.out.println("\n[Test 5] Testing Demo Payment Simulation...");
            PaymentService paymentService = PaymentService.getInstance();
            Payment payment = paymentService.processPayment(booking, PaymentMethod.UPI);
            assertCondition(payment.getStatus() == com.errorcab.model.PaymentStatus.PAID, "Payment status not PAID");
            System.out.println("✓ UPI Payment successful: ₹" + (int) payment.getAmount() + " (Ref: " + payment.getTransactionRef() + ")");

            // 6. Rating System
            System.out.println("\n[Test 6] Testing Driver Rating System...");
            RatingService ratingService = RatingService.getInstance();
            Rating rating = ratingService.submitRating(booking, 5, "Smooth drive, driver was courteous.");
            assertCondition(rating.getStars() == 5, "Rating stars mismatch");
            System.out.println("✓ Submitted 5-star rating for driver: " + booking.getDriverName());

            boolean duplicateBlocked = false;
            try {
                ratingService.submitRating(booking, 4, "Duplicate rating attempt");
            } catch (IllegalStateException e) {
                duplicateBlocked = true;
            }
            assertCondition(duplicateBlocked, "Duplicate rating was not blocked!");
            System.out.println("✓ Duplicate rating correctly prevented.");

            // 7. Cancellation Test
            System.out.println("\n[Test 7] Testing Ride Cancellation Workflow...");
            Booking cancelBooking = rideService.requestRide(passenger, "Edappally", "Fort Kochi", CabType.PREMIUM, 16.5, 42, 410.0);
            rideService.assignAvailableDriver(cancelBooking);
            boolean cancelled = rideService.cancelRide(cancelBooking, "Changed plans");
            assertCondition(cancelled && cancelBooking.getStatus() == RideStatus.CANCELLED, "Cancellation failed");
            System.out.println("✓ Ride " + cancelBooking.getBookingCode() + " successfully cancelled with reason: " + cancelBooking.getCancellationReason());

            // 8. Map Service Test
            System.out.println("\n[Test 8] Testing Map Service...");
            MapService mapService = MapService.getInstance();
            double kToV = mapService.getDistanceKm("Kakkanad", "Vyttila");
            assertCondition(kToV == 9.2, "Distance calculation mismatch: " + kToV);
            System.out.println("✓ Route Kakkanad → Vyttila verified: " + kToV + " km (" + mapService.getEstimatedMinutes(kToV) + " mins)");

            System.out.println("\n==================================================");
            System.out.println("       ALL 8 SYSTEM VERIFICATION TESTS PASSED!    ");
            System.out.println("==================================================");

        } catch (Throwable t) {
            System.err.println("\n❌ Test failed with exception:");
            t.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertCondition(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }
}
