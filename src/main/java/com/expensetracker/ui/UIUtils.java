package com.expensetracker.ui;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import com.formdev.flatlaf.extras.FlatSVGIcon;

/**
 * Central design-system utility class.
 * Defines colours, fonts, and reusable styled Swing components
 * that give the application its modern dark-themed look.
 */
public final class UIUtils {

    private UIUtils() {} // no instances

    // ══════════════════════════════════════════════════════════
    //  COLOUR PALETTE
    // ══════════════════════════════════════════════════════════

    public static final Color BG_DARK       = new Color(9, 20, 19);        // #091413
    public static final Color BG_SURFACE    = new Color(9, 20, 19);        // #091413
    public static final Color BG_CARD       = new Color(40, 90, 72);       // #285A48
    public static final Color BORDER_COLOR  = new Color(64, 138, 113);     // #408A71
    public static final Color ACCENT        = new Color(64, 138, 113);     // #408A71
    public static final Color ACCENT_LIGHT  = new Color(176, 228, 204);    // #B0E4CC
    public static final Color TEAL          = new Color(176, 228, 204);    // #B0E4CC
    public static final Color TEAL_DARK     = new Color(64, 138, 113);     // #408A71
    public static final Color TEXT_PRIMARY  = new Color(255, 255, 255);    // White
    public static final Color TEXT_SECONDARY= new Color(176, 228, 204);    // #B0E4CC
    public static final Color TEXT_MUTED    = new Color(176, 228, 204, 150); // #B0E4CC translucent
    public static final Color SUCCESS       = new Color(63, 185, 80);      // #3fb950
    public static final Color WARNING       = new Color(210, 153, 34);     // #d29922
    public static final Color DANGER        = new Color(248, 81, 73);      // #f85149
    public static final Color INPUT_BG      = new Color(9, 20, 19);        // #091413
    public static final Color SIDEBAR_BG    = new Color(9, 20, 19);        // #091413
    public static final Color HOVER_BG      = new Color(64, 138, 113, 120);

    // Gradient colours for the welcome banner
    public static final Color GRAD_START    = new Color(40, 90, 72);       // #285A48
    public static final Color GRAD_END      = new Color(64, 138, 113);     // #408A71

    // ══════════════════════════════════════════════════════════
    //  SVG ICONS
    // ══════════════════════════════════════════════════════════

    public static FlatSVGIcon getIcon(String name, int size, Color color) {
        java.net.URL url = UIUtils.class.getResource("/icons/" + name + ".svg");
        if (url == null) {
            System.err.println("Icon not found: /icons/" + name + ".svg");
        }
        FlatSVGIcon icon = url != null ? new FlatSVGIcon(url).derive(size, size) 
                                       : new FlatSVGIcon("icons/" + name + ".svg", size, size);
        if (color != null) {
            icon.setColorFilter(new FlatSVGIcon.ColorFilter(c -> color));
        }
        return icon;
    }

    // ══════════════════════════════════════════════════════════
    //  FONTS
    // ══════════════════════════════════════════════════════════

    public static final Font FONT_REGULAR   = new Font("Hedvig Letters Serif 24pt", Font.PLAIN, 14);
    public static final Font FONT_BOLD      = new Font("Hedvig Letters Serif 24pt", Font.BOLD, 14);
    public static final Font FONT_TITLE     = new Font("Hedvig Letters Serif 24pt", Font.BOLD, 26);
    public static final Font FONT_SUBTITLE  = new Font("Hedvig Letters Serif 24pt", Font.PLAIN, 16);
    public static final Font FONT_SMALL     = new Font("Hedvig Letters Serif 24pt", Font.PLAIN, 12);
    public static final Font FONT_HEADER    = new Font("Hedvig Letters Serif 24pt", Font.BOLD, 20);
    public static final Font FONT_BIG_NUM   = new Font("Hedvig Letters Serif 24pt", Font.BOLD, 32);
    public static final Font FONT_NAV       = new Font("Hedvig Letters Serif 24pt", Font.PLAIN, 14);
    public static final Font FONT_ICON      = new Font("Segoe UI Symbol", Font.PLAIN, 18);

    // ══════════════════════════════════════════════════════════
    //  DIMENSIONS
    // ══════════════════════════════════════════════════════════

    public static final int SIDEBAR_WIDTH   = 230;
    public static final int CARD_ARC        = 28;
    public static final int BUTTON_ARC      = 20;
    public static final int INPUT_ARC       = 12;
    public static final int FIELD_HEIGHT    = 42;

