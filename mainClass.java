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
        
        String IP1 = localTest ? "localhost" : "localhost";
        String IP2 = localTest ? "localhost" : "localhost";
        String IP3 = localTest ? "localhost" : "localhost";
        String IP4 = localTest ? "localhost" : "localhost";
        String IP5 = localTest ? "localhost" : "localhost";
        String IP6 = localTest ? "localhost" : "localhost";

        // DANH SÁCH CÁC NODE THAM GIA
        List<Node.Neighbor> allNodes = new ArrayList<>();
        allNodes.add(new Node.Neighbor(1, IP1, 9001));
        allNodes.add(new Node.Neighbor(2, IP2, 9002));
        allNodes.add(new Node.Neighbor(3, IP3, 9003));
        allNodes.add(new Node.Neighbor(4, IP4, 9004));
        allNodes.add(new Node.Neighbor(5, IP5, 9005));
        allNodes.add(new Node.Neighbor(6, IP6, 9006));

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
        
        // CÁCH CHẠY: TERMINAL máy 1: GÕ run1.bat, máy 2: run2.bat, ...
        for (Node.Neighbor n : allNodes) {
            if (n.id != myId) {
                node.addNeighbors(n);
            }
        }

        // Kích hoạt Server TCP để nhận tin nhắn thuật toán Ricart-Agrawala
        node.startServer();

        

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