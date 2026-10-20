package ui;

import exception.RoomNotAvailableException;
import java.time.LocalDate;
import java.util.Scanner;
import model.booking.Booking;
import model.booking.Invoice;
import model.person.Guest;
import model.person.Staff;
import model.room.Room;
import service.Hotel;

public class CustomerMenu extends RoleMenu {
    private final Guest guest;
    private final Staff desk;

    public CustomerMenu(Hotel hotel, Scanner in, Guest guest, Staff desk) {
        super(hotel, in);
        this.guest = guest;
        this.desk = desk;
    }

    @Override
    public String getRoleName() {
        return "Customer: " + guest.getName() + " (" + guest.getId() + ")";
    }

    @Override
    protected void showMenu() {
        System.out.println("\n1. See available rooms and prices");
        System.out.println("2. Book a room");
        System.out.println("3. My bookings");
        System.out.println("4. Check out and pay");
        System.out.println("5. Log out");
    }

    @Override
    protected boolean handle(String choice)
            throws RoomNotAvailableException {
        switch (choice) {
            case "1":
                viewRooms();
                return true;
            case "2":
                bookRoom();
                return true;
            case "3":
                myBookings();
                return true;
            case "4":
                checkOut();
                return true;
            case "5":
                return false;
            default:
                System.out.println(" please choose 1 to 5");
                return true;
        }
    }

    private void viewRooms() {
        int nights = askInt("price for how many nights? ");
        System.out.println("available rooms");

        for (Room r : hotel.getAvailableRooms()) {
            System.out.printf(
                    " room %d %-8s rs.%.2f for %d night(s) + gst [%s]%n",
                    r.getRoomNumber(),
                    r.getType(),
                    r.calculateTariff(nights),
                    nights,
                    r.getAmenities()
            );
        }
    }

    private void bookRoom() throws RoomNotAvailableException {
        int roomNumber = askInt("room number to book: ");
        int nights = askInt("number of nights: ");

        LocalDate today = LocalDate.now();

        Booking b = hotel.bookRoom(
                guest,
                roomNumber,
                today,
                today.plusDays(nights),
                desk
        );

        System.out.println(" booked! " + b);
    }

    private void myBookings() {
        System.out.println("your bookings");

        if (guest.getBookings().isEmpty()) {
            System.out.println(" (none)");
        }

        for (Booking b : guest.getBookings()) {
            System.out.println(" " + b);
        }
    }

    private void checkOut() {
        String id = ask(
                "booking id to check out, for example BK1001: "
        ).toUpperCase();

        Booking booking = hotel.findBooking(id);

        if (booking.getGuest() != guest) {
            throw new IllegalArgumentException(
                    "that booking is not yours"
            );
        }

        Invoice invoice = hotel.checkOut(id);
        payInvoice(invoice);
    }
}

/*
im tired of typing , so im gonna prompt chatgpt and for this code and debug it and start working tmrw with ts*/