    // ══════════════════════════════════════════════════════════
    //  EXPENSE CATEGORIES
    // ══════════════════════════════════════════════════════════

    public static final String[] CATEGORIES = {
            "Food & Dining", "Transportation", "Entertainment",
            "Shopping", "Bills & Utilities", "Health & Fitness",
            "Education", "Travel", "Groceries", "Other"
    };

    /** Returns a unique colour for each category. */
    public static Color getCategoryColor(String category) {
        switch (category) {
            case "Food & Dining":      return new Color(248, 113, 113);  // red
            case "Transportation":     return new Color(96, 165, 250);   // blue
            case "Entertainment":      return new Color(251, 191, 36);   // amber
            case "Shopping":           return new Color(167, 139, 250);  // purple
            case "Bills & Utilities":  return new Color(52, 211, 153);   // emerald
            case "Health & Fitness":   return new Color(244, 114, 182);  // pink
            case "Education":          return new Color(45, 212, 191);   // teal
            case "Travel":             return new Color(251, 146, 60);   // orange
            case "Groceries":          return new Color(74, 222, 128);   // green
            default:                   return TEXT_SECONDARY;
        }
    }

    // ══════════════════════════════════════════════════════════
    //  STYLED COMPONENTS
    // ══════════════════════════════════════════════════════════

    // ── Rounded Panel ─────────────────────────────────────────

    /**
     * A JPanel with rounded corners and an optional background colour.
     */
    public static class RoundedPanel extends JPanel {
        private final int arc;
        private Color bgColor;

        public RoundedPanel(int arc, Color bg) {
            this.arc = arc;
            this.bgColor = bg;
            setOpaque(false);
        }

        public RoundedPanel(int arc) {
            this(arc, BG_CARD);
        }

        public void setBgColor(Color c) { this.bgColor = c; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bgColor);
            g2.fill(new RoundRectangle2D.Double(0, 0,
                    getWidth() - 1, getHeight() - 1, arc, arc));
            // Add a subtle border to lift it
            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(0, 0,
                    getWidth() - 1, getHeight() - 1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Gradient Panel ────────────────────────────────────────

    /** A panel with a horizontal gradient background and rounded corners. */
    public static class GradientPanel extends JPanel {
        private final Color c1, c2;
        private final int arc;

        public GradientPanel(Color start, Color end, int arc) {
            this.c1 = start;
            this.c2 = end;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
            g2.setPaint(gp);
            g2.fill(new RoundRectangle2D.Double(0, 0,
                    getWidth() - 1, getHeight() - 1, arc, arc));
            g2.setColor(BORDER_COLOR);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(0, 0,
                    getWidth() - 1, getHeight() - 1, arc, arc));
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Styled Button ─────────────────────────────────────────

    /**
     * A custom-painted button with rounded corners, configurable
     * colours, and hover/press effects.
     */
    public static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            private boolean pressed = false;
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; pressed = false; repaint(); }
                    @Override public void mousePressed(MouseEvent e) { pressed = true; repaint(); }
                    @Override public void mouseReleased(MouseEvent e){ pressed = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                Color fill = bg;
                if (pressed) fill = bg.darker();
                else if (hovered) fill = bg.brighter();

                g2.setColor(fill);
                g2.fill(new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));

                // Draw text centred
                g2.setColor(fg);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(fg);
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
    }

    /** Primary accent button. */
    public static JButton createPrimaryButton(String text) {
        return createStyledButton(text, ACCENT, Color.WHITE);
    }

    /** Teal / secondary accent button. */
    public static JButton createSecondaryButton(String text) {
        return createStyledButton(text, TEAL, Color.WHITE);
    }

