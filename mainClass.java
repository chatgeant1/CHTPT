import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;

public class mainClass {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("SỬ DỤNG: java mainClass [ID_CỦA_MÌNH] [PORT_TCP_CỦA_MÌNH]");
            System.out.println("Ví dụ: java mainClass 1 9001");
            System.exit(1);
        }

        // TEST LOCAL:
        boolean localTest = false;

        // DANH SÁCH CÁC NODE THAM GIA
        List<Node.Neighbor> allNodes = new ArrayList<>();
        allNodes.add(new Node.Neighbor(1, "10.251.9.200", 9001));
        allNodes.add(new Node.Neighbor(2, "10.251.9.215", 9002));

        int myId = Integer.parseInt(args[0]);
        int myPort = Integer.parseInt(args[1]);

        String ip = "";
        for (Node.Neighbor n : allNodes) {
            if (myId == n.id) {
                ip = n.ip;
            }
        }
        SharedResource sharedResource = new SharedResource(myId);
        Node node = new Node(myId, ip, myPort, sharedResource, localTest);    

        if (localTest) { 
            // CÁCH CHẠY: MỞ 2 TERMINAL: GÕ RUN1.BAT, RUN2.BAT
            int totalNodes = 2;
            // Tự động kết nối mạng Full-Mesh theo quy ước Port = 9000 + ID
            for (int i = 1; i <= totalNodes; i++) {
                if (i != myId) {
                    node.addNeighbors(new Node.Neighbor(i, "localhost", 9000+i));
                }
            }
            node.startServer();
        }
        else {
            // CÁCH CHẠY: TERMINAL máy 1: GÕ run1.bat, máy 2: run2.bat, ...
            for (Node.Neighbor n : allNodes) {
                if (n.id != myId) {
                    node.addNeighbors(n);
                }
            }

            // 1. Kích hoạt Server TCP để nhận tin nhắn thuật toán Ricart-Agrawala
            node.startServer();
            // 2. Kích hoạt Server UDP Auto-Discovery tự quét tìm hàng xóm
            // node.startDiscovery();
        }

        // =========================================================
        // KHỞI CHẠY CỬA SỔ GIAO DIỆN GUI
        // =========================================================
        System.out.println("\n>>> ĐANG KHỞI CHẠY GIAO DIỆN GUI CHO NODE " + myId + "...");
        
        javax.swing.SwingUtilities.invokeLater(() -> {
            try {
                AppGUI gui = new AppGUI(node);
                node.setGUI(gui); // Gắn giao diện vào node backend để nhận diện nhau
                gui.setVisible(true); // Bật cửa sổ app lên
            } catch (Exception e) {
                System.err.println("Lỗi khởi chạy giao diện: " + e.getMessage());
            }
        });

        // =========================================================
        // ĐƯA SCANNER VÀO THREAD RIÊNG (Tránh làm đóng băng giao diện)
        // =========================================================
        new Thread(() -> {
            System.out.println("\n=================================================");
            System.out.println(" Có thể gõ 'REQ' ở đây HOẶC bấm nút trên GUI.");
            System.out.println("=================================================\n");
            
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine().trim();
                if (command.equalsIgnoreCase("REQ")) {
                    new Thread(() -> node.requestCriticalSection()).start();
                }
                else if (command.equalsIgnoreCase("EXIT")){
                    scanner.close();
                    System.exit(0);
                }
                else {
                    System.out.println("Lệnh không hợp lệ! Chỉ nhận lệnh 'REQ' hoặc 'EXIT'.");
                }
            }
        }).start();
    }
}