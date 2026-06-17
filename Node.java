// import java.io.BufferedReader;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.net.ServerSocket;
// import java.net.Socket;
// import java.util.ArrayList;
// import java.util.List;

// public class Node {
//     public enum State { RELEASED, WANTED, HELD }

//     private final int id;
//     private final int myPort;
//     private final List<Integer> neighborPorts = new ArrayList<>(); // Quản lý hàng xóm bằng PORT mạng
//     private final LamportClock clock = new LamportClock();
//     private final SharedResource sharedResource;

//     private State state = State.RELEASED;
//     private int myRequestTimestamp = 0;
//     private int replyCount = 0;
//     private final List<Integer> deferredQueue = new ArrayList<>();

//     public Node(int id, SharedResource sharedResource) {
//         this.id = id;
//         this.myPort = 9000 + id; // Quy ước Port
//         this.sharedResource = sharedResource;
//     }

//     public void addNeighborPort(int port) {
//         this.neighborPorts.add(port);
//     }

//     public int getId(){
//         return this.id;
//     }
    
//     // ==========================================
//     // PHẦN 1: SERVER NGẦM - LẮNG NGHE MẠNG
//     // ==========================================
//     public void startServer() {
//         Thread serverThread = new Thread(() -> {
//             try {
//                 ServerSocket serverSocket = new ServerSocket();
//                 // Ép OS giải phóng Port ngay lập tức nếu tiến trình trước đó bị crash
//                 serverSocket.setReuseAddress(true);
//                 serverSocket.bind(new java.net.InetSocketAddress(myPort));
                
//                 System.out.println(String.format("[Node %d] Server đang chạy ở port %d...", id, myPort));
//                 while (true) {
//                     Socket socket = serverSocket.accept();
//                     // Tạo Thread phụ xử lý mỗi tin nhắn đến để tránh nghẽn Server
//                     new Thread(() -> handleIncomingConnection(socket)).start();
//                 }
                
                
//             } catch (Exception e) {
//                 System.err.println(String.format("[Node %d] Lỗi Server: %s", id, e.getMessage()));
//             }
//         });
        
//         // Khi tất cả các luồng chính (User Threads) đã làm xong việc và thoát ra, 
//         // JVM sẽ tự động ép chết toàn bộ các Daemon Thread đang chạy ngầm
//         serverThread.setDaemon(true);
        
//         serverThread.start();
//     }

//     private void handleIncomingConnection(Socket socket) {
//         try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            
            
//             String rawMessage = in.readLine();
//             if (rawMessage == null) return;

//             // BƯỚC 12 & 13: Bóc tách tin nhắn mạng dạng "TYPE,SENDER_ID,TIMESTAMP"
//             String[] parts = rawMessage.split(",");
//             String type = parts[0];
//             int senderId = Integer.parseInt(parts[1]);
//             int senderTimestamp = Integer.parseInt(parts[2]);

//             if (type.equals("REQUEST")) {
//                 receiveRequest(senderId, senderTimestamp);
//             } else if (type.equals("REPLY")) {
//                 receiveReply(senderId, senderTimestamp);
//             }
            
            
//         } catch (Exception e) {
//             System.err.println("Lỗi đọc socket: " + e.getMessage());
//         }
//     }

//     // ==========================================
//     // PHẦN 2: CLIENT - GỬI TIN QUA SOCKET
//     // ==========================================
//     private void sendMessageViaSocket(int targetPort, String message) {
//         try (Socket socket = new Socket()) {
//             socket.setReuseAddress(true);
//             // Nếu sau 2 giây không kết nối được tới Node bạn (do họ chưa mở máy), hủy kết nối để tránh treo luồng
//             socket.connect(new java.net.InetSocketAddress("localhost", targetPort), 2000);
            
//             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
//             // Socket socket = new Socket("localhost", targetPort)
//             out.println(message);
            
            
//         } catch (Exception e) {
//             // Log nhẹ lỗi nếu Node kia chưa kịp bật Server
//             System.err.println(String.format("[Node %d] Không thể gửi tới port %d (Node chưa online?)", id, targetPort));
//         }
//     }

//     // ==========================================
//     // PHẦN 3: LOGIC THUẬT TOÁN (Giữ nguyên từ bước trước)
//     // ==========================================
//     public synchronized void requestCriticalSection() {
//         while (this.state != State.RELEASED) {
//             try { wait(); } catch (InterruptedException e) {}
//         }

//         this.state = State.WANTED;
//         clock.increment();
//         // Timestamp của lần request CS hiện tại
//         this.myRequestTimestamp = clock.getTimestamp();
//         this.replyCount = 0;

//         System.out.println(String.format("[Node %d] TRẠNG THÁI: WANTED (T_req=%d)", this.id, this.myRequestTimestamp));
        
