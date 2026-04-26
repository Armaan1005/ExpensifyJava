package com.expensetracker.ui;

import com.expensetracker.db.DBConnection;
import com.expensetracker.model.Split;
import com.expensetracker.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Split Expenses panel — allows users to split expenses with others
 * and manage pending splits.
 */
public class SplitPanel extends JPanel {

    private final User user;
    private final MainFrame parent;
    private final DBConnection db = DBConnection.getInstance();

    private JComboBox<UserWrapper> userCombo;
    private JTextField amountField;
    private JTextField descField;
    private JPanel splitListPanel;

    public SplitPanel(User user, MainFrame parent) {
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

        // ── Header ────────────────────────────────────────────
        JLabel title = UIUtils.createLabel("Split Expenses", UIUtils.FONT_TITLE, UIUtils.TEXT_PRIMARY);
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(UIUtils.vSpace(8));
        
        JLabel subtitle = UIUtils.createLabel("Share costs with other registered users", 
                UIUtils.FONT_SUBTITLE, UIUtils.TEXT_SECONDARY);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        content.add(subtitle);
        content.add(UIUtils.vSpace(24));

        // ── Main Content Split (Form + List) ──────────────────
        JPanel mainRow = new JPanel(new BorderLayout(24, 0));
        mainRow.setOpaque(false);
        mainRow.setAlignmentX(LEFT_ALIGNMENT);

        // Left: Create Split Form
        mainRow.add(createSplitForm(), BorderLayout.WEST);

        // Center: Split History / Pending
        mainRow.add(createSplitList(), BorderLayout.CENTER);

        content.add(mainRow);

        // Wrap in scroll pane
        JScrollPane scroll = UIUtils.createStyledScrollPane(content);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel createSplitForm() {
        UIUtils.RoundedPanel form = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(24, 24, 24, 24));
        form.setPreferredSize(new Dimension(360, 450));
        form.setMaximumSize(new Dimension(360, 450));

        JLabel h = UIUtils.createLabel("Create New Split", UIUtils.FONT_HEADER, UIUtils.ACCENT_LIGHT);
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(h);
        
        Component v1 = UIUtils.vSpace(20);
        ((JComponent)v1).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v1);

        // User Selection
        JLabel userLabel = UIUtils.createLabel("Split With", UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(userLabel);
        
        Component v2 = UIUtils.vSpace(6);
        ((JComponent)v2).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v2);
        
        List<User> users = db.getAllUsers(user.getUserId());
        UserWrapper[] wrappers = users.stream().map(UserWrapper::new).toArray(UserWrapper[]::new);
        userCombo = new JComboBox<>(wrappers);
        userCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        userCombo.setFont(UIUtils.FONT_REGULAR);
        userCombo.setBackground(UIUtils.INPUT_BG);
        userCombo.setForeground(UIUtils.TEXT_PRIMARY);
        userCombo.setBorder(BorderFactory.createCompoundBorder(
                new UIUtils.RoundedBorder(UIUtils.INPUT_ARC, UIUtils.BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        userCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(UIUtils.FONT_REGULAR);
                setBackground(isSelected ? UIUtils.ACCENT : UIUtils.BG_CARD);
                setForeground(UIUtils.TEXT_PRIMARY);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
        userCombo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton("\u25BE");  // ▾
                b.setFont(UIUtils.FONT_REGULAR);
                b.setForeground(UIUtils.TEXT_SECONDARY);
                b.setBackground(UIUtils.INPUT_BG);
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setFocusPainted(false);
                b.setContentAreaFilled(false);
                return b;
            }
        });
        userCombo.setPreferredSize(new Dimension(300, 42));
        userCombo.setMaximumSize(new Dimension(300, 42)); // Fixed width
        form.add(userCombo);
        
