
package ui;

import exception.RoomNotAvailableException;
import java.util.List;
import java.util.Scanner;
import model.booking.Invoice;
import model.payment.*;
import model.room.Room;
import service.Hotel;

public abstract class RoleMenu {
    protected final Hotel hotel;
    private final Scanner in;

    protected RoleMenu(Hotel hotel, Scanner in) {
        this.hotel = hotel;
        this.in = in;
    }

    public abstract String getRoleName();

    protected abstract void showMenu();

    protected abstract boolean handle(String choice)
            throws RoomNotAvailableException;

    protected void greet() {
        System.out.println("\nLogged in as " + getRoleName());
    }

    public final void run() {
        greet();
        boolean running = true;

        while (running) {
            showMenu();
            String choice = ask("Choose an option: ");

            try {
                running = handle(choice);
            } catch (RoomNotAvailableException
                    | IllegalArgumentException
                    | IllegalStateException e) {
                System.out.println("! " + e.getMessage());
            }
        }

        System.out.println("Logged out.");
    }

    protected String ask(String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    protected int askInt(String prompt) {
        String text = ask(prompt);

        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "'" + text + "' is not a whole number");
        }
    }

    protected double askDouble(String prompt) {
        String text = ask(prompt);

        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "'" + text + "' is not a number");
        }
    }

    protected boolean askYes(String question) {
        String answer = ask(question + " (yes/no): ").toLowerCase();
        return answer.equals("yes") || answer.equals("y");
    }

    protected void printRooms(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("(none)");
            return;
        }

        for (Room r : rooms) {
            System.out.println(" " + r);
        }
    }

    protected void payInvoice(Invoice invoice) {
        invoice.print();

        while (!invoice.isPaid()) {
            String mode = ask(
                    "Pay by cash/card/upi (or later): ").toLowerCase();

            if (mode.equals("later")) {
                System.out.println(
                        "Invoice " + invoice.getInvoiceId()
                                + " left unpaid");
                return;
            }

            try {
                Payment payment = createPayment(
                        mode, invoice.getTotal());

                boolean success = invoice.settle(payment);

                System.out.println(" " + payment.getReceipt());

                if (success && payment instanceof CashPayment) {
                    System.out.printf(" Change: Rs.%.2f%n",
                            ((CashPayment) payment).getChange());
                }

            } catch (IllegalArgumentException e) {
                System.out.println("! " + e.getMessage());
            }
        }
    }

    private Payment createPayment(String mode, double amount) {
        if (mode.equals("cash")) {
            double given = askDouble("Cash handed over: Rs.");
            return new CashPayment(amount, given);
        }

        if (mode.equals("card")) {
            String number = ask("Card number (16 digits): ");
            String holder = ask("Card holder name: ");
            return new CardPayment(amount, number, holder);
        }

        if (mode.equals("upi")) {
            String upiId = ask("UPI id: ");
            return new UpiPayment(amount, upiId);
        }

        throw new IllegalArgumentException(
                "Unknown payment mode: " + mode);
    }
}


/*
    first run: RoleMenu.java:4: error: package model.booking does not exist
import model.booking.Invoice;
                    ^
RoleMenu.java:6: error: package model.room does not exist
import model.room.Room;
                 ^
RoleMenu.java:7: error: package service does not exist
import service.Hotel;
              ^
RoleMenu.java:12: error: cannot find symbol
    protected final Hotel hotel;
                    ^
  symbol:   class Hotel
  location: class RoleMenu
RoleMenu.java:15: error: cannot find symbol
    protected RoleMenu(Hotel hotel, Scanner in){
                       ^
  symbol:   class Hotel
  location: class RoleMenu
RoleMenu.java:25: error: cannot find symbol
        throws RoomNotAvailableException;
               ^
  symbol:   class RoomNotAvailableException
  location: class RoleMenu
RoleMenu.java:75: error: cannot find symbol
    protected void printRooms(List<Room> rooms){
                                   ^
  symbol:   class Room
  location: class RoleMenu
RoleMenu.java:85: error: cannot find symbol
    protected void payInvoice(Invoice invoice){
                              ^
  symbol:   class Invoice
  location: class RoleMenu
RoleMenu.java:111: error: cannot find symbol
        private Payment createPayment(String mode, double amount){
                ^
  symbol:   class Payment
  location: class RoleMenu
RoleMenu.java:3: error: package exception does not exist
import exception.*;
       ^
RoleMenu.java:5: error: package model.payment does not exist
import model.payment.*;
            ^
RoleMenu.java:39: error: cannot find symbol
            } catch (RoomNotAvailableException | IllegalArgumentException | IllegaStateException e) {
                     ^
  symbol:   class RoomNotAvailableException
  location: class RoleMenu
RoleMenu.java:39: error: cannot find symbol
            } catch (RoomNotAvailableException | IllegalArgumentException | IllegaStateException e) {
                                                                            ^
  symbol:   class IllegaStateException
  location: class RoleMenu
RoleMenu.java:80: error: cannot find symbol
        for(Room r: rooms){
            ^
  symbol:   class Room
  location: class RoleMenu
RoleMenu.java:95: error: cannot find symbol
                Payment payment = createPayment(mode,invoice.getTotal());
                ^
  symbol:   class Payment
  location: class RoleMenu
RoleMenu.java:98: error: cannot find symbol
                    if(payment.isSuccessful() && payment instanceof CashPayment){
                                                                    ^
  symbol:   class CashPayment
  location: class RoleMenu
RoleMenu.java:99: error: cannot find symbol
                        System.out.println(" Change: rs.%2f%n)" , ((CashPayment)payment).getChange());
                                                                    ^
  symbol:   class CashPayment
  location: class RoleMenu
RoleMenu.java:102: error: cannot find symbol
        ((CashPayment) payment).getChange());
          ^
  symbol:   class CashPayment
  location: class RoleMenu
RoleMenu.java:114: error: cannot find symbol
                return new CashPayment(amount,given);
                           ^
  symbol:   class CashPayment
  location: class RoleMenu
RoleMenu.java:119: error: cannot find symbol
                return new CardPayment(amount,number,holder);
                           ^
  symbol:   class CardPayment
  location: class RoleMenu
RoleMenu.java:122: error: cannot find symbol
                return new UpiPayment(amount,ask("UPI id: "));            
                           ^
  symbol:   class UpiPayment
  location: class RoleMenu
21 errors





*/

