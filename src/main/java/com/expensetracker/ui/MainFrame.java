package com.expensetracker.ui;

import com.expensetracker.model.Expense;
import com.expensetracker.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Main application frame with a sidebar navigation and a CardLayout
 * content area. Acts as the controller that wires together all panels.
 */
public class MainFrame extends JFrame {

    private final User user;

    // Content panels
    private DashboardPanel dashboardPanel;
    private ExpenseFormPanel expenseFormPanel;
    private ExpenseTablePanel expenseTablePanel;
    private BudgetPanel budgetPanel;

    // Navigation
    private CardLayout cardLayout;
    private JPanel contentArea;
    private final List<JButton> navButtons = new ArrayList<>();
    private static final String DASHBOARD = "dashboard";
    private static final String ADD_EXPENSE = "add_expense";
    private static final String VIEW_EXPENSES = "view_expenses";
    private static final String BUDGET = "budget";

    public MainFrame(User user) {
        this.user = user;
        setTitle("Expensify – " + user.getUsername());
        setSize(1350, 850);
        setMinimumSize(new Dimension(1100, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        UIUtils.centerOnScreen(this);

        buildUI();
    }

    // ══════════════════════════════════════════════════════════
    // UI CONSTRUCTION
    // ══════════════════════════════════════════════════════════

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIUtils.BG_DARK);

        // ── Sidebar ───────────────────────────────────────────
        root.add(createSidebar(), BorderLayout.WEST);

        // ── Content Area ──────────────────────────────────────
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(UIUtils.BG_DARK);

        dashboardPanel = new DashboardPanel(user);
        expenseFormPanel = new ExpenseFormPanel(user, this);
        expenseTablePanel = new ExpenseTablePanel(user, this);
        budgetPanel = new BudgetPanel(user, this);

        contentArea.add(dashboardPanel, DASHBOARD);
        contentArea.add(expenseFormPanel, ADD_EXPENSE);
        contentArea.add(expenseTablePanel, VIEW_EXPENSES);
        contentArea.add(budgetPanel, BUDGET);

        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    /** Builds the dark sidebar with logo, nav buttons, and logout. */
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UIUtils.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(
                0, 0, 0, 1, UIUtils.BORDER_COLOR));

