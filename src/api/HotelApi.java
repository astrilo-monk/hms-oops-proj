package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import exception.RoomNotAvailableException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import model.booking.Booking;
import model.booking.Invoice;
import model.payment.CardPayment;
import model.payment.CashPayment;
import model.payment.Payment;
import model.payment.UpiPayment;
import model.person.Guest;
import model.person.Staff;
import model.room.DeluxeRoom;
import model.room.Room;
import model.room.RoomStatus;
import model.room.StandardRoom;
import model.room.SuiteRoom;
import service.Hotel;

/** Small JDK-only HTTP API for the existing in-memory hotel domain model. */
public class HotelApi {
    private static final Hotel hotel = new Hotel("Grand Orchid Hotel");
    private static final Map<String, Invoice> invoices = new ConcurrentHashMap<>();
    private static final Map<String, Guest> guests = new ConcurrentHashMap<>();
    private static final Map<String, Staff> staffMembers = new ConcurrentHashMap<>();
    private static final Staff onlineDesk = new Staff("S000", "Online Desk", "0000000000", "Online Desk", 1, "Always");
    private static final Staff maintenanceDesk = new Staff("S003", "Biswajit Mondal", "9830077777", "Maintenance", 22000, "Day");

    public static void main(String[] args) throws IOException {
        seed();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 8080), 0);
        server.createContext("/api/", HotelApi::handle);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("hotel api running at http://127.0.0.1:8080/api");
    }

    private static void seed() {
        hotel.addRoom(new StandardRoom(101, 1, 2000));
        hotel.addRoom(new StandardRoom(102, 1, 2000));
        hotel.addRoom(new DeluxeRoom(201, 2, 3500));
        hotel.addRoom(new DeluxeRoom(202, 2, 3500));
        hotel.addRoom(new SuiteRoom(301, 3, 8000));
        addGuest("Aarav Sen", "9830012345", "aarav@example.com", "DEMO-1111");
        addGuest("Meera Das", "9830054321", "meera@example.com", "DEMO-2222");
        Staff receptionist = new Staff("S001", "Rohit Ghosh", "9830099999", "Receptionist", 28000, "Morning");
        staffMembers.put(receptionist.getId(), receptionist);
        staffMembers.put("S002", new Staff("S002", "Anita Basu", "9830088888", "Manager", 42000, "Morning"));
        staffMembers.put(maintenanceDesk.getId(), maintenanceDesk);
        try {
            hotel.bookRoom(guests.get("G001"), 102, LocalDate.now(), LocalDate.now().plusDays(2), receptionist);
            hotel.reportMaintenance(202, "air conditioner needs inspection", receptionist);
        } catch (RoomNotAvailableException | IllegalArgumentException ignored) {
            // demo seed only
        }
    }

    private static Guest addGuest(String name, String phone, String email, String proof) {
        Guest guest = hotel.registerNewGuest(name, phone, email, proof);
        guests.put(guest.getId(), guest);
        return guest;
    }

    private static void handle(HttpExchange exchange) throws IOException {
        cors(exchange);
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) { send(exchange, 204, ""); return; }
        String path = exchange.getRequestURI().getPath().substring("/api".length());
        String method = exchange.getRequestMethod();
        try {
            if (method.equals("GET") && path.equals("/health")) send(exchange, 200, "{\"status\":\"ok\"}");
            else if (method.equals("GET") && path.equals("/dashboard")) dashboard(exchange);
            else if (method.equals("GET") && path.equals("/rooms")) rooms(exchange);
            else if (method.equals("GET") && path.equals("/bookings")) bookings(exchange);
            else if (method.equals("GET") && path.equals("/guests")) guestList(exchange);
            else if (method.equals("GET") && path.equals("/staff")) staffList(exchange);
            else if (method.equals("POST") && path.equals("/guests")) createGuest(exchange);
            else if (method.equals("POST") && path.equals("/staff")) createStaff(exchange);
            else if (method.equals("POST") && path.equals("/bookings")) createBooking(exchange);
            else if (method.equals("POST") && path.equals("/maintenance")) maintenance(exchange);
            else if (method.equals("POST") && path.equals("/maintenance/fix")) fixMaintenance(exchange);
            else if (method.equals("POST") && path.startsWith("/checkout/")) checkout(exchange, path.substring("/checkout/".length()));
            else if (method.equals("POST") && path.startsWith("/pay/")) pay(exchange, path.substring("/pay/".length()));
            else send(exchange, 404, error("endpoint not found"));
} catch (IllegalArgumentException | IllegalStateException e) {            send(exchange, 400, error(e.getMessage()));
        } catch (Exception e) {
            send(exchange, 500, error("server error: " + e.getMessage()));
        }
    }

    private static void dashboard(HttpExchange x) throws IOException {
        int available = 0, occupied = 0, maintenance = 0;
        for (Room r : hotel.getRooms()) {
            if (r.getStatus() == RoomStatus.AVAILABLE) available++;
            else if (r.getStatus() == RoomStatus.OCCUPIED) occupied++;
            else maintenance++;
        }
        send(x, 200, "{\"hotel\":" + q(hotel.getName()) + ",\"totalRooms\":" + hotel.getRooms().size()
                + ",\"available\":" + available + ",\"occupied\":" + occupied + ",\"maintenance\":" + maintenance
                + ",\"occupancy\":" + hotel.getOccupancyPercent() + "}");
    }

    private static void rooms(HttpExchange x) throws IOException {
        StringBuilder b = new StringBuilder("[");
        boolean first = true;
        for (Room r : hotel.getRooms()) {
            if (!first) b.append(','); first = false;
            b.append("{\"number\":").append(r.getRoomNumber()).append(",\"floor\":").append(r.getFloor())
             .append(",\"type\":").append(q(r.getType())).append(",\"status\":").append(q(r.getStatus().name()))
             .append(",\"rate\":").append(r.getBaseRate()).append(",\"amenities\":").append(q(r.getAmenities()))
             .append(",\"maintenanceIssue\":").append(q(r.getMaintenIssue() == null ? "" : r.getMaintenIssue())).append('}');
        }
        send(x, 200, b.append(']').toString());
    }

    private static void bookings(HttpExchange x) throws IOException {
        StringBuilder b = new StringBuilder("["); boolean first = true;
        for (Booking v : hotel.getBookings()) {
            if (!first) b.append(','); first = false;
            b.append("{\"id\":").append(q(v.getBookingId())).append(",\"guest\":").append(q(v.getGuest().getName()))
             .append(",\"guestId\":").append(q(v.getGuest().getId())).append(",\"room\":").append(v.getRoom().getRoomNumber())
             .append(",\"type\":").append(q(v.getRoom().getType())).append(",\"checkIn\":").append(q(v.getCheckIn().toString()))
             .append(",\"checkOut\":").append(q(v.getCheckOut().toString())).append(",\"nights\":").append(v.getNights())
             .append(",\"amount\":").append(v.calculateAmount() * 1.12).append(",\"status\":").append(q(v.getStatus().name())).append('}');
        }
        send(x, 200, b.append(']').toString());
    }

    private static void guestList(HttpExchange x) throws IOException {
        StringBuilder b = new StringBuilder("["); boolean first = true;
        for (Guest g : guests.values()) {
            if (!first) b.append(','); first = false;
            b.append("{\"id\":").append(q(g.getId())).append(",\"name\":").append(q(g.getName()))
             .append(",\"phone\":").append(q(g.getPhone())).append(",\"email\":").append(q(g.getEmail())).append('}');
        }
        send(x, 200, b.append(']').toString());
    }

    private static void createGuest(HttpExchange x) throws IOException {
        String body = body(x);
        Guest g = addGuest(required(body, "name"), required(body, "phone"), required(body, "email"), value(body, "idProof", "not provided"));
        send(x, 201, "{\"id\":" + q(g.getId()) + ",\"name\":" + q(g.getName()) + "}");
    }

    private static void staffList(HttpExchange x) throws IOException {
        StringBuilder b = new StringBuilder("[");
        boolean first = true;
        for (Staff staff : staffMembers.values()) {
            if (!first) b.append(',');
            first = false;
            b.append("{\"id\":").append(q(staff.getId())).append(",\"name\":").append(q(staff.getName()))
             .append(",\"phone\":").append(q(staff.getPhone())).append(",\"designation\":").append(q(staff.getDesignation()))
             .append(",\"salary\":").append(staff.getSalary()).append(",\"shift\":").append(q(staff.getShift())).append('}');
        }
        send(x, 200, b.append(']').toString());
    }

    private static void createStaff(HttpExchange x) throws IOException {
        String body = body(x);
        String id = required(body, "id");
        if (staffMembers.containsKey(id)) throw new IllegalArgumentException("staff id already exists");
        Staff staff = new Staff(id, required(body, "name"), required(body, "phone"),
                required(body, "designation"), Double.parseDouble(required(body, "salary")),
                required(body, "shift"));
        staffMembers.put(id, staff);
        send(x, 201, "{\"id\":" + q(staff.getId()) + ",\"name\":" + q(staff.getName())
                + ",\"designation\":" + q(staff.getDesignation()) + "}");
    }

    private static void createBooking(HttpExchange x) throws IOException {
        String body = body(x);
        String guestId = required(body, "guestId");
        int room = Integer.parseInt(required(body, "roomNumber"));
        int nights = Integer.parseInt(required(body, "nights"));
        if (nights < 1 || nights > 365) throw new IllegalArgumentException("nights must be between 1 and 365");
        Guest guest = guests.get(guestId);
        if (guest == null) throw new IllegalArgumentException("unknown guest id");
        Staff receptionist = new Staff("S001", "Rohit Ghosh", "9830099999", "Receptionist", 28000, "Morning");
        Booking booking;
        try {
            booking = hotel.bookRoom(
                guest,
                room,
                LocalDate.now(),
                LocalDate.now().plusDays(nights),
                receptionist
            );
        } catch (RoomNotAvailableException e) {
            throw new IllegalStateException("Room is not available: " + e.getMessage(), e);
}
        send(x, 201, "{\"id\":" + q(booking.getBookingId()) + ",\"room\":" + room + ",\"guest\":" + q(guest.getName()) + "}");
    }

    private static void maintenance(HttpExchange x) throws IOException {
        String body = body(x);
        int room = Integer.parseInt(required(body, "roomNumber"));
        String issue = required(body, "issue");
        Staff receptionist = new Staff("S001", "Rohit Ghosh", "9830099999", "Receptionist", 28000, "Morning");
        hotel.reportMaintenance(room, issue, receptionist);
        send(x, 200, "{\"status\":\"UNDER_MAINTENANCE\"}");
    }

    private static void fixMaintenance(HttpExchange x) throws IOException {
        String body = body(x);
        int room = Integer.parseInt(required(body, "roomNumber"));
        hotel.markFixed(room, maintenanceDesk);
        send(x, 200, "{\"status\":\"AVAILABLE\"}");
    }

    private static void checkout(HttpExchange x, String id) throws IOException {
        Invoice invoice = hotel.checkOut(id);
        invoices.put(invoice.getInvoiceId(), invoice);
        send(x, 200, "{\"invoiceId\":" + q(invoice.getInvoiceId()) + ",\"bookingId\":" + q(id)
                + ",\"subtotal\":" + invoice.getSubtotal() + ",\"gst\":" + invoice.getGst()
                + ",\"total\":" + invoice.getTotal() + ",\"paid\":false}");
    }

    private static void pay(HttpExchange x, String invoiceId) throws IOException {
        Invoice invoice = invoices.get(invoiceId);
        if (invoice == null) throw new IllegalArgumentException("invoice not found; check out the booking first");
        String body = body(x); String mode = required(body, "mode").toLowerCase();
        Payment payment;
        switch (mode) {
            case "cash": payment = new CashPayment(invoice.getTotal(), Double.parseDouble(required(body, "amountTendered"))); break;
            case "card": payment = new CardPayment(invoice.getTotal(), required(body, "cardNumber"), value(body, "cardHolder", "Guest")); break;
            case "upi": payment = new UpiPayment(invoice.getTotal(), required(body, "upiId")); break;
            default: throw new IllegalArgumentException("mode must be cash, card or upi");
        }
        boolean ok = invoice.settle(payment);
        send(x, ok ? 200 : 400, "{\"success\":" + ok + ",\"receipt\":" + q(payment.getReceipt()) + ",\"paid\":" + invoice.isPaid() + "}");
    }

    private static String body(HttpExchange x) throws IOException {
        return new String(x.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }
    private static String required(String json, String key) {
        String value = value(json, key, null);
        if (value == null || value.trim().isEmpty()) throw new IllegalArgumentException("missing field: " + key);
        return value;
    }
    private static String value(String json, String key, String fallback) {
        Pattern p = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(?:\\\"((?:\\\\.|[^\\\"\\\\])*)\\\"|(-?\\d+(?:\\.\\d+)?))");
        Matcher m = p.matcher(json);
        if (!m.find()) return fallback;
        return m.group(1) != null ? m.group(1).replace("\\\"", "\"").replace("\\\\", "\\") : m.group(2);
    }
    private static String error(String message) { return "{\"error\":" + q(message == null ? "request failed" : message) + "}"; }
    private static String q(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }
    private static void cors(HttpExchange x) {
        String origin = x.getRequestHeaders().getFirst("Origin");
        if ("http://localhost:5173".equals(origin) || "http://127.0.0.1:5173".equals(origin)) {
            x.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            x.getResponseHeaders().set("Vary", "Origin");
        }
        x.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        x.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
        x.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
    }
    private static void send(HttpExchange x, int status, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        if (status == 204 || status == 304) {
    x.sendResponseHeaders(status, -1);
} else {
    x.sendResponseHeaders(status, bytes.length);
}
        try (var out = x.getResponseBody()) { out.write(bytes); }
    }
}
