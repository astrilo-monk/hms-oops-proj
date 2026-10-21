package model.booking;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import model.person.Guest;
import model.person.Staff;
import model.room.Room; //chrnounit is an enum that provides standard date and time units to calculate the exact time between two dates , the units are like days or hours

public class Booking {
    private static int counter = 1001;

    private final String bookingId;
    private final Guest guest;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final Staff handledBy;
    private BookingStatus status;

    public Booking(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut, Staff handledBy){
        if(guest == null || room == null || handledBy == null){
            throw new IllegalArgumentException("Guest, room and staff required");
        }
        if(!checkOut.isAfter(checkIn)){
            throw new IllegalArgumentException("Checkout must be after checkin");
        }
        this.bookingId = "BK" + counter++;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.handledBy=handledBy;
        this.status= BookingStatus.CONFIRMED;
    }

    public Booking(String bookingId, Guest guest, Room room, LocalDate checkIn,
                   LocalDate checkOut, Staff handledBy, BookingStatus status){
        if (bookingId == null || bookingId.trim().isEmpty() || guest == null
                || room == null || handledBy == null || status == null) {
            throw new IllegalArgumentException("Saved booking data is incomplete");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Checkout must be after checkin");
        }
        this.bookingId = bookingId;
        this.guest = guest;
        this.room = room;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.handledBy = handledBy;
        this.status = status;
        updateCounter(bookingId);
    }

    private static void updateCounter(String id) {
        if (id.startsWith("BK")) {
            try {
                counter = Math.max(counter, Integer.parseInt(id.substring(2)) + 1);
            } catch (NumberFormatException ignored) {
                // Preserve the current counter for malformed legacy ids.
            }
        }
    }

    public String getBookingId(){
        return bookingId;
    }

    public Guest getGuest(){
        return guest;
    }

    public Room getRoom(){
        return room;
    }

    public LocalDate getCheckIn(){
        return checkIn;
    }

    public LocalDate getCheckOut(){
        return checkOut;
    }

    public Staff getHandledBy(){
        return handledBy;
    }

    public BookingStatus getStatus(){
        return status;
    }

    public int getNights(){
        return (int) ChronoUnit.DAYS.between(checkIn,checkOut);
    }

    public double calculateAmount(){
        return room.calculateTariff(getNights());
    }

    public void checkOut(){
        if(status != BookingStatus.CONFIRMED){
            throw new IllegalStateException("Booking is already:"+status);
        }
        status = BookingStatus.COMPLETED;
        room.vacate();
    }

    public void cancel(){
        if(status !=BookingStatus.CONFIRMED){
            throw  new IllegalStateException("Booking is already"+status);
        }
        status = BookingStatus.CANCELLED;
        room.vacate();
    }

    @Override 
    public String toString(){
        return String.format("%s | %s | Room %d | %s to %s | %d night(s) | %s", bookingId, guest.getName(), room.getRoomNumber(),checkIn,checkOut,getNights(),status);
    }
}
