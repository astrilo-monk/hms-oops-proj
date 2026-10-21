package app;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;
import model.person.Guest;
import model.person.Staff;
import model.room.DeluxeRoom;
import model.room.StandardRoom;
import model.room.SuiteRoom;
import service.Hotel;
import storage.DataStore;
import ui.CustomerMenu;
import ui.MaintenanceMenu;
import ui.ManagerMenu;
import ui.ReceptionistMenu;
import ui.RoleMenu;

public class HotelConsole {
    private static final String HOTEL_NAME = "Grand Orchid Hotel";

    public static void main(String[] args) {
        Staff receptionist = new Staff("S001", "Rohit Ghosh", "9830099999",
                "Receptionist", 28000, "Morning");
        Staff manager = new Staff("S002", "Anita Basu", "9830088888",
                "Manager", 65000, "Day");
        Staff maintenance = new Staff("S003", "Biswajit Mondal", "9830077777",
                "Maintenance", 22000, "Day");
        Staff onlineDesk = new Staff("S000", "Online Desk", "9830000000",
                "Online Desk", 1, "Always");
        List<Staff> staff = Arrays.asList(receptionist, manager, maintenance, onlineDesk);
        DataStore store = new DataStore("data");
        final Hotel hotel;
        try {
            if (store.exists()) {
                hotel = store.load(HOTEL_NAME, staff);
                System.out.println("Loaded saved data from the 'data' folder.");
            } else {
                hotel = sampleHotel();
                store.save(hotel);
                System.out.println("First run: sample data saved to the 'data' folder.");
            }
        } catch (RuntimeException e) {
            System.out.println("Could not read the saved data: " + e.getMessage());
            System.out.println("Delete the 'data' folder to start fresh.");
            return;
        }

        Scanner in = new Scanner(System.in);
        System.out.println("=== " + hotel.getName() + " : role-based demo ===");
        try {
            boolean open = true;
            while (open) {
                System.out.println("\nWho are you?");
                System.out.println("customer / receptionist / manager / maintenance / exit");
                System.out.print("> ");
                String who = in.nextLine().trim().toLowerCase();
                RoleMenu menu = null;
                switch (who) {
                    case "customer": menu = loginCustomer(hotel, in, onlineDesk); break;
                    case "receptionist": menu = new ReceptionistMenu(hotel, in, receptionist); break;
                    case "manager": menu = new ManagerMenu(hotel, in, manager); break;
                    case "maintenance": menu = new MaintenanceMenu(hotel, in, maintenance); break;
                    case "exit": open = false; break;
                    default: System.out.println("  I did not understand '" + who + "'.");
                }
                if (menu != null) {
                    menu.setAutosave(() -> store.save(hotel));
                    menu.run();
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println();
        }
        System.out.println("Goodbye.");
    }

    private static Hotel sampleHotel() {
        Hotel hotel = new Hotel(HOTEL_NAME);
        hotel.addRoom(new StandardRoom(101, 1, 2000));
        hotel.addRoom(new StandardRoom(102, 1, 2000));
        hotel.addRoom(new DeluxeRoom(201, 2, 3500));
        hotel.addRoom(new DeluxeRoom(202, 2, 3500));
        hotel.addRoom(new SuiteRoom(301, 3, 8000));
        hotel.registerNewGuest("Aarav Sen", "9830012345",
                "aarav@example.com", "AADHAAR-1111");
        hotel.registerNewGuest("Meera Das", "9830054321",
                "meera@example.com", "PASSPORT-2222");
        return hotel;
    }

    private static RoleMenu loginCustomer(Hotel hotel, Scanner in, Staff desk) {
        System.out.print("Your guest ID (for example G001), or NEW to register: ");
        String id = in.nextLine().trim();
        try {
            Guest guest;
            if (id.equalsIgnoreCase("new")) {
                guest = hotel.registerNewGuest(
                        prompt(in, "Name: "), prompt(in, "Phone (10 digits): "),
                        prompt(in, "Email: "), prompt(in, "ID proof: "));
                System.out.println("  Welcome! Your guest ID is " + guest.getId());
            } else {
                guest = hotel.findGuest(id);
            }
            return new CustomerMenu(hotel, in, guest, desk);
        } catch (IllegalArgumentException e) {
            System.out.println("  ! " + e.getMessage());
            return null;
        }
    }

    private static String prompt(Scanner in, String label) {
        System.out.print(label);
        return in.nextLine().trim();
    }
}
