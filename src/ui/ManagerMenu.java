package ui;

import java.util.Scanner;
import model.person.Staff;
import model.room.DeluxeRoom;
import model.room.Room;
import model.room.RoomStatus;
import model.room.StandardRoom;
import model.room.SuiteRoom;
import service.Hotel;

public class ManagerMenu extends StaffMenu {
    public ManagerMenu(Hotel hotel, Scanner in, Staff user) {
        super(hotel, in, user);
    }

    @Override
    protected void showMenu() {
        System.out.println("\n1. Room status board");
        System.out.println("2. Occupancy summary");
        System.out.println("3. View all bookings");
        System.out.println("4. Send a room to maintenance");
        System.out.println("5. Rooms currently under maintenance");
        System.out.println("6. Add a new room");
        System.out.println("7. Log out");
    }

    @Override
    protected boolean handle(String choice) {
        switch (choice) {
            case "1": showBoard(); return true;
            case "2": occupancySummary(); return true;
            case "3": listBookings(); return true;
            case "4": reportMaintenance(); return true;
            case "5": printRooms(hotel.getRoomsUnderMaintenance()); return true;
            case "6": addRoom(); return true;
            case "7": return false;
            default:
                System.out.println("  Please choose 1 to 7.");
                return true;
        }
    }

    private void occupancySummary() {
        int free = 0, busy = 0, broken = 0;
        for (Room room : hotel.getRooms()) {
            if (room.getStatus() == RoomStatus.AVAILABLE) free++;
            else if (room.getStatus() == RoomStatus.OCCUPIED) busy++;
            else broken++;
        }
        System.out.printf("  Available: %d   Occupied: %d   Under maintenance: %d%n",
                free, busy, broken);
        System.out.printf("  Occupancy: %.1f%%%n", hotel.getOccupancyPercent());
    }

    private void addRoom() {
        String type = ask("Type (standard / deluxe / suite): ").toLowerCase();
        int number = askInt("Room number: ");
        int floor = askInt("Floor: ");
        double rate = askDouble("Base rate per night: Rs.");
        Room room;
        if (type.equals("standard")) room = new StandardRoom(number, floor, rate);
        else if (type.equals("deluxe")) room = new DeluxeRoom(number, floor, rate);
        else if (type.equals("suite")) room = new SuiteRoom(number, floor, rate);
        else throw new IllegalArgumentException("Unknown room type: " + type);
        hotel.addRoom(room);
        System.out.println("  Added: " + room);
    }
}
