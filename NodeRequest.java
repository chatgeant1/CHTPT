
// đại diện cho [một lời yêu cầu] (ở đây là 1 request vào CS)
// sẽ tự biết so sánh nó với một lời yêu cầu khác 
// bằng cách triển khai Interface Comparable của Java
// Node 1 request, chờ reply
// Node 2 cũng request ... tới node 1 => so sánh 2 NodeRequest

public class NodeRequest implements Comparable<NodeRequest> {
    private final int timestamp;
    private final int nodeId;

    public NodeRequest(int timestamp, int nodeId) {
        this.timestamp = timestamp;
        this.nodeId = nodeId;
    }

    public int getTimestamp() { return timestamp; } 
    public int getNodeId() { return nodeId; }

//    /**
//     * Hàm so sánh độ ưu tiên.
//     * Trả về [số âm]: Đối tượng này có ưu tiên CAO HƠN (đến trước)
//     * Trả về [số dương]: Đối tượng này có ưu tiên THẤP HƠN (đến sau)
//     */
    @Override
    public int compareTo(NodeRequest other) {
        // 1. So sánh Timestamp trước
        if (this.timestamp != other.timestamp) {
            return Integer.compare(this.timestamp, other.timestamp);
        }
        // 2. Nếu Timestamp bằng nhau, ss bằng Node ID
        return Integer.compare(this.nodeId, other.nodeId);
    }

    @Override
    public String toString() {
        // Định dạng in ra cho dễ nhìn lúc debug
        return String.format("Request[T=%d, ID=%d]", timestamp, nodeId);
    }
}
