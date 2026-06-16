import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Node {
    public enum State { RELEASED, WANTED, HELD }

    private final int id;
    private final int myPort;
    private final List<Integer> neighborPorts = new ArrayList<>(); // Quản lý hàng xóm bằng PORT mạng
    private final LamportClock clock = new LamportClock();
    private final SharedResource sharedResource;

    private State state = State.RELEASED;
    private int myRequestTimestamp = 0;
    private int replyCount = 0;
    private final List<Integer> deferredQueue = new ArrayList<>();

    public Node(int id, SharedResource sharedResource) {
        this.id = id;
        this.myPort = 9000 + id; // Quy ước Port
        this.sharedResource = sharedResource;
    }

    public void addNeighborPort(int port) {
        this.neighborPorts.add(port);
    }

    public int getId(){
        return this.id;
    }
    
    // ==========================================
    // PHẦN 1: SERVER NGẦM - LẮNG NGHE MẠNG
    // ==========================================
    public void startServer() {
        Thread serverThread = new Thread(() -> {
            try {
                ServerSocket serverSocket = new ServerSocket();
                // Ép OS giải phóng Port ngay lập tức nếu tiến trình trước đó bị crash
                serverSocket.setReuseAddress(true);
                serverSocket.bind(new java.net.InetSocketAddress(myPort));
                
                System.out.println(String.format("[Node %d] Server đang chạy ở port %d...", id, myPort));
                while (true) {
                    Socket socket = serverSocket.accept();
                    // Tạo Thread phụ xử lý mỗi tin nhắn đến để tránh nghẽn Server
                    new Thread(() -> handleIncomingConnection(socket)).start();
                }
                
                
            } catch (Exception e) {
                System.err.println(String.format("[Node %d] Lỗi Server: %s", id, e.getMessage()));
            }
        });
        
        // Khi tất cả các luồng chính (User Threads) đã làm xong việc và thoát ra, 
        // JVM sẽ tự động ép chết toàn bộ các Daemon Thread đang chạy ngầm
        serverThread.setDaemon(true);
        
        serverThread.start();
    }

    private void handleIncomingConnection(Socket socket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            
            String rawMessage = in.readLine();
            if (rawMessage == null) return;

            // BƯỚC 12 & 13: Bóc tách tin nhắn mạng dạng "TYPE,SENDER_ID,TIMESTAMP"
            String[] parts = rawMessage.split(",");
            String type = parts[0];
            int senderId = Integer.parseInt(parts[1]);
            int senderTimestamp = Integer.parseInt(parts[2]);

            if (type.equals("REQUEST")) {
                receiveRequest(senderId, senderTimestamp);
            } else if (type.equals("REPLY")) {
                receiveReply(senderId, senderTimestamp);
            }
            
            
        } catch (Exception e) {
            System.err.println("Lỗi đọc socket: " + e.getMessage());
        }
    }

    // ==========================================
    // PHẦN 2: CLIENT - GỬI TIN QUA SOCKET
    // ==========================================
    private void sendMessageViaSocket(int targetPort, String message) {
        try (Socket socket = new Socket()) {
            socket.setReuseAddress(true);
            // Nếu sau 2 giây không kết nối được tới Node bạn (do họ chưa mở máy), hủy kết nối để tránh treo luồng
            socket.connect(new java.net.InetSocketAddress("localhost", targetPort), 2000);
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // Socket socket = new Socket("localhost", targetPort)
            out.println(message);
            
            
        } catch (Exception e) {
            // Log nhẹ lỗi nếu Node kia chưa kịp bật Server
            System.err.println(String.format("[Node %d] Không thể gửi tới port %d (Node chưa online?)", id, targetPort));
        }
    }

    // ==========================================
    // PHẦN 3: LOGIC THUẬT TOÁN (Giữ nguyên từ bước trước)
    // ==========================================
    public synchronized void requestCriticalSection() {
        while (this.state != State.RELEASED) {
            try { wait(); } catch (InterruptedException e) {}
        }

        this.state = State.WANTED;
        clock.increment();
        this.myRequestTimestamp = clock.getTimestamp();
        this.replyCount = 0;

        System.out.println(String.format("[Node %d] TRẠNG THÁI: WANTED (T_req=%d)", this.id, this.myRequestTimestamp));
        
        // Broadcast REQUEST qua mạng Socket
        for (int port : neighborPorts) {
            System.out.println(String.format("[Node %d] ---> GỬI REQUEST (T=%d) tới Port %d", this.id, this.myRequestTimestamp, port));
            String msg = "REQUEST," + this.id + "," + this.myRequestTimestamp;
            new Thread(() -> sendMessageViaSocket(port, msg)).start();
        }

        while (this.state != State.RELEASED) {
            try { wait(); } catch (InterruptedException e) {}
        }
    }

    public synchronized void receiveRequest(int senderId, int senderTimestamp) {
        // BƯỚC 14: Đồng bộ Lamport Clock thực tế qua mạng
        clock.update(senderTimestamp);
        
        NodeRequest myReq = new NodeRequest(this.myRequestTimestamp, this.id);
        NodeRequest incomingReq = new NodeRequest(senderTimestamp, senderId);

        boolean shouldDefer = (this.state == State.HELD) || 
                (this.state == State.WANTED && myReq.compareTo(incomingReq) < 0);

        if (shouldDefer) {
            System.out.println(String.format("[Node %d] <--- NHẬN REQUEST từ Node %d (T_req=%d). !!! HOÃN BINH !!!", this.id, senderId, senderTimestamp));
            this.deferredQueue.add(senderId);
        } else {
            System.out.println(String.format("[Node %d] <--- NHẬN REQUEST từ Node %d (T_req=%d). REPLY ngay.", this.id, senderId, senderTimestamp));
            sendReply(senderId);
        }
    }

    private void sendReply(int targetId) {
        clock.increment();
        int targetPort = 9000 + targetId;
        String msg = "REPLY," + this.id + "," + clock.getTimestamp();
        new Thread(() -> sendMessageViaSocket(targetPort, msg)).start();
    }

    public synchronized void receiveReply(int senderId, int senderTimestamp) {
        // BƯỚC 14: Đồng bộ Lamport Clock khi nhận REPLY qua mạng
        clock.update(senderTimestamp);
        replyCount++;
        System.out.println(String.format("[Node %d] <--- NHẬN REPLY từ Node %d. (Đã có %d/%d REPLY)", 
                this.id, senderId, replyCount, neighborPorts.size()));

        if (replyCount == neighborPorts.size()) {
            enterCriticalSection();
        }
    }

    private void enterCriticalSection() {
        this.state = State.HELD;
        sharedResource.access(this.id);
        this.state = State.RELEASED;
        
        System.out.println(String.format("[Node %d] Thoát CS. Giải phóng hàng đợi...", this.id));
        List<Integer> toRelease = new ArrayList<>(deferredQueue);
        deferredQueue.clear(); 
        
        for (int nodeId : toRelease) {
            System.out.println(String.format("[Node %d] ---> TRẢ NỢ REPLY cho Node %d.", this.id, nodeId));
            sendReply(nodeId);
        }
        notifyAll();
    }
}