    /** Danger (red) button. */
    public static JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER, Color.WHITE);
    }

    /** Subtle / outline-style button. */
    public static JButton createGhostButton(String text) {
        JButton btn = new JButton(text) {
            private boolean hovered = false;
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                if (hovered) {
                    g2.setColor(HOVER_BG);
                    g2.fill(new RoundRectangle2D.Double(0, 0,
                            getWidth(), getHeight(), BUTTON_ARC, BUTTON_ARC));
                }
                g2.setColor(BORDER_COLOR);
                g2.setStroke(new BasicStroke(1.5f));
                g2.draw(new RoundRectangle2D.Double(1, 1,
                        getWidth() - 2, getHeight() - 2, BUTTON_ARC, BUTTON_ARC));
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btn.setFont(FONT_BOLD);
        btn.setForeground(TEXT_PRIMARY);
        btn.setPreferredSize(new Dimension(160, 40));
        return btn;
    }

    // ── Styled Text Field ─────────────────────────────────────

    /** Creates a dark-themed, rounded text field with placeholder text. */
    public static JTextField createStyledTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), INPUT_ARC, INPUT_ARC));
                g2.dispose();
                super.paintComponent(g);

                // Draw placeholder
                if (getText().isEmpty() && !hasFocus()) {
                    Graphics2D ph = (Graphics2D) g.create();
                    ph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    ph.setColor(TEXT_MUTED);
                    ph.setFont(getFont());
                    Insets ins = getInsets();
                    ph.drawString(placeholder, ins.left,
                            getHeight() / 2 + ph.getFontMetrics().getAscent() / 2 - 2);
                    ph.dispose();
                }
            }
        };
        stylizeField(tf);
        // Repaint on focus changes so placeholder shows/hides
        tf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { tf.repaint(); }
            @Override public void focusLost(FocusEvent e)   { tf.repaint(); }
        });
        return tf;
    }

    /** Creates a dark-themed, rounded password field with placeholder text. */
    public static JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField pf = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fill(new RoundRectangle2D.Double(0, 0,
                        getWidth(), getHeight(), INPUT_ARC, INPUT_ARC));
                g2.dispose();
                super.paintComponent(g);

                if (getPassword().length == 0 && !hasFocus()) {
                    Graphics2D ph = (Graphics2D) g.create();
                    ph.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    ph.setColor(TEXT_MUTED);
                    ph.setFont(getFont());
                    Insets ins = getInsets();
                    ph.drawString(placeholder, ins.left,
                            getHeight() / 2 + ph.getFontMetrics().getAscent() / 2 - 2);
                    ph.dispose();
                }
            }
        };
        stylizeField(pf);
        pf.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { pf.repaint(); }
            @Override public void focusLost(FocusEvent e)   { pf.repaint(); }
        });
        return pf;
    }

    /** Applies the common dark-theme styling to a text component. */
    private static void stylizeField(JTextField tf) {
        tf.setFont(FONT_REGULAR);
        tf.setForeground(TEXT_PRIMARY);
        tf.setBackground(INPUT_BG);
        tf.setCaretColor(ACCENT);
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(INPUT_ARC, BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 14, 8, 14)
        ));
        tf.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        // Highlight border on focus
        tf.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(INPUT_ARC, ACCENT),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }
            @Override
            public void focusLost(FocusEvent e) {
                tf.setBorder(BorderFactory.createCompoundBorder(
                        new RoundedBorder(INPUT_ARC, BORDER_COLOR),
                        BorderFactory.createEmptyBorder(8, 14, 8, 14)
                ));
            }
        });
    }

    // ── Styled ComboBox ───────────────────────────────────────

    /** Creates a dark-themed combo box. */
    public static JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> cb = new JComboBox<>(items);
        cb.setFont(FONT_REGULAR);
        cb.setForeground(TEXT_PRIMARY);
        cb.setBackground(INPUT_BG);
        cb.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(INPUT_ARC, BORDER_COLOR),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        cb.setPreferredSize(new Dimension(300, FIELD_HEIGHT));
        cb.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_REGULAR);
                setBackground(isSelected ? ACCENT : BG_CARD);
                setForeground(TEXT_PRIMARY);
                setBorder(new EmptyBorder(6, 10, 6, 10));
                return this;
            }
        });
        cb.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = new JButton("\u25BE");  // ▾
                b.setFont(FONT_REGULAR);
                b.setForeground(TEXT_SECONDARY);
                b.setBackground(INPUT_BG);
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setFocusPainted(false);
                b.setContentAreaFilled(false);
                return b;
            }
        });
        return cb;
    }

    // ── Styled Label ──────────────────────────────────────────

    /** Creates a standard label with the given font and colour. */
    public static JLabel createLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        return lbl;
    }

    // ── Styled ScrollPane ─────────────────────────────────────

    /** Wraps a component in a dark-themed scroll pane with styled scrollbar. */
    public static JScrollPane createStyledScrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(BG_SURFACE);
        sp.setBackground(BG_SURFACE);

        // Custom scrollbar styling
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_SURFACE;
            }
            @Override protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x + 2, r.y, r.width - 4, r.height, 8, 8);
                g2.dispose();
            }
        });
        sp.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = BORDER_COLOR;
                this.trackColor = BG_SURFACE;
            }
            @Override protected JButton createDecreaseButton(int orientation) {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
            @Override protected JButton createIncreaseButton(int orientation) {
                JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b;
            }
            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(thumbColor);
                g2.fillRoundRect(r.x, r.y + 2, r.width, r.height - 4, 8, 8);
                g2.dispose();
            }
        });
        return sp;
    }

    // ── Dark-themed JOptionPane ───────────────────────────────

    /** Sets up dark look-and-feel defaults for JOptionPane. */
    public static void setupOptionPaneDefaults() {
        UIManager.put("OptionPane.background", BG_SURFACE);
        UIManager.put("Panel.background", BG_SURFACE);
        UIManager.put("OptionPane.messageForeground", TEXT_PRIMARY);
        UIManager.put("OptionPane.messageFont", FONT_REGULAR);
        UIManager.put("Button.background", ACCENT);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", FONT_BOLD);
        UIManager.put("TextField.background", INPUT_BG);
        UIManager.put("TextField.foreground", TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", TEXT_PRIMARY);
    }

    // ── Rounded Border Helper ─────────────────────────────────

    /** A rounded border with configurable arc and colour. */
    public static class RoundedBorder extends AbstractBorder {
        private final int arc;
        private final Color color;

        public RoundedBorder(int arc, Color color) {
            this.arc = arc;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y,
                                int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.draw(new RoundRectangle2D.Double(x, y,
                    width - 1, height - 1, arc, arc));
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(4, 4, 4, 4);
        }
    }

    // ══════════════════════════════════════════════════════════
    //  SIDEBAR NAV BUTTON
    // ══════════════════════════════════════════════════════════

    /**
     * Creates a sidebar navigation button with an icon symbol,
     * label text, active-state accent stripe, and hover effect.
     */
    public static JButton createNavButton(String icon, String label, boolean active) {
        JButton btn = new JButton() {
            private boolean hovered = false;
            {
                setContentAreaFilled(false);
                setFocusPainted(false);
                setBorderPainted(false);
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                boolean isActive = "true".equals(getClientProperty("active"));

                // Background fill
                if (isActive) {
                    g2.setColor(new Color(64, 138, 113, 40));
                    g2.fill(new RoundRectangle2D.Double(4, 0,
                            getWidth() - 8, getHeight(), 8, 8));
                } else if (hovered) {
                    g2.setColor(HOVER_BG);
                    g2.fill(new RoundRectangle2D.Double(4, 0,
                            getWidth() - 8, getHeight(), 8, 8));
                }

                // Active indicator stripe
                if (isActive) {
                    g2.setColor(ACCENT);
                    g2.fillRoundRect(0, 6, 3, getHeight() - 12, 3, 3);
                }

                // Icon
                FlatSVGIcon svgIcon = UIUtils.getIcon(icon, 18, isActive ? ACCENT : TEXT_SECONDARY);
                svgIcon.paintIcon(this, g2, 22, (getHeight() - 18) / 2);

                // Label
                g2.setFont(FONT_NAV);
                g2.setColor(isActive ? TEXT_PRIMARY : TEXT_SECONDARY);
                g2.drawString(label, 50, getHeight() / 2 + 5);

                g2.dispose();
            }
        };
        btn.putClientProperty("active", active ? "true" : "false");
        btn.setPreferredSize(new Dimension(SIDEBAR_WIDTH, 44));
        btn.setMaximumSize(new Dimension(SIDEBAR_WIDTH, 44));
        btn.setMinimumSize(new Dimension(SIDEBAR_WIDTH, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    // ══════════════════════════════════════════════════════════
    //  UTILITY METHODS
    // ══════════════════════════════════════════════════════════

    /** Centres a window on the screen. */
    public static void centerOnScreen(Window w) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        w.setLocation(
                (screen.width - w.getWidth()) / 2,
                (screen.height - w.getHeight()) / 2
        );
    }

    /** Formats a number as currency string. */
    public static String formatCurrency(double amount) {
        return String.format("Rs. %,.2f", amount);
    }

    /** Creates vertical spacing. */
    public static Component vSpace(int height) {
        return Box.createRigidArea(new Dimension(0, height));
    }

    /** Creates horizontal spacing. */
    public static Component hSpace(int width) {
        return Box.createRigidArea(new Dimension(width, 0));
    }
}
