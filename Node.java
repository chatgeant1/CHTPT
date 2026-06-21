import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
// import java.net.DatagramPacket;
// import java.net.DatagramSocket;
// import java.net.InetAddress;
import java.net.InetSocketAddress;
// import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
// import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

public class Node {
    public enum State { RELEASED, WANTED, HELD }

    public static class Neighbor {
        int id;
        String ip;
        int port;

        public Neighbor(int id, String ip, int port) {
            this.id = id;
            this.ip = ip;
            this.port = port;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Neighbor) {
                Neighbor other = (Neighbor) obj;
                return this.id == other.id;
            }
            return false;
        }

        public int getId() { return id; }
        public String getIp() { return ip; }
        public int getPort() { return port; }

    }

    private final int id;
    private final String myIp; 
    private final int myPort;

    public final List<Neighbor> neighbors = new ArrayList<>();
    private final LamportClock clock = new LamportClock();
    private final SharedResource sharedResource;

    private State state = State.RELEASED;
    private int myRequestTimestamp = 0;
    private int replyCount = 0;
    private final List<Neighbor> deferredQueue = new ArrayList<>();

    // =========================================================
    // KHÚC 1: BIẾN KẾT NỐI SANG GIAO DIỆN APPGUI
    // =========================================================
    private AppGUI gui;

    public void setGUI(AppGUI gui) {
        this.gui = gui;
        triggerGuiUpdate();
    }


    private void triggerGuiUpdate() {
        if (this.gui != null) {
            String queueText = deferredQueue.isEmpty()
            ? "Empty"
            : deferredQueue.stream()
                .map(n -> "P" + n.id)
                .collect(Collectors.joining(","));

            this.gui.updateUI(this.state.toString(), this.clock.getTimestamp(), this.replyCount, getNeighborCount(), sharedResource.getSharedValue(), queueText);
        }
    }

    private void logToBoth(String msg) {
        System.out.println(msg); 
        if (this.gui != null) {
            this.gui.appendLog(msg);
        }
    }

    // Cổng UDP chung cố định cho tất cả các máy để phát hiện nhau
    private static final int DISCOVERY_PORT = 8888; 

    public Node(int id, String ip, int myPort, SharedResource sharedResource, boolean localTest) {
        this.id = id;
        this.myPort = myPort;
        this.sharedResource = sharedResource;
        this.myIp = localTest ? "localhost" : ip; 
    }

    public void addNeighbors(Neighbor nb){
        neighbors.add(nb);
    }
    public String getNodeIP(){
        return this.myIp;
    }
    public int getNodeID(){
        return this.id;
    }

// ================================================================================================================================================================================================================================
//     private String autoDiscoverEnvironmentIP() {
//     try {
//         Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
//         while (interfaces.hasMoreElements()) {
//             NetworkInterface iface = interfaces.nextElement();
            
//             // Lọc bỏ card loopback, card đang tắt hoặc card ảo
//             if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;

//             // Bỏ qua các card mạng ảo của VMware/VirtualBox dựa trên tên hiển thị (Chống sót)
//             String displayName = iface.getDisplayName().toLowerCase();
//             if (displayName.contains("vmware") || displayName.contains("virtualbox") || displayName.contains("vbox") || displayName.contains("virtual")) {
//                 continue; 
//             }

//             Enumeration<InetAddress> addresses = iface.getInetAddresses();
//             while (addresses.hasMoreElements()) {
//                 InetAddress addr = addresses.nextElement();
                
//                 // Chỉ lấy IPv4
//                 if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
//                     String ip = addr.getHostAddress();
                    
//                     // LÀM ĐIỀU KIỆN ƯU TIÊN: Nếu thấy dải 192 thì chốt luôn và trả về ngay
//                     if (ip.startsWith("192.")) {
//                         return ip;
//                     }
//                 }
//             }
//         }

//         // Phương án dự phòng 2: Nếu không tìm thấy dải 192 ưu tiên, thì quét lại một lượt lấy IP đầu tiên hợp lệ
//         interfaces = NetworkInterface.getNetworkInterfaces();
//         while (interfaces.hasMoreElements()) {

//             NetworkInterface iface = interfaces.nextElement();
//             if (iface.isLoopback() || !iface.isUp()) continue;

//             Enumeration<InetAddress> addresses = iface.getInetAddresses();
//             while (addresses.hasMoreElements()) {
//                 InetAddress addr = addresses.nextElement();
//                 if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
//                     return addr.getHostAddress();
//                 }
//             }

//         }
        
