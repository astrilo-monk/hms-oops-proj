package model.booking;

import model.payment.Payment;

public class Invoice {
    public static final double GST_RATE=0.12; //12% gst
    private static int counter = 9001;

    private final String invoiceId;
    private final Booking booking;
    private final double subtotal;
    private final double gst;
    private final double total;
    private Payment payment;

    public Invoice(Booking booking){
        this.invoiceId="INV"+counter++;
        this.booking = booking;
        this.subtotal = booking.calculateAmount();
        this.gst = subtotal*GST_RATE;
        this.total = subtotal+gst;
    }

    public String getInvoiceId(){
        return invoiceId;
    }

    public double getSubtotal(){
        return subtotal;
    }

    public double getGst(){
        return gst;
    }

    public double getTotal(){
        return total;
    }

    public boolean isPaid(){
        return payment != null && payment.isSuccessful();
    }

    public boolean settle(Payment p){ //workds with any Payment subclass, invoice never asks which one it is
        if(Math.abs(p.getAmount()-total)>0.01){
            throw new IllegalArgumentException("Payment amount dont match invoice total");
        }
        boolean ok = p.processPayment();
        if(ok){
            payment = p;
        }
        return ok;
    }

    public void print(){
        String line = "-".repeat(50);
        System.out.println(line);
        System.out.println("INVOICE "+invoiceId + " (Booking"+booking.getBookingId()+")");
        System.out.println(line);
        System.out.printf("Guest: %s %n",booking.getGuest().getName());
        System.out.printf("Room : %d (%s) %n",booking.getRoom().getRoomNumber(),booking.getRoom().getType());
        System.out.printf("Stay: %s to %s (%d nights)%n",booking.getCheckIn(),booking.getCheckOut(),booking.getNights());
        System.out.printf("Subtotal: Rs.%10.2f%n",subtotal);
        System.out.printf("GST 12%: Rs.%10.2f%n",gst);
        System.out.printf("Total: Rs%10.2f%n",total);
    }
}


/*
first run:errors
[Running] cd "c:\Users\mdowa\OneDrive\Desktop\choding works\hms-oops-proj\src\model\booking\" && javac Invoice.java && java Invoice
Invoice.java:3: error: package model.payment does not exist
import model.payment.Payment;
                    ^
Invoice.java:10: error: cannot find symbol
    private final Booking booking;
                  ^
  symbol:   class Booking
  location: class Invoice
Invoice.java:14: error: cannot find symbol
    private Payment payment;
            ^
  symbol:   class Payment
  location: class Invoice
Invoice.java:16: error: cannot find symbol
    public Invoice(Booking booking){
                   ^
  symbol:   class Booking
  location: class Invoice
Invoice.java:44: error: cannot find symbol
    public boolean settle(Payment p){ //workds with any Payment subclass, invoice never asks which one it is
                          ^
  symbol:   class Payment
  location: class Invoice
Invoice.java:17: error: cannot find symbol
        this.invoiced="INV"+counter++;
            ^
  symbol: variable invoiced
Invoice.java:18: error: cannot find symbol
        tihs.booking = booking;
        ^
  symbol:   variable tihs
  location: class Invoice
Invoice.java:63: error: no suitable method found for println(String,double)
        System.out.println("Subtotal: Rs.%10.2f%n",subtotal);
                  ^
    method PrintStream.println() is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(boolean) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(char) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(int) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(long) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(float) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(double) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(char[]) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(String) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(Object) is not applicable
      (actual and formal argument lists differ in length)
Invoice.java:64: error: no suitable method found for println(String,double)
        System.out.println("GST 12%: Rs.%10.2f%n",gst);
                  ^
    method PrintStream.println() is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(boolean) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(char) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(int) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(long) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(float) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(double) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(char[]) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(String) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(Object) is not applicable
      (actual and formal argument lists differ in length)
Invoice.java:65: error: no suitable method found for println(String,double)
        System.out.println("Total: Rs%10.2f%n",total);
                  ^
    method PrintStream.println() is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(boolean) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(char) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(int) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(long) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(float) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(double) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(char[]) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(String) is not applicable
      (actual and formal argument lists differ in length)
    method PrintStream.println(Object) is not applicable
      (actual and formal argument lists differ in length)
10 errors


sum spelling errors , fixed , used println instead of printf to print multiple values 

*/
