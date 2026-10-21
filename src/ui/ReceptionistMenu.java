package ui;

import exception.RoomNotAvailableException;
import java.time.LocalDate;
import java.util.Scanner;
import model.booking.Booking;
import model.booking.Invoice;
import model.person.Guest;
import model.person.Staff;
import service.Hotel;

public class ReceptionistMenu extends StaffMenu {
    public ReceptionistMenu(Hotel hotel, Scanner in, Staff user) {
        super(hotel, in, user);
    }

    @Override
    protected void showMenu() {
        System.out.println("\n1. Room status board");
        System.out.println("2. Register a new guest");
        System.out.println("3. Book a room for a guest");
        System.out.println("4. Check out a guest and take payment");
        System.out.println("5. Send a room to maintenance");
        System.out.println("6. View all bookings");
        System.out.println("7. Log out");
    }

    @Override
    protected boolean handle(String choice) throws RoomNotAvailableException {
        switch (choice) {
            case "1": showBoard(); return true;
            case "2": registerGuest(); return true;
            case "3": bookForGuest(); return true;
            case "4": checkOutGuest(); return true;
            case "5": reportMaintenance(); return true;
            case "6": listBookings(); return true;
            case "7": return false;
            default:
                System.out.println("  Please choose 1 to 7.");
                return true;
        }
    }

    private void registerGuest() {
        Guest guest = hotel.registerNewGuest(ask("Guest name: "), ask("Phone (10 digits): "),
                ask("Email: "), ask("ID proof: "));
        System.out.println("  Registered: " + guest + "   Guest ID: " + guest.getId());
    }

    private void bookForGuest() throws RoomNotAvailableException {
        Guest guest = hotel.findGuest(ask("Guest ID (for example G001): "));
        int roomNumber = askInt("Room number: ");
        int nights = askInt("Number of nights: ");
        LocalDate today = LocalDate.now();
        Booking booking = hotel.bookRoom(guest, roomNumber, today,
                today.plusDays(nights), getUser());
        System.out.println("  Booked! " + booking);
    }

    private void checkOutGuest() {
        String id = ask("Booking id: ").toUpperCase();
        Invoice invoice = hotel.checkOut(id);
        payInvoice(invoice);
    }
}
