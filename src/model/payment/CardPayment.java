package model.payment;

public class CardPayment extends Payment {
    private final String cardNumber;
    private final String cardHolder;

    public CardPayment(double amount, String cardNumber, String cardHolder){
        super(amount);
        this.cardNumber = cardNumber;
        this.cardHolder = cardHolder;
    }

    @Override 
    public boolean processPayment(){
        if(cardNumber != null && cardNumber.matches("\\d{16}")){
            markSuccessful();
            return true;
        }
        return false;
    }

    public String getMaskedCard(){
        if(cardNumber == null || cardNumber.length()<4){
            return "INVALID";
        }
        return "XXXX-" + cardNumber.substring(cardNumber.length()-4); //this gives an output like XXXX-9876 , only last 4 digits of the card is visible , basically masking ts
    }

    public String getCardHolder(){
        return cardHolder;
    }

    @Override 
    public String getMode(){
        return "Card" + getMaskedCard();
    }
}
