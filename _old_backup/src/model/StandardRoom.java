package model;
public class StandardRoom extends Room{
    public StandardRoom(int roomNumber,int floor, double baseRate){
        super(roomNumber,floor,baseRate);
    }

    @Override 
    public String getType(){
        return "Standard";
    }

    @Override
    public double calculateTariff(int nights){
        validateNights(nights);
        return getBaseRate()*nights;
    }

    @Override 
    public String getAmenities(){
        return "Fan , TV , WIFI";
    }
}
