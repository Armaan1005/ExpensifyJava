package com.expensetracker.ui;

import com.expensetracker.db.DBConnection;
import com.expensetracker.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Budget settings panel — allows the user to set a monthly budget
 * and see a visual progress indicator of their current spending.
 */
public class BudgetPanel extends JPanel {

    private final User user;
    private final MainFrame parent;
    private final DBConnection db = DBConnection.getInstance();
    private JTextField budgetField;

    public BudgetPanel(User user, MainFrame parent) {
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

    /** Rebuilds UI on data changes. */
    public void refresh() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Page Header ───────────────────────────────────────
        JLabel title = UIUtils.createLabel(
                "Budget Settings", UIUtils.FONT_TITLE, UIUtils.TEXT_PRIMARY);
        title.setIcon(UIUtils.getIcon("scale", 28, UIUtils.TEXT_PRIMARY));
        title.setIconTextGap(12);
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(UIUtils.vSpace(6));

        JLabel sub = UIUtils.createLabel(
                "Set a monthly budget to keep your spending in check.",
                UIUtils.FONT_SUBTITLE, UIUtils.TEXT_SECONDARY);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub);
        content.add(UIUtils.vSpace(28));

        // ── Budget Input Card ─────────────────────────────────
        UIUtils.RoundedPanel inputCard = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        inputCard.setLayout(new BoxLayout(inputCard, BoxLayout.Y_AXIS));
        inputCard.setBorder(new EmptyBorder(28, 32, 28, 32));
        inputCard.setMaximumSize(new Dimension(500, 200));
        inputCard.setAlignmentX(LEFT_ALIGNMENT);

