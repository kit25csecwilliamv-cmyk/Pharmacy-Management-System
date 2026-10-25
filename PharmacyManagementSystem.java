import java.sql.*;
import java.util.Scanner;

public class PharmacyManagementSystem {

    static final String URL = "jdbc:mysql://localhost:3306/pharmacy_db";
    static final String USER = "root";
    static final String PASSWORD = "YOUR_MYSQL_PASSWORD";

    static Scanner sc = new Scanner(System.in);

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void addMedicine() {
        String sql = "INSERT INTO medicines (name, category, quantity, price, expiry_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            System.out.print("Medicine Name: ");
            String name = sc.nextLine();

            System.out.print("Category: ");
            String category = sc.nextLine();

            System.out.print("Quantity: ");
            int quantity = Integer.parseInt(sc.nextLine());

            System.out.print("Price: ");
            double price = Double.parseDouble(sc.nextLine());

            System.out.print("Expiry Date (YYYY-MM-DD): ");
            String expiry = sc.nextLine();

            if (name.isBlank() || quantity < 0 || price < 0) {
                System.out.println("Invalid medicine details.");
                return;
            }

            ps.setString(1, name);
            ps.setString(2, category);
            ps.setInt(3, quantity);
            ps.setDouble(4, price);
            ps.setDate(5, Date.valueOf(expiry));

            ps.executeUpdate();

            System.out.println("Medicine added successfully!");

        } catch (SQLException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void viewMedicines() {
        String sql = "SELECT * FROM medicines ORDER BY id";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- MEDICINE INVENTORY ---");

            System.out.printf(
                "%-5s %-20s %-15s %-10s %-10s %-12s%n",
                "ID", "Name", "Category", "Quantity", "Price", "Expiry"
            );

            while (rs.next()) {
                System.out.printf(
                    "%-5d %-20s %-15s %-10d %-10.2f %-12s%n",
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getDate("expiry_date")
                );
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void searchMedicine() {
        System.out.print("Enter Medicine ID: ");
        int id;

        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        String sql = "SELECT * FROM medicines WHERE id = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("ID: " + rs.getInt("id"));
                    System.out.println("Name: " + rs.getString("name"));
                    System.out.println("Category: " + rs.getString("category"));
                    System.out.println("Quantity: " + rs.getInt("quantity"));
                    System.out.println("Price: " + rs.getDouble("price"));
                    System.out.println("Expiry: " + rs.getDate("expiry_date"));
                } else {
                    System.out.println("Medicine not found.");
                }
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void updateMedicine() {
        System.out.print("Enter Medicine ID: ");
        int id;

        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        System.out.print("New Medicine Name: ");
        String name = sc.nextLine();

        System.out.print("New Quantity: ");
        int quantity;

        System.out.print("New Price: ");
        double price;

        try {
            quantity = Integer.parseInt(sc.nextLine());
            price = Double.parseDouble(sc.nextLine());

            if (name.isBlank() || quantity < 0 || price < 0) {
                System.out.println("Invalid medicine details.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity or price.");
            return;
        }

        String sql = "UPDATE medicines SET name = ?, quantity = ?, price = ? WHERE id = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, quantity);
            ps.setDouble(3, price);
            ps.setInt(4, id);

            int rows = ps.executeUpdate();

            System.out.println(rows > 0
                ? "Medicine updated successfully!"
                : "Medicine not found.");

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void deleteMedicine() {
        System.out.print("Enter Medicine ID to delete: ");
        int id;

        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        String sql = "DELETE FROM medicines WHERE id = ?";

        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            System.out.println(rows > 0
                ? "Medicine deleted successfully!"
                : "Medicine not found.");

        } catch (SQLException e) {
            System.out.println(
                "Unable to delete medicine. It may have sales records."
            );
        }
    }

    public static void sellMedicine() {
        System.out.print("Enter Medicine ID: ");
        int id;

        try {
            id = Integer.parseInt(sc.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
            return;
        }

        System.out.print("Enter Quantity to Sell: ");
        int quantity;

        try {
            quantity = Integer.parseInt(sc.nextLine());

            if (quantity <= 0) {
                System.out.println("Quantity must be greater than zero.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid quantity.");
            return;
        }

        String select = "SELECT quantity, price FROM medicines WHERE id = ? FOR UPDATE";
        String update = "UPDATE medicines SET quantity = quantity - ? WHERE id = ?";
        String sale = "INSERT INTO sales (medicine_id, quantity, total_price) VALUES (?, ?, ?)";

        try (Connection con = connect()) {
            con.setAutoCommit(false);

            try {
                int available;
                double price;

                try (PreparedStatement ps = con.prepareStatement(select)) {
                    ps.setInt(1, id);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Medicine not found.");
                        }

                        available = rs.getInt("quantity");
                        price = rs.getDouble("price");
                    }
                }

                if (available < quantity) {
                    throw new SQLException("Insufficient stock.");
                }

                double total = price * quantity;

                try (PreparedStatement ps = con.prepareStatement(update)) {
                    ps.setInt(1, quantity);
                    ps.setInt(2, id);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(sale)) {
                    ps.setInt(1, id);
                    ps.setInt(2, quantity);
                    ps.setDouble(3, total);
                    ps.executeUpdate();
                }

                con.commit();

                System.out.println("Sale completed successfully!");
                System.out.printf("Total Bill: Rs. %.2f%n", total);

            } catch (SQLException e) {
                con.rollback();
                System.out.println("Sale failed: " + e.getMessage());
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    public static void lowStock() {
        String sql = "SELECT * FROM medicines WHERE quantity < 10 ORDER BY quantity";

        try (Connection con = connect();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            System.out.println("\n--- LOW STOCK MEDICINES ---");

            boolean found = false;

            while (rs.next()) {
                found = true;
                System.out.println(
                    rs.getInt("id") + " | " +
                    rs.getString("name") + " | Stock: " +
                    rs.getInt("quantity")
                );
            }

            if (!found) {
                System.out.println("No low-stock medicines.");
            }

        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("\n===== PHARMACY MANAGEMENT SYSTEM =====");
            System.out.println("1. Add Medicine");
            System.out.println("2. View Medicines");
            System.out.println("3. Search Medicine");
            System.out.println("4. Update Medicine");
            System.out.println("5. Delete Medicine");
            System.out.println("6. Sell Medicine");
            System.out.println("7. Low Stock Alert");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");

            String input = sc.nextLine();

            switch (input) {
                case "1":
                    addMedicine();
                    break;
                case "2":
                    viewMedicines();
                    break;
                case "3":
                    searchMedicine();
                    break;
                case "4":
                    updateMedicine();
                    break;
                case "5":
                    deleteMedicine();
                    break;
                case "6":
                    sellMedicine();
                    break;
                case "7":
                    lowStock();
                    break;
                case "8":
                    System.out.println("Thank you!");
                    sc.close();
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }
}
