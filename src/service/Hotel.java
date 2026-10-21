package service;

import exception.RoomNotAvailableException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.booking.Booking;
import model.booking.Invoice;
import model.person.Guest;
import model.person.Staff;
import model.room.Room;
import model.room.RoomStatus;

public class Hotel {
    private final String name;
    private final List<Room> rooms= new ArrayList<>();
    private final List<Guest> guests = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();

    public Hotel(String name){
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public void addRoom(Room room){
        for(Room r: rooms){
            if(r.getRoomNumber() == room.getRoomNumber())
                throw new IllegalArgumentException("Duplicate room number: " + room.getRoomNumber());
        }
        rooms.add(room);
    }

    public void registerGuest(Guest guest){
        guests.add(guest);
    }

    public Room findRoom(int roomNumber){
        for(Room r: rooms){
            if(r.getRoomNumber()==roomNumber)
                return r;
        }
        throw new IllegalArgumentException("No such room: "+roomNumber);
    }

    public Booking findBooking(String bookingId){
        for(Booking b: bookings){
            if(b.getBookingId().equals(bookingId))
                return b;
        }
        throw new IllegalArgumentException("No such booking: "+bookingId);
    }
    
    public List<Room> getAvailableRooms(){
        List<Room> result = new ArrayList<>();
        for(Room r: rooms){
            if(r.isAvailable()){
                result.add(r);
            }
        }
        return result;
    }

    public List<Room> getAvailableRooms(String type){
        List<Room> result = new ArrayList<>();
        for(Room r: getAvailableRooms()){
            if(r.getType().equalsIgnoreCase(type)){
                result.add(r);
            }
        }
        return result;
    }

    public Booking bookRoom(Guest guest, int roomNumber, LocalDate checkIn, LocalDate checkOut, Staff staff) throws RoomNotAvailableException{
        if(!guests.contains(guest)){
            throw new IllegalArgumentException("Guest is not registered");
        }
        Room room = findRoom(roomNumber);
        if(!room.isAvailable()){
            throw new RoomNotAvailableException("Room "+roomNumber+" is currently " + room.getStatus());
        }
        Booking booking = new Booking(guest,room,checkIn,checkOut,staff);
        room.occupy();
        guest.addBooking(booking);
        bookings.add(booking);
        return booking;
    }

    public Invoice checkOut(String bookingId){
        Booking booking = findBooking(bookingId);
        booking.checkOut();
        return new Invoice(booking);
    }

    public double getOccupancyPercent(){
        int occupied=0;
        for(Room r: rooms){
            if(r.getStatus() == RoomStatus.OCCUPIED){
                occupied++;
            }
        }
        return rooms.isEmpty() ? 0 : occupied * 100.0 / rooms.size();
    }

    public void printRoomStatus(){
        for(Room r: rooms){
            System.out.println(" " + r);
        }
    }

    public List<Room> getRooms(){
        return Collections.unmodifiableList(rooms);
    }

    public List<Booking> getBookings(){
        return Collections.unmodifiableList(bookings);
    }

    public Guest registerNewGuest(String name , String phone, String email, String idProof){
        String id= String.format("G%03d" , guests.size()+1);
        Guest guest = new Guest(id, name, phone , email, idProof);
        guests.add(guest);
        return guest;
    }

    public Guest findGuest(String guestId){
        for(Guest g: guests){
            if(g.getId().equalsIgnoreCase(guestId)){
                return g;
            }
        }
        throw new IllegalArgumentException("No such guest:" +guestId);
    }

    public void reportMaintenance(int roomNumber, String issue, Staff reporter){
        String role = reporter.getDesignation();
        if(!role.equals("Receptionist") && !role.equals("Manager")){
            throw new IllegalArgumentException("Only a receptionist or manager can report a problem");
        }
        findRoom(roomNumber).sendToMaintenance(issue, reporter.getName() + "(" + role+ ")");
    }

    public List<Room> getRoomsUnderMaintenance(){
        List<Room> result = new ArrayList<>();
        for (Room r: rooms){
            if (r.getStatus() == RoomStatus.UNDER_MAINTENANCE){
                result.add(r);
            }
        }
        return result;
    }

    public void markFixed(int roomNumber, Staff worked){
        if(!worked.getDesignation().equals("Maintenance")){
            throw new IllegalArgumentException("Only maintenance staff can mark a room as fixed");
        }
        Room room = findRoom(roomNumber);
        if(room.getStatus() != RoomStatus.UNDER_MAINTENANCE){
            throw new IllegalStateException("Room "+ roomNumber +"is not under maintenance");
        }
        room.completeMaintenance();
    }
}
