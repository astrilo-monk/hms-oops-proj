package model.room;

public class SuiteRoom extends Room{
    private static final double CONCIERGE_FEE = 1000;
    private static final int LONG_STAY_NIGHTS = 5;
    private static final double LONG_STAY_DISCOUNT = 0.10;

    public SuiteRoom(int roomNumber, int floor, double baseRate){
        super(roomNumber,floor,baseRate);
    }

    @Override
    public String getType(){
        return "Suite";
    }

    @Override 
    public double calculateTariff(int nights){
        validateNights(nights);
        double cost = getBaseRate()*nights;
        if(nights >= LONG_STAY_NIGHTS){
            cost -= cost*LONG_STAY_DISCOUNT;
        }
        return cost + CONCIERGE_FEE;
    }

    @Override 
    public String getAmenities(){
        return "AC, Living area , Bathtub, butler, Breakfast";

    }
}



/*
@override cathes mistakes in overriden method names
- super(..) calls Room's constructor to initialize the shared fields 
-rpivate static final defines a fixed , shared constant such as breakfast cost
*/



/*
first compilation of Room.java StandardRoom.java DeluxeRoom.java SuiteRoom.java

error:    private RoomStatus status;
            ^
  symbol:   class RoomStatus
  location: class Room
Room.java:30: error: cannot find symbol
    public RoomStatus getStatus(){
           ^
  symbol:   class RoomStatus
  location: class Room
Room.java:15: error: cannot find symbol
        this.status = RoomStatus.AVAILABLE;
                      ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:35: error: cannot find symbol
        return status == RoomStatus.AVAILABLE;
                         ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:42: error: cannot find symbol
        status= RoomStatus.OCCUPIED;
                ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:46: error: cannot find symbol
        if(status != RoomStatus.OCCUPIED){
                     ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:49: error: cannot find symbol
        status = RoomStatus.AVAILABLE;
                 ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:53: error: cannot find symbol
        if(status == RoomStatus.OCCUPIED){
                     ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:56: error: cannot find symbol
        status = RoomStatus.UNDER_MAINTENANCE;
                 ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:60: error: cannot find symbol
        if(status == RoomStatus.UNDER_MAINTENANCE)
                     ^
  symbol:   variable RoomStatus
  location: class Room
Room.java:61: error: cannot find symbol
            status = RoomStatus.AVAILABLE;
                     ^
  symbol:   variable RoomStatus
  location: class Room
11 errors

error was the compilation command: PS C:\Users\mdowa\OneDrive\Desktop\choding works\hms-oops-proj> cd src\model
PS C:\Users\mdowa\OneDrive\Desktop\choding works\hms-oops-proj\src\model> javac -d out Room.java StandardRoom.java DeluxeRoom.java SuiteRoom.java


i wasnt compiling or ever compiled RoomStatus.java , hence the problem came, compiled with RoomStatus.java it worked perfectly 

*/