//         // Broadcast REQUEST qua mạng Socket
//         for (int port : neighborPorts) {
//             System.out.println(String.format("[Node %d] ---> GỬI REQUEST (T=%d, id=%d) tới Port %d", this.id, this.myRequestTimestamp, this.id, port));
//             String msg = "REQUEST," + this.id + "," + this.myRequestTimestamp;
//             new Thread(() -> sendMessageViaSocket(port, msg)).start();
//         }

//         while (this.state != State.RELEASED) {
//             try { wait(); } catch (InterruptedException e) {}
//         }
//     }

//     public synchronized void receiveRequest(int senderId, int senderTimestamp) {
//         // BƯỚC 14: Đồng bộ Lamport Clock thực tế qua mạng
//         clock.update(senderTimestamp);
        
//         NodeRequest myReq = new NodeRequest(this.myRequestTimestamp, this.id);
//         NodeRequest incomingReq = new NodeRequest(senderTimestamp, senderId);

//         boolean shouldDefer = (this.state == State.HELD) || 
//                 (this.state == State.WANTED && myReq.compareTo(incomingReq) < 0);

//         if (shouldDefer) {
            
//             StringBuilder sb = new StringBuilder();
//             sb.append(String.format(
//                     "[Node %d] <--- NHẬN REQUEST từ Node %d (T_req=%d,id_req=%d). "
//                             + "So sánh:\nmyReq(T=%d, id=%d)\nother(T_req=%d, id_req=%d)\n"
//                             + "Kết quả: Node %d ưu tiên hơn\n"
//                             + "Hành động: Cho Node %d vào hàng đợi, hoãn phản hồi Node %d\n", 
//                     this.id, 
//                     senderId, 
//                     senderTimestamp,
//                     senderId,
//                     this.myRequestTimestamp,
//                     this.id,
//                     senderTimestamp,
//                     senderId,
//                     this.id,
//                     senderId,
//                     senderId
//             ));
            
//             this.deferredQueue.add(senderId);
            
//             sb.append(String.format("[Node %d] Hàng đợi: ", this.id));
//             for(int q : this.deferredQueue){
//                 sb.append(q + " "); 
//             }
//             sb.append("\n");
//             System.out.println(sb.toString());
            
//         } else {
            
//             String msg = "";
//             if(myReq.compareTo(incomingReq) > 0){
//                 msg = this.id + " có độ ưu tiên thấp hơn " + senderId + ". Chờ và phản hồi " + senderId;
//             }
//             else if(this.state == State.RELEASED){
//                 msg = this.id + " không bận, phản hồi " + senderId + " ngay";
//             }
            
            
//             System.out.println(String.format(
//                     "[Node %d] <--- NHẬN REQUEST từ Node %d (T_req=%d)."
//                             + "So sánh:\nmyReq(T=%d, id=%d)\nother(T_req=%d, id_req=%d)\n"
//                             + "Kết quả: " + msg, 
//                     this.id, 
//                     senderId, 
//                     senderTimestamp,
//                     this.myRequestTimestamp,
//                     this.id,
//                     senderTimestamp,
//                     senderId
//             ));
            
//             sendReply(senderId);
//         }
//     }

//     private void sendReply(int targetId) {
//         clock.increment();
//         int targetPort = 9000 + targetId;
//         String msg = "REPLY," + this.id + "," + clock.getTimestamp();
//         System.out.println("Đang gửi phản hồi tới " + targetId + ": " + msg);
//         new Thread(() -> sendMessageViaSocket(targetPort, msg)).start();
//     }

//     public synchronized void receiveReply(int senderId, int senderTimestamp) {      
//         // BƯỚC 14: Đồng bộ Lamport Clock khi nhận REPLY qua mạng
//         clock.update(senderTimestamp);
//         replyCount++;
//         System.out.println(String.format("[Node %d] <--- NHẬN REPLY từ Node %d. Timestamp: %d (Đã có %d/%d REPLY)", 
//                 this.id, senderId, clock.getTimestamp(), replyCount, neighborPorts.size()));

//         if (replyCount == neighborPorts.size()) {
//             this.state = State.HELD;
//             new Thread(() -> enterCriticalSection()).start();
//         }        
//     }

//     private void enterCriticalSection() {
//         sharedResource.access(this.id);
//         this.state = State.RELEASED;
        
//         System.out.println(String.format("[Node %d] Thoát CS. Giải phóng hàng đợi...", this.id));
//         List<Integer> toRelease = new ArrayList<>(deferredQueue);
//         deferredQueue.clear(); 
        
//         for (int nodeId : toRelease) {
//             System.out.println(String.format("[Node %d] ---> TRẢ NỢ REPLY cho Node %d.", this.id, nodeId));
//             sendReply(nodeId);
//         }
        
