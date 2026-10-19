package app;

import exception.RoomNotAvailableException;
import java.time.LocalDate;
import model.booking.Booking;
import model.booking.Invoice;
import model.payment.CardPayment;
import model.payment.CashPayment;
import model.payment.Payment;
import model.payment.UpiPayment;
import model.person.Guest;
import model.person.Staff;
import model.room.DeluxeRoom;
import model.room.Room;
import model.room.StandardRoom;
import model.room.SuiteRoom;
import service.Hotel;

public class HotelApp {

    public static void main(String[] args) {

        // ---- Setup: build the hotel, its rooms, guests and staff ----
        Hotel hotel = new Hotel("Grand Orchid Hotel");

        hotel.addRoom(new StandardRoom(101, 1, 2000));
        hotel.addRoom(new StandardRoom(102, 1, 2000));
        hotel.addRoom(new DeluxeRoom(201, 2, 3500));
        hotel.addRoom(new SuiteRoom(301, 3, 8000));

        Guest aarav = new Guest(
                "G001", "Aarav Sen", "9830012345",
                "aarav@example.com", "AADHAAR-1111");

        Guest meera = new Guest(
                "G002", "Meera Das", "9830054321",
                "meera@example.com", "PASSPORT-2222");

        Guest kabir = new Guest(
                "G003", "Kabir Roy", "9830011111",
                "kabir@example.com", "VOTER-3333");

        hotel.registerGuest(aarav);
        hotel.registerGuest(meera);
        hotel.registerGuest(kabir);

        Staff rohit = new Staff(
                "S001", "Rohit Ghosh", "9830099999",
                "Receptionist", 28000, "Morning");

        System.out.println("=== " + hotel.getName() + " ===");

        // ---- Demo 1: polymorphism. Same call, different price rules ----
        System.out.println("\n[1] Tariff for 5 nights (polymorphism)");

        for (Room r : hotel.getAvailableRooms()) {
            System.out.printf(" %-8s Room %d : Rs.%9.2f (%s)%n",
                    r.getType(), r.getRoomNumber(),
                    r.calculateTariff(5), r.getAmenities());
        }

        try {
            // ---- Demo 2: object interaction. Guest + Room + Staff ----
            System.out.println("\n[2] Making bookings");

            Booking b1 = hotel.bookRoom(
                    aarav, 101,
                    LocalDate.of(2026, 11, 10),
                    LocalDate.of(2026, 11, 13), rohit);

            Booking b2 = hotel.bookRoom(
                    meera, 301,
                    LocalDate.of(2026, 11, 10),
                    LocalDate.of(2026, 11, 15), rohit);

            System.out.println(" " + b1);
            System.out.println(" " + b2);

            System.out.printf(" Occupancy now: %.1f%%%n",
                    hotel.getOccupancyPercent());

            // ---- Demo 3: error handling. Double booking is rejected ----
            System.out.println("\n[3] Trying to double-book room 101");

            try {
                hotel.bookRoom(
                        kabir, 101,
                        LocalDate.of(2026, 11, 11),
                        LocalDate.of(2026, 11, 12), rohit);

            } catch (RoomNotAvailableException e) {
                System.out.println(" Rejected: " + e.getMessage());
            }

            // ---- Demo 4: encapsulation. Bad dates are rejected ----
            System.out.println("\n[4] Trying a booking with bad dates");

            try {
                hotel.bookRoom(
                        kabir, 102,
                        LocalDate.of(2026, 11, 15),
                        LocalDate.of(2026, 11, 12), rohit);

            } catch (IllegalArgumentException e) {
                System.out.println(" Rejected: " + e.getMessage());
            }

            System.out.println("\n[5] Room status board");
            hotel.printRoomStatus();

            // ---- Demo 5: checkout, invoice, and abstract payments ----
            System.out.println("\n[6] Aarav checks out and pays by UPI");

            Invoice inv1 = hotel.checkOut(b1.getBookingId());
            inv1.print();

            Payment upi = new UpiPayment(
                    inv1.getTotal(), "aarav@okbank");

            inv1.settle(upi);
            System.out.println(" " + upi.getReceipt());

            System.out.println(
                    "\n[7] Meera checks out. Card fails, cash works");

            Invoice inv2 = hotel.checkOut(b2.getBookingId());
            inv2.print();

            Payment card = new CardPayment(
                    inv2.getTotal(), "1234", "Meera Das");

            inv2.settle(card);
            System.out.println(" " + card.getReceipt());

            CashPayment cash = new CashPayment(
                    inv2.getTotal(), 42000);

            inv2.settle(cash);
            System.out.println(" " + cash.getReceipt());

            System.out.printf(" Change returned: Rs.%.2f%n",
                    cash.getChange());

            // ---- Demo 6: final state ----
            System.out.println("\n[8] Final state");

            hotel.printRoomStatus();

            System.out.printf(" Occupancy now: %.1f%%%n",
                    hotel.getOccupancyPercent());

            System.out.println(" Aarav nights stayed: "
                    + aarav.getTotalNightsStayed());

            System.out.println(" Both invoices paid: "
                    + (inv1.isPaid() && inv2.isPaid()));

        } catch (RoomNotAvailableException e) {
            System.out.println(
                    "Unexpected booking failure: " + e.getMessage());
        }
    }
}


//this was just to check if all my functions work correctly or not , and it worked like a fucking charm , also thism arks the first checkpoint for this project 

/*
- created 4 rooms, 3 guests and 1 staff member
- tested polymorphism using calculateTariff()
-tested booking and double-booking errors.
-tested invalid check-in/check-out dates
-checked room status and occupancy.
- generated invoices and tested payment methods
- ysed try-catch to handle exceptions
-checked the final status after checkout and payment
*/