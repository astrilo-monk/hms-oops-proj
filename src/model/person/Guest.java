package model.person;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.booking.Booking;
import model.booking.BookingStatus;

public class Guest extends Person{
    private String email;
    private final String idProof;
    private final List<Booking> bookings= new ArrayList<>();

    public Guest(String id, String name, String phone, String email, String idProof){
        super(id,name,phone);
        this.email=email;
        this.idProof=idProof;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getIdProof(){
        return idProof;
    }

    public void addBooking(Booking booking){
        bookings.add(booking);
    }

    public List<Booking> getBookings() {
        return Collections.unmodifiableList(bookings);
    } 
    
   public int getTotalNightsStayed(){
        int total=0;
        for(Booking b: bookings){
            if(b.getStatus() == BookingStatus.COMPLETED){
                total += b.getNights();
            }
        }
        return total;
    }

    @Override 
    public String getRole(){
        return "Guest";
    }

}
