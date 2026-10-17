package model.room;

public abstract class Room {
    private final int roomNumber;
    private final int floor;
    private final double baseRate;
    private RoomStatus status;

    protected Room(int roomNumber, int floor , double baseRate){
        if(baseRate<=0){
            throw new IllegalArgumentException("Base rate must be positive!");
        }
        this.roomNumber = roomNumber;
        this.floor = floor;
        this.baseRate = baseRate;
        this.status = RoomStatus.AVAILABLE;
    }

    public int getRoomNumber(){
        return roomNumber;
    }

    public int getFloor(){
        return floor;
    }

    public double getBaseRate(){
        return baseRate;
    }

    public RoomStatus getStatus(){
        return status;
    }

    public boolean isAvailable(){
        return status == RoomStatus.AVAILABLE;
    }

    public void occupy(){
        if(!isAvailable()){
            throw new IllegalStateException("Room" + roomNumber + "US NOT available");
        }
        status= RoomStatus.OCCUPIED;
    }

    public void vacate(){
        if(status != RoomStatus.OCCUPIED){
            throw new IllegalStateException("Room" + roomNumber + "IS Available.");
        }
        status = RoomStatus.AVAILABLE;
    }

    public void sendToMaintenance(){
        if(status == RoomStatus.OCCUPIED){
            throw new IllegalStateException("Room "+roomNumber+"is not occupied");
        }
        status = RoomStatus.UNDER_MAINTENANCE;
    }

    public void completeMaintenance(){
        if(status == RoomStatus.UNDER_MAINTENANCE)
            status = RoomStatus.AVAILABLE;
    }

    protected void validateNights(int nights){
        if(nights <=0)
            throw new IllegalArgumentException("Nights must be atleast 1");
    }
    
    public abstract String getType();
    
    public abstract double calculateTariff(int nights);

    public abstract String getAmenities();

    @Override 
    public String toString(){
        return String.format("Room %d (%s, floor %d)", roomNumber,getType(),floor,baseRate,status); //wrote like this jus to be less messy
    }
}


/*
-private fields protect room data because status changes only through methods than reject illegal moves;
-validateNights() is protected so subclasses can reuse shared validation
-three abstract methods let subclasses define room type,tariff and amenities
-toString() uses String.format() for readable output , gettype90 resolves to the subclasse's implementation
*/