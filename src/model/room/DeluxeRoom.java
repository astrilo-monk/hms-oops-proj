package model.room;

public class DeluxeRoom extends Room{
    private static final double BREAKFAST_PER_NIGHT = 300;

    public DeluxeRoom(int roomNumber, int floor, double baseRate){
        super(roomNumber,floor,baseRate);
    }

    @Override 
    public String getType(){
        return "Deluxe";
    }

    @Override 
    public double calculateTariff(int nights){
        validateNights(nights);
        return(getBaseRate()+BREAKFAST_PER_NIGHT) * nights;
    }

    @Override 
    public String getAmenities(){
        return "AC, Smart TV, WIFI , Breakfast";
    }
}
