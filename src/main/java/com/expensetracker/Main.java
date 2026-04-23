package com.expensetracker;

import com.expensetracker.ui.LoginForm;
import com.expensetracker.ui.UIUtils;

import javax.swing.*;

/**
 * Application entry point.
 * Sets up the system look-and-feel overrides and launches the login screen.
 */
public class Main {

    public static void main(String[] args) {
        // Use system-native anti-aliasing
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Set up dark theme overrides for standard dialogs
        setupGlobalTheme();

        // Launch UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            LoginForm loginForm = new LoginForm();
            loginForm.setVisible(true);
        });
    }

    /**
     * Applies dark-theme UIManager defaults so that standard Swing
     * components (menus, tooltips, dialogs, etc.) match the app design.
     */
    private static void setupGlobalTheme() {
        try {
            // Don't use the system look-and-feel — we draw our own theme
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global colour overrides
        UIManager.put("Panel.background",        UIUtils.BG_SURFACE);
        UIManager.put("OptionPane.background",    UIUtils.BG_SURFACE);
        UIManager.put("OptionPane.messageForeground", UIUtils.TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont",   UIUtils.FONT_REGULAR);
        UIManager.put("Button.background",        UIUtils.ACCENT);
        UIManager.put("Button.foreground",        java.awt.Color.WHITE);
        UIManager.put("Button.font",              UIUtils.FONT_BOLD);
        UIManager.put("Button.focus",             UIUtils.ACCENT);
        UIManager.put("Label.foreground",         UIUtils.TEXT_PRIMARY);
        UIManager.put("Label.font",               UIUtils.FONT_REGULAR);
        UIManager.put("TextField.background",     UIUtils.INPUT_BG);
        UIManager.put("TextField.foreground",     UIUtils.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground",UIUtils.TEXT_PRIMARY);
        UIManager.put("TextArea.background",      UIUtils.INPUT_BG);
        UIManager.put("TextArea.foreground",      UIUtils.TEXT_PRIMARY);
        UIManager.put("ComboBox.background",      UIUtils.INPUT_BG);
        UIManager.put("ComboBox.foreground",      UIUtils.TEXT_PRIMARY);
        UIManager.put("ComboBox.selectionBackground", UIUtils.ACCENT);
        UIManager.put("ComboBox.selectionForeground", java.awt.Color.WHITE);
        UIManager.put("ScrollPane.background",    UIUtils.BG_SURFACE);
        UIManager.put("Viewport.background",      UIUtils.BG_SURFACE);
        UIManager.put("Table.background",         UIUtils.BG_SURFACE);
        UIManager.put("Table.foreground",         UIUtils.TEXT_PRIMARY);
        UIManager.put("Table.selectionBackground",new java.awt.Color(124, 58, 237, 60));
        UIManager.put("Table.selectionForeground",UIUtils.TEXT_PRIMARY);
        UIManager.put("Table.gridColor",          UIUtils.BORDER_COLOR);
        UIManager.put("TableHeader.background",   UIUtils.BG_CARD);
        UIManager.put("TableHeader.foreground",   UIUtils.TEXT_SECONDARY);
        UIManager.put("Separator.foreground",     UIUtils.BORDER_COLOR);
        UIManager.put("ToolTip.background",       UIUtils.BG_CARD);
        UIManager.put("ToolTip.foreground",       UIUtils.TEXT_PRIMARY);
        UIManager.put("ToolTip.font",             UIUtils.FONT_SMALL);
    }
}
