package com.expensetracker.ui;

import com.expensetracker.db.DBConnection;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Panel displaying all expenses in a styled JTable with filter controls,
 * search, and action buttons (edit/delete).
 */
public class ExpenseTablePanel extends JPanel {

    private final User user;
    private final MainFrame parent;
    private final DBConnection db = DBConnection.getInstance();

    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> filterCategoryBox;
    private JTextField filterStartDate;
    private JTextField filterEndDate;
    private JTextField searchField;

    public ExpenseTablePanel(User user, MainFrame parent) {
        this.user = user;
        this.parent = parent;
        setOpaque(false);
        setLayout(new BorderLayout());
        buildUI();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0, 0, UIUtils.BG_DARK,
                0, getHeight(), UIUtils.BG_CARD);
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
    }

    /** Reloads data from the database. */
    public void refresh() {
        loadData();
    }

    private void buildUI() {
        JPanel content = new JPanel(new BorderLayout(0, 16));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Header + Search Row ───────────────────────────────
        JPanel headerRow = new JPanel(new BorderLayout(16, 0));
        headerRow.setOpaque(false);

        JLabel pageTitle = UIUtils.createLabel(
                "View Expenses", UIUtils.FONT_TITLE, UIUtils.TEXT_PRIMARY);
        pageTitle.setIcon(UIUtils.getIcon("list", 28, UIUtils.TEXT_PRIMARY));
        pageTitle.setIconTextGap(12);
        headerRow.add(pageTitle, BorderLayout.WEST);

        // Search field
        searchField = UIUtils.createStyledTextField("Search expenses...");
        searchField.setPreferredSize(new Dimension(250, UIUtils.FIELD_HEIGHT));
        searchField.addActionListener(e -> filterData());
        headerRow.add(searchField, BorderLayout.EAST);

        // ── Filter Controls Card ──────────────────────────────
        JPanel centre = new JPanel(new BorderLayout(0, 12));
        centre.setOpaque(false);

        UIUtils.RoundedPanel filterCard = new UIUtils.RoundedPanel(12, UIUtils.BG_SURFACE);
        filterCard.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        filterCard.setBorder(new EmptyBorder(8, 16, 8, 16));

        JLabel filterLabel = UIUtils.createLabel("Filters:",
                UIUtils.FONT_BOLD, UIUtils.TEXT_SECONDARY);
        filterCard.add(filterLabel);

        // Category filter
        String[] filterCategories = new String[UIUtils.CATEGORIES.length + 1];
        filterCategories[0] = "All";
        System.arraycopy(UIUtils.CATEGORIES, 0, filterCategories, 1, UIUtils.CATEGORIES.length);
        filterCategoryBox = UIUtils.createStyledComboBox(filterCategories);
        filterCategoryBox.setPreferredSize(new Dimension(180, 36));
        filterCategoryBox.addActionListener(e -> filterData());
        filterCard.add(filterCategoryBox);

        filterCard.add(UIUtils.createLabel("From:", UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY));
        filterStartDate = UIUtils.createStyledTextField("YYYY-MM-DD");
        filterStartDate.setPreferredSize(new Dimension(140, 36));
        filterCard.add(filterStartDate);

        filterCard.add(UIUtils.createLabel("To:", UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY));
        filterEndDate = UIUtils.createStyledTextField("YYYY-MM-DD");
        filterEndDate.setPreferredSize(new Dimension(140, 36));
        filterCard.add(filterEndDate);

        JButton applyBtn = UIUtils.createPrimaryButton("Apply");
        applyBtn.setPreferredSize(new Dimension(90, 36));
        applyBtn.addActionListener(e -> filterData());
        filterCard.add(applyBtn);

        JButton resetBtn = UIUtils.createGhostButton("Reset");
        resetBtn.setPreferredSize(new Dimension(80, 36));
        resetBtn.addActionListener(e -> {
            filterCategoryBox.setSelectedIndex(0);
            filterStartDate.setText("");
            filterEndDate.setText("");
            searchField.setText("");
            loadData();
        });
        filterCard.add(resetBtn);

        centre.add(filterCard, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────
        String[] columns = {"ID", "Amount (Rs.)", "Category", "Date", "Description"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;  // read-only
            }
        };
        table = new JTable(tableModel);
        table.setRowSorter(new TableRowSorter<>(tableModel));
        styleTable();

        JScrollPane scrollPane = UIUtils.createStyledScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(800, 400));
        centre.add(scrollPane, BorderLayout.CENTER);

        // ── Action Buttons Row ────────────────────────────────
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setBorder(new EmptyBorder(8, 0, 0, 0));

        JButton editBtn = UIUtils.createPrimaryButton("Edit");
        editBtn.setIcon(UIUtils.getIcon("pencil", 18, Color.WHITE));
        editBtn.setIconTextGap(8);
        editBtn.setPreferredSize(new Dimension(130, 40));
        editBtn.addActionListener(this::handleEdit);

        JButton deleteBtn = UIUtils.createDangerButton("Delete");
        deleteBtn.setIcon(UIUtils.getIcon("trash-2", 18, Color.WHITE));
        deleteBtn.setIconTextGap(8);
        deleteBtn.setPreferredSize(new Dimension(130, 40));
        deleteBtn.addActionListener(this::handleDelete);

        JButton refreshBtn = UIUtils.createGhostButton("Refresh");
        refreshBtn.setIcon(UIUtils.getIcon("refresh-cw", 18, UIUtils.TEXT_PRIMARY));
        refreshBtn.setIconTextGap(8);
        refreshBtn.setPreferredSize(new Dimension(130, 40));
        refreshBtn.addActionListener(e -> loadData());

        actionRow.add(editBtn);
        actionRow.add(deleteBtn);
        actionRow.add(refreshBtn);

        centre.add(actionRow, BorderLayout.SOUTH);

        content.add(centre, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        // Load initial data
        loadData();
    }

    /** Applies the dark theme to the JTable. */
    private void styleTable() {
        table.setFont(UIUtils.FONT_REGULAR);
        table.setForeground(UIUtils.TEXT_PRIMARY);
        table.setBackground(UIUtils.BG_SURFACE);
        table.setSelectionBackground(new Color(64, 138, 113, 60));
        table.setSelectionForeground(UIUtils.TEXT_PRIMARY);
        table.setGridColor(UIUtils.BORDER_COLOR);
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setFillsViewportHeight(true);

        // Hide the ID column (but keep it in model for reference)
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setPreferredWidth(0);

        // Custom cell renderer for alternating rows and category colours
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(UIUtils.FONT_REGULAR);
                setBorder(new EmptyBorder(4, 12, 4, 12));

                if (isSelected) {
                    setBackground(new Color(64, 138, 113, 60));
                    setForeground(UIUtils.TEXT_PRIMARY);
                } else {
                    setBackground(row % 2 == 0 ? UIUtils.BG_SURFACE : UIUtils.BG_CARD);
                    setForeground(UIUtils.TEXT_PRIMARY);
                }

                // Colour the category column
                if (column == 2 && value != null) {
                    setForeground(UIUtils.getCategoryColor(value.toString()));
                }

                // Right-align amount
                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setForeground(UIUtils.TEAL);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return this;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Header styling
        JTableHeader header = table.getTableHeader();
        header.setFont(UIUtils.FONT_BOLD);
        header.setForeground(UIUtils.TEXT_SECONDARY);
        header.setBackground(UIUtils.BG_CARD);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.BORDER_COLOR));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setFont(UIUtils.FONT_BOLD);
                setForeground(UIUtils.TEXT_SECONDARY);
                setBackground(UIUtils.BG_CARD);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, UIUtils.BORDER_COLOR),
                        new EmptyBorder(8, 12, 8, 12)
                ));
                return this;
            }
        });
    }

    /** Loads all expenses (unfiltered) into the table. */
    private void loadData() {
        List<Expense> expenses = db.getExpenses(user.getUserId());
        populateTable(expenses);
    }

    /** Loads filtered expenses based on current filter values. */
    private void filterData() {
        String category  = (String) filterCategoryBox.getSelectedItem();
        String startDate = filterStartDate.getText().trim();
        String endDate   = filterEndDate.getText().trim();
        String search    = searchField.getText().trim().toLowerCase();

        List<Expense> expenses = db.getExpenses(user.getUserId(),
                category, startDate.isEmpty() ? null : startDate,
                endDate.isEmpty() ? null : endDate);

        // Apply text search filter
        if (!search.isEmpty()) {
            expenses.removeIf(e ->
                    !e.getCategory().toLowerCase().contains(search)
                            && !e.getDescription().toLowerCase().contains(search)
                            && !e.getDate().contains(search)
                            && !String.valueOf(e.getAmount()).contains(search));
        }

        populateTable(expenses);
    }

    /** Populates the table model from a list of expenses. */
    private void populateTable(List<Expense> expenses) {
        tableModel.setRowCount(0);
        for (Expense exp : expenses) {
            tableModel.addRow(new Object[]{
                    exp.getExpenseId(),
                    UIUtils.formatCurrency(exp.getAmount()),
                    exp.getCategory(),
                    exp.getDate(),
                    exp.getDescription()
            });
        }
    }

    /** Handles editing the selected expense row. */
    private void handleEdit(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showInfo("Please select an expense to edit.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(selectedRow);
        int expenseId = (int) tableModel.getValueAt(modelRow, 0);

        // Find the expense in the loaded data
        List<Expense> expenses = db.getExpenses(user.getUserId());
        for (Expense exp : expenses) {
            if (exp.getExpenseId() == expenseId) {
                parent.editExpense(exp);
                return;
            }
        }
    }

    /** Handles deleting the selected expense row. */
    private void handleDelete(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showInfo("Please select an expense to delete.");
            return;
        }

        UIUtils.setupOptionPaneDefaults();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this expense?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            int expenseId = (int) tableModel.getValueAt(modelRow, 0);
            boolean success = db.deleteExpense(expenseId, user.getUserId());
            if (success) {
                loadData();
                parent.refreshAll();
                JOptionPane.showMessageDialog(this, "Expense deleted.",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                showError("Failed to delete expense.");
            }
        }
    }

    private void showInfo(String message) {
        UIUtils.setupOptionPaneDefaults();
        JOptionPane.showMessageDialog(this, message,
                "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        UIUtils.setupOptionPaneDefaults();
        JOptionPane.showMessageDialog(this, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }
}