//         return InetAddress.getLocalHost().getHostAddress();
//     } catch (Exception e) {
//         return "127.0.0.1";
//     }
// }
// // ================================================================================================================================================================================================================================
//     // ==========================================================
//     // CƠ CHẾ TỰ ĐỘNG PHÁT HIỆN HÀNG XÓM BẰNG UDP BROADCAST
//     // ==========================================================
//     public void startDiscovery() {
//         // 1. Thread lắng nghe tín hiệu "Chào hỏi" từ các máy khác
//         Thread listenerThread = new Thread(() -> {
            
//             try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {

//                 logToBoth("UDP listener started on " + DISCOVERY_PORT);
//                 socket.setBroadcast(true);
//                 byte[] buffer = new byte[1024];
                
//                 while (true) {
//                     DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                     socket.receive(packet); // Nhận gói tin phát sóng
                    
//                     String message = new String(packet.getData(), 0, packet.getLength()).trim();
//                     if (message.startsWith("DISCOVER_NODE")) {
//                         // Định dạng gói tin nhận được: DISCOVER_NODE:[ID]:[PORT]
//                         String[] parts = message.split(":");
//                         int remoteId = Integer.parseInt(parts[1]);
//                         int remotePort = Integer.parseInt(parts[2]);
//                         String remoteIp = packet.getAddress().getHostAddress();

//                         // Nếu không phải là chính mình và chưa có trong danh sách hàng xóm thì tự động thêm vào
//                         if (remoteId != this.id) {
//                             Neighbor newNeighbor = new Neighbor(remoteId, remoteIp, remotePort);
//                             synchronized (neighbors) {
//                                 if (!neighbors.contains(newNeighbor)) {
//                                     neighbors.add(newNeighbor);
//                                     logToBoth(String.format("\n[TỰ ĐỘNG PHÁT HIỆN]: Đã tìm thấy Node %d tại địa chỉ LAN (%s:%d)", remoteId, remoteIp, remotePort));
//                                     System.out.print("Nhập lệnh (REQ để chiếm miền găng): ");
//                                 }
//                             }
//                         }
//                     }
//                 }
//             } catch (Exception e) {
//                 System.err.println("Lỗi tầng dò tìm UDP: " + e.getMessage());
//             }
//         });
//         listenerThread.setDaemon(true);
//         listenerThread.start();

//         // 2. Thread liên tục phát sóng định kỳ để báo cho máy khác biết mình đang online
//         Thread broadcasterThread = new Thread(() -> {
//             try (DatagramSocket socket = new DatagramSocket()) {
//                 socket.setBroadcast(true);
                
//                 // Gửi thông điệp chứa ID và Port TCP của mình đi khắp mạng LAN
//                 String broadcastMessage = String.format("DISCOVER_NODE:%d:%d", this.id, this.myPort);
//                 byte[] buffer = broadcastMessage.getBytes();
//                 InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

