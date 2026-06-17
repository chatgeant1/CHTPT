
public class LamportClock {
    private int timestamp = 0;

    // Lấy giá trị thời gian hiện tại
    public synchronized int getTimestamp() {
        return timestamp;
    }

    // Tăng đồng hồ lên 1 (gọi trước khi gửi tin nhắn hoặc khi có sự kiện nội bộ)
    public synchronized void increment() {
        timestamp++;
    }

    // Đồng bộ đồng hồ khi nhận được tin nhắn từ bên ngoài (phản hồi)
    public synchronized void update(int receivedTimestamp) {
        this.timestamp = Math.max(this.timestamp, receivedTimestamp) + 1;
    }

    // Hàm bổ trợ để in ra cho đẹp
    @Override
    public String toString() {
        return "Clock(" + timestamp + ")";
    }
    
    public synchronized void resetClock(){
        this.timestamp = 0;
    }
}
