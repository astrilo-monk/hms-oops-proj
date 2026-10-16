public abstract class Person {
    private final String id;
    private String name;
    private String phone;

    protected Person(String id , String name, String phone){
        if(name == null || name.trim().isEmpty()){
            throw new IllegalArgumentException("Nmae cannot be empty!");
        } 
        this.id = id;
        this.name = name.trim();
        this.phone = validatePhone(phone);
    }

    public String getId(){
        return id;
    }
    public String getName(){
        return name;
    }
    public String getPhone(){
        return phone;
    }
    public void setPhone(String phone){
        this.phone = validatePhone(phone);
    }

    private static String validatePhone(String phone){
        if(phone == null || !phone.matches("\\d{10}")){  // '\\d{10}' is a regular exp , used to check wether the string contains exactly 10 digits or not
            throw new IllegalArgumentException("Phone must be exactly 10 digits!");
        }
        return phone;
    }

    public abstract String getRole();

    @Override 
    public String toString(){
        return getRole() + " ["+ id + "}" + name; 
        
        /*custom text representation of a Person object ,
        ex: id = "101", name="farhan", desig = "owner" 
        output: Manager [101] farhan */
    }
}

/* --abstract class - cannont create new Person() , only subclasses lik guest or staff can be expressed
-protected constructor- can be called by subclasses , final id cannot change because there is no setter , 
-pvt tatic validatePhone() - internal helper used by the constructor and setPhone(0 , regex checks for exactly 10 digits (\\d{10})
-abstract getrole() - every concrete subclass must implement it
-pilymorphism toString90 calls getRole() , which executes the correct subclass's version

*/