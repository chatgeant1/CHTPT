import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class AppGUI extends JFrame {
    private Node node;

    // Các thành phần giao diện chính
    private GraphPanel panelGraph;
    private JTable tableStatus;
    private DefaultTableModel tableModel;
    private JTextArea txtLogs;
    private JLabel lblCurrentStep, lblGlobalClock, lblStatusBadge;
    private JButton btnRequest;
    private JSlider sliderNodes, sliderSpeed;

    // Bảng màu Dark Mode chuẩn UI cao cấp
    private final Color COLOR_BG_DARK    = new Color(18, 18, 20);
    private final Color COLOR_CARD_BG    = new Color(30, 30, 35);
    private final Color COLOR_TEXT_LIGHT = new Color(220, 220, 225);
    private final Color COLOR_TEXT_MUTED = new Color(140, 140, 150);
    private final Color COLOR_ACCENT     = new Color(81, 103, 230); // Xanh dương công nghệ

    public AppGUI(Node node) {
        this.node = node;

        // Cấu hình JFrame chính
        setTitle("Ricart & Agrawala — Mutual Exclusion Simulator");
        setSize(1100, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG_DARK);
        setLayout(new BorderLayout(10, 10));

        // =================================================================
        // HỆ THỐNG HEADER (Thanh trên cùng)
        // =================================================================
        JPanel panelHeader = new JPanel(new BorderLayout());
        panelHeader.setBackground(COLOR_CARD_BG);
        panelHeader.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JLabel lblTitle = new JLabel("Ricart & Agrawala — Mutual Exclusion", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_LIGHT);
        panelHeader.add(lblTitle, BorderLayout.WEST);

        JPanel panelHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelHeaderRight.setBackground(COLOR_CARD_BG);
        lblGlobalClock = new JLabel("clock = 0");
        lblGlobalClock.setFont(new Font("Consolas", Font.BOLD, 14));
        lblGlobalClock.setForeground(COLOR_TEXT_LIGHT);
        
        lblStatusBadge = new JLabel(" Idle ", SwingConstants.CENTER);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(60, 60, 65));
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        panelHeaderRight.add(lblGlobalClock);
        panelHeaderRight.add(lblStatusBadge);
        panelHeader.add(panelHeaderRight, BorderLayout.EAST);
        add(panelHeader, BorderLayout.NORTH);

        // =================================================================
        // KHU VỰC TRÁI: ĐỒ HỌA MẠNG MÔ PHỎNG (GRAPH CANVAS)
        // =================================================================
        JPanel panelLeftContainer = new JPanel(new BorderLayout());
        panelLeftContainer.setBackground(COLOR_BG_DARK);
        panelLeftContainer.setBorder(new EmptyBorder(0, 10, 10, 5));

        panelGraph = new GraphPanel();
        panelLeftContainer.add(panelGraph, BorderLayout.CENTER);

        // Thanh chú thích màu sắc ở dưới cùng đồ họa
        JPanel panelLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        panelLegend.setBackground(COLOR_BG_DARK);
        panelLegend.add(createLegendItem("Hoạt động ổn định", new Color(81, 103, 230)));
        panelLegend.add(createLegendItem("Muốn vào CS (WANTED)", new Color(218, 165, 32)));
        panelLegend.add(createLegendItem("Trong CS (HELD)", new Color(198, 40, 40)));
        panelLeftContainer.add(panelLegend, BorderLayout.SOUTH);
        
        add(panelLeftContainer, BorderLayout.CENTER);

        // =================================================================
        // KHU VỰC PHẢI: BẢNG ĐIỀU KHIỂN & SỐ LIỆU (CONTROL PANEL)
        // =================================================================
        JPanel panelRightContainer = new JPanel(new VerticalLayout(12));
        panelRightContainer.setBackground(COLOR_BG_DARK);
        panelRightContainer.setPreferredSize(new Dimension(420, 0));
        panelRightContainer.setBorder(new EmptyBorder(0, 5, 10, 10));

        // 1. Khung Điều Khiển Sliders & Buttons
        JPanel panelControls = createCardPanel("ĐIỀU KHIỂN");
        panelControls.setLayout(new GridLayout(5, 1, 5, 5));

        JPanel pSlider1 = new JPanel(new BorderLayout()); pSlider1.setBackground(COLOR_CARD_BG);
        JLabel lblS1 = new JLabel("Tiến trình"); lblS1.setForeground(COLOR_TEXT_LIGHT);
        sliderNodes = new JSlider(2, 5, 3); configureSlider(sliderNodes);
        pSlider1.add(lblS1, BorderLayout.WEST); pSlider1.add(sliderNodes, BorderLayout.CENTER);

        JPanel pSlider2 = new JPanel(new BorderLayout()); pSlider2.setBackground(COLOR_CARD_BG);
        JLabel lblS2 = new JLabel("Tốc độ   "); lblS2.setForeground(COLOR_TEXT_LIGHT);
        sliderSpeed = new JSlider(1, 5, 3); configureSlider(sliderSpeed);
        pSlider2.add(lblS2, BorderLayout.WEST); pSlider2.add(sliderSpeed, BorderLayout.CENTER);

        JPanel pButtons = new JPanel(new GridLayout(1, 1, 8, 0));
        pButtons.setBackground(COLOR_CARD_BG);
        btnRequest = createModernButton("Yêu cầu CS (REQ)", COLOR_ACCENT);
        pButtons.add(btnRequest);

        panelControls.add(pSlider1);
        panelControls.add(pSlider2);
        panelControls.add(new JLabel("Nhấn nút để bắt đầu yêu cầu vào miền găng", SwingConstants.CENTER) {{ setForeground(COLOR_TEXT_MUTED); setFont(new Font("Segoe UI", Font.ITALIC, 11)); }});
        panelControls.add(pButtons);
        panelRightContainer.add(panelControls);

        // 2. Khung Trạng Thái Tiến Trình (JTable)
        JPanel panelTableCard = createCardPanel("TRẠNG THÁI TIẾN TRÌNH");
        String[] columns = {"P", "Host:Port", "Trạng thái", "Clock", "Reply", "Queue"};
        tableModel = new DefaultTableModel(columns, 0);
        tableStatus = new JTable(tableModel);
        tableStatus.setBackground(COLOR_CARD_BG);
        tableStatus.setForeground(COLOR_TEXT_LIGHT);
        tableStatus.getTableHeader().setBackground(new Color(45, 45, 50));
        tableStatus.getTableHeader().setForeground(COLOR_TEXT_LIGHT);
        tableStatus.setGridColor(new Color(50, 50, 55));
        JScrollPane scrollTable = new JScrollPane(tableStatus);
        scrollTable.setPreferredSize(new Dimension(390, 110));
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        panelTableCard.add(scrollTable, BorderLayout.CENTER);
        panelRightContainer.add(panelTableCard);

        // 3. Khung Bước Hiện Tại
        JPanel panelStepCard = createCardPanel("BƯỚC HIỆN TẠI");
        lblCurrentStep = new JLabel("Hệ thống đang hoạt động ổn định. Đang nghe kết nối...");
        lblCurrentStep.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblCurrentStep.setForeground(new Color(138, 180, 248));
        panelStepCard.add(lblCurrentStep, BorderLayout.CENTER);
        panelRightContainer.add(panelStepCard);

        // 4. Khung Nhật Ký Sự Kiện (Logs Box)
        JPanel panelLogCard = createCardPanel("NHẬT KÝ SỰ KIỆN");
        txtLogs = new JTextArea(10, 30);
        txtLogs.setEditable(false);
        txtLogs.setBackground(new Color(24, 24, 27));
        txtLogs.setForeground(new Color(170, 170, 180));
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLogs.setMargin(new Insets(6, 6, 6, 6));
        JScrollPane scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setBorder(BorderFactory.createEmptyBorder());
        panelLogCard.add(scrollLogs, BorderLayout.CENTER);
        panelRightContainer.add(panelLogCard);

        add(panelRightContainer, BorderLayout.EAST);

        // Cài đặt sự kiện nút bấm điều khiển
        btnRequest.addActionListener(e -> {
            if (node != null) new Thread(() -> node.requestCriticalSection()).start();
        });
        
        initTableData();
    }

    // Thiết lập bảng ban đầu CHỈ hiện đúng duy nhất chính bản thân Node này
    private void initTableData() {
        tableModel.setRowCount(0);
        if (node != null) {
            String myIpPort = node.getNodeIP() + ":" + (9000 + node.getNodeID());
            tableModel.addRow(new Object[]{"P" + node.getNodeID(), myIpPort, "RELEASED", "0", "0/0", "Empty"});
        }
    }

    // Hàm nhận cập nhật từ dữ liệu lõi backend đổ về giao diện
    public void updateUI(String state, int clockValue, int currentReply, int totalNeighbors, int sharedVal) {
        SwingUtilities.invokeLater(() -> {
            lblGlobalClock.setText("clock=" + clockValue);
            lblStatusBadge.setText(" " + state + " ");
            
            int myId = node != null ? node.getNodeID() : 1;
            
            if (state.equals("RELEASED")) {
                lblStatusBadge.setBackground(new Color(46, 139, 87));
                lblCurrentStep.setText("Tiến trình giải phóng Miền Găng. Giá trị tài nguyên chung: " + sharedVal);
                btnRequest.setEnabled(true);
                btnRequest.setText("Yêu cầu CS (REQ)");
            } else if (state.equals("WANTED")) {
                lblStatusBadge.setBackground(new Color(218, 165, 32));
                lblCurrentStep.setText("P" + myId + " muốn vào CS với timestamp " + clockValue + ". Đang chờ thu thập REPLY...");
                btnRequest.setEnabled(false);
                btnRequest.setText("ĐANG CHỜ REPLY...");
            } else if (state.equals("HELD")) {
                lblStatusBadge.setBackground(new Color(198, 40, 40));
                lblCurrentStep.setText("P" + myId + " ĐÃ VÀO MIỀN GĂNG ĐỘC CHIẾM TÀI NGUYÊN.");
            }

            // Đồng bộ dữ liệu lên JTable dòng đầu tiên
            tableModel.setValueAt(state, 0, 2);
            tableModel.setValueAt(String.valueOf(clockValue), 0, 3);
            tableModel.setValueAt(currentReply + "/" + totalNeighbors, 0, 4);

            // Bắn tín hiệu đổi màu và vẽ lại đồ họa canvas
            panelGraph.setNodeState(state);
        });
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String prefix = message.contains("REQUEST") ? "▶ REQ  " : (message.contains("REPLY") ? "◀ REPLY " : "INFO   ");
            txtLogs.append(prefix + message + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());

            // TỰ ĐỘNG PHÁT HIỆN: Nhìn thấy log tương tác với Node nào thì vẽ thêm Node đó lên vòng tròn mạng
            if (message.contains("Node 1") || message.contains("từ 1") || message.contains("tới 1")) panelGraph.discoverNode(1);
            if (message.contains("Node 2") || message.contains("từ 2") || message.contains("tới 2")) panelGraph.discoverNode(2);
            if (message.contains("Node 3") || message.contains("từ 3") || message.contains("tới 3")) panelGraph.discoverNode(3);
        });
    }

    // =================================================================
    // LỚP VẼ ĐỒ HỌA MẠNG DỰA TRÊN CÁC NODE THỰC TẾ ONLINE
    // =================================================================
    private class GraphPanel extends JPanel {
        private String currentState = "RELEASED";
        private java.util.Set<Integer> activeNodes = new java.util.HashSet<>();

        public GraphPanel() {
            setBackground(new Color(24, 24, 27));
            setBorder(new LineBorder(new Color(50, 50, 55), 1));
            // Mặc định luôn đưa chính máy mình vào danh sách kích hoạt vẽ
            if (node != null) {
                activeNodes.add(node.getNodeID());
            } else {
                activeNodes.add(2); // Giá trị an toàn mặc định nếu test độc lập
            }
        }

        public void setNodeState(String state) {
            this.currentState = state;
            repaint();
        }

        public void discoverNode(int nodeId) {
            if (!activeNodes.contains(nodeId)) {
                activeNodes.add(nodeId);
                
                // Đồng thời thêm dòng thông tin Node mới này vào JTable phía dưới cho đồng bộ
                String neighborIpPort = "10.251.2." + (100 + nodeId) + ":" + (9000 + nodeId);
                // Tránh add trùng dòng bằng cách kiểm tra số lượng Node
                if (tableModel.getRowCount() < activeNodes.size()) {
                    tableModel.addRow(new Object[]{"P" + nodeId, neighborIpPort, "ONLINE", "-", "-", "-"});
                }
                repaint();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int centerX = width / 2;
            int centerY = height / 2;
            int radius = Math.min(width, height) / 3;

            // Quy hoạch vị trí tối đa 3 Node theo mô hình tròn toán học
            int[][] positions = {
                {centerX, centerY - radius},                            // P1
                {centerX + (int)(radius * 0.86), centerY + radius / 2},   // P2
                {centerX - (int)(radius * 0.86), centerY + radius / 2}    // P3
            };

            int myId = (node != null) ? node.getNodeID() : 2;

            // Chỉ vẽ vòng quỹ đạo nét đứt nếu phát hiện từ 2 Node online chung trở lên
            if (activeNodes.size() > 1) {
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0));
                g2.setColor(new Color(60, 60, 65));
                g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            }

            // Duyệt kiểm tra để vẽ
            for (int i = 0; i < 3; i++) {
                int currNodeId = i + 1;

                // NÚT NÀO CHƯA ONLINE/CHƯA KẾT NỐI CHUNG -> ẨN LUÔN KHÔNG VẼ!
                if (!activeNodes.contains(currNodeId)) {
                    continue;
                }

                int x = positions[i][0];
                int y = positions[i][1];
                int nodeSize = 55;

                Color nodeColor = new Color(81, 103, 230); // Xanh dương (Hoạt động bình thường)

                // Nếu là chính máy mình thì đổi màu theo trạng thái thuật toán
                if (currNodeId == myId) {
                    if (currentState.equals("WANTED")) nodeColor = new Color(218, 165, 32);     // Vàng
                    else if (currentState.equals("HELD")) nodeColor = new Color(198, 40, 40);   // Đỏ
                } else {
                    nodeColor = new Color(45, 156, 219); // Màu xanh lơ cho các Node hàng xóm đã online
                }

                // Vẽ node
                g2.setColor(nodeColor);
                g2.fillOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);

                // Viền ngoài phát sáng
                g2.setStroke(new BasicStroke(2));
                g2.setColor(currNodeId == myId ? Color.WHITE : new Color(150, 150, 160));
                g2.drawOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);

                // Viết chữ nhãn P1, P2, P3
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String pName = "P" + currNodeId;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(pName, x - fm.stringWidth(pName) / 2, y + fm.getAscent() / 2 - 2);

                // In Port mạng LAN bên dưới nút tròn
                g2.setFont(new Font("Consolas", Font.PLAIN, 10));
                g2.setColor(COLOR_TEXT_MUTED);
                String hostLabel = "10.251.2." + (100 + currNodeId) + ":" + (9000 + currNodeId);
                g2.drawString(hostLabel, x - g2.getFontMetrics().stringWidth(hostLabel) / 2, y + nodeSize / 2 + 15);
            }
        }
    }

    // =================================================================
    // CÁC HÀM TIỆN ÍCH TẠO KHUNG LAYOUT (HELPER COMPONENT UI)
    // =================================================================
    private JPanel createCardPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(new LineBorder(new Color(60, 60, 65), 1), title, 
                0, 0, new Font("Segoe UI", Font.BOLD, 11), COLOR_TEXT_MUTED),
            new EmptyBorder(8, 10, 8, 10)
        ));
        return panel;
    }

    private JButton createModernButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void configureSlider(JSlider slider) {
        slider.setBackground(COLOR_CARD_BG);
        slider.setForeground(COLOR_TEXT_MUTED);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(1);
    }

    private JPanel createLegendItem(String text, Color color) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setBackground(COLOR_BG_DARK);
        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(color);
                g.fillOval(0, 0, 10, 10);
            }
        };
        dot.setPreferredSize(new Dimension(10, 10));
        dot.setBackground(COLOR_BG_DARK);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(COLOR_TEXT_MUTED);
        p.add(dot); p.add(lbl);
        return p;
    }

    private static class VerticalLayout implements LayoutManager {
        private int gap;
        public VerticalLayout(int gap) { this.gap = gap; }
        public void addLayoutComponent(String name, Component comp) {}
        public void removeLayoutComponent(Component comp) {}
        public Dimension preferredLayoutSize(Container parent) {
            synchronized (parent.getTreeLock()) {
                int h = 0, w = 0;
                for (Component c : parent.getComponents()) {
                    Dimension d = c.getPreferredSize();
                    w = Math.max(w, d.width);
                    h += d.height + gap;
                }
                return new Dimension(w, h);
            }
        }
        public Dimension minimumLayoutSize(Container parent) { return preferredLayoutSize(parent); }
        public void layoutContainer(Container parent) {
            synchronized (parent.getTreeLock()) {
                int y = 0;
                for (Component c : parent.getComponents()) {
                    Dimension d = c.getPreferredSize();
                    c.setBounds(0, y, parent.getWidth(), d.height);
                    y += d.height + gap;
                }
            }
        }
    }
}