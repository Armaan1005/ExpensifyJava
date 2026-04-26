package com.expensetracker.ui;

import com.expensetracker.db.DBConnection;
import com.expensetracker.model.Expense;
import com.expensetracker.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Dashboard panel — the main overview screen showing:
 *   • Welcome gradient banner
 *   • Four stat cards (Total, This Month, Budget, Categories)
 *   • Category breakdown donut chart
 *   • Monthly spending bar chart (last 12 months)
 *   • Recent transactions list
 */
public class DashboardPanel extends JPanel {

    private final User user;
    private final DBConnection db = DBConnection.getInstance();

    public DashboardPanel(User user) {
        this.user = user;
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

    /** Rebuilds the dashboard (call after data changes). */
    public void refresh() {
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        // Scrollable content
        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(new EmptyBorder(24, 28, 24, 28));

        // ── Welcome Banner ────────────────────────────────────
        UIUtils.GradientPanel banner = new UIUtils.GradientPanel(
                UIUtils.GRAD_START, UIUtils.GRAD_END, UIUtils.CARD_ARC);
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBorder(new EmptyBorder(28, 32, 28, 32));
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        banner.setAlignmentX(LEFT_ALIGNMENT);

        JLabel welcome = UIUtils.createLabel(
                "Welcome back, " + user.getUsername() + "!",
                UIUtils.FONT_TITLE, Color.WHITE);
        welcome.setAlignmentX(LEFT_ALIGNMENT);

        JLabel tagline = UIUtils.createLabel(
                "Here\u2019s your financial overview for "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                UIUtils.FONT_SUBTITLE, new Color(255, 255, 255, 200));
        tagline.setAlignmentX(LEFT_ALIGNMENT);

        banner.add(welcome);
        banner.add(UIUtils.vSpace(6));
        banner.add(tagline);

        content.add(banner);
        content.add(UIUtils.vSpace(24));

        // ── Split Notifications ───────────────────────────────
        int pendingSplits = db.getPendingSplitCount(user.getUserId());
        if (pendingSplits > 0) {
            UIUtils.RoundedPanel splitBanner = new UIUtils.RoundedPanel(12, new Color(64, 138, 113, 30));
            splitBanner.setLayout(new BorderLayout(16, 0));
            splitBanner.setBorder(new EmptyBorder(12, 16, 12, 16));
            splitBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            splitBanner.setAlignmentX(LEFT_ALIGNMENT);

            JPanel splitLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            splitLeft.setOpaque(false);
            JLabel splitIcon = new JLabel(UIUtils.getIcon("user", 24, UIUtils.ACCENT));
            JLabel splitText = UIUtils.createLabel("  You have " + pendingSplits + " pending split request" 
                    + (pendingSplits > 1 ? "s" : "") + " to review.",
                    UIUtils.FONT_BOLD, UIUtils.TEXT_PRIMARY);
            splitLeft.add(splitIcon);
            splitLeft.add(splitText);

            JButton viewSplits = UIUtils.createGhostButton("View All");
            viewSplits.setPreferredSize(new Dimension(100, 30));
            viewSplits.addActionListener(e -> {
                // We don't have direct access to MainFrame.switchTo, 
                // but we can find the MainFrame from the component hierarchy
                Window w = SwingUtilities.getWindowAncestor(this);
                if (w instanceof MainFrame) {
                    // Navigate to Splits tab (index might vary, but we can call a method)
                    // For now, let's assume parent is MainFrame
                }
            });

            splitBanner.add(splitLeft, BorderLayout.WEST);
            // splitBanner.add(viewSplits, BorderLayout.EAST);

            content.add(splitBanner);
            content.add(UIUtils.vSpace(24));
        }

        // ── Stat Cards Row ────────────────────────────────────
        double totalAll    = db.getTotalExpenses(user.getUserId());
        double totalMonth  = db.getMonthlyExpenses(user.getUserId());
        double budget      = db.getBudget(user.getUserId());
        int catCount       = db.getCategoryCount(user.getUserId());

        JPanel cardsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        cardsRow.setOpaque(false);
        cardsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        cardsRow.setAlignmentX(LEFT_ALIGNMENT);

        cardsRow.add(createStatCard("Total Expenses",
                UIUtils.formatCurrency(totalAll), UIUtils.TEAL));
        cardsRow.add(createStatCard("This Month",
                UIUtils.formatCurrency(totalMonth), UIUtils.ACCENT));

        // Budget card with warning logic
        String budgetText;
        Color budgetColor;
        if (budget <= 0) {
            budgetText = "Not Set";
            budgetColor = UIUtils.TEXT_SECONDARY;
        } else {
            double pct = (totalMonth / budget) * 100;
            budgetText = UIUtils.formatCurrency(totalMonth) + " / " + UIUtils.formatCurrency(budget);
            if (pct >= 100) budgetColor = UIUtils.DANGER;
            else if (pct >= 80) budgetColor = UIUtils.WARNING;
            else budgetColor = UIUtils.SUCCESS;
        }
        cardsRow.add(createStatCard("Budget Status", budgetText, budgetColor));
        cardsRow.add(createStatCard("Categories",
                String.valueOf(catCount), new Color(251, 191, 36)));

        content.add(cardsRow);
        content.add(UIUtils.vSpace(24));

        // ── Budget Warning Banner ─────────────────────────────
        if (budget > 0 && totalMonth > budget) {
            UIUtils.RoundedPanel warningBanner = new UIUtils.RoundedPanel(12, new Color(248, 81, 73, 30));
            warningBanner.setLayout(new FlowLayout(FlowLayout.LEFT, 16, 12));
            warningBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
            warningBanner.setAlignmentX(LEFT_ALIGNMENT);

            JLabel warnIcon = new JLabel(UIUtils.getIcon("triangle-alert", 24, UIUtils.DANGER));
            JLabel warnText = UIUtils.createLabel(
                    "  You have exceeded your monthly budget by "
                            + UIUtils.formatCurrency(totalMonth - budget) + "!",
                    UIUtils.FONT_BOLD, UIUtils.DANGER);
            warningBanner.add(warnIcon);
            warningBanner.add(warnText);

            content.add(warningBanner);
            content.add(UIUtils.vSpace(24));
        }

        // ── Charts Row (Category Donut + Monthly Bar) ─────────
        JPanel chartsRow = new JPanel(new GridLayout(1, 2, 16, 0));
        chartsRow.setOpaque(false);
        chartsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));
        chartsRow.setAlignmentX(LEFT_ALIGNMENT);

