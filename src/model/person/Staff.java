package model.person;

public class Staff extends Person {
    private final String designation;
    private double salary;
    private String shift;

    public Staff(String id, String name, String phone, String designation, double salary , String shift){
        super(id,name,phone);
        if(salary <=0){
            throw new IllegalArgumentException("Salary must be positive1");
        }
        this.designation = designation;
        this.salary = salary;
        this.shift = shift;
    }

    public String getDesignation(){
        return designation;
    }

    public double getSalary(){
        return salary;
    }

    public String getShift(){
        return shift;
    }

    public void setShift(String shift){
        this.shift= shift;
    }

    public void raiseSalary(double percent){
        if(percent <= 0){
            throw new  IllegalArgumentException("Raise mut be positive.");
        }
        salary += salary * percent/100.0;
    }

    @Override 
    public String getRole(){
        return designation;
    }
}


/* 1st compilation error::Staff.java:39: error: getRole() in Staff does not override or implement a method from a supertype
    @Override 
    ^
1 error

this happened because i forgot to create a getRole() method in Person.java class 

fixed and no error during second compilation

-super(id,name,phone) must be the first contructor statement , it initializes the inherited person field before staff initializes its own 
-getRole() - returns the staff members designation , eg 'Receptionist'
-raiseSalary(10) - increases salary by 10% using salary += salary*percent/100
*/