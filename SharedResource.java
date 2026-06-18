import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class SharedResource {
    
    private final int nodeId;
    private final String fileName;
    private int sharedValue = 0; // "Tài nguyên chung" trong bộ nhớ code

    public SharedResource(int nodeId) {
        this.nodeId = nodeId;
        this.fileName = "./files/"+ "database_node_" + nodeId + ".txt"; // File vật lý riêng của từng Node
    }

    public synchronized void accessAndModify(int currentLamportTime) {
        // Tăng giá trị tài nguyên lên
        this.sharedValue += 10; 
        saveToFile("[Node " + nodeId + "] cập nhật trong CS | Value = " + sharedValue + " | T = " + currentLamportTime);
    }

    // Hàm này dành cho các Node khác bị động gọi khi nhận được lệnh REPLICATE từ mạng
    public synchronized void passiveUpdate(int fromNodeId, int newValue) {
        this.sharedValue = newValue; // Đồng bộ giá trị theo node đang giữ CS
        saveToFile("Đồng bộ từ Node " + fromNodeId + " | Value = " + sharedValue);
    }


    private void saveToFile(String content) {
        try (BufferedWriter writer = Files.newBufferedWriter(
                Paths.get(fileName),
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
                    writer.write(content);
                    writer.newLine();
                } catch (Exception e) {
                    System.err.println("Lỗi ghi file local: " + e.getMessage());
                }
    }

    public int getSharedValue() {
        return this.sharedValue;
    }
}


