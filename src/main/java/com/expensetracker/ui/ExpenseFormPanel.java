package com.expensetracker.ui;

import com.expensetracker.db.DBConnection;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Panel for adding or editing an expense.
 * Provides a clean form with input validation, category selector,
 * and styled submit/cancel buttons.
 */
public class ExpenseFormPanel extends JPanel {

    private final User user;
    private final MainFrame parent;
    private final DBConnection db = DBConnection.getInstance();

    // Form fields
    private JTextField amountField;
    private JComboBox<String> categoryBox;
    private JTextField dateField;
    private JTextField descriptionField;
    private JButton submitBtn;
    private JButton clearBtn;

    // If editing an existing expense
    private Expense editingExpense = null;

    public ExpenseFormPanel(User user, MainFrame parent) {
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

    /** Pre-fills the form for editing an existing expense. */
    public void setEditExpense(Expense expense) {
        this.editingExpense = expense;
        amountField.setText(String.valueOf(expense.getAmount()));
        categoryBox.setSelectedItem(expense.getCategory());
        dateField.setText(expense.getDate());
        descriptionField.setText(expense.getDescription());
        submitBtn.setText(editingExpense != null ? "Update Expense" : "Add Expense");
        repaint();
    }

    /** Resets the form to its default (add) state. */
    public void resetForm() {
        editingExpense = null;
        amountField.setText("");
        categoryBox.setSelectedIndex(0);
        dateField.setText(LocalDate.now().toString());
        descriptionField.setText("");
        submitBtn.setText("Add Expense");
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Page Header ───────────────────────────────────────
        JLabel pageTitle = UIUtils.createLabel(
                "Add Expense", UIUtils.FONT_TITLE, UIUtils.TEXT_PRIMARY);
        pageTitle.setIcon(UIUtils.getIcon("circle-plus", 28, UIUtils.TEXT_PRIMARY));
        pageTitle.setIconTextGap(12);
        pageTitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(pageTitle);
        content.add(UIUtils.vSpace(6));
        JLabel pageSub = UIUtils.createLabel(
                "Record a new expense to keep track of your spending.",
                UIUtils.FONT_SUBTITLE, UIUtils.TEXT_SECONDARY);
        pageSub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(pageSub);
        content.add(UIUtils.vSpace(28));

        // ── Form Card ─────────────────────────────────────────
        UIUtils.RoundedPanel formCard = new UIUtils.RoundedPanel(
                UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));
        formCard.setBorder(new EmptyBorder(32, 36, 32, 36));
        formCard.setMaximumSize(new Dimension(600, 520));
        formCard.setAlignmentX(LEFT_ALIGNMENT);

        // Amount
        formCard.add(createFieldLabel("Amount (\u20B9)"));
        formCard.add(UIUtils.vSpace(6));
        amountField = UIUtils.createStyledTextField("Enter amount (e.g. 250.00)");
        amountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        amountField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(amountField);
        formCard.add(UIUtils.vSpace(18));

        // Category
        formCard.add(createFieldLabel("Category"));
        formCard.add(UIUtils.vSpace(6));
        categoryBox = UIUtils.createStyledComboBox(UIUtils.CATEGORIES);
        categoryBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        categoryBox.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(categoryBox);
        formCard.add(UIUtils.vSpace(18));

        // Date
        formCard.add(createFieldLabel("Date (YYYY-MM-DD)"));
        formCard.add(UIUtils.vSpace(6));
        dateField = UIUtils.createStyledTextField("e.g. 2026-04-23");
        dateField.setText(LocalDate.now().toString());
        dateField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        dateField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(dateField);
        formCard.add(UIUtils.vSpace(18));

        // Description
        formCard.add(createFieldLabel("Description (optional)"));
        formCard.add(UIUtils.vSpace(6));
        descriptionField = UIUtils.createStyledTextField("What was this expense for?");
        descriptionField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        descriptionField.setAlignmentX(LEFT_ALIGNMENT);
        formCard.add(descriptionField);
        formCard.add(UIUtils.vSpace(28));

        // Buttons
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(LEFT_ALIGNMENT);

        submitBtn = UIUtils.createPrimaryButton("Add Expense");
        submitBtn.setPreferredSize(new Dimension(180, 44));
        submitBtn.addActionListener(this::handleSubmit);

        clearBtn = UIUtils.createGhostButton("Clear");
        clearBtn.setPreferredSize(new Dimension(120, 44));
        clearBtn.addActionListener(e -> resetForm());

        btnRow.add(submitBtn);
        btnRow.add(clearBtn);
        formCard.add(btnRow);

        content.add(formCard);

        JScrollPane scroll = UIUtils.createStyledScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    /** Creates a styled field label. */
    private JLabel createFieldLabel(String text) {
        JLabel lbl = UIUtils.createLabel(text, UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    /** Validates input and either adds or updates an expense. */
    private void handleSubmit(ActionEvent e) {
        // ── Validate Amount ───────────────────────────────────
        String amountStr = amountField.getText().trim();
        if (amountStr.isEmpty()) {
            showError("Amount is required.");
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid positive number for amount.");
            return;
        }

        // ── Validate Date ─────────────────────────────────────
        String dateStr = dateField.getText().trim();
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException ex) {
            showError("Invalid date format. Use YYYY-MM-DD.");
            return;
        }

        // ── Build Expense Object ──────────────────────────────
        String category    = (String) categoryBox.getSelectedItem();
        String description = descriptionField.getText().trim();

        Expense expense;
        boolean success;

        if (editingExpense != null) {
            // Update mode
            expense = new Expense(
                    editingExpense.getExpenseId(), user.getUserId(),
                    amount, category, dateStr, description);
            success = db.updateExpense(expense);
        } else {
            // Add mode
            expense = new Expense(user.getUserId(), amount, category, dateStr, description);
            success = db.addExpense(expense);
        }

        UIUtils.setupOptionPaneDefaults();
        if (success) {
            String msg = editingExpense != null
                    ? "Expense updated successfully!"
                    : "Expense added successfully!";
            JOptionPane.showMessageDialog(this, msg, "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            // Refresh other panels
            parent.refreshAll();
        } else {
            showError("Failed to save expense. Please try again.");
        }
    }

    private void showError(String message) {
        UIUtils.setupOptionPaneDefaults();
        JOptionPane.showMessageDialog(this, message,
                "Validation Error", JOptionPane.WARNING_MESSAGE);
    }
}
