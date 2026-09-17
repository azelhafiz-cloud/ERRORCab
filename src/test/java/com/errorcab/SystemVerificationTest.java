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
import com.errorcab.repository.UserRepository;
import com.errorcab.service.AuthService;
import com.errorcab.service.BookingService;
import com.errorcab.service.DriverService;
import com.errorcab.service.FareService;
import com.errorcab.service.MapService;
import com.errorcab.service.NotificationService;
import com.errorcab.service.PaymentService;
import com.errorcab.service.RatingService;
import com.errorcab.service.RideService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * End-to-end automated verification test for ERRORCab.
 * Validates database, authentication, polymorphic fare formulas,
 * OTP lifecycle, decline/reassign, acceptance rates, scheduled rides,
 * payment history, profile updates, and driver earnings.
 */
public class SystemVerificationTest {

    @Test
    public void runAllSystemTests() {
        main(new String[0]);
    }

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

            // 4. Ride Lifecycle & OTP Verification
            System.out.println("\n[Test 4] Testing Ride Lifecycle & 4-Digit OTP Verification...");
            Passenger passenger = (Passenger) pUser.get();
            RideService rideService = RideService.getInstance();

            Booking booking = rideService.requestRide(passenger, "Kakkanad", "Vyttila", CabType.ECONOMY, 9.2, 24, 179.0);
            System.out.println("Created booking " + booking.getBookingCode() + " with status: " + booking.getStatus());
            assertCondition(booking.getStatus() == RideStatus.SEARCHING, "Initial status must be SEARCHING");

            boolean assigned = rideService.assignAvailableDriver(booking);
            assertCondition(assigned && booking.getStatus() == RideStatus.DRIVER_ASSIGNED, "Driver assignment failed");
            System.out.println("✓ Driver assigned: " + booking.getDriverName() + " (" + booking.getVehiclePlateNumber() + ")");

            assertCondition(booking.getOtp() != null && booking.getOtp().length() == 4, "Generated OTP must be 4 digits");
            System.out.println("✓ Backend-generated OTP: " + booking.getOtp());

            rideService.advanceRideStatus(booking, RideStatus.DRIVER_ARRIVING);
            assertCondition(booking.getStatus() == RideStatus.DRIVER_ARRIVING, "Advance to DRIVER_ARRIVING failed");
            System.out.println("✓ Status advanced to: " + booking.getStatus());

            // Wrong OTP rejected
            boolean wrongOtpBlocked = false;
            try {
                rideService.advanceRideStatus(booking, RideStatus.RIDE_STARTED, "0000");
            } catch (IllegalArgumentException e) {
                wrongOtpBlocked = true;
            }
            assertCondition(wrongOtpBlocked, "Invalid OTP was not blocked!");
            System.out.println("✓ Invalid OTP rejected correctly.");

            // Valid OTP accepted
            rideService.advanceRideStatus(booking, RideStatus.RIDE_STARTED, booking.getOtp());
            assertCondition(booking.getStatus() == RideStatus.RIDE_STARTED, "Advance to RIDE_STARTED failed with correct OTP");
            System.out.println("✓ Status advanced to RIDE_STARTED using verified passenger OTP.");

            rideService.advanceRideStatus(booking, RideStatus.RIDE_COMPLETED);
            assertCondition(booking.getStatus() == RideStatus.RIDE_COMPLETED, "Advance to RIDE_COMPLETED failed");
            System.out.println("✓ Status advanced to: " + booking.getStatus());

            // 5. Driver Decline & Reassignment Flow
            System.out.println("\n[Test 5] Testing Driver Decline & Reassignment...");
            Booking declineBooking = rideService.requestRide(passenger, "Edappally", "Fort Kochi", CabType.ECONOMY, 16.5, 42, 305.0);
            rideService.assignAvailableDriver(declineBooking);
            int firstDriverId = declineBooking.getDriverId();
            assertCondition(firstDriverId > 0, "First driver assignment failed");
            System.out.println("First assigned driver: ID " + firstDriverId);

            Map<String, Object> reassignResult = rideService.declineAndReassign(declineBooking.getId(), firstDriverId);
            assertCondition(reassignResult != null && Boolean.TRUE.equals(reassignResult.get("success")), "Reassigned booking returned failure");
            Booking updatedDeclineBooking = rideService.getBookingById(declineBooking.getId()).orElse(null);
            assertCondition(updatedDeclineBooking != null && updatedDeclineBooking.hasDriverDeclined(firstDriverId), "Declined driver ID was not recorded in booking exclusion list");
            System.out.println("✓ Driver " + firstDriverId + " successfully declined. Excluded in subsequent assignment.");

