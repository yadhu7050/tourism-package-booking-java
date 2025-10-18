import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class CompatServer {

    public static void main(String[] args) throws Exception {
        int port = 9999;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Static files - serve from current directory
        server.createContext("/", CompatServer::serveStatic);

        // API endpoints
        server.createContext("/api/destinations", ex -> withCors(ex, CompatServer::handleGetDestinations));
        server.createContext("/api/packages", ex -> withCors(ex, CompatServer::handleGetPackages));
        server.createContext("/api/bookings", ex -> withCors(ex, CompatServer::handleBookingsApi));

        // Legacy servlet-style endpoints
        server.createContext("/tourismpro/packages", ex -> withCors(ex, CompatServer::handleGetPackages));
        server.createContext("/tourismpro/bookings", ex -> withCors(ex, CompatServer::handleBookingsCompat));

        server.setExecutor(null);
        System.out.println("========================================");
        System.out.println("CompatServer running on http://localhost:" + port);
        System.out.println("Open: http://localhost:" + port + "/index.html");
        System.out.println("========================================");
        server.start();
    }

    private static void withCors(HttpExchange exchange, HttpHandler handler) throws IOException {
        Headers h = exchange.getResponseHeaders();
        h.add("Access-Control-Allow-Origin", "*");
        h.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        h.add("Access-Control-Allow-Headers", "Content-Type");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }
        handler.handle(exchange);
    }

    private static void serveStatic(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed");
            return;
        }
        
        String path = exchange.getRequestURI().getPath();
        
        // Default to index.html
        if (path.equals("/") || path.equals("")) {
            path = "/index.html";
        }
        
        // Remove leading slash for file system
        String filePath = path.startsWith("/") ? path.substring(1) : path;
        
        File file = new File(filePath);
        
        // Try alternate locations
        if (!file.exists() || file.isDirectory()) {
            file = new File("web/" + filePath);
        }
        if (!file.exists() || file.isDirectory()) {
            file = new File("src/main/webapp/" + filePath);
        }
        
        if (!file.exists() || file.isDirectory()) {
            sendText(exchange, 404, "Not Found: " + path);
            return;
        }
        
        String mime = URLConnection.guessContentTypeFromName(file.getName());
        if (mime == null) {
            if (filePath.endsWith(".html")) mime = "text/html";
            else if (filePath.endsWith(".css")) mime = "text/css";
            else if (filePath.endsWith(".js")) mime = "application/javascript";
            else mime = "application/octet-stream";
        }
        
        byte[] bytes = Files.readAllBytes(file.toPath());
        exchange.getResponseHeaders().add("Content-Type", mime);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void handleGetDestinations(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed");
            return;
        }
        
        String sql = "SELECT id, name, location, description, image_url, rating, reviews, price, duration, category, featured FROM destinations WHERE status='ACTIVE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                
                json.append('{')
                    .append("\"id\":").append(rs.getInt("id")).append(',')
                    .append("\"name\":\"").append(escape(rs.getString("name"))).append("\",")
                    .append("\"location\":\"").append(escape(rs.getString("location"))).append("\",")
                    .append("\"description\":\"").append(escape(rs.getString("description"))).append("\",")
                    .append("\"image_url\":\"").append(escape(rs.getString("image_url"))).append("\",")
                    .append("\"rating\":").append(rs.getBigDecimal("rating")).append(',')
                    .append("\"reviews\":").append(rs.getInt("reviews")).append(',')
                    .append("\"price\":").append(rs.getBigDecimal("price")).append(',')
                    .append("\"duration\":\"").append(escape(rs.getString("duration"))).append("\",")
                    .append("\"category\":\"").append(escape(rs.getString("category"))).append("\",")
                    .append("\"featured\":").append(rs.getBoolean("featured"))
                    .append('}');
            }
            
            json.append(']');
            sendJson(exchange, 200, json.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500, jsonError(e.getMessage()));
        }
    }

    private static void handleGetPackages(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendText(exchange, 405, "Method Not Allowed");
            return;
        }
        
        String sql = "SELECT id, title, description, destination, price, original_price, duration, max_guests, category, image_url, features, includes FROM packages WHERE status='ACTIVE'";
        
        try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery()) {
            
            StringBuilder json = new StringBuilder("[");
            boolean first = true;
            
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                
                json.append('{')
                    .append("\"id\":").append(rs.getInt("id")).append(',')
                    .append("\"title\":\"").append(escape(rs.getString("title"))).append("\",")
                    .append("\"description\":\"").append(escape(rs.getString("description"))).append("\",")
                    .append("\"destination\":\"").append(escape(rs.getString("destination"))).append("\",")
                    .append("\"price\":").append(rs.getBigDecimal("price")).append(',')
                    .append("\"original_price\":").append(rs.getBigDecimal("original_price") != null ? rs.getBigDecimal("original_price") : "null").append(',')
                    .append("\"duration\":\"").append(escape(rs.getString("duration"))).append("\",")
                    .append("\"max_guests\":").append(rs.getInt("max_guests")).append(',')
                    .append("\"category\":\"").append(escape(rs.getString("category"))).append("\",")
                    .append("\"image_url\":\"").append(escape(rs.getString("image_url"))).append("\",")
                    .append("\"features\":").append(stringOrNull(rs.getString("features"))).append(',')
                    .append("\"includes\":").append(stringOrNull(rs.getString("includes")))
                    .append('}');
            }
            
            json.append(']');
            sendJson(exchange, 200, json.toString());
            
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500, jsonError(e.getMessage()));
        }
    }

    private static void handleBookingsApi(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 200, listBookingsJson());
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            insertBooking(exchange, false);
            return;
        }
        sendText(exchange, 405, "Method Not Allowed");
    }

    private static void handleBookingsCompat(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String query = Optional.ofNullable(exchange.getRequestURI().getQuery()).orElse("");
            if (query.contains("action=list")) {
                sendJson(exchange, 200, listBookingsJson());
                return;
            }
            sendText(exchange, 400, "Missing action=list");
            return;
        }
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            insertBooking(exchange, true);
            return;
        }
        sendText(exchange, 405, "Method Not Allowed");
    }

    private static String listBookingsJson() {
        String sql = "SELECT id, first_name, last_name, email, phone, checkin_date, checkout_date, guests, package_id, total_amount, status FROM bookings ORDER BY booking_date DESC LIMIT 100";
        
        StringBuilder json = new StringBuilder("[");
        boolean first = true;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                if (!first) json.append(',');
                first = false;
                
                json.append('{')
                    .append("\"id\":").append(rs.getInt("id")).append(',')
                    .append("\"firstName\":\"").append(escape(rs.getString("first_name"))).append("\",")
                    .append("\"lastName\":\"").append(escape(rs.getString("last_name"))).append("\",")
                    .append("\"email\":\"").append(escape(rs.getString("email"))).append("\",")
                    .append("\"checkinDate\":\"").append(rs.getDate("checkin_date")).append("\",")
                    .append("\"checkoutDate\":\"").append(rs.getDate("checkout_date")).append("\",")
                    .append("\"guests\":").append(rs.getInt("guests")).append(',')
                    .append("\"packageId\":").append(rs.getInt("package_id")).append(',')
                    .append("\"status\":\"").append(escape(rs.getString("status"))).append("\"")
                    .append('}');
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            return jsonError(e.getMessage());
        }
        
        json.append(']');
        return json.toString();
    }

    private static void insertBooking(HttpExchange exchange, boolean camelCase) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> m = parseSimpleJson(body);
        
        if (m == null) {
            sendJson(exchange, 400, jsonError("Invalid JSON"));
            return;
        }
        
        String firstName = m.getOrDefault(camelCase ? "firstName" : "first_name", "");
        String lastName = m.getOrDefault(camelCase ? "lastName" : "last_name", "");
        String email = m.getOrDefault("email", "");
        String phone = m.getOrDefault("phone", "");
        String checkin = m.getOrDefault(camelCase ? "checkinDate" : "checkin_date", LocalDate.now().toString());
        String checkout = m.getOrDefault(camelCase ? "checkoutDate" : "checkout_date", LocalDate.now().plusDays(1).toString());
        int guests = Integer.parseInt(m.getOrDefault("guests", "1"));
        int packageId = Integer.parseInt(m.getOrDefault(camelCase ? "packageId" : "package_id", "1"));
        String special = m.getOrDefault(camelCase ? "specialRequests" : "special_requests", null);
        String total = m.getOrDefault(camelCase ? "totalAmount" : "total_amount", null);

        String sql = "INSERT INTO bookings (first_name, last_name, email, phone, checkin_date, checkout_date, guests, package_id, special_requests, total_amount, status) VALUES (?,?,?,?,?,?,?,?,?,?,'PENDING')";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            ps.setString(1, firstName);
            ps.setString(2, lastName);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.setDate(5, java.sql.Date.valueOf(LocalDate.parse(checkin)));
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.parse(checkout)));
            ps.setInt(7, guests);
            ps.setInt(8, packageId);
            ps.setString(9, special);
            
            if (total != null && !total.isEmpty()) {
                ps.setBigDecimal(10, new java.math.BigDecimal(total));
            } else {
                ps.setNull(10, Types.DECIMAL);
            }
            
            ps.executeUpdate();
            
            try (ResultSet keys = ps.getGeneratedKeys()) {
                int id = keys.next() ? keys.getInt(1) : -1;
                sendJson(exchange, 201, "{\"bookingId\":" + id + ",\"success\":true}");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            sendJson(exchange, 500, jsonError(e.getMessage()));
        }
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    private static String stringOrNull(String s) {
        if (s == null) return "null";
        String t = s.trim();
        if ((t.startsWith("[") && t.endsWith("]")) || (t.startsWith("{") && t.endsWith("}"))) {
            return t;
        }
        return "\"" + escape(s) + "\"";
    }

    private static String jsonError(String msg) {
        return "{\"error\":\"" + escape(msg) + "\"}";
    }

    private static void sendText(HttpExchange exchange, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseSimpleJson(String body) {
        try {
            Map<String, String> map = new HashMap<>();
            String t = body.trim();
            
            if (!t.startsWith("{") || !t.endsWith("}")) return null;
            
            t = t.substring(1, t.length() - 1).trim();
            if (t.isEmpty()) return map;
            
            List<String> pairs = new ArrayList<>();
            StringBuilder cur = new StringBuilder();
            boolean inStr = false;
            int braceDepth = 0;
            
            for (int i = 0; i < t.length(); i++) {
                char c = t.charAt(i);
                
                if (c == '"' && (i == 0 || t.charAt(i - 1) != '\\')) {
                    inStr = !inStr;
                }
                if (!inStr) {
                    if (c == '{' || c == '[') braceDepth++;
                    if (c == '}' || c == ']') braceDepth--;
                }
                if (c == ',' && !inStr && braceDepth == 0) {
                    pairs.add(cur.toString());
                    cur.setLength(0);
                } else {
                    cur.append(c);
                }
            }
            
            if (cur.length() > 0) pairs.add(cur.toString());
            
            for (String p : pairs) {
                int idx = p.indexOf(':');
                if (idx <= 0) continue;
                
                String k = p.substring(0, idx).trim();
                String v = p.substring(idx + 1).trim();
                
                if (k.startsWith("\"") && k.endsWith("\"")) {
                    k = k.substring(1, k.length() - 1);
                }
                if (v.startsWith("\"") && v.endsWith("\"")) {
                    v = v.substring(1, v.length() - 1)
                         .replace("\\\"", "\"")
                         .replace("\\n", "\n")
                         .replace("\\\\", "\\");
                }
                
                map.put(k, v);
            }
            
            return map;
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
