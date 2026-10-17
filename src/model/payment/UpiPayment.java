package model.payment;

public class UpiPayment extends Payment{
    private final String upiId;

    public UpiPayment(double amount, String upiId){
        super(amount);
        this.upiId = upiId;
    }

    @Override 
    public boolean processPayment(){
        if(upiId != null && upiId.matches("[A-Za-z0-9._-]+@[A-Za=z]+")){
            markSuccessful();
            return true;
        }
        return false;
    }

    public String getUpiId(){
        return upiId;
    }

    @Override 
    public String getMode(){
        return "UPI" + upiId; 
    }
}

/*
-each class implements processPayment() with its own rule, cash needs enough money tendered , a card needs exactly 16 digits , and a upi id must look like name@bank
-cardPayment.getMaskedCard() shows only the last 4 digits , instead of printing all digits , like in an actual invoice
-the upi regex , [A-Za-z0-9._-]+@[A-Za=z]+ reads one or more letters ,digits,dots,underscore, - then an @ then one or more letters , ex- astrilo@ybl (my actual upi id dont be shy to send some love)


error:
The assignment to variable upiId has no effectJava(536871090)
Assignment To Itself(hints(1): 8:9-8:27)
model.​UpiPayment
private final String upiId
String upiId
Source: hms-oops-proj_83482431

--this happened because in method i wrote String UpiId , a U instead of u , fixed and works fine
COMPILED AND RAN PERFECTly
*/
