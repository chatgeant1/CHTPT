import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SharedResource {
    private static final String FILE_NAME = "critical_section.txt";

    public void access(int nodeId) {
        try {
            // 1. Khi bước vào: Ghi đè file báo hiệu Node này đang chiếm giữ
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                writer.write("Node " + nodeId + " ĐANG ĐỘC CHIẾM MIỀN GĂNG!");
            }
            System.out.println(String.format("\n>>> [ENTER] [Node %d] Đã chiếm file %s", nodeId, FILE_NAME));

            // Mô phỏng làm việc trong miền găng 3 giây
            Thread.sleep(5000);

            // 2. Trước khi rời đi: Xóa trắng file hoặc ghi tự do
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
                writer.write("Trống - Không có ai.");
            }
            System.out.println(String.format("\n<<< [EXIT]  [Node %d] Đã giải phóng file.", nodeId));

        } catch (IOException | InterruptedException e) {
            System.err.println("Lỗi thao tác trên Miền Găng: " + e.getMessage());
        }
    }
}
