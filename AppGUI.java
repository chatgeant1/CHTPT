import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;

public class AppGUI extends JFrame {
    private Node node;

    private JLabel lblMyId, lblMyStatus, lblMyClock, lblReplyCount, lblCSStatus, lblSharedValue;
    private JTextArea txtLogs;
    private JButton btnRequest;
    private JPanel panelMainCard;

    // Bảng màu chuẩn Matrix / Terminal Dashboard (Theo ảnh mẫu)
    private final Color COLOR_BG        = new Color(13, 13, 13);      // Nền đen tuyền thẫm
    private final Color COLOR_MATRIX_GREEN = new Color(50, 205, 50);  // Xanh lá neon sáng (LimeGreen)
    private final Color COLOR_TEXT_MUTED = new Color(150, 150, 150);  // Xanh lá tối / Xám cho chữ phụ
    
    // Màu trạng thái của text
    private final Color STATE_RELEASED  = new Color(50, 205, 50);     // Xanh lá sáng
    private final Color STATE_WANTED    = new Color(255, 215, 0);     // Vàng hổ phách
    private final Color STATE_HELD      = new Color(255, 69, 0);      // Đỏ cam rực

    public AppGUI(Node node) {
        this.node = node;

        // Cấu hình cửa sổ ứng dụng giống Dashboard
        setTitle("Ricart-Agrawala Dashboard");
        setSize(650, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BG);

        // Layout chính
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(COLOR_BG);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ==========================================
        // TITLE: RICART-AGRAWALA DASHBOARD
        // ==========================================
        JLabel lblTitle = new JLabel("Ricart-Agrawala Dashboard", SwingConstants.LEFT);
        lblTitle.setFont(new Font("Consolas", Font.BOLD, 28));
        lblTitle.setForeground(COLOR_MATRIX_GREEN);
        lblTitle.setBorder(new EmptyBorder(0, 0, 15, 0));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        // ==========================================
        // CENTER: KHU VỰC THÔNG TIN VÀ ĐIỀU KHIỂN
        // ==========================================
        JPanel centerContainer = new JPanel(new BorderLayout(15, 15));
        centerContainer.setBackground(COLOR_BG);

        // 1. Khung thông tin Tiến trình hiện tại
        panelMainCard = new JPanel(new GridLayout(5, 1, 5, 5));
        panelMainCard.setBackground(COLOR_BG);
        panelMainCard.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(50, 205, 50, 50), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        String idText = node != null ? "Node #" + node.getNodeID() + " (IP: " + node.getNodeIP() + ")" : "Node #Đang kết nối...";
        lblMyId = new JLabel(idText, SwingConstants.LEFT);
        lblMyId.setFont(new Font("Consolas", Font.BOLD, 16));
        lblMyId.setForeground(COLOR_MATRIX_GREEN);
        
        lblMyStatus = new JLabel("Status: RELEASED", SwingConstants.LEFT);
        lblMyStatus.setFont(new Font("Consolas", Font.BOLD, 16));
        lblMyStatus.setForeground(STATE_RELEASED);
        
        lblMyClock = new JLabel("Logical Clock: 0", SwingConstants.LEFT);
        lblMyClock.setFont(new Font("Consolas", Font.PLAIN, 15));
        lblMyClock.setForeground(COLOR_MATRIX_GREEN);
        
        lblReplyCount = new JLabel("Deferred Queue: Empty | Replies Awaited: None", SwingConstants.LEFT);
        lblReplyCount.setFont(new Font("Consolas", Font.PLAIN, 15));
        lblReplyCount.setForeground(COLOR_MATRIX_GREEN);
        
        lblSharedValue = new JLabel("Shared Resource Value: 0", SwingConstants.LEFT);
        lblSharedValue.setFont(new Font("Consolas", Font.ITALIC, 15));
        lblSharedValue.setForeground(COLOR_MATRIX_GREEN);

        panelMainCard.add(lblMyId);
        panelMainCard.add(lblMyStatus);
        panelMainCard.add(lblMyClock);
        panelMainCard.add(lblReplyCount);
        panelMainCard.add(lblSharedValue);
        centerContainer.add(panelMainCard, BorderLayout.NORTH);

        // 2. Khung trạng thái Miền Găng & Nút bấm
        JPanel actionPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        actionPanel.setBackground(COLOR_BG);

        lblCSStatus = new JLabel(">> CRITICAL SECTION: EMPTY", SwingConstants.LEFT);
        lblCSStatus.setFont(new Font("Consolas", Font.BOLD, 16));
        lblCSStatus.setForeground(COLOR_MATRIX_GREEN);
        actionPanel.add(lblCSStatus);

        // Nút bấm REQUEST chuẩn giao diện Dashboard phẳng
        btnRequest = new JButton("⚡ REQUEST CS");
        btnRequest.setFont(new Font("Consolas", Font.BOLD, 14));
        btnRequest.setBackground(Color.WHITE);
        btnRequest.setForeground(Color.BLACK);
        btnRequest.setFocusPainted(false);
        btnRequest.setBorder(new LineBorder(Color.WHITE, 1));
        btnRequest.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnRequest.addActionListener(e -> {
            if (node != null) {
                new Thread(() -> node.requestCriticalSection()).start();
            }
        });
        
        // Bọc nút bấm vào flow panel để nó không bị kéo dãn to đùng
        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnWrapper.setBackground(COLOR_BG);
        btnWrapper.add(btnRequest);
        actionPanel.add(btnWrapper);

        centerContainer.add(actionPanel, BorderLayout.CENTER);
        mainPanel.add(centerContainer, BorderLayout.CENTER);

        // ==========================================
        // SOUTH: KHU VỰC LOGS TERMINAL (BASH CONSOLE)
        // ==========================================
        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBackground(COLOR_BG);
        
        JLabel lblConsoleTitle = new JLabel("bash - system_console.log", SwingConstants.LEFT);
        lblConsoleTitle.setFont(new Font("Consolas", Font.PLAIN, 15));
        lblConsoleTitle.setForeground(COLOR_MATRIX_GREEN);
        logPanel.add(lblConsoleTitle, BorderLayout.NORTH);
        
        txtLogs = new JTextArea(12, 50);
        txtLogs.setEditable(false);
        txtLogs.setBackground(COLOR_BG);
        txtLogs.setForeground(COLOR_MATRIX_GREEN);
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtLogs.setBorder(new LineBorder(new Color(50, 205, 50, 40), 1));
        
        // Thêm một chút padding trong ô văn bản
        txtLogs.setMargin(new java.awt.Insets(8, 8, 8, 8));
        
        JScrollPane scrollPane = new JScrollPane(txtLogs);
        scrollPane.setBorder(null);
        logPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    // Hàm cập nhật trạng thái đồng bộ thời gian thực theo ảnh mẫu
    public void updateUI(String state, int clockValue, int currentReply, int totalNeighbors, int sharedVal) {
        SwingUtilities.invokeLater(() -> {
            lblMyClock.setText("Logical Clock: " + clockValue);
            lblSharedValue.setText("Shared Resource Value: " + sharedVal);
            lblMyStatus.setText("Status: " + state);

            if (state.equals("RELEASED")) {
                lblMyStatus.setForeground(STATE_RELEASED);
                lblCSStatus.setText(">> CRITICAL SECTION: EMPTY");
                lblCSStatus.setForeground(COLOR_MATRIX_GREEN);
                lblReplyCount.setText("Deferred Queue: Empty | Replies Awaited: None");
                
                btnRequest.setText("⚡ REQUEST CS");
                btnRequest.setBackground(Color.WHITE);
                btnRequest.setForeground(Color.BLACK);
                btnRequest.setEnabled(true);
            } else if (state.equals("WANTED")) {
                lblMyStatus.setForeground(STATE_WANTED);
                lblCSStatus.setText(">> CRITICAL SECTION: WAITING FOR REPLIES...");
                lblCSStatus.setForeground(STATE_WANTED);
                lblReplyCount.setText("Deferred Queue: Empty | Replies Awaited: " + (totalNeighbors - currentReply));
                
                btnRequest.setText("⏳ WAITING...");
                btnRequest.setBackground(new Color(40, 40, 40));
                btnRequest.setForeground(Color.GRAY);
                btnRequest.setEnabled(false);
            } else if (state.equals("HELD")) {
                lblMyStatus.setForeground(STATE_HELD);
                lblCSStatus.setText(">> CRITICAL SECTION: OCCUPIED BY ME ❌");
                lblCSStatus.setForeground(STATE_HELD);
                lblReplyCount.setText("Deferred Queue: Managing | Replies Awaited: 0");
            }
        });
    }

    public void appendLog(String message) {
        SwingUtilities.invokeLater(() -> {
            txtLogs.append(">> " + message + "\n");
            txtLogs.setCaretPosition(txtLogs.getDocument().getLength());
        });
    }
}