        Component v3 = UIUtils.vSpace(16);
        ((JComponent)v3).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v3);

        // Amount
        JLabel amountLabelHint = UIUtils.createLabel("Amount", UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        amountLabelHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(amountLabelHint);
        
        Component v4 = UIUtils.vSpace(6);
        ((JComponent)v4).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v4);
        
        amountField = UIUtils.createStyledTextField("0.00");
        amountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        amountField.setMaximumSize(new Dimension(300, 42)); // Fixed width
        form.add(amountField);
        
        Component v5 = UIUtils.vSpace(16);
        ((JComponent)v5).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v5);

        // Description
        JLabel descLabelHint = UIUtils.createLabel("Description", UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        descLabelHint.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(descLabelHint);
        
        Component v6 = UIUtils.vSpace(6);
        ((JComponent)v6).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v6);
        
        descField = UIUtils.createStyledTextField("e.g. Dinner, Rent...");
        descField.setAlignmentX(Component.LEFT_ALIGNMENT);
        descField.setMaximumSize(new Dimension(300, 42)); // Fixed width
        form.add(descField);
        
        Component v7 = UIUtils.vSpace(24);
        ((JComponent)v7).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v7);

        // Submit Button
        JButton submitBtn = UIUtils.createPrimaryButton("Create Split");
        submitBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Make it full width of the form elements (300px)
        submitBtn.setMaximumSize(new Dimension(300, 44)); 
        submitBtn.setPreferredSize(new Dimension(300, 44));
        submitBtn.addActionListener(e -> handleCreateSplit());
        form.add(submitBtn);

        // Help text
        Component v8 = UIUtils.vSpace(12);
        ((JComponent)v8).setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(v8);
        
        JLabel help = UIUtils.createLabel("The selected user will see this split on their dashboard.", 
                UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);
        help.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(help);

        return form;
    }

    private JPanel createSplitList() {
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);

        JLabel h = UIUtils.createLabel("Split Activity", UIUtils.FONT_HEADER, UIUtils.TEXT_PRIMARY);
        h.setBorder(new EmptyBorder(0, 0, 16, 0));
        container.add(h, BorderLayout.NORTH);

        splitListPanel = new JPanel();
        splitListPanel.setOpaque(false);
        splitListPanel.setLayout(new BoxLayout(splitListPanel, BoxLayout.Y_AXIS));

        List<Split> splits = db.getSplitsForUser(user.getUserId());
        if (splits.isEmpty()) {
            JLabel empty = UIUtils.createLabel("No split activity yet.", UIUtils.FONT_SUBTITLE, UIUtils.TEXT_MUTED);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            splitListPanel.add(UIUtils.vSpace(40));
            splitListPanel.add(empty);
        } else {
            for (Split s : splits) {
                splitListPanel.add(createSplitRow(s));
                splitListPanel.add(UIUtils.vSpace(12));
            }
        }

        container.add(splitListPanel, BorderLayout.CENTER);
        return container;
    }

    private JPanel createSplitRow(Split s) {
        UIUtils.RoundedPanel row = new UIUtils.RoundedPanel(16, UIUtils.BG_SURFACE);
        row.setLayout(new BorderLayout(16, 0));
        row.setBorder(new EmptyBorder(16, 20, 16, 20)); // Increased padding for even spacing
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        boolean isPayer = s.getPayerId() == user.getUserId();
        
        // Icon / Status
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel iconLabel = new JLabel(UIUtils.getIcon(isPayer ? "user" : "coins", 
                24, isPayer ? UIUtils.ACCENT : UIUtils.WARNING));
        left.add(iconLabel);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        String titleText = isPayer ? "You requested " + s.getPayeeName() : s.getPayerName() + " requested you";
        JLabel titleLabel = UIUtils.createLabel(titleText, UIUtils.FONT_BOLD, UIUtils.TEXT_PRIMARY);
        JLabel metaLabel = UIUtils.createLabel(s.getDescription() + "  \u2022  " + s.getDate(), 
                UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);
        
        info.add(titleLabel);
        info.add(metaLabel);
        left.add(info);

        row.add(left, BorderLayout.WEST);

        // Right side: Amount + Action
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        JLabel amountLabel = UIUtils.createLabel(UIUtils.formatCurrency(s.getAmount()), 
                UIUtils.FONT_HEADER, isPayer ? UIUtils.TEXT_PRIMARY : UIUtils.DANGER);
        right.add(amountLabel);

        if (s.getStatus().equals("PENDING")) {
            if (!isPayer) {
                JButton payBtn = UIUtils.createPrimaryButton("Pay Now");
                payBtn.setPreferredSize(new Dimension(100, 32));
                payBtn.addActionListener(e -> handlePay(s));
                right.add(payBtn);
            } else {
                JLabel pending = UIUtils.createLabel("PENDING", UIUtils.FONT_SMALL, UIUtils.WARNING);
                pending.setBorder(BorderFactory.createLineBorder(UIUtils.WARNING, 1));
                right.add(pending);
            }
        } else {
            JLabel paid = UIUtils.createLabel("PAID", UIUtils.FONT_SMALL, UIUtils.SUCCESS);
            right.add(paid);
        }

        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void handleCreateSplit() {
        UserWrapper selected = (UserWrapper) userCombo.getSelectedItem();
        if (selected == null) return;

        String amtStr = amountField.getText().trim();
        String desc = descField.getText().trim();

        if (amtStr.isEmpty()) {
            showError("Please enter an amount.");
            return;
        }

        try {
            double amount = Double.parseDouble(amtStr);
            if (amount <= 0) {
                showError("Amount must be greater than zero.");
                return;
            }

            Split split = new Split(user.getUserId(), selected.user.getUserId(), 
                    amount, desc, LocalDate.now().toString(), "PENDING");
            
            if (db.addSplit(split)) {
                JOptionPane.showMessageDialog(this, "Split request sent to " + selected.user.getUsername());
                amountField.setText("");
                descField.setText("");
                refresh();
                parent.refreshAll();
            } else {
                showError("Failed to create split.");
            }

        } catch (NumberFormatException ex) {
            showError("Invalid amount format.");
        }
    }

    private void handlePay(Split s) {
        UIUtils.setupOptionPaneDefaults();
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Mark " + UIUtils.formatCurrency(s.getAmount()) + " as paid to " + s.getPayerName() + "?",
                "Confirm Payment", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (db.updateSplitStatus(s.getSplitId(), "PAID")) {
                // Also add it as an expense for the payee? 
                // The user requested: "would be able to see their split and pay their amount"
                // Usually "paying" a split means it becomes an expense for you.
                // Let's add it to Expenses table too so it reflects in their budget.
                com.expensetracker.model.Expense exp = new com.expensetracker.model.Expense(
                        user.getUserId(), s.getAmount(), "Split Payment", 
                        LocalDate.now().toString(), "Paid split to " + s.getPayerName() + ": " + s.getDescription()
                );
                db.addExpense(exp);
                
                JOptionPane.showMessageDialog(this, "Payment marked as paid and added to your expenses.");
                refresh();
                parent.refreshAll();
            } else {
                showError("Failed to update status.");
            }
        }
    }

    private void showError(String msg) {
        UIUtils.setupOptionPaneDefaults();
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Helper class for JComboBox to display username but store User object. */
    private static class UserWrapper {
        final User user;
        UserWrapper(User user) { this.user = user; }
        @Override public String toString() { return user.getUsername(); }
    }
}
