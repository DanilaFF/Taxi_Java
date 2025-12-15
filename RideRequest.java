// RideRequest.java
public class RideRequest {
    private final int id;
    private final int fromX;
    private final int fromY;
    private final int toX;
    private final int toY;
    private final long createdAt;
    public RideRequest(int id, int fromX, int fromY, int toX, int toY, long createdAt) {
        this.id = id;
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.createdAt = createdAt;
    }
    public int getId() {
        return id;
    }
    public int getFromX() {
        return fromX;
    }
    public int getFromY() {
        return fromY;
    }
    public int getToX() {
        return toX;
    }
    public int getToY() {
        return toY;
    }
    public long getCreatedAt() {
        return createdAt;
    }
    // Проверка что маршрут не нулевой
    public boolean isValidTrip() {
        return !(fromX == toX && fromY == toY);
    }
    public int getTripDistance() {
        return Math.abs(toX - fromX) + Math.abs(toY - fromY);
    }
    @Override
    public String toString() {
        return "Request#" + id + " from(" + fromX + "," + fromY + ")" + " to(" + toX + "," + toY + ")";
    }
}