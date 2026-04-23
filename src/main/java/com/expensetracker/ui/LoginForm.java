package com.expensetracker.ui;

import com.expensetracker.db.DBConnection;
import com.expensetracker.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.geom.RoundRectangle2D;

/**
 * Login screen – the first window the user sees.
 * Features a dark-themed card centred on a gradient background
 * with a modern, clean layout.
 */
public class LoginForm extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginBtn;
    private JButton registerLink;

    public LoginForm() {
        setTitle("Expense Tracker – Sign In");
        setSize(480, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setUndecorated(false);
        UIUtils.centerOnScreen(this);

        // Main background panel with gradient
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
        root.setBackground(UIUtils.BG_DARK);

        // Login card
        UIUtils.RoundedPanel card = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
        card.setPreferredSize(new Dimension(400, 480));

        // ── Logo / Title ──────────────────────────────────────
        JLabel logoIcon = new JLabel("\uD83D\uDCB0");  // 💰
        logoIcon.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 42));
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = UIUtils.createLabel("Expensify",
                new Font("Parisienne", Font.PLAIN, 42), UIUtils.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = UIUtils.createLabel("Sign in to your account",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Fields ────────────────────────────────────────────
        JLabel userLabel = UIUtils.createLabel("Username",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        userLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        userLabel.setHorizontalAlignment(SwingConstants.LEFT);

        usernameField = UIUtils.createStyledTextField("Enter your username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel passLabel = UIUtils.createLabel("Password",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        passLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        passLabel.setHorizontalAlignment(SwingConstants.LEFT);

        passwordField = UIUtils.createStyledPasswordField("Enter your password");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, UIUtils.FIELD_HEIGHT));
        passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Login Button ──────────────────────────────────────
        loginBtn = UIUtils.createPrimaryButton("Sign In");
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.addActionListener(this::handleLogin);

        // ── Register Link ─────────────────────────────────────
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        linkPanel.setOpaque(false);
        linkPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noAccount = UIUtils.createLabel("Don't have an account? ",
                UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        registerLink = new JButton("Register");
        registerLink.setFont(UIUtils.FONT_SMALL);
        registerLink.setForeground(UIUtils.ACCENT);
        registerLink.setContentAreaFilled(false);
        registerLink.setBorderPainted(false);
        registerLink.setFocusPainted(false);
        registerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerLink.addActionListener(e -> {
            dispose();
            new RegisterForm().setVisible(true);
        });
        linkPanel.add(noAccount);
        linkPanel.add(registerLink);

        // ── Assemble Card ─────────────────────────────────────
        card.add(logoIcon);
        card.add(UIUtils.vSpace(8));
        card.add(title);
        card.add(UIUtils.vSpace(4));
        card.add(subtitle);
        card.add(UIUtils.vSpace(30));
        card.add(userLabel);
        card.add(UIUtils.vSpace(6));
        card.add(usernameField);
        card.add(UIUtils.vSpace(16));
        card.add(passLabel);
        card.add(UIUtils.vSpace(6));
        card.add(passwordField);
        card.add(UIUtils.vSpace(28));
        card.add(loginBtn);
        card.add(UIUtils.vSpace(20));
        card.add(linkPanel);

        root.add(card);
        setContentPane(root);

        // Enter key triggers login
        getRootPane().setDefaultButton(loginBtn);
    }

    /** Handles the login action — validates input and authenticates. */
    private void handleLogin(ActionEvent e) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Input validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }

        // Authenticate
        User user = DBConnection.getInstance().loginUser(username, password);
        if (user != null) {
            dispose();
            new MainFrame(user).setVisible(true);
        } else {
            showError("Invalid username or password.");
        }
    }

    /** Shows an error message dialog styled to the dark theme. */
    private void showError(String message) {
        UIUtils.setupOptionPaneDefaults();
        JOptionPane.showMessageDialog(this, message,
                "Login Failed", JOptionPane.ERROR_MESSAGE);
    }
}
