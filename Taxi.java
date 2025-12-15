// Taxi.java
public class Taxi implements Runnable {
    private final int id;
    private int x;
    private int y;
    private final Object lock = new Object();
    private volatile boolean running = true;
    private RideRequest currentRequest;
    private final Dispatcher dispatcher;
    // Константы времени для симуляции
    private static final long BASE_TO_CLIENT_DELAY_MS = 200;
    private static final long TIME_PER_UNIT_TO_CLIENT_MS = 20;
    private static final long BASE_TRIP_DELAY_MS = 300;
    private static final long TIME_PER_UNIT_TRIP_MS = 30;
    
    public Taxi(int id, int startX, int startY, Dispatcher dispatcher) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.dispatcher = dispatcher;
    }
    public int getId() {
        return id;
    }
    // Проверяем свободно лии такси
    public boolean isFree() {
        synchronized (lock) {
            return currentRequest == null;
        }
    }
    // Простое расстояние по принцепу манхэтена
    public int distanceTo(int px, int py) {
        synchronized (lock) {
            return Math.abs(px - x) + Math.abs(py - y);
        }
    }
    // Диспетчер назначает заказ
    public void assignRequest(RideRequest request) {
        synchronized (lock) {
            if (currentRequest != null) {
                throw new IllegalStateException("Такси #" + id + " уже занято");
            }
            currentRequest = request;
            lock.notifyAll();
        }
    }

    public void stopTaxi() {
        running = false;
        synchronized (lock) {
            lock.notifyAll();
        }
    }
    @Override
    public void run() {
        log("стартует");

        while (running) {
            RideRequest request;
            // Ждем пока нам выдадут заказ
            synchronized (lock) {
                while (currentRequest == null && running) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                if (!running) {
                    break;
                }
                request = currentRequest;
            }
            // Едем к клиенту
            int toClientDist;
            synchronized (lock) {
                toClientDist = Math.abs(request.getFromX() - x) + Math.abs(request.getFromY() - y);
            }
            log("получил " + request + ", еду к клиенту (расстояние " + toClientDist + ")");
            sleepSim(BASE_TO_CLIENT_DELAY_MS + toClientDist * TIME_PER_UNIT_TO_CLIENT_MS);
            // Перевозим клиента
            int tripDist = Math.abs(request.getToX() - request.getFromX())
                    + Math.abs(request.getToY() - request.getFromY());
            log("везу клиента, расстояние " + tripDist);
            long start = System.currentTimeMillis();
            sleepSim(BASE_TRIP_DELAY_MS + tripDist * TIME_PER_UNIT_TRIP_MS);
            long end = System.currentTimeMillis();

            // Обновляем координаты такси в конце поездки
            synchronized (lock) {
                x = request.getToX();
                y = request.getToY();
                currentRequest = null;
            }
            // Считаем полное время (с момента создания заказа)
            long totalTime = end - request.getCreatedAt();
            dispatcher.onRideCompleted(this, request, end - start, totalTime);
        }
        log("останавливается");
    }
    private void sleepSim(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    private void log(String msg) {
        System.out.printf("[Taxi-%d] %s%n", id, msg);
    }
}