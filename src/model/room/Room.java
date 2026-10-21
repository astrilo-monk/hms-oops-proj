package model.room;

public abstract class Room {
    private final int roomNumber;
    private final int floor;
    private final double baseRate;
    private RoomStatus status;

    private String maintenanceIssue;
    private String reportedBy;

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
        sendToMaintenance("Unspecified problem","system");
    }

    public void sendToMaintenance(String issue,String reportedBy){
        if(status == RoomStatus.OCCUPIED){
            throw new IllegalStateException("Cannot service an occupied room");
        }
        if(status == RoomStatus.UNDER_MAINTENANCE){
            throw new IllegalStateException("Room"+roomNumber+" is already under maintenance"+maintenanceIssue);
        }

        if(issue == null || issue.trim().isEmpty()){
            throw new IllegalArgumentException("Please desribe the problem");
        }

        status = RoomStatus.UNDER_MAINTENANCE;
        this.maintenanceIssue = issue.trim();
        this.reportedBy=reportedBy;
    }

    public void completeMaintenance(){
        if(status == RoomStatus.UNDER_MAINTENANCE){
            status = RoomStatus.AVAILABLE;
            maintenanceIssue = null;
            reportedBy = null;
        }
    }

    public String getMaintenIssue(){
        return maintenanceIssue;
    }

    public String getMaintenanceIssue(){
        return maintenanceIssue;
    }

    public String getReportedBy(){
        return reportedBy;
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
        return String.format("Room %d (%s, floor %d) Rs.%.2f/night [%s]",
                roomNumber, getType(), floor, baseRate, status);
    }
}


/*
-private fields protect room data because status changes only through methods than reject illegal moves;
-validateNights() is protected so subclasses can reuse shared validation
-three abstract methods let subclasses define room type,tariff and amenities
-toString() uses String.format() for readable output , gettype90 resolves to the subclasse's implementation
*/

/*checkpoint2= 
sendToMaintenance() now has two forms the old no arguement form still works and delegates to the new one that is overloading and this means nothing that called the old methods breaks
-the new form checks 3 illegal cases before changing anything , the room is occupied, the room is alread under maintenance or the description is blank
-completeMaintenance() clears the issue reported so a repaired room carries no stale text
*/