package com.expensetracker.ui;

import com.expensetracker.db.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Registration screen – allows new users to create an account.
 * Mirrors the LoginForm aesthetic with a centred card on a gradient background.
 */
public class RegisterForm extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton registerBtn;

    public RegisterForm() {
        setTitle("Expense Tracker – Create Account");
        setSize(480, 660);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        UIUtils.centerOnScreen(this);

        // Gradient background
        JPanel root = new JPanel(new GridBagLayout()) {
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
            }
        };

        // Registration card
        UIUtils.RoundedPanel card = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(400, 540));

        // ── Header ────────────────────────────────────────────
        JLabel icon = new JLabel("\u270D");  // ✍
        icon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 42));
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = UIUtils.createLabel("Create Account",
                UIUtils.FONT_TITLE, UIUtils.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = UIUtils.createLabel("Start tracking your expenses today",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Username ──────────────────────────────────────────
        JLabel userLabel = UIUtils.createLabel("Username",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        userLabel.setHorizontalAlignment(SwingConstants.LEFT);

        usernameField = UIUtils.createStyledTextField("Choose a username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Password ──────────────────────────────────────────
        JLabel passLabel = UIUtils.createLabel("Password",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        passLabel.setHorizontalAlignment(SwingConstants.LEFT);

        passwordField = UIUtils.createStyledPasswordField("Create a password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Confirm Password ──────────────────────────────────
        JLabel confirmLabel = UIUtils.createLabel("Confirm Password",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        confirmLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        confirmLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        confirmLabel.setHorizontalAlignment(SwingConstants.LEFT);

        confirmPasswordField = UIUtils.createStyledPasswordField("Re-enter your password");
        confirmPasswordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        confirmPasswordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Register Button ───────────────────────────────────
        registerBtn = UIUtils.createPrimaryButton("Create Account");
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        registerBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        registerBtn.addActionListener(this::handleRegister);

        // ── Login Link ────────────────────────────────────────
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setOpaque(false);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel haveAccount = UIUtils.createLabel("Already have an account? ",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        JButton loginLink = new JButton("Sign In");
        loginLink.setFont(UIUtils.FONT_SMALL);
        loginLink.setForeground(UIUtils.TEAL);
        loginLink.setContentAreaFilled(false);
        loginLink.setBorderPainted(false);
        loginLink.setFocusPainted(false);
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addActionListener(e -> {
            dispose();
            new LoginForm().setVisible(true);
        });
        linkPanel.add(haveAccount);
        linkPanel.add(loginLink);

        // ── Assemble Card ─────────────────────────────────────
        card.add(icon);
        card.add(UIUtils.vSpace(8));
        card.add(title);
        card.add(UIUtils.vSpace(4));
        card.add(subtitle);
        card.add(UIUtils.vSpace(28));
        card.add(userLabel);
        card.add(UIUtils.vSpace(6));
        card.add(usernameField);
        card.add(UIUtils.vSpace(14));
        card.add(passLabel);
        card.add(UIUtils.vSpace(6));
        card.add(passwordField);
        card.add(UIUtils.vSpace(14));
        card.add(confirmLabel);
        card.add(UIUtils.vSpace(6));
        card.add(confirmPasswordField);
        card.add(UIUtils.vSpace(28));
        card.add(registerBtn);
        card.add(UIUtils.vSpace(18));
        card.add(linkPanel);

        root.add(card);
        setContentPane(root);
        getRootPane().setDefaultButton(registerBtn);
    }

    /** Validates inputs and registers a new user. */
    private void handleRegister(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm  = new String(confirmPasswordField.getPassword());

        // Validation
        if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            showError("All fields are required.");
            return;
        }
        if (username.length() < 3) {
            showError("Username must be at least 3 characters.");
            return;
        }
        if (password.length() < 4) {
            showError("Password must be at least 4 characters.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        // Attempt registration
        boolean success = DBConnection.getInstance().registerUser(username, password);
        if (success) {
            UIUtils.setupOptionPaneDefaults();
            JOptionPane.showMessageDialog(this,
                    "Account created successfully!\nYou can now sign in.",
                    "Welcome!", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginForm().setVisible(true);
        } else {
            showError("Username already taken. Please choose another.");
        }
    }

    private void showError(String message) {
        UIUtils.setupOptionPaneDefaults();
        JOptionPane.showMessageDialog(this, message,
                "Registration Error", JOptionPane.ERROR_MESSAGE);
    }
}
