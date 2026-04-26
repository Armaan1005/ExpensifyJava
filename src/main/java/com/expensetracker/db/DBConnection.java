package com.expensetracker.db;

import com.expensetracker.model.Expense;
import com.expensetracker.model.Split;
import com.expensetracker.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles all database operations: connection management, user auth,
 * expense CRUD, dashboard aggregations, and budget management.
 *
 * Uses SQLite via JDBC — the database file is created automatically
 * in the working directory as {@code expense_tracker.db}.
 */
public class DBConnection {

    private static final String DB_URL = "jdbc:sqlite:expense_tracker.db";
    private static DBConnection instance;
    private Connection connection;

    // ── Singleton ─────────────────────────────────────────────

    private DBConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            // Enable WAL mode for better concurrency
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA foreign_keys=ON");
            }
            initTables();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    /** Returns the singleton DBConnection instance. */
    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    /** Returns the raw JDBC connection. */
    public Connection getConnection() { return connection; }

    // ── Schema Initialization ─────────────────────────────────

    private void initTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS Users ("
                    + "user_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "username TEXT NOT NULL UNIQUE, "
                    + "password TEXT NOT NULL, "
                    + "monthly_budget REAL DEFAULT 0.0)");

            st.execute("CREATE TABLE IF NOT EXISTS Expenses ("
                    + "expense_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "user_id INTEGER NOT NULL, "
                    + "amount REAL NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "date TEXT NOT NULL, "
                    + "description TEXT DEFAULT '', "
                    + "FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE)");

            st.execute("CREATE INDEX IF NOT EXISTS idx_expenses_user ON Expenses(user_id)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_expenses_category ON Expenses(user_id, category)");

            st.execute("CREATE TABLE IF NOT EXISTS Splits ("
                    + "split_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "payer_id INTEGER NOT NULL, "
                    + "payee_id INTEGER NOT NULL, "
                    + "amount REAL NOT NULL, "
                    + "description TEXT DEFAULT '', "
                    + "date TEXT NOT NULL, "
                    + "status TEXT DEFAULT 'PENDING', "
                    + "FOREIGN KEY (payer_id) REFERENCES Users(user_id) ON DELETE CASCADE, "
                    + "FOREIGN KEY (payee_id) REFERENCES Users(user_id) ON DELETE CASCADE)");

            st.execute("CREATE INDEX IF NOT EXISTS idx_splits_payee ON Splits(payee_id)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_splits_payer ON Splits(payer_id)");
        }
    }

    // ── Password Hashing (SHA-256) ────────────────────────────

    /** Hashes a plaintext password using SHA-256. */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ── User Authentication ───────────────────────────────────

    /**
     * Registers a new user. Returns true on success, false if the
     * username already exists.
     */
    public boolean registerUser(String username, String password) {
        String sql = "INSERT INTO Users (username, password) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // UNIQUE constraint violation → username taken
            return false;
        }
    }

    /**
     * Authenticates a user by username and password.
     * Returns the User object on success, or null on failure.
     */
    public User loginUser(String username, String password) {
        String sql = "SELECT * FROM Users WHERE username = ? AND password = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, hashPassword(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getDouble("monthly_budget")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ── Expense CRUD ──────────────────────────────────────────

    /** Inserts a new expense record. */
    public boolean addExpense(Expense expense) {
        String sql = "INSERT INTO Expenses (user_id, amount, category, date, description) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, expense.getUserId());
            ps.setDouble(2, expense.getAmount());
            ps.setString(3, expense.getCategory());
            ps.setString(4, expense.getDate());
            ps.setString(5, expense.getDescription());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Updates an existing expense record. */
    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE Expenses SET amount = ?, category = ?, date = ?, description = ? "
                + "WHERE expense_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, expense.getAmount());
            ps.setString(2, expense.getCategory());
            ps.setString(3, expense.getDate());
            ps.setString(4, expense.getDescription());
            ps.setInt(5, expense.getExpenseId());
            ps.setInt(6, expense.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Deletes an expense by ID. */
    public boolean deleteExpense(int expenseId, int userId) {
        String sql = "DELETE FROM Expenses WHERE expense_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, expenseId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Retrieves all expenses for a given user, ordered by date descending. */
    public List<Expense> getExpenses(int userId) {
        return getExpenses(userId, null, null, null);
    }

    /**
     * Retrieves expenses with optional filters.
     * @param category filter by category (null = all)
     * @param startDate filter from this date inclusive (null = no lower bound)
     * @param endDate   filter to this date inclusive (null = no upper bound)
     */
    public List<Expense> getExpenses(int userId, String category,
                                     String startDate, String endDate) {
        List<Expense> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT * FROM Expenses WHERE user_id = ?");
        List<Object> params = new ArrayList<>();
        params.add(userId);

        if (category != null && !category.isEmpty() && !category.equals("All")) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND date >= ?");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND date <= ?");
            params.add(endDate);
        }
        sql.append(" ORDER BY date DESC, expense_id DESC");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) ps.setInt(i + 1, (Integer) p);
                else ps.setString(i + 1, (String) p);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Expense(
                        rs.getInt("expense_id"),
                        rs.getInt("user_id"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("date"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Dashboard Aggregations ────────────────────────────────

    /** Total of all expenses for the user. */
    public double getTotalExpenses(int userId) {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM Expenses WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Total expenses for the current month. */
    public double getMonthlyExpenses(int userId) {
        String monthPrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM Expenses "
                + "WHERE user_id = ? AND date LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, monthPrefix + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Sum of expenses grouped by category (ordered by total descending). */
    public Map<String, Double> getCategorySummary(int userId) {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT category, SUM(amount) AS total FROM Expenses "
                + "WHERE user_id = ? GROUP BY category ORDER BY total DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getDouble("total"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    /** Monthly totals for the past 12 months (YYYY-MM → total). */
    public Map<String, Double> getMonthlyOverview(int userId) {
        Map<String, Double> map = new LinkedHashMap<>();
        // Pre-fill last 12 months with 0
        LocalDate now = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 11; i >= 0; i--) {
            map.put(now.minusMonths(i).format(fmt), 0.0);
        }
        String sql = "SELECT substr(date, 1, 7) AS month, SUM(amount) AS total "
                + "FROM Expenses WHERE user_id = ? AND date >= ? "
                + "GROUP BY month ORDER BY month";
        String startDate = now.minusMonths(11).withDayOfMonth(1).toString();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, startDate);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String month = rs.getString("month");
                if (map.containsKey(month)) {
                    map.put(month, rs.getDouble("total"));
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return map;
    }

    /** Number of distinct categories used by the user. */
    public int getCategoryCount(int userId) {
        String sql = "SELECT COUNT(DISTINCT category) FROM Expenses WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Last 5 expenses for the user. */
    public List<Expense> getRecentExpenses(int userId, int limit) {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT * FROM Expenses WHERE user_id = ? ORDER BY date DESC, expense_id DESC LIMIT ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, limit);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Expense(
                        rs.getInt("expense_id"),
                        rs.getInt("user_id"),
                        rs.getDouble("amount"),
                        rs.getString("category"),
                        rs.getString("date"),
                        rs.getString("description")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // ── Budget Management ─────────────────────────────────────

    /** Updates the monthly budget for a user. */
    public boolean updateBudget(int userId, double budget) {
        String sql = "UPDATE Users SET monthly_budget = ? WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setDouble(1, budget);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Gets the monthly budget for a user. */
    public double getBudget(int userId) {
        String sql = "SELECT monthly_budget FROM Users WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ── Split Management ─────────────────────────────────────

    /** Retrieves all registered users except the current one. */
    public List<User> getAllUsers(int excludeUserId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT user_id, username FROM Users WHERE user_id != ? ORDER BY username ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, excludeUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User u = new User();
                u.setUserId(rs.getInt("user_id"));
                u.setUsername(rs.getString("username"));
                list.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Creates a new split record. */
    public boolean addSplit(Split split) {
        String sql = "INSERT INTO Splits (payer_id, payee_id, amount, description, date, status) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, split.getPayerId());
            ps.setInt(2, split.getPayeeId());
            ps.setDouble(3, split.getAmount());
            ps.setString(4, split.getDescription());
            ps.setString(5, split.getDate());
            ps.setString(6, split.getStatus());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Retrieves splits where the user is either the payer or the payee. */
    public List<Split> getSplitsForUser(int userId) {
        List<Split> list = new ArrayList<>();
        String sql = "SELECT s.*, u1.username as payer_name, u2.username as payee_name " +
                     "FROM Splits s " +
                     "JOIN Users u1 ON s.payer_id = u1.user_id " +
                     "JOIN Users u2 ON s.payee_id = u2.user_id " +
                     "WHERE s.payer_id = ? OR s.payee_id = ? " +
                     "ORDER BY s.date DESC, s.split_id DESC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Split(
                        rs.getInt("split_id"),
                        rs.getInt("payer_id"),
                        rs.getString("payer_name"),
                        rs.getInt("payee_id"),
                        rs.getString("payee_name"),
                        rs.getDouble("amount"),
                        rs.getString("description"),
                        rs.getString("date"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Updates the status of a split (e.g., mark as PAID). */
    public boolean updateSplitStatus(int splitId, String status) {
        String sql = "UPDATE Splits SET status = ? WHERE split_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, splitId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Checks if the user has any pending splits where they are the payee. */
    public int getPendingSplitCount(int userId) {
        String sql = "SELECT COUNT(*) FROM Splits WHERE payee_id = ? AND status = 'PENDING'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Closes the database connection. */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
