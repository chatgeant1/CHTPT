import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppGUI extends JFrame {
    private Node node;

    // Các thành phần giao diện chính
    private GraphPanel panelGraph;
    private JTable tableStatus;
    private DefaultTableModel tableModel;
    private JTextArea txtLogs;
    private JLabel lblCurrentStep, lblGlobalClock, lblStatusBadge;
    private JButton btnRequest;

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
        
        // Tiêu đề bên trái
        JLabel lblTitle = new JLabel("Ricart & Agrawala — Mutual Exclusion", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(COLOR_TEXT_LIGHT);
        // Đặt tiêu đề ở bên trái của header.
        panelHeader.add(lblTitle, BorderLayout.WEST);

        // Tạo cụm bên phải (các thành phần được xếp từ trái sang phải nhưng dồn về phía phải)
        JPanel panelHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelHeaderRight.setBackground(COLOR_CARD_BG);
        // Label đồng hồ logic
        lblGlobalClock = new JLabel("clock = 0");
        lblGlobalClock.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGlobalClock.setForeground(COLOR_TEXT_LIGHT);
        
        // Label trạng thái (badge) (Mặc định JLabel trong suốt)
        lblStatusBadge = new JLabel(" Idle ", SwingConstants.CENTER);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBackground(new Color(60, 60, 65));
        lblStatusBadge.setForeground(Color.WHITE);
        lblStatusBadge.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        // Đưa 2 label clock và badge vào panel phải
        // [đổi vùng phải, đưa lên header] thêm nút req
        btnRequest = createModernButton("Yêu cầu vào Miền Găng", COLOR_ACCENT);
        btnRequest.setPreferredSize(new Dimension(220, 36));
        panelHeaderRight.add(btnRequest);

        panelHeaderRight.add(lblGlobalClock);
        panelHeaderRight.add(lblStatusBadge);

        // Đưa panel phải vào header
        panelHeader.add(panelHeaderRight, BorderLayout.EAST);

        // Đưa header lên cửa sổ chính
        add(panelHeader, BorderLayout.NORTH);

// =================================================================
// KHU VỰC TRÁI: ĐỒ HỌA MẠNG MÔ PHỎNG (GRAPH CANVAS)
// =================================================================
        // Tạo panel bên trái của giao diện.
        JPanel panelLeftContainer = new JPanel(new BorderLayout());
        panelLeftContainer.setBackground(COLOR_BG_DARK);
        panelLeftContainer.setBorder(new EmptyBorder(0, 10, 10, 5));

        // Khu vực vẽ mạng - Tạo đối tượng đồ họa tự viết.
        panelGraph = new GraphPanel();
        // Đặt vùng vẽ chiếm gần như toàn bộ diện tích panel trái.
        panelLeftContainer.add(panelGraph, BorderLayout.CENTER);

        // Thanh chú thích (legend) màu sắc ở dưới cùng đồ họa
        JPanel panelLegend = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 8));
        panelLegend.setBackground(COLOR_BG_DARK);
        panelLegend.add(createLegendItem("Hoạt động ổn định", new Color(81, 103, 230)));
        panelLegend.add(createLegendItem("Muốn vào CS (WANTED)", new Color(218, 165, 32)));
        panelLegend.add(createLegendItem("Trong CS (HELD)", new Color(198, 40, 40)));
        
        panelLeftContainer.add(panelLegend, BorderLayout.SOUTH);
        
        // Đưa toàn bộ khu trái vào cửa sổ
        add(panelLeftContainer, BorderLayout.CENTER);

