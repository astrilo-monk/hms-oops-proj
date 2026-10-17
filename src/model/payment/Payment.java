package model.payment;

public abstract class Payment {
    private static int counter = 5001;

    private final String paymentId;
    private final double amount;
    private boolean successful;

    protected Payment(double amount){
        if(amount<=0){
            throw new IllegalArgumentException("Amount must be positive");
        }
        this.paymentId = "PAY" + counter++;
        this.amount = amount;
        this.successful=false;
    }

    public String getPaymentId(){
        return paymentId;
    }

    public double getAmount(){
        return amount;
    }

    public boolean isSuccessful(){
        return successful;
    }

    protected void markSuccessful(){
        successful = true;
    }

    public abstract boolean processPayment();

    public abstract String getMode();

    public String getReceipt(){
        return String.format("%s | %s | Rs.%.2f | %s ", paymentId, getMode(),amount,successful? "SUCCESS" : "FAULED"); //again using "" + amount + "" made is messier so used it also eliminated an if-else block by using ternary operator
    }
}

/*
-private static int counter belongs to class , not to any object , so every new payment takes the next number so ids are unique like pay5001,pay5002 and so on
-a payment always begins unsuccessful only a subclass can flit it through the protected method, markSuccessful() and only after its own validation passes
-getReceipt() is a concrete methods that calls the abstract getMode() the parent writes the receipt format once , each child supplies its own mode text

*/
