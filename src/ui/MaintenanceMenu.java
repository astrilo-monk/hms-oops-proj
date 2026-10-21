package ui;

import java.util.List;
import java.util.Scanner;
import model.person.Staff;
import model.room.Room;
import service.Hotel;

public class MaintenanceMenu extends StaffMenu {
    public MaintenanceMenu(Hotel hotel, Scanner in, Staff user) {
        super(hotel, in, user);
    }

    @Override
    protected void greet() {
        super.greet();
        showProblems();
    }

    @Override
    protected void showMenu() {
        System.out.println("\n1. Show rooms with problems");
        System.out.println("2. Go through problems and mark fixed");
        System.out.println("3. Log out");
    }

    @Override
    protected boolean handle(String choice) {
        switch (choice) {
            case "1": showProblems(); return true;
            case "2": reviewProblems(); return true;
            case "3": return false;
            default:
                System.out.println("  Please choose 1 to 3.");
                return true;
        }
    }

    private void showProblems() {
        List<Room> broken = hotel.getRoomsUnderMaintenance();
        if (broken.isEmpty()) {
            System.out.println("  No problems reported. All rooms are in working order.");
            return;
        }
        System.out.println("  You have " + broken.size() + " room(s) to fix:");
        for (Room room : broken) {
            System.out.println("  Room " + room.getRoomNumber() + " (" + room.getType()
                    + "): " + room.getMaintenanceIssue());
            System.out.println("      reported by " + room.getReportedBy());
        }
    }

    private void reviewProblems() {
        List<Room> broken = hotel.getRoomsUnderMaintenance();
        if (broken.isEmpty()) {
            System.out.println("  Nothing to fix.");
            return;
        }
        for (Room room : broken) {
            System.out.println("\n  Room " + room.getRoomNumber() + ": "
                    + room.getMaintenanceIssue());
            if (askYes("  Is it fixed?")) {
                hotel.markFixed(room.getRoomNumber(), getUser());
                System.out.println("  Room " + room.getRoomNumber()
                        + " is back in service (AVAILABLE).");
            } else {
                System.out.println("  Left as it is. Still under maintenance.");
            }
        }
    }
}
