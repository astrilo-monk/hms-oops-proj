package ui;

import exception.RoomNotAvailableException;
import java.util.List;
import java.util.Scanner;
import model.booking.Invoice;
import model.payment.CardPayment;
import model.payment.CashPayment;
import model.payment.Payment;
import model.payment.UpiPayment;
import model.room.Room;
import service.Hotel;

public abstract class RoleMenu {
    protected final Hotel hotel;
    private final Scanner in;
    private Runnable autosave = () -> { };

    protected RoleMenu(Hotel hotel, Scanner in) {
        this.hotel = hotel;
        this.in = in;
    }

    public void setAutosave(Runnable autosave) {
        this.autosave = autosave == null ? () -> { } : autosave;
    }

    public abstract String getRoleName();
    protected abstract void showMenu();
    protected abstract boolean handle(String choice) throws RoomNotAvailableException;

    protected void greet() {
        System.out.println("\n--- Logged in as " + getRoleName() + " ---");
    }

    public final void run() {
        greet();
        boolean running = true;
        while (running) {
            showMenu();
            String choice = ask("Choose an option: ");
            try {
                running = handle(choice);
            } catch (RoomNotAvailableException | IllegalArgumentException
                     | IllegalStateException e) {
                System.out.println("  ! " + e.getMessage());
            }
            try {
                autosave.run();
            } catch (RuntimeException e) {
                System.out.println("  ! Could not save: " + e.getMessage());
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
            throw new IllegalArgumentException("'" + text + "' is not a whole number");
        }
    }

    protected double askDouble(String prompt) {
        String text = ask(prompt);
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("'" + text + "' is not a number");
        }
    }

    protected boolean askYes(String question) {
        String answer = ask(question + " (yes/no): ").toLowerCase();
        return answer.equals("yes") || answer.equals("y");
    }

    protected void printRooms(List<Room> rooms) {
        if (rooms.isEmpty()) {
            System.out.println("  (none)");
            return;
        }
        for (Room room : rooms) {
            System.out.println("  " + room);
        }
    }

    protected void payInvoice(Invoice invoice) {
        invoice.print();
        while (!invoice.isPaid()) {
            String mode = ask("Pay by cash / card / upi (or 'later'): ").toLowerCase();
            if (mode.equals("later")) {
                System.out.println("  Invoice " + invoice.getInvoiceId() + " left UNPAID.");
                return;
            }
            try {
                Payment payment = createPayment(mode, invoice.getTotal());
                invoice.settle(payment);
                System.out.println("  " + payment.getReceipt());
                if (payment.isSuccessful() && payment instanceof CashPayment) {
                    System.out.printf("  Change: Rs.%.2f%n",
                            ((CashPayment) payment).getChange());
                }
            } catch (IllegalArgumentException e) {
                System.out.println("  ! " + e.getMessage());
            }
        }
    }

    private Payment createPayment(String mode, double amount) {
        if (mode.equals("cash")) {
            return new CashPayment(amount, askDouble("Cash handed over: Rs."));
        }
        if (mode.equals("card")) {
            return new CardPayment(amount, ask("Card number (16 digits): "),
                    ask("Card holder name: "));
        }
        if (mode.equals("upi")) {
            return new UpiPayment(amount, ask("UPI id (name@bank): "));
        }
        throw new IllegalArgumentException("Unknown payment mode: " + mode);
    }
}