        JLabel fieldLabel = UIUtils.createLabel("Monthly Budget (\u20B9)",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        fieldLabel.setAlignmentX(LEFT_ALIGNMENT);
        inputCard.add(fieldLabel);
        inputCard.add(UIUtils.vSpace(6));

        budgetField = UIUtils.createStyledTextField("Enter your monthly budget");
        double currentBudget = db.getBudget(user.getUserId());
        if (currentBudget > 0) {
            budgetField.setText(String.valueOf(currentBudget));
        }
        budgetField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        budgetField.setAlignmentX(LEFT_ALIGNMENT);
        inputCard.add(budgetField);
        inputCard.add(UIUtils.vSpace(20));

        JButton saveBtn = UIUtils.createPrimaryButton("Save Budget");
        saveBtn.setPreferredSize(new Dimension(180, 44));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.addActionListener(this::handleSave);
        inputCard.add(saveBtn);

        content.add(inputCard);
        content.add(UIUtils.vSpace(24));

        // ── Budget Progress Card ──────────────────────────────
        if (currentBudget > 0) {
            double monthlySpent = db.getMonthlyExpenses(user.getUserId());
            double pct = Math.min((monthlySpent / currentBudget) * 100, 100);
            double overAmount = monthlySpent - currentBudget;

            Color progressColor;
            String statusText;
            if (pct >= 100) {
                progressColor = UIUtils.DANGER;
                statusText = "Over budget by " + UIUtils.formatCurrency(overAmount) + "!";
            } else if (pct >= 80) {
                progressColor = UIUtils.WARNING;
                statusText = "Approaching budget limit (" + String.format("%.0f%%", pct) + " used)";
            } else {
                progressColor = UIUtils.SUCCESS;
                statusText = "On track! " + String.format("%.0f%%", pct) + " of budget used";
            }

            UIUtils.RoundedPanel progressCard = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
            progressCard.setLayout(new BoxLayout(progressCard, BoxLayout.Y_AXIS));
            progressCard.setBorder(new EmptyBorder(28, 32, 28, 32));
            progressCard.setMaximumSize(new Dimension(500, 260));
            progressCard.setAlignmentX(LEFT_ALIGNMENT);

            JLabel progressTitle = UIUtils.createLabel("This Month's Progress",
                    UIUtils.FONT_HEADER, UIUtils.TEXT_PRIMARY);
            progressTitle.setAlignmentX(LEFT_ALIGNMENT);
            progressCard.add(progressTitle);
            progressCard.add(UIUtils.vSpace(16));

            // Spent / Budget values
            JPanel valRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            valRow.setOpaque(false);
            valRow.setAlignmentX(LEFT_ALIGNMENT);
            JLabel spentLabel = UIUtils.createLabel(
                    UIUtils.formatCurrency(monthlySpent),
                    UIUtils.FONT_BIG_NUM, progressColor);
            JLabel ofLabel = UIUtils.createLabel(
                    "  of  " + UIUtils.formatCurrency(currentBudget),
                    UIUtils.FONT_SUBTITLE, UIUtils.TEXT_SECONDARY);
            valRow.add(spentLabel);
            valRow.add(ofLabel);
            progressCard.add(valRow);
            progressCard.add(UIUtils.vSpace(16));

            // Progress bar
            Color finalProgressColor = progressColor;
            double finalPct = pct;
            JPanel progressBar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();
                    int arc = 10;

                    // Track
                    g2.setColor(UIUtils.BORDER_COLOR);
                    g2.fill(new RoundRectangle2D.Double(0, 0, w, h, arc, arc));

                    // Fill
                    int fillW = (int) (w * finalPct / 100);
                    if (fillW > 0) {
                        GradientPaint gp = new GradientPaint(
                                0, 0, finalProgressColor.brighter(),
                                fillW, 0, finalProgressColor);
                        g2.setPaint(gp);
                        g2.fill(new RoundRectangle2D.Double(0, 0, fillW, h, arc, arc));
                    }

                    g2.dispose();
                }
            };
            progressBar.setOpaque(false);
            progressBar.setPreferredSize(new Dimension(Integer.MAX_VALUE, 14));
            progressBar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 14));
            progressBar.setAlignmentX(LEFT_ALIGNMENT);
            progressCard.add(progressBar);
            progressCard.add(UIUtils.vSpace(12));

            // Status text
            JLabel status = UIUtils.createLabel(statusText,
                    UIUtils.FONT_BOLD, progressColor);
            status.setAlignmentX(LEFT_ALIGNMENT);
            progressCard.add(status);

            // Remaining
            if (pct < 100) {
                JLabel remaining = UIUtils.createLabel(
                        "Remaining: " + UIUtils.formatCurrency(currentBudget - monthlySpent),
                        UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);
                remaining.setAlignmentX(LEFT_ALIGNMENT);
                progressCard.add(UIUtils.vSpace(4));
                progressCard.add(remaining);
            }

            content.add(progressCard);
        }

        JScrollPane scroll = UIUtils.createStyledScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);
    }

    /** Saves the budget value. */
    private void handleSave(ActionEvent e) {
        String text = budgetField.getText().trim();
        if (text.isEmpty()) {
            showError("Please enter a budget amount.");
            return;
        }
        double budget;
        try {
            budget = Double.parseDouble(text);
            if (budget < 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Please enter a valid non-negative number.");
            return;
        }

        boolean success = db.updateBudget(user.getUserId(), budget);
        UIUtils.setupOptionPaneDefaults();
        if (success) {
            user.setMonthlyBudget(budget);
            JOptionPane.showMessageDialog(this,
                    budget > 0 ? "Budget set to " + UIUtils.formatCurrency(budget) + " per month."
                            : "Budget has been removed.",
                    "Budget Updated", JOptionPane.INFORMATION_MESSAGE);
            refresh();
            parent.refreshAll();
        } else {
            showError("Failed to save budget.");
        }
    }

    private void showError(String message) {
        UIUtils.setupOptionPaneDefaults();
        JOptionPane.showMessageDialog(this, message,
                "Error", JOptionPane.WARNING_MESSAGE);
    }
}
