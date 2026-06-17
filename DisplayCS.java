import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DisplayCS {
    public static void main(String[] args) {
        int displayPort = 9999; // Cổng phụ chuyên hứng log Miền Găng
        try (ServerSocket serverSocket = new ServerSocket(displayPort)) {
            System.out.println("=== MÀN HÌNH HIỂN THỊ MIỀN GĂNG ĐÃ ONLINE (Port " + displayPort + ") ===");
            System.out.println("Đang đợi dữ liệu từ các Node phân tán...\n");

            while (true) {
                try (Socket socket = serverSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                    
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (Exception e) {
                    // Lỗi kết nối nhỏ, bỏ qua để lắng nghe lượt tiếp theo
                }
            }
        } catch (Exception e) {
            System.err.println("Không thể mở cổng hiển thị: " + e.getMessage());
        }
    }
}