// =================================================================
// KHU VỰC PHẢI: BẢNG ĐIỀU KHIỂN & SỐ LIỆU (TỐI ƯU KHÔNG GIAN)
// =================================================================
        JPanel panelRightContainer = new JPanel(new VerticalLayout(12));
        panelRightContainer.setBackground(COLOR_BG_DARK);
        panelRightContainer.setPreferredSize(new Dimension(450, 0)); 
        panelRightContainer.setBorder(new EmptyBorder(0, 5, 10, 10));

        // 1. Khung Nút Bấm Gửi Yêu Cầu
        // JPanel panelControls = createCardPanel("HÀNH ĐỘNG");
        // panelControls.setLayout(new BorderLayout(0, 5));
        
        // btnRequest = createModernButton("Yêu cầu vào Miền Găng (REQ)", COLOR_ACCENT);
        // btnRequest.setPreferredSize(new Dimension(0, 45)); 
        // panelControls.add(btnRequest, BorderLayout.CENTER);
        // panelRightContainer.add(panelControls);

        // 2. Khung Trạng Thái Tiến Trình (MỞ RỘNG)
        JPanel panelTableCard = createCardPanel("TRẠNG THÁI TIẾN TRÌNH TRONG MẠNG");
        String[] columns = {"P", "Host:Port", "Trạng thái", "Clock", "Reply", "Queue"};
        tableModel = new DefaultTableModel(columns, 0);
        tableStatus = new JTable(tableModel);
        tableStatus.setBackground(COLOR_CARD_BG);
        tableStatus.setForeground(COLOR_TEXT_LIGHT);
        tableStatus.setRowHeight(24); 
        tableStatus.getTableHeader().setBackground(new Color(45, 45, 50));
        tableStatus.getTableHeader().setForeground(COLOR_TEXT_LIGHT);
        tableStatus.setGridColor(new Color(50, 50, 55));
        
        // Dòng này làm JTable kéo dài hết viewport
        tableStatus.setFillsViewportHeight(true);
        
        tableStatus.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableStatus.getColumnModel().getColumn(0).setPreferredWidth(40);   
        tableStatus.getColumnModel().getColumn(1).setPreferredWidth(130);  
        tableStatus.getColumnModel().getColumn(2).setPreferredWidth(85);   
        tableStatus.getColumnModel().getColumn(3).setPreferredWidth(50);   
        tableStatus.getColumnModel().getColumn(4).setPreferredWidth(55);   
        tableStatus.getColumnModel().getColumn(5).setPreferredWidth(50);   

        JScrollPane scrollTable = new JScrollPane(tableStatus);
        scrollTable.setPreferredSize(new Dimension(420, 160)); 
        scrollTable.setBorder(BorderFactory.createEmptyBorder());
        
        // ĐÃ SỬA: Nhuộm đen phần nền trống của bảng cuộn
        scrollTable.setBackground(COLOR_CARD_BG);
        scrollTable.getViewport().setBackground(COLOR_CARD_BG);
        
        panelTableCard.add(scrollTable, BorderLayout.CENTER);
        panelRightContainer.add(panelTableCard);

        // 3. Khung Bước Hiện Tại
        JPanel panelStepCard = createCardPanel("BƯỚC HIỆN TẠI");
        lblCurrentStep = new JLabel("Hệ thống đang hoạt động ổn định. Đang nghe kết nối mạng...");
        lblCurrentStep.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCurrentStep.setForeground(new Color(138, 180, 248));
        panelStepCard.add(lblCurrentStep, BorderLayout.CENTER);
        panelRightContainer.add(panelStepCard);

        // 4. Khung Nhật Ký Sự Kiện
        JPanel panelLogCard = createCardPanel("NHẬT KÝ SỰ KIỆN CHI TIẾT");
        txtLogs = new JTextArea();
        txtLogs.setEditable(false);
        txtLogs.setBackground(new Color(24, 24, 27));
        txtLogs.setForeground(new Color(170, 170, 180));
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 16));
        txtLogs.setMargin(new Insets(6, 6, 6, 6));

        // xuống dòng
        txtLogs.setLineWrap(true);
        txtLogs.setWrapStyleWord(true);

        JScrollPane scrollLogs = new JScrollPane(txtLogs);
        scrollLogs.setPreferredSize(new Dimension(420, 350)); 
        scrollLogs.setBorder(BorderFactory.createEmptyBorder());
        
        // Đồng bộ luôn nền đen cho hộp cuộn của nhật ký sự kiện
        scrollLogs.setBackground(new Color(24, 24, 27));
        scrollLogs.getViewport().setBackground(new Color(24, 24, 27));
        
        panelLogCard.add(scrollLogs, BorderLayout.CENTER);
        panelRightContainer.add(panelLogCard);

        add(panelRightContainer, BorderLayout.EAST);

        btnRequest.addActionListener(e -> {
            if (node != null) new Thread(() -> node.requestCriticalSection()).start();
        });
        
        initTableData();
    }

    private void initTableData() {
        tableModel.setRowCount(0);
        if (node != null) {
            String myIpPort = node.getNodeIP() + ":" + (9000 + node.getNodeID());
            tableModel.addRow(new Object[]{"P" + node.getNodeID(), myIpPort, "RELEASED", "0", "0/0", "Empty"});

            for (Node.Neighbor n : node.neighbors){
                tableModel.addRow(
                    new Object[]{
                        "P" + n.getId(), n.ip + ":" + n.getPort(), "ONLINE", "-", "-", "-"
                    }
                );
            }
        }
    }

    public void updateUI(String state, int clockValue, int currentReply, int totalNeighbors, int sharedVal, String queueText) {
        SwingUtilities.invokeLater(() -> {
            lblGlobalClock.setText("clock=" + clockValue);
            lblStatusBadge.setText(" " + state + " ");
            
            int myId = node != null ? node.getNodeID() : 1;
            
            if (state.equals("RELEASED")) {
                lblStatusBadge.setBackground(new Color(46, 139, 87));
                lblCurrentStep.setText("Tiến trình giải phóng Miền Găng. Giá trị tài nguyên: " + sharedVal);
                btnRequest.setEnabled(true);
                btnRequest.setText("Yêu cầu vào Miền Găng");
            } else if (state.equals("WANTED")) {
                lblStatusBadge.setBackground(new Color(218, 165, 32));
                lblCurrentStep.setText("P" + myId + " muốn vào CS với mốc T=" + clockValue + ". Chờ REPLY...");
                btnRequest.setEnabled(false);
                btnRequest.setText("ĐANG CHỜ PHẢN HỒI (REPLY)...");
            } else if (state.equals("HELD")) {
                lblStatusBadge.setBackground(new Color(198, 40, 40));
                lblCurrentStep.setText("P" + myId + " ĐÃ VÀO MIỀN GĂNG ĐỘC CHIẾM TÀI NGUYÊN.");
            }

            tableModel.setValueAt(state, 0, 2);
            tableModel.setValueAt(String.valueOf(clockValue), 0, 3);
            tableModel.setValueAt(currentReply + "/" + totalNeighbors, 0, 4);
            tableModel.setValueAt(queueText, 0, 5);

            panelGraph.setNodeState(state);
        });
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            String prefix = message.contains("REQUEST") ? "\n-> REQ\n" : (message.contains("REPLY") ? "\n<- REPLY\n" : "\nINFO\n");
            txtLogs.append(prefix + message + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());

            for (int id = 1; id <= 10; id++) {
                extractAndDiscover(message, id);
            }
        });
    }

    private void extractAndDiscover(String msg, int targetNodeId) {
        if (msg.contains("Node " + targetNodeId) || msg.contains("phản hồi tới " + targetNodeId) || msg.contains("từ " + targetNodeId) || msg.contains("tới " + targetNodeId)) {
            String realIpPort = null;
            
            Pattern pattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})[: ,](\\d{4})");
            Matcher matcher = pattern.matcher(msg);
            
            while (matcher.find()) {
                String ip = matcher.group(1);
                String port = matcher.group(2);
                if (port.equals(String.valueOf(9000 + targetNodeId))) {
                    realIpPort = ip + ":" + port;
                    break;
                }
            }

            if (realIpPort != null) {
                panelGraph.discoverNode(targetNodeId, realIpPort);
            }
        }
    }

    // =================================================================
    // LỚP VẼ ĐỒ HỌA MẠNG TỰ ĐỘNG CHIA GÓC (HỖ TRỢ LÊN TỚI N MÁY)
    // =================================================================
    private class GraphPanel extends JPanel {
        private String currentState = "RELEASED";
        private java.util.Set<Integer> activeNodes = new java.util.TreeSet<>(); 
        private java.util.Map<Integer, String> nodeIpMap = new java.util.HashMap<>();

        public GraphPanel() {
            setBackground(new Color(24, 24, 27));
            setBorder(new LineBorder(new Color(50, 50, 55), 1));
            
            if (node != null) {
                activeNodes.add(node.getNodeID());
                nodeIpMap.put(node.getNodeID(), node.getNodeIP() + ":" + (9000 + node.getNodeID()));

                for (Node.Neighbor n : node.neighbors) {
                    activeNodes.add(n.getId());          
                    nodeIpMap.put(
                        n.getId(),                       
                        n.getIp() + ":" + n.getPort()    
                    );
                }

            } else {
                activeNodes.add(2);
                nodeIpMap.put(2, "10.251.2.110:9002");
            }
        }

        public void setNodeState(String state) {
            this.currentState = state;
            repaint();
        }

        public void discoverNode(int nodeId, String realIpPort) {
            nodeIpMap.put(nodeId, realIpPort);
            if (!activeNodes.contains(nodeId)) {
                activeNodes.add(nodeId);
                
                boolean existing = false;
                for (int r = 0; r < tableModel.getRowCount(); r++) {
                    if (tableModel.getValueAt(r, 0).equals("P" + nodeId)) {
                        existing = true; break;
                    }
                }
                if (!existing) {
                    tableModel.addRow(new Object[]{"P" + nodeId, realIpPort, "ONLINE", "-", "-", "-"});
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

            int myId = (node != null) ? node.getNodeID() : 2;

            if (activeNodes.size() > 1) {
                g2.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0));
                g2.setColor(new Color(60, 60, 65));
                g2.drawOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
            }

            int totalNodes = activeNodes.size();
            int index = 0;

            for (int currNodeId : activeNodes) {
                double angle = -Math.PI / 2 + (index * 2 * Math.PI / totalNodes);
                int x = centerX + (int) (radius * Math.cos(angle));
                int y = centerY + (int) (radius * Math.sin(angle));
                int nodeSize = 55;

                Color nodeColor = new Color(81, 103, 230); 

                if (currNodeId == myId) {
                    if (currentState.equals("WANTED")) nodeColor = new Color(218, 165, 32);     
                    else if (currentState.equals("HELD")) nodeColor = new Color(198, 40, 40);   
                } else {
                    nodeColor = new Color(45, 156, 219); 
                }

                g2.setColor(nodeColor);
                g2.fillOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);

                g2.setStroke(new BasicStroke(2));
                g2.setColor(currNodeId == myId ? Color.WHITE : new Color(150, 150, 160));
                g2.drawOval(x - nodeSize / 2, y - nodeSize / 2, nodeSize, nodeSize);

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String pName = "P" + currNodeId;
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(pName, x - fm.stringWidth(pName) / 2, y + fm.getAscent() / 2 - 2);

                g2.setFont(new Font("Consolas", Font.PLAIN, 10));
                g2.setColor(COLOR_TEXT_MUTED);
                
                String hostLabel = nodeIpMap.get(currNodeId);
                if (hostLabel == null) {
                    hostLabel = "10.251.2." + (100 + currNodeId) + ":" + (9000 + currNodeId);
                }
                
                g2.drawString(hostLabel, x - g2.getFontMetrics().stringWidth(hostLabel) / 2, y + nodeSize / 2 + 15);
                index++;
            }
        }
    }

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
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
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