//                 while (true) {
//                     DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, DISCOVERY_PORT);
//                     socket.send(packet);
//                     Thread.sleep(2000); // Cứ 2 giây phát sóng một lần
//                 }
//             } catch (Exception e) {
//                 System.err.println("Lỗi phát sóng UDP: " + e.getMessage());
//             }
//         });
//         broadcasterThread.setDaemon(true);
//         broadcasterThread.start();
//     }

    public int getNeighborCount() {
        synchronized(neighbors) {
            return neighbors.size();
        }
    }

    // ==========================================
    // SERVER NGẦM LẮNG NGHE TCP 
    // ==========================================
    public void startServer() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket()) {

            // chỉ là “lắng nghe tất cả interface”
            // Mạng public + chưa rule firewall inbound port: localhost + ko tcp từ bên ngoài vào dc 
            // Mạng private + có rule firewall inbound port: localhost + tcp từ ngoài vào dc (mạng có password)
            serverSocket.bind(new InetSocketAddress("0.0.0.0", myPort));

            logToBoth(String.format("[Node %d] Server (IP:%s) đang chạy ở port %d...", id, myIp, myPort));

                while (true) {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> handleIncomingConnection(socket)).start();
                }
            } catch (Exception e) {
                System.err.println(String.format("[Node %d] Lỗi TCP Server: %s", id, e.getMessage()));
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();
    }

    // sendMessageViaSocket, truyền msg tới socket của node bên kia, bên kia (server) accept và gọi handle này:
    private void handleIncomingConnection(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                // type, id, ip, port, msg
                String[] parts = line.split(",");
                String type = parts[0];
                int senderId = Integer.parseInt(parts[1]);
                String senderIp = parts[2];
                int senderPort = Integer.parseInt(parts[3]);
                int senderTimestamp = Integer.parseInt(parts[4]);

                Neighbor sender = new Neighbor(senderId, senderIp, senderPort);

                // Node này Nhận request của bên gửi:
                if (type.equals("REQUEST")) {
                    receiveRequest(sender, senderTimestamp);
                } 
                // Node này nhận reply từ bên gửi
                else if (type.equals("REPLY")) {
                    receiveReply(senderId, senderTimestamp);
                }
                // Node nhận replicate
                else if (type.equals("REPLICATE")) {
                    // Nhận lệnh đồng bộ dữ liệu từ node đang ở trong CS
                    int newValue = Integer.parseInt(parts[4]);
                    sharedResource.passiveUpdate(senderId, newValue);
                    logToBoth(String.format("[HỆ THỐNG] Đã đồng bộ dữ liệu theo Node %d (Value mới = %d)", senderId, newValue));
                }
            }
        } catch (Exception e) {
            System.err.println(String.format("[Node %d] Lỗi nhận tin nhắn TCP: %s", id, e.getMessage()));
        }
    }

    // ==========================================
    // LOGIC RICART-AGRAWALA (Được tối ưu động)
    // ==========================================
    public synchronized void requestCriticalSection() {

        // Tránh request nhiều lần liên tục khi 1 lần request chưa hoàn thành
        while (this.state != State.RELEASED) {
            try { wait(); } catch (InterruptedException e) {}
        }

        this.state = State.WANTED;
        clock.increment();

        // Thời gian timestamp bắt đầu request vào CS
        this.myRequestTimestamp = clock.getTimestamp();
        this.replyCount = 0;

        triggerGuiUpdate();

        logToBoth(String.format("[Node %d] TRẠNG THÁI: WANTED (T_req=%d)", this.id, this.myRequestTimestamp));

        synchronized (neighbors) {
            if (neighbors.isEmpty()) {
                logToBoth(String.format("[Node %d] Chưa tìm thấy hàng xóm nào trong mạng. Tự tiến vào miền găng.", this.id));
                this.state = State.HELD;
                new Thread(() -> enterCriticalSection()).start();
                return;
            }

            // Duyệt danh sách, gửi request tới từng hàng xóm 
            String msg = String.format("REQUEST,%d,%s,%d,%d", this.id, this.myIp, this.myPort, myRequestTimestamp);
            for (Neighbor neighbor : neighbors) {
                logToBoth(String.format("[Node %d] ---> GỬI REQUEST (T=%d, id=%d) tới Node %d:%s:%d", this.id, this.myRequestTimestamp, this.id, neighbor.id, neighbor.ip, neighbor.port));
                new Thread(() -> sendMessageViaSocket(neighbor.ip, neighbor.port, msg)).start();
            }
        }

        // Kết thúc while khi notifyAll() tại enterCS (kết thúc quá trình request CS)
        while (this.state != State.RELEASED) {
            try { wait(); } catch (InterruptedException e) {}
        }
    }

    public synchronized void receiveRequest(Neighbor sender, int senderTimestamp) {
        logToBoth(String.format("[Node %d] <--- NHẬN REQUEST từ Node %d [T_req=%d]", this.id, sender.id, senderTimestamp));
        clock.update(senderTimestamp);

        // So sánh 2 request (n1 -> n2, n2 -> n1)
        NodeRequest myReq = new NodeRequest(myRequestTimestamp, this.id);
        NodeRequest otherReq = new NodeRequest(senderTimestamp, sender.id);

        boolean holdReply = (this.state == State.HELD) || 
                            (this.state == State.WANTED && myReq.compareTo(otherReq) < 0);

        if (holdReply) {
            
            // Logging:
            StringBuilder sb = new StringBuilder();
            sb.append(String.format(
                    "[Node %d] <--- NHẬN REQUEST từ Node %d (T_req=%d,id_req=%d). "
                            + "So sánh:\nmyReq(T=%d, id=%d)\nother(T_req=%d, id_req=%d)\n"
                            + "Kết quả: Node %d ưu tiên hơn\n"
                            + "Hành động: Cho Node %d vào hàng đợi, hoãn phản hồi Node %d\n", 
                    this.id, sender.id, senderTimestamp, sender.id, this.myRequestTimestamp,
                    this.id, senderTimestamp, sender.id, this.id, sender.id, sender.id
            ));

            // Thêm vào hàng đợi hoãn:
            if (!deferredQueue.contains(sender)) {
                deferredQueue.add(sender);
            }

            triggerGuiUpdate();

            // In danh sách hàng đợi:
            sb.append(String.format("[Node %d] Hàng đợi: ", this.id));
            for(Neighbor q : this.deferredQueue){
                sb.append(q.id + " "); 
            }
            sb.append("\n");
            logToBoth(sb.toString());

        } else {

            // Logging: 
            String msg = "";
            if(myReq.compareTo(otherReq) > 0){
                msg = this.id + " có độ ưu tiên thấp hơn " + sender.id + ". Chờ và phản hồi " + sender.id;
            }
            else if(this.state == State.RELEASED){
                msg = this.id + " không bận, phản hồi " + sender.id + " ngay";
            }
            logToBoth(String.format(
                    "[Node %d] <--- NHẬN REQUEST từ Node %d (T_req=%d)."
                            + "So sánh:\nmyReq(T=%d, id=%d)\nother(T_req=%d, id_req=%d)\n"
                            + "Kết quả: " + msg, 
                    this.id, sender.id, senderTimestamp, this.myRequestTimestamp,
                    this.id, senderTimestamp, sender.id
            ));

            // Gửi phản hồi tới người gửi request
            sendReply(sender);
        }

    }

    private void sendReply(Neighbor target) {
        clock.increment();
        triggerGuiUpdate();
        String msg = String.format("REPLY,%d,%s,%d,%d", this.id, this.myIp, this.myPort, clock.getTimestamp());
        logToBoth("Đang gửi phản hồi tới " + target.id + ": " + msg);
        new Thread(() -> sendMessageViaSocket(target.ip, target.port, msg)).start();
    }

    public synchronized void receiveReply(int senderId, int senderTimestamp) {      
        clock.update(senderTimestamp);
        replyCount++;
        
        triggerGuiUpdate();

        int totalNeighbors = 0;
        synchronized(neighbors) { totalNeighbors = neighbors.size(); }

        logToBoth(String.format("[Node %d] <--- NHẬN REPLY từ Node %d. (Đã thu thập %d/%d REPLY)", 
                this.id, senderId, replyCount, totalNeighbors));

        // Nếu đủ số reply thì vào CS
        if (replyCount == totalNeighbors) {
            this.state = State.HELD;
            new Thread(() -> enterCriticalSection()).start();
        }        
    }

    private void enterCriticalSection() {
        this.state = State.HELD; 
        triggerGuiUpdate();

        logToBoth(String.format(">>> [ENTER] Node %d bắt đầu độc chiếm tài nguyên", this.id));
        
        // 1. Thay đổi dữ liệu trên chính máy mình
        sharedResource.accessAndModify(this.clock.getTimestamp());

        // 2. Phát lệnh mạng ép các Node khác đồng bộ theo giá trị mới qua Socket đang có sẵn
        int newValue = sharedResource.getSharedValue();
        for (Neighbor neighbor : neighbors) { 
            // "REQUEST,%d,%s,%d,%d", this.id, this.myIp, this.myPort, myRequestTimestamp)
            String syncMsg = String.format("REPLICATE,%d,%s,%d,%d", this.id, this.myIp, this.myPort, newValue);
            new Thread(() -> sendMessageViaSocket(neighbor.ip, neighbor.port, syncMsg)).start();
        }

        // Giả lập làm việc trong CS 2 giây
        try { Thread.sleep(7000); } catch (InterruptedException e) {}

        logToBoth(String.format("<<< [EXIT] Node %d rời miền găng.", this.id));

        this.state = State.RELEASED;
        
        triggerGuiUpdate();
        logToBoth(String.format("[Node %d] Thoát CS. Giải phóng hàng đợi...", this.id));
        List<Neighbor> toRelease = new ArrayList<>(deferredQueue);
        deferredQueue.clear(); 
        
        triggerGuiUpdate();

        for (Neighbor neighbor : toRelease) {
            logToBoth(String.format("[Node %d] ---> TRẢ NỢ REPLY cho Node %d tại (%s:%d)", this.id, neighbor.id, neighbor.ip, neighbor.port));
            sendReply(neighbor);
        }

        // Thông báo cho requestCS.wait() tỉnh và kết thúc request, Chung lock (monitor) với cNode.requestCS.wait()
        synchronized(this){notifyAll();}

    }

    private void sendMessageViaSocket(String targetIp, int targetPort, String message) {
        try (Socket socket = new Socket(targetIp, targetPort);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)) {
            writer.println(message);
        } catch (Exception e) {
            System.err.println(String.format("[Node %d] Không thể kết nối tới %s:%d", this.id, targetIp, targetPort));
        }
    }
}