//         // Chung lock (monitor) với cNode.requestCS.wait()
//         synchronized(this)
//         {
//             notifyAll();
//         }
        
//     }
// }


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

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
    }

    private final int id;
    private final String myIp; 
    private final int myPort;
    private final List<Neighbor> neighbors = new ArrayList<>();
    private final LamportClock clock = new LamportClock();
    private final SharedResource sharedResource;

    private State state = State.RELEASED;
    private int myRequestTimestamp = 0;
    private int replyCount = 0;
    private final List<Neighbor> deferredQueue = new ArrayList<>();

    // Cổng UDP chung cố định cho tất cả các máy để phát hiện nhau
    private static final int DISCOVERY_PORT = 8888; 

    public Node(int id, int myPort, SharedResource sharedResource) {
        this.id = id;
        this.myPort = myPort;
        this.sharedResource = sharedResource;
        this.myIp = autoDiscoverEnvironmentIP(); 
        
        System.out.println(String.format("[Node %d] Đang chạy trên IP cá nhân: %s:%d", this.id, this.myIp, this.myPort));
    }

    private String autoDiscoverEnvironmentIP() {
    try {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            
            // Lọc bỏ card loopback, card đang tắt hoặc card ảo
            if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;

            // Bỏ qua các card mạng ảo của VMware/VirtualBox dựa trên tên hiển thị (Chống sót)
            String displayName = iface.getDisplayName().toLowerCase();
            if (displayName.contains("vmware") || displayName.contains("virtualbox") || displayName.contains("vbox") || displayName.contains("virtual")) {
                continue; 
            }

            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                
                // Chỉ lấy IPv4
                if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                    String ip = addr.getHostAddress();
                    
                    // LÀM ĐIỀU KIỆN ƯU TIÊN: Nếu thấy dải 192 thì chốt luôn và trả về ngay
                    if (ip.startsWith("192.")) {
                        return ip;
                    }
                }
            }
        }

        // Phương án dự phòng 2: Nếu không tìm thấy dải 192 ưu tiên, thì quét lại một lượt lấy IP đầu tiên hợp lệ
        interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface iface = interfaces.nextElement();
            if (iface.isLoopback() || !iface.isUp()) continue;
            Enumeration<InetAddress> addresses = iface.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                    return addr.getHostAddress();
                }
            }
        }
        
        return InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
        return "127.0.0.1";
    }
}

    // ==========================================================
    // CƠ CHẾ TỰ ĐỘNG PHÁT HIỆN HÀNG XÓM BẰNG UDP BROADCAST
    // ==========================================================
    public void startDiscovery() {
        // 1. Thread lắng nghe tín hiệu "Chào hỏi" từ các máy khác
        Thread listenerThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket(DISCOVERY_PORT)) {
                socket.setBroadcast(true);
                byte[] buffer = new byte[1024];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet); // Nhận gói tin phát sóng
                    
                    String message = new String(packet.getData(), 0, packet.getLength()).trim();
                    if (message.startsWith("DISCOVER_NODE")) {
                        // Định dạng gói tin nhận được: DISCOVER_NODE:[ID]:[PORT]
                        String[] parts = message.split(":");
                        int remoteId = Integer.parseInt(parts[1]);
                        int remotePort = Integer.parseInt(parts[2]);
                        String remoteIp = packet.getAddress().getHostAddress();

                        // Nếu không phải là chính mình và chưa có trong danh sách hàng xóm thì tự động thêm vào
                        if (remoteId != this.id) {
                            Neighbor newNeighbor = new Neighbor(remoteId, remoteIp, remotePort);
                            synchronized (neighbors) {
                                if (!neighbors.contains(newNeighbor)) {
                                    neighbors.add(newNeighbor);
                                    System.out.println(String.format("\n[⚡ TỰ ĐỘNG PHÁT HIỆN]: Đã tìm thấy Node %d tại địa chỉ LAN (%s:%d)", remoteId, remoteIp, remotePort));
                                    System.out.print("Nhập lệnh (REQ để chiếm miền găng): ");
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Lỗi tầng dò tìm UDP: " + e.getMessage());
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();

        // 2. Thread liên tục phát sóng định kỳ để báo cho máy khác biết mình đang online
        Thread broadcasterThread = new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                // Gửi thông điệp chứa ID và Port TCP của mình đi khắp mạng LAN
                String broadcastMessage = String.format("DISCOVER_NODE:%d:%d", this.id, this.myPort);
                byte[] buffer = broadcastMessage.getBytes();
                InetAddress broadcastAddress = InetAddress.getByName("255.255.255.255");

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length, broadcastAddress, DISCOVERY_PORT);
                    socket.send(packet);
                    Thread.sleep(2000); // Cứ 2 giây phát sóng một lần
                }
            } catch (Exception e) {
                System.err.println("Lỗi phát sóng UDP: " + e.getMessage());
            }
        });
        broadcasterThread.setDaemon(true);
        broadcasterThread.start();
    }

    public int getNeighborCount() {
        synchronized(neighbors) {
            return neighbors.size();
        }
    }

    // ==========================================
    // SERVER NGẦM LẮNG NGHE TCP (Giữ nguyên logic cũ)
    // ==========================================
    public void startServer() {
        Thread serverThread = new Thread(() -> {
            try (ServerSocket serverSocket = new ServerSocket(myPort)) {
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

    private void handleIncomingConnection(Socket socket) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                String[] parts = line.split(",");
                String type = parts[0];
                int senderId = Integer.parseInt(parts[1]);
                String senderIp = parts[2];
                int senderPort = Integer.parseInt(parts[3]);
                int senderTimestamp = Integer.parseInt(parts[4]);

                Neighbor sender = new Neighbor(senderId, senderIp, senderPort);

                if (type.equals("REQUEST")) {
                    receiveRequest(sender, senderTimestamp);
                } else if (type.equals("REPLY")) {
                    receiveReply(senderId, senderTimestamp);
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
        this.state = State.WANTED;
        clock.increment();
        this.myRequestTimestamp = clock.getTimestamp();
        this.replyCount = 0;

        System.out.println(String.format("\n[Node %d] !!! ĐANG ĐÒI VÀO MIỀN GĂNG !!! (Timestamp = %d)", this.id, myRequestTimestamp));

        synchronized (neighbors) {
            if (neighbors.isEmpty()) {
                System.out.println(String.format("[Node %d] Chưa tìm thấy hàng xóm nào trong mạng. Tự tiến vào miền găng.", this.id));
                new Thread(() -> enterCriticalSection()).start();
                return;
            }

            String msg = String.format("REQUEST,%d,%s,%d,%d", this.id, this.myIp, this.myPort, myRequestTimestamp);
            for (Neighbor neighbor : neighbors) {
                new Thread(() -> sendMessageViaSocket(neighbor.ip, neighbor.port, msg)).start();
            }
        }
    }

    public synchronized void receiveRequest(Neighbor sender, int senderTimestamp) {
        System.out.println(String.format("[Node %d] <--- NHẬN REQUEST từ Node %d [T_req=%d]", this.id, sender.id, senderTimestamp));
        clock.update(senderTimestamp);

        NodeRequest myReq = new NodeRequest(myRequestTimestamp, this.id);
        NodeRequest otherReq = new NodeRequest(senderTimestamp, sender.id);

        boolean holdReply = (this.state == State.HELD) || 
                            (this.state == State.WANTED && myReq.compareTo(otherReq) < 0);

        if (holdReply) {
            System.out.println(String.format("[Node %d] ===> HOÃN BINH Node %d.", this.id, sender.id));
            if (!deferredQueue.contains(sender)) {
                deferredQueue.add(sender);
            }
        } else {
            System.out.println(String.format("[Node %d] ===> CHẤP THUẬN Node %d. REPLY ngay.", this.id, sender.id));
            sendReply(sender);
        }
    }

    private void sendReply(Neighbor target) {
        clock.increment();
        String msg = String.format("REPLY,%d,%s,%d,%d", this.id, this.myIp, this.myPort, clock.getTimestamp());
        new Thread(() -> sendMessageViaSocket(target.ip, target.port, msg)).start();
    }

    public synchronized void receiveReply(int senderId, int senderTimestamp) {      
        clock.update(senderTimestamp);
        replyCount++;
        
        int totalNeighbors = 0;
        synchronized(neighbors) { totalNeighbors = neighbors.size(); }

        System.out.println(String.format("[Node %d] <--- NHẬN REPLY từ Node %d. (Đã thu thập %d/%d REPLY)", 
                this.id, senderId, replyCount, totalNeighbors));

        if (replyCount == totalNeighbors) {
            this.state = State.HELD;
            new Thread(() -> enterCriticalSection()).start();
        }        
    }

    private void enterCriticalSection() {
        sharedResource.access(this.id);
        this.state = State.RELEASED;
        
        System.out.println(String.format("[Node %d] Thoát CS. Giải phóng hàng đợi...", this.id));
        List<Neighbor> toRelease = new ArrayList<>(deferredQueue);
        deferredQueue.clear(); 
        
        for (Neighbor neighbor : toRelease) {
            System.out.println(String.format("[Node %d] ---> TRẢ NỢ REPLY cho Node %d tại (%s:%d)", this.id, neighbor.id, neighbor.ip, neighbor.port));
            sendReply(neighbor);
        }
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