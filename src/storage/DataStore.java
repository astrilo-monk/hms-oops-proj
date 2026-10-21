package storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.booking.Booking;
import model.booking.BookingStatus;
import model.person.Guest;
import model.person.Staff;
import model.room.DeluxeRoom;
import model.room.Room;
import model.room.RoomStatus;
import model.room.StandardRoom;
import model.room.SuiteRoom;
import service.Hotel;

public class DataStore {
    private final Path folder;

    public DataStore(String folderName) {
        this.folder = Paths.get(folderName);
    }

    public boolean exists() {
        return Files.exists(folder.resolve("rooms.csv"));
    }

    public void save(Hotel hotel) {
        try {
            Files.createDirectories(folder);
            writeFile("rooms.csv", roomLines(hotel));
            writeFile("guests.csv", guestLines(hotel));
            writeFile("bookings.csv", bookingLines(hotel));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<String> roomLines(Hotel hotel) {
        List<String> lines = new ArrayList<>();
        lines.add("type,roomNumber,floor,baseRate,status,issue,reportedBy");
        for (Room room : hotel.getRooms()) {
            lines.add(row(room.getType(), String.valueOf(room.getRoomNumber()),
                    String.valueOf(room.getFloor()), String.valueOf(room.getBaseRate()),
                    room.getStatus().name(), room.getMaintenanceIssue(),
                    room.getReportedBy()));
        }
        return lines;
    }

    private List<String> guestLines(Hotel hotel) {
        List<String> lines = new ArrayList<>();
        lines.add("id,name,phone,email,idProof");
        for (Guest guest : hotel.getGuests()) {
            lines.add(row(guest.getId(), guest.getName(), guest.getPhone(),
                    guest.getEmail(), guest.getIdProof()));
        }
        return lines;
    }

    private List<String> bookingLines(Hotel hotel) {
        List<String> lines = new ArrayList<>();
        lines.add("bookingId,guestId,roomNumber,checkIn,checkOut,staffId,status");
        for (Booking booking : hotel.getBookings()) {
            lines.add(row(booking.getBookingId(), booking.getGuest().getId(),
                    String.valueOf(booking.getRoom().getRoomNumber()),
                    booking.getCheckIn().toString(), booking.getCheckOut().toString(),
                    booking.getHandledBy().getId(), booking.getStatus().name()));
        }
        return lines;
    }

    private void writeFile(String name, List<String> lines) throws IOException {
        Path temp = folder.resolve(name + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);
        Files.move(temp, folder.resolve(name), StandardCopyOption.REPLACE_EXISTING);
    }

    public Hotel load(String hotelName, List<Staff> staff) {
        try {
            Hotel hotel = new Hotel(hotelName);
            for (List<String> fields : readRows("rooms.csv")) {
                hotel.addRoom(buildRoom(fields));
            }
            for (List<String> fields : readRows("guests.csv")) {
                hotel.registerGuest(new Guest(fields.get(0), fields.get(1), fields.get(2),
                        fields.get(3), fields.get(4)));
            }
            for (List<String> fields : readRows("bookings.csv")) {
                hotel.restoreBooking(new Booking(fields.get(0), hotel.findGuest(fields.get(1)),
                        hotel.findRoom(Integer.parseInt(fields.get(2))),
                        LocalDate.parse(fields.get(3)), LocalDate.parse(fields.get(4)),
                        findStaff(staff, fields.get(5)),
                        BookingStatus.valueOf(fields.get(6))));
            }
            return hotel;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Room buildRoom(List<String> fields) {
        int number = Integer.parseInt(fields.get(1));
        int floor = Integer.parseInt(fields.get(2));
        double rate = Double.parseDouble(fields.get(3));
        Room room;
        switch (fields.get(0)) {
            case "Standard": room = new StandardRoom(number, floor, rate); break;
            case "Deluxe": room = new DeluxeRoom(number, floor, rate); break;
            case "Suite": room = new SuiteRoom(number, floor, rate); break;
            default: throw new IllegalArgumentException("Unknown room type: " + fields.get(0));
        }
        RoomStatus status = RoomStatus.valueOf(fields.get(4));
        if (status == RoomStatus.OCCUPIED) {
            room.occupy();
        } else if (status == RoomStatus.UNDER_MAINTENANCE) {
            room.sendToMaintenance(fields.get(5), fields.get(6));
        }
        return room;
    }

    private Staff findStaff(List<Staff> staff, String id) {
        for (Staff member : staff) {
            if (member.getId().equals(id)) {
                return member;
            }
        }
        throw new IllegalArgumentException("Unknown staff id in saved data: " + id);
    }

    private List<List<String>> readRows(String name) throws IOException {
        List<String> lines = Files.readAllLines(folder.resolve(name), StandardCharsets.UTF_8);
        List<List<String>> rows = new ArrayList<>();
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).trim().isEmpty()) {
                rows.add(splitCsv(lines.get(i)));
            }
        }
        return rows;
    }

    private static String row(String... fields) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) result.append(',');
            result.append(quote(fields[i]));
        }
        return result.toString();
    }

    private static String quote(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private static List<String> splitCsv(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"' && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else if (c == '"') {
                    inQuotes = false;
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }
}