        // ── Logo Area ─────────────────────────────────────────
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.X_AXIS));
        logoPanel.setOpaque(false);
        logoPanel.setBorder(new EmptyBorder(24, 20, 20, 20));
        logoPanel.setMaximumSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 74));
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel logoIcon = new JLabel(UIUtils.getIcon("wallet", 24, UIUtils.ACCENT));

        JLabel logoText = UIUtils.createLabel("Expensify",
                new Font("Parisienne", Font.PLAIN, 28), UIUtils.TEXT_PRIMARY);

        logoPanel.add(logoIcon);
        logoPanel.add(UIUtils.hSpace(10));
        logoPanel.add(logoText);
        sidebar.add(logoPanel);

        // Divider
        JSeparator sep = new JSeparator();
        sep.setForeground(UIUtils.BORDER_COLOR);
        sep.setBackground(UIUtils.BORDER_COLOR);
        sep.setMaximumSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 1));
        sep.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sep);
        sidebar.add(UIUtils.vSpace(16));

        // ── Nav Label ─────────────────────────────────────────
        JLabel navLabel = UIUtils.createLabel("MENU",
                UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);
        navLabel.setBorder(new EmptyBorder(0, 22, 0, 0));
        navLabel.setAlignmentX(LEFT_ALIGNMENT);
        navLabel.setMaximumSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 20));
        sidebar.add(navLabel);
        sidebar.add(UIUtils.vSpace(12));

        // ── Navigation Buttons ────────────────────────────────
        JButton dashBtn = UIUtils.createNavButton("layout-dashboard", "Dashboard", true);
        dashBtn.addActionListener(e -> switchTo(DASHBOARD, dashBtn));
        navButtons.add(dashBtn);
        sidebar.add(dashBtn);
        sidebar.add(UIUtils.vSpace(4));

        JButton addBtn = UIUtils.createNavButton("circle-plus", "Add Expense", false);
        addBtn.addActionListener(e -> {
            expenseFormPanel.resetForm();
            switchTo(ADD_EXPENSE, addBtn);
        });
        navButtons.add(addBtn);
        sidebar.add(addBtn);
        sidebar.add(UIUtils.vSpace(4));

        JButton viewBtn = UIUtils.createNavButton("list", "View Expenses", false);
        viewBtn.addActionListener(e -> {
            expenseTablePanel.refresh();
            switchTo(VIEW_EXPENSES, viewBtn);
        });
        navButtons.add(viewBtn);
        sidebar.add(viewBtn);
        sidebar.add(UIUtils.vSpace(4));

        JButton budgetBtn = UIUtils.createNavButton("scale", "Budget", false);
        budgetBtn.addActionListener(e -> {
            budgetPanel.refresh();
            switchTo(BUDGET, budgetBtn);
        });
        navButtons.add(budgetBtn);
        sidebar.add(budgetBtn);

        // ── Spacer ────────────────────────────────────────────
        sidebar.add(Box.createVerticalGlue());

        // ── User Info ─────────────────────────────────────────
        UIUtils.RoundedPanel userPanel = new UIUtils.RoundedPanel(16, UIUtils.BG_SURFACE);
        userPanel.setLayout(new BoxLayout(userPanel, BoxLayout.X_AXIS));
        userPanel.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(16, UIUtils.BORDER_COLOR),
                new EmptyBorder(8, 14, 8, 14)));
        userPanel.setMaximumSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 54));
        userPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Avatar circle
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIUtils.ACCENT);
                g2.fillOval(0, 0, 32, 32);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String initial = user.getUsername().substring(0, 1).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initial,
                        (32 - fm.stringWidth(initial)) / 2,
                        (32 + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(32, 32));

        JLabel userName = UIUtils.createLabel(user.getUsername(),
                UIUtils.FONT_BOLD, UIUtils.TEXT_PRIMARY);

        userPanel.add(avatar);
        userPanel.add(UIUtils.hSpace(14));
        userPanel.add(userName);

        // Stretch wrapper to fill sidebar width
        JPanel userWrapper = new JPanel(new BorderLayout());
        userWrapper.setOpaque(false);
        userWrapper.setBorder(new EmptyBorder(0, 10, 0, 10));
        userWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        userWrapper.setMaximumSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 60));
        userWrapper.add(userPanel, BorderLayout.CENTER);
        sidebar.add(userWrapper);

        // ── Logout Button ─────────────────────────────────────
        JButton logoutBtn = UIUtils.createPrimaryButton("Logout");
        logoutBtn.setIcon(UIUtils.getIcon("log-out", 18, Color.WHITE));
        logoutBtn.setIconTextGap(10);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.setMaximumSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 44));

        logoutBtn.addActionListener(e -> {
            UIUtils.setupOptionPaneDefaults();
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?",
                    "Confirm Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginForm().setVisible(true);
            }
        });

        // Stretch wrapper to fill sidebar width
        JPanel logoutWrapper = new JPanel(new BorderLayout());
        logoutWrapper.setOpaque(false);
        logoutWrapper.setBorder(new EmptyBorder(0, 10, 0, 10));
        logoutWrapper.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutWrapper.setMaximumSize(new Dimension(UIUtils.SIDEBAR_WIDTH, 50));
        logoutWrapper.add(logoutBtn, BorderLayout.CENTER);

        sidebar.add(UIUtils.vSpace(12));
        sidebar.add(logoutWrapper);
        sidebar.add(UIUtils.vSpace(16));

        return sidebar;
    }

    // ══════════════════════════════════════════════════════════
    // NAVIGATION
    // ══════════════════════════════════════════════════════════

    /** Switches the visible panel and updates the active nav state. */
    private void switchTo(String panelName, JButton activeBtn) {
        cardLayout.show(contentArea, panelName);
        for (JButton btn : navButtons) {
            btn.putClientProperty("active", "false");
        }
        activeBtn.putClientProperty("active", "true");
        // Repaint sidebar to update active indicator
        for (JButton btn : navButtons)
            btn.repaint();
    }

    // ══════════════════════════════════════════════════════════
    // CROSS-PANEL ACTIONS
    // ══════════════════════════════════════════════════════════

    /** Refreshes all panels (called after data modifications). */
    public void refreshAll() {
        dashboardPanel.refresh();
        expenseTablePanel.refresh();
        budgetPanel.refresh();
    }

    /** Navigates to the expense form in edit mode. */
    public void editExpense(Expense expense) {
        expenseFormPanel.setEditExpense(expense);
        // Find the Add Expense button and activate it
        if (navButtons.size() > 1) {
            switchTo(ADD_EXPENSE, navButtons.get(1));
        }
    }
}
