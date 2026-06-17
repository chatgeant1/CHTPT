import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

// public class SharedResource {
//     private static final String FILE_NAME = "critical_section.txt";

//     public void access(int nodeId) {
//         try {
//             // 1. Khi bước vào: Ghi đè file báo hiệu Node này đang chiếm giữ
//             try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
//                 writer.write("Node " + nodeId + " ĐANG ĐỘC CHIẾM MIỀN GĂNG!");
//             }
//             System.out.println(String.format("\n>>> [ENTER] [Node %d] Đã chiếm file %s", nodeId, FILE_NAME));

//             // Mô phỏng làm việc trong miền găng 3 giây
//             Thread.sleep(5000);

//             // 2. Trước khi rời đi: Xóa trắng file hoặc ghi tự do
//             try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
//                 writer.write("Trống - Không có ai.");
//             }
//             System.out.println(String.format("\n<<< [EXIT]  [Node %d] Đã giải phóng file.", nodeId));

//         } catch (IOException | InterruptedException e) {
//             System.err.println("Lỗi thao tác trên Miền Găng: " + e.getMessage());
//         }
//     }
// }


import java.io.PrintWriter;
import java.net.Socket;

public class SharedResource {
    
    
    // SỬA THEO IP CỦA 1 NODE - NODE ĐÓ SẼ LÀM MONITOR HIỂN THỊ "NODE NÀO ĐANG Ở TRONG CS" (MỌI NODE GỬI MSG TỚI ĐỊA CHỈ NÀY)
    private static final String displayHost = "192.168.222.220";
    
    
    private static final int displayPort = 9999;

    public void access(int nodeId, int lamportTimestamp) {
        // Thiết lập kết nối nhanh tới Terminal hiển thị (Máy của bạn)
        try (Socket socket = new Socket();
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            // Timeout 1 giây nếu không kết nối được
            socket.connect(new java.net.InetSocketAddress(displayHost, displayPort), 1000);

            // 1. In thông báo BẮT ĐẦU vào Miền Găng
            out.println(String.format(">>> [ENTER] Node %d đang ĐỘC CHIẾM Miền Găng tại T = %d", nodeId, lamportTimestamp));
            
            // 2. Mô phỏng làm việc trong miền găng 3 giây
            Thread.sleep(5000);

            // 3. In thông báo THOÁT khỏi Miền Găng
            out.println(String.format("<<< [EXIT]  Node %d đã RỜI ĐI.\n", nodeId));

        } catch (Exception e) {
            // Nếu không kết nối được tới màn hình hiển thị, vẫn in tạm ra Terminal local để không hỏng thuật toán
            System.out.println(String.format(">>> [LOCAL-CS] Node %d đang giữ CS tại T = %d (Không kết nối được màn hình chung)", nodeId, lamportTimestamp));
            try { Thread.sleep(3000); } catch (InterruptedException ie) {}
        }
    }
}