        chartsRow.add(createCategoryChart());
        chartsRow.add(createMonthlyChart());

        content.add(chartsRow);
        content.add(UIUtils.vSpace(24));

        // ── Recent Transactions ───────────────────────────────
        content.add(createRecentTransactions());

        // Wrap content to restrict width
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        
        content.setMaximumSize(new Dimension(1200, Integer.MAX_VALUE));
        content.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(content);

        JScrollPane scroll = UIUtils.createStyledScrollPane(wrapper);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);
    }

    // ══════════════════════════════════════════════════════════
    //  STAT CARD
    // ══════════════════════════════════════════════════════════

    private JPanel createStatCard(String title, String value, Color accent) {
        UIUtils.RoundedPanel card = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(20, 22, 20, 22));

        // Accent dot + title
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        header.setOpaque(false);
        header.setAlignmentX(LEFT_ALIGNMENT);

        // Small coloured dot
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillOval(0, 4, 8, 8);
                g2.dispose();
            }
        };
        dot.setOpaque(false);
        dot.setPreferredSize(new Dimension(14, 16));

        JLabel lbl = UIUtils.createLabel(title, UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);
        header.add(dot);
        header.add(lbl);

        // Value
        JLabel val = UIUtils.createLabel(value, UIUtils.FONT_BIG_NUM, accent);
        val.setAlignmentX(LEFT_ALIGNMENT);

        // Auto-shrink long values
        if (value.length() > 12) {
            val.setFont(UIUtils.FONT_HEADER);
        }

        card.add(header);
        card.add(UIUtils.vSpace(10));
        card.add(val);

        return card;
    }

    // ══════════════════════════════════════════════════════════
    //  CATEGORY DONUT CHART
    // ══════════════════════════════════════════════════════════

    private JPanel createCategoryChart() {
        Map<String, Double> summary = db.getCategorySummary(user.getUserId());
        double total = summary.values().stream().mapToDouble(Double::doubleValue).sum();

        UIUtils.RoundedPanel panel = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel header = UIUtils.createLabel("Category Breakdown",
                UIUtils.FONT_HEADER, UIUtils.TEXT_PRIMARY);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.add(header, BorderLayout.NORTH);

        if (summary.isEmpty()) {
            JLabel empty = UIUtils.createLabel("No expenses yet",
                    UIUtils.FONT_SUBTITLE, UIUtils.TEXT_MUTED);
            empty.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(empty, BorderLayout.CENTER);
            return panel;
        }

        // Donut chart + legend side by side
        JPanel body = new JPanel(new GridLayout(1, 2, 12, 0));
        body.setOpaque(false);

        // Donut
        JPanel donut = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                int size = Math.min(getWidth(), getHeight()) - 20;
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;

                double startAngle = 90;
                for (Map.Entry<String, Double> entry : summary.entrySet()) {
                    double sweep = (entry.getValue() / total) * 360;
                    g2.setColor(UIUtils.getCategoryColor(entry.getKey()));
                    g2.fill(new Arc2D.Double(x, y, size, size,
                            startAngle, -sweep, Arc2D.PIE));
                    startAngle -= sweep;
                }

                // Hollow centre for donut effect
                int inner = size / 2;
                int ix = x + (size - inner) / 2;
                int iy = y + (size - inner) / 2;
                g2.setColor(UIUtils.BG_SURFACE);
                g2.fillOval(ix, iy, inner, inner);

                // Centre text
                g2.setColor(UIUtils.TEXT_PRIMARY);
                g2.setFont(UIUtils.FONT_BOLD);
                String totalStr = UIUtils.formatCurrency(total);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(totalStr,
                        ix + (inner - fm.stringWidth(totalStr)) / 2,
                        iy + inner / 2 + fm.getAscent() / 2 - 2);

                g2.dispose();
            }
        };
        donut.setOpaque(false);

        // Legend
        JPanel legend = new JPanel();
        legend.setLayout(new BoxLayout(legend, BoxLayout.Y_AXIS));
        legend.setOpaque(false);

        for (Map.Entry<String, Double> entry : summary.entrySet()) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 3));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));

            Color catColor = UIUtils.getCategoryColor(entry.getKey());
            JPanel colorDot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(catColor);
                    g2.fillRoundRect(0, 2, 10, 10, 3, 3);
                    g2.dispose();
                }
            };
            colorDot.setOpaque(false);
            colorDot.setPreferredSize(new Dimension(12, 14));

            double pct = (entry.getValue() / total) * 100;
            JLabel catLabel = UIUtils.createLabel(
                    entry.getKey() + "  " + String.format("%.0f%%", pct),
                    UIUtils.FONT_SMALL, UIUtils.TEXT_SECONDARY);

            row.add(colorDot);
            row.add(catLabel);
            legend.add(row);
        }

        body.add(donut);
        body.add(legend);
        panel.add(body, BorderLayout.CENTER);

        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  MONTHLY BAR CHART
    // ══════════════════════════════════════════════════════════

    private JPanel createMonthlyChart() {
        Map<String, Double> monthlyData = db.getMonthlyOverview(user.getUserId());
        double maxVal = monthlyData.values().stream()
                .mapToDouble(Double::doubleValue).max().orElse(1);

        UIUtils.RoundedPanel panel = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        panel.setLayout(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));

        JLabel header = UIUtils.createLabel("Monthly Spending",
                UIUtils.FONT_HEADER, UIUtils.TEXT_PRIMARY);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));
        panel.add(header, BorderLayout.NORTH);

        // Bar chart drawn with Graphics2D
        JPanel chart = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                        RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int padding = 10;
                int bottomPad = 30;
                int chartH = h - bottomPad - padding;

                int barCount = monthlyData.size();
                if (barCount == 0) return;

                int totalBarSpace = w - padding * 2;
                int barWidth = Math.max(8, (totalBarSpace / barCount) - 8);
                int gap = (totalBarSpace - barWidth * barCount) / (barCount + 1);

                int i = 0;
                for (Map.Entry<String, Double> entry : monthlyData.entrySet()) {
                    int x = padding + gap + i * (barWidth + gap);
                    double ratio = maxVal > 0 ? entry.getValue() / maxVal : 0;
                    int barH = (int) (ratio * chartH);

                    // Bar with gradient
                    GradientPaint gp = new GradientPaint(
                            x, h - bottomPad - barH, UIUtils.ACCENT,
                            x, h - bottomPad, UIUtils.TEAL);
                    g2.setPaint(gp);
                    g2.fill(new RoundRectangle2D.Double(
                            x, h - bottomPad - barH,
                            barWidth, barH, 6, 6));

                    // Month label (short)
                    String month = entry.getKey().substring(5); // MM
                    g2.setColor(UIUtils.TEXT_MUTED);
                    g2.setFont(UIUtils.FONT_SMALL);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(month,
                            x + (barWidth - fm.stringWidth(month)) / 2,
                            h - 8);

                    i++;
                }
                g2.dispose();
            }
        };
        chart.setOpaque(false);
        panel.add(chart, BorderLayout.CENTER);

        return panel;
    }

    // ══════════════════════════════════════════════════════════
    //  RECENT TRANSACTIONS
    // ══════════════════════════════════════════════════════════

    private JPanel createRecentTransactions() {
        List<Expense> recent = db.getRecentExpenses(user.getUserId(), 5);

        UIUtils.RoundedPanel panel = new UIUtils.RoundedPanel(UIUtils.CARD_ARC, UIUtils.BG_SURFACE);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 22, 20, 22));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 350));
        panel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel header = UIUtils.createLabel("Recent Transactions",
                UIUtils.FONT_HEADER, UIUtils.TEXT_PRIMARY);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(header);
        panel.add(UIUtils.vSpace(16));

        if (recent.isEmpty()) {
            JLabel empty = UIUtils.createLabel("No transactions yet — add your first expense!",
                    UIUtils.FONT_SUBTITLE, UIUtils.TEXT_MUTED);
            empty.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(empty);
        } else {
            for (Expense exp : recent) {
                JPanel row = createTransactionRow(exp);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.add(row);
                panel.add(UIUtils.vSpace(4));
            }
        }

        return panel;
    }

    /** Creates a single transaction row with category dot, description, and amount. */
    private JPanel createTransactionRow(Expense exp) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        row.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Left: category colour indicator + info
        Color catColor = UIUtils.getCategoryColor(exp.getCategory());
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setOpaque(false);

        JPanel catDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(catColor);
                int dotH = (int)(getHeight() * 0.7);
                g2.fillRoundRect(0, (getHeight() - dotH) / 2, 6, dotH, 4, 4);
                g2.dispose();
            }
        };
        catDot.setOpaque(false);
        catDot.setPreferredSize(new Dimension(8, 40));

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        String desc = exp.getDescription().isEmpty() ? exp.getCategory() : exp.getDescription();
        JLabel descLabel = UIUtils.createLabel(desc, UIUtils.FONT_BOLD, UIUtils.TEXT_PRIMARY);
        JLabel metaLabel = UIUtils.createLabel(
                exp.getCategory() + "  •  " + exp.getDate(),
                UIUtils.FONT_SMALL, UIUtils.TEXT_MUTED);

        info.add(descLabel);
        info.add(metaLabel);

        leftPanel.add(catDot);
        leftPanel.add(info);

        // Right: amount
        JLabel amountLabel = UIUtils.createLabel(
                "- " + UIUtils.formatCurrency(exp.getAmount()),
                UIUtils.FONT_BOLD, UIUtils.DANGER);

        row.add(leftPanel, BorderLayout.WEST);
        row.add(amountLabel, BorderLayout.EAST);

        return row;
    }
}
