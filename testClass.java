// import java.util.ArrayList;
// import java.util.List;

// public class testClass {
//     public void testClock(){
//         LamportClock clockNodeA = new LamportClock();
        
//         clockNodeA.increment(); // A tự làm việc -> Tăng lên 1
//         clockNodeA.increment(); // A lại làm việc -> Tăng lên 2
//         System.out.println("Current Node A : " + clockNodeA.getTimestamp()); // Kỳ vọng: 2

//         // Giả sử Node B gửi một tin nhắn có timestamp là 5 tới cho Node A
//         int receivedTimestampFromB = 5;
//         clockNodeA.update(receivedTimestampFromB); // A nhận và đồng bộ
//         System.out.println("Node A after receive from B: " + clockNodeA.getTimestamp()); // Kỳ vọng: max(2, 5) + 1 = 6
        
//         clockNodeA.resetClock();
//         System.out.println(clockNodeA);
//     }
    
//     public void testNodeRequest(){
//         // Tình huống 1: Timestamp khác nhau -> Ông T=5 phải thắng ông T=10
//         NodeRequest req1 = new NodeRequest(5, 2);  // Node 2 yêu cầu lúc T=5
//         NodeRequest req2 = new NodeRequest(10, 1); // Node 1 yêu cầu lúc T=10
        
//         System.out.println("--- Test 1: Different Timestamp ---");
//         if (req1.compareTo(req2) < 0) {
//             System.out.println(req1 + " > " + req2);
//         } else {
//             System.out.println(req2 + " > " + req1);
//         }

//         // Tình huống 2: Timestamp bằng nhau -> Ông ID=1 phải thắng ông ID=3
//         NodeRequest req3 = new NodeRequest(7, 1); // Node 1 yêu cầu lúc T=7
//         NodeRequest req4 = new NodeRequest(7, 3); // Node 3 yêu cầu lúc T=7

//         System.out.println("\n--- Test 2: Same Timestamp, check ID ---");
//         if (req3.compareTo(req4) < 0) {
//             System.out.println(req3 + " >> " + req4);
//         } else {
//             System.out.println(req4 + " >> " + req3);
//         }

//     }
    
//     public void testSharedResource(){
//          // Tạo ra 1 tài nguyên dùng chung duy nhất
//         SharedResource resource = new SharedResource();

//         // Tạo Thread cho Node 1
//         Thread node1 = new Thread(() -> {
//             System.out.println("Node 1 want to go in CS...");
//             resource.access(1); // Lao thẳng vào không xin phép
//         });

//         // Tạo Thread cho Node 2
//         Thread node2 = new Thread(() -> {
//             System.out.println("Node 2 want to go in CS...");
//             resource.access(2); // Lao thẳng vào không xin phép
//         });

//         // Kích hoạt cả 2 ông chạy cùng lúc
//         node1.start();
//         node2.start();

//     }
    
//     // public void testNode() throws InterruptedException{
//     //     SharedResource sharedResource = new SharedResource();

//     //     // 1. Tạo 2 Node độc lập
//     //     Node node1 = new Node(1, sharedResource);
//     //     Node node2 = new Node(2, sharedResource);

//     //     // 2. Cấu hình Port hàng xóm (Quy ước kết nối mạng)
//     //     node1.addNeighborPort(9002); // Node 1 biết Node 2 ở port 9002
//     //     node2.addNeighborPort(9001); // Node 2 biết Node 1 ở port 9001

//     //     // 3. KÍCH HOẠT SERVER MẠNG CHO CẢ 2 NODE
//     //     node1.startServer();
//     //     node2.startServer();

//     //     // Ngủ 1 giây chờ 2 Server mở cổng mượt mà
//     //     Thread.sleep(1000);

//     //     System.out.println("\n--- BẮT ĐẦU TRANH CHẤP QUA MẠNG SOCKET ---");

//     //     // 4. Cho cả 2 Node cùng giành giật Miền Găng qua Socket mạng
//     //     Thread t1 = new Thread(() -> node1.requestCriticalSection());
//     //     Thread t2 = new Thread(() -> node2.requestCriticalSection());

//     //     t1.start();
//     //     t2.start();

//     // }
    
//     // public void testNodeSR() throws InterruptedException{
//     //     SharedResource sharedResource = new SharedResource();
//     //     int totalNodes = 5;
//     //     List<Node> ALL_NODES = new ArrayList<>();

//     //     // 1. Khởi tạo 5 Node
//     //     for (int i = 1; i <= totalNodes; i++) {
//     //         ALL_NODES.add(new Node(i, sharedResource));
//     //     }

//     //     // 2. Kết nối chéo Full-Mesh (Mỗi ông kết nối tới 4 ông còn lại)
        
//     //     for (Node node : ALL_NODES) {
//     //         for (Node other : ALL_NODES) {
//     //             if (node != other) {
//     //                 node.addNeighborPort(9000+other.getId());
//     //             }
//     //         }
//     //     }

//     //     System.out.println("=========================================================");
//     //     System.out.println("START STRESS TEST: 5 NODE TRANH CHẤP LIÊN TỤC (THREAD-BASED)");
//     //     System.out.println("=========================================================");

//     //      for (Node node : ALL_NODES){
//     //          node.startServer();
             
//     //      }
        
//     //      // 2. Ngủ một mạch 3 giây tại đây để toàn bộ 5 Server kịp mở cổng an toàn
//     //     System.out.println("Đang đợi các cổng mạng cấu hình ổn định...");
//     //     Thread.sleep(3000); 
//     //     System.out.println("Tất cả các cổng đã online. Bắt đầu chạy đua!");
        
//     //     // 3. Kích hoạt 5 Node chạy đua độc lập
//     //     for (Node node : ALL_NODES) {
//     //         new Thread(() -> {
//     //             // Mỗi node sẽ đòi vào miền găng 3 lần
//     //             for (int attempt = 1; attempt <= 3; attempt++) {
//     //                 try {
//     //                     // BƯỚC 9: Delay ngẫu nhiên từ 0 đến 2 giây trước khi "đòi ăn"
//     //                     Thread.sleep((long) (Math.random() * 2000));
                        
//     //                     node.requestCriticalSection();
                        
//     //                 } catch (InterruptedException e) {
//     //                     e.printStackTrace();
//     //                 }
//     //             }
//     //         }).start();
//     //     }

//     //     Thread.sleep(30000); 
//     //     System.out.println("=== TOÀN BỘ CÁC LƯỢT TEST ĐÃ HOÀN THÀNH. CHƯƠNG TRÌNH TỰ ĐỘNG ĐÓNG ===");
//     // }
    
// }
