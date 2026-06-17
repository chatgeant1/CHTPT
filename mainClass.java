
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