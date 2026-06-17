// import java.util.Scanner;

// public class mainClass {
//     public static void main(String[] args) throws Exception {
        
//     new testClass().testNodeSR();
//     }
// }

// // MAIN TEST:
// //// Kiểm tra xem người dùng có truyền ID vào không
// //        if (args.length < 1) {
// //            System.err.println("LỖI: Vui lòng nhập Node ID khi chạy! Ví dụ: java Main 1");
// //            System.exit(1);
// //        }
// //
// //        int myId = Integer.parseInt(args[0]);
// //        int totalNodes = 3; // Thử nghiệm hệ thống cấu hình 3 Node 
// //
// //        SharedResource sharedResource = new SharedResource();
// //        Node node = new Node(myId, sharedResource);
// //
// //        // Tự động kết nối mạng Full-Mesh theo quy ước Port = 9000 + ID
// //        for (int i = 1; i <= totalNodes; i++) {
// //            if (i != myId) {
// //                node.addNeighborPort(9000 + i);
// //            }
// //        }
// //
// //        // Kích hoạt Server của Node này để mở cổng chờ hàng xóm kết nối
// //        node.startServer();
// //        Thread.sleep(1000); // Chờ 1 giây cho cổng mạng mở mượt mà
// //
// //        System.out.println("\n=================================================");
// //        System.out.println(String.format("  HỆ THỐNG PHÂN TÁN - NODE %d ĐÃ ONLINE (Port %d)", myId, (9000 + myId)));
// //        System.out.println("  Gõ 'REQ' và nhấn Enter để tranh chấp Miền Găng.");
// //        System.out.println("=================================================\n");
// //
// //        // Vòng lặp đọc lệnh từ bàn phím Terminal
// //        Scanner scanner = new Scanner(System.in);
// //        while (scanner.hasNextLine()) {
// //            String command = scanner.nextLine().trim();
// //            
// //            if (command.equalsIgnoreCase("REQ")) {
// //                // Tạo một Thread chạy ngầm để đi đòi quyền vào miền găng, không làm treo bàn phím
// //                new Thread(() -> node.requestCriticalSection()).start();
// //            } else {
// //                System.out.println("Lệnh không hợp lệ! Chỉ nhận lệnh 'REQ'.");
// //            }
// //        }


import java.util.Scanner;

public class mainClass {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("SỬ DỤNG: java mainClass [ID_CỦA_MÌNH] [PORT_TCP_CỦA_MÌNH]");
            System.out.println("Ví dụ: java mainClass 1 9001");
            System.exit(1);
        }

        int myId = Integer.parseInt(args[0]);
        int myPort = Integer.parseInt(args[1]);

        SharedResource sharedResource = new SharedResource();
        Node node = new Node(myId, myPort, sharedResource);

        // 1. Kích hoạt Server TCP để nhận tin nhắn thuật toán Ricart-Agrawala
        node.startServer();

        // 2. Kích hoạt Server UDP Auto-Discovery tự quét tìm hàng xóm
        node.startDiscovery();

        System.out.println("\n=================================================");
        System.out.println(" HỆ THỐNG ĐANG TỰ ĐỘNG DÒ TÌM THIẾT BỊ TRONG LAN... ");
        System.out.println(" Gõ 'REQ' và nhấn Enter để tranh chấp Miền Găng.");
        System.out.println("=================================================\n");

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String command = scanner.nextLine().trim();
            if (command.equalsIgnoreCase("REQ")) {
                node.requestCriticalSection();
            }
        }
    }
}