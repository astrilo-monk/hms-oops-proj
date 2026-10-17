package model;

public class CashPayment extends Payment {
    private final double amountTendered;

    public CashPayment(double amount, double amountTendered){
        super(amount);
        this.amountTendered=amountTendered;
    }

    @Override 
    public boolean processPayment(){
        if(amountTendered>=getAmount()){
            markSuccessful();
            return true;
        }
        return false;
    }

    public double getChange(){
        return Math.max(0,amountTendered-getAmount());
    }

    @Override 
    public String getMode(){
        return "Cash";
    }
}

/*
-errors: line 13 
The method getAmount() is undefined for the type CashPaymentJava(67108964)
cannot find symbol
  symbol:   method getAmount()
  location: class CashPayment(errors(1): 13:28-13:37)
line 14: 
The method markSuccessful() is undefined for the type CashPaymentJava(67108964)
cannot find symbol
  symbol:   method markSuccessful()
  location: class CashPayment(errors(2): 14:13-14:27)
line 21:
The method getAmount() is undefined for the type CashPaymentJava(67108964)
cannot find symbol
  symbol:   method getAmount()
  location: class CashPayment(errors(3): 21:42-21:51)


possible cause , cashPayment class is trying to call methods that java cant find on the parent class, possibly forgot to write the method or spelling error , lemme check

minor spelling error 

    _.-^^---....,,--
_--'              --_
<       BOOM!       >
\._             _./
   ```--. .--'

     |  |
     |  |

markSuccessfull() to markSuccessful() , fixed error  at line 14, but getAmount error stilL EXISTS, checking the parent class's code again 

found the problem , didn't build the method to get the amount , fixed
*/