package ui;

import java.util.Scanner;
import model.booking.Booking;
import model.person.Staff;
import model.room.Room;
import model.room.RoomStatus;
import service.Hotel;

public abstract class StaffMenu extends RoleMenu {
    private final Staff user;

    protected StaffMenu(Hotel hotel, Scanner in, Staff user) {
        super(hotel, in);
        this.user = user;
    }

    protected Staff getUser() {
        return user;
    }

    @Override
    public String getRoleName() {
        return user.getDesignation() + ": " + user.getName();
    }

    protected void showBoard() {
        System.out.println("\nRoom status board:");
        for (Room room : hotel.getRooms()) {
            System.out.println("  " + room);
            if (room.getStatus() == RoomStatus.UNDER_MAINTENANCE) {
                System.out.println("      problem: " + room.getMaintenanceIssue());
            }
        }
    }

    protected void reportMaintenance() {
        int roomNumber = askInt("Room number: ");
        String issue = ask("Describe the problem: ");
        hotel.reportMaintenance(roomNumber, issue, user);
        System.out.println("  Room " + roomNumber + " is now UNDER_MAINTENANCE.");
        System.out.println("  Maintenance staff will see it when they log in.");
    }

    protected void listBookings() {
        System.out.println("\nAll bookings:");
        if (hotel.getBookings().isEmpty()) {
            System.out.println("  (none)");
        }
        for (Booking booking : hotel.getBookings()) {
            System.out.println("  " + booking);
        }
    }
}