            // 6. Dynamic Driver Acceptance Rate
            System.out.println("\n[Test 6] Testing Dynamic Driver Acceptance Rate...");
            Driver testDriver = (Driver) dUser.get();
            double accRate = testDriver.getAcceptanceRate();
            assertCondition(accRate >= 0.0 && accRate <= 100.0, "Acceptance rate out of range: " + accRate);
            System.out.println("✓ Dynamic driver acceptance rate verified: " + accRate + "%");

            // 7. Scheduled Rides Workflow
            System.out.println("\n[Test 7] Testing Scheduled Rides Workflow...");
            BookingService bookingService = new BookingService();
            String scheduledDate = LocalDateTime.now().plusHours(2).toString();
            Booking scheduledRide = bookingService.scheduleRide(passenger, "MG Road", "Airport", CabType.PREMIUM, scheduledDate, "");
            assertCondition(scheduledRide != null && scheduledRide.isScheduled(), "Scheduled ride flag missing");
            assertCondition(scheduledRide.getStatus() == RideStatus.SCHEDULED, "Scheduled ride status must be SCHEDULED");
            System.out.println("✓ Scheduled ride created with code " + scheduledRide.getBookingCode() + " for: " + scheduledRide.getScheduledTime());

            List<Booking> passengerScheds = bookingService.getScheduledBookings(passenger.getId());
            assertCondition(!passengerScheds.isEmpty(), "Scheduled rides list returned empty");
            System.out.println("✓ Retrieved " + passengerScheds.size() + " scheduled rides for passenger.");

            boolean schedCancelled = bookingService.cancelScheduledRide(scheduledRide.getId());
            assertCondition(schedCancelled, "Failed to cancel scheduled ride");
            System.out.println("✓ Scheduled ride successfully cancelled.");

            // 8. Payment Processing & Payment History Audit
            System.out.println("\n[Test 8] Testing Payment Processing & Payment History Audit...");
            PaymentService paymentService = PaymentService.getInstance();
            Payment payment = paymentService.processPayment(booking, PaymentMethod.UPI);
            assertCondition(payment.getStatus() == com.errorcab.model.PaymentStatus.PAID, "Payment status not PAID");
            System.out.println("✓ UPI Payment processed: ₹" + (int) payment.getAmount() + " (Ref: " + payment.getTransactionRef() + ")");

            List<Map<String, Object>> userPayments = paymentService.getPaymentsByUser(passenger.getId());
            assertCondition(!userPayments.isEmpty(), "Payment history audit ledger returned empty");
            System.out.println("✓ Payment history audit ledger verified: " + userPayments.size() + " transaction records.");

            // 9. Driver Earnings Analytics
            System.out.println("\n[Test 9] Testing Driver Earnings Analytics...");
            DriverService driverService = new DriverService();
            Map<String, Object> earnings = driverService.getDriverEarningsAnalytics(firstDriverId > 0 ? firstDriverId : 2);
            assertCondition(earnings != null && earnings.containsKey("dailyTrend"), "Earnings analytics missing daily trend");
            System.out.println("✓ Driver earnings analytics verified: " + earnings.get("dailyTrend"));

            // 10. Profile Update
            System.out.println("\n[Test 10] Testing Profile Update...");
            UserRepository userRepo = new UserRepository();
            String newPhone = "+91 99999 88888";
            String newAddress = "Infopark Phase 2, Kakkanad";
            boolean updated = userRepo.updateUserProfile(passenger.getId(), "Rahul Passenger", newPhone, newAddress);
            assertCondition(updated, "Profile update failed");
            Optional<User> updatedUser = userRepo.findById(passenger.getId());
            assertCondition(updatedUser.isPresent() && newPhone.equals(updatedUser.get().getPhone()), "Profile phone mismatch");
            System.out.println("✓ User profile updated successfully: " + updatedUser.get().getName() + " (" + updatedUser.get().getPhone() + ")");

            // 11. Notification Center
            System.out.println("\n[Test 11] Testing Notification Center...");
            NotificationService notificationService = NotificationService.getInstance();
            int unread = notificationService.getUnreadCount(passenger.getId());
            System.out.println("✓ Passenger unread notifications count: " + unread);
            notificationService.markAllAsRead(passenger.getId());
            int afterRead = notificationService.getUnreadCount(passenger.getId());
            assertCondition(afterRead == 0, "Mark all as read failed, unread: " + afterRead);
            System.out.println("✓ Mark all notifications as read verified.");

            // 12. Rating System
            System.out.println("\n[Test 12] Testing Driver Rating System...");
            RatingService ratingService = RatingService.getInstance();
            Rating rating = ratingService.submitRating(booking, 5, "Smooth drive, driver was courteous.");
            assertCondition(rating.getStars() == 5, "Rating stars mismatch");
            System.out.println("✓ Submitted 5-star rating for driver: " + booking.getDriverName());

            System.out.println("\n==================================================");
            System.out.println("     ALL 12 SYSTEM VERIFICATION TESTS PASSED!     ");
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
