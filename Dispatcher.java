// Dispatcher.java
import java.util.List;
import java.util.concurrent.BlockingQueue;
public class Dispatcher implements Runnable {
    private final BlockingQueue<RideRequest> queue;
    private final List<Taxi> taxis;
    private volatile boolean running = true;
    
    // Статистика
    private int totalRides = 0;
    private long totalWaitTime = 0;
    private final Object statsLock = new Object(); 
    private static final long NO_FREE_TAXI_WAIT_MS = 100;
    public Dispatcher(BlockingQueue<RideRequest> queue, List<Taxi> taxis) {
        this.queue = queue;
        this.taxis = taxis;
    }
    public void stopDispatcher() {
        running = false;
    }
    // Такси вызывает после завершения поездки
    public void onRideCompleted(Taxi taxi, RideRequest request, long rideMillis, long totalMillis) {
        synchronized (statsLock) {
            totalRides++;
            totalWaitTime += totalMillis;
        }
        System.out.printf("[Dispatcher] Такси #%d завершило %s, время в пути ~%d мс, полное время %d мс%n", taxi.getId(), request, rideMillis, totalMillis);
    }
    public String getStatistics() {
        synchronized (statsLock) {
            if (totalRides == 0) {
                return "Статистика: поездок пока не было";
            }
            double avgWait = (double) totalWaitTime / totalRides;
            return String.format("Статистика: выполнено поездок: %d, среднее время обслуживания: %.1f мс", totalRides, avgWait);
        }
    }
    
    @Override
    public void run() {
        System.out.println("[Dispatcher] запущен");
        while (running) {
            try {
                // Берем следующий заказ из очереди и (блокируется, если заказов нет)
                RideRequest request = queue.take();
                System.out.println("[Dispatcher] Новый заказ из очереди: " + request);
                
                // Ждем пока появится свободное такси
                Taxi taxi = null;
                while (taxi == null && running) {
                    taxi = findBestTaxi(request);
                    if (taxi == null) {
                        System.out.println("[Dispatcher] нет свободных такси, ожидаю...");
                        Thread.sleep(NO_FREE_TAXI_WAIT_MS);
                    }
                }
                if (taxi != null) {
                    System.out.println("[Dispatcher] Назначаю " + request + " для такси #" + taxi.getId());
                    taxi.assignRequest(request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[Dispatcher] " + getStatistics());
        System.out.println("[Dispatcher] остановлен");
    }
    // Ищем свободное такси с минимальным расстоянием до клиента
    private Taxi findBestTaxi(RideRequest request) {
        Taxi best = null;
        int bestDist = Integer.MAX_VALUE;
        for (Taxi taxi : taxis) {
            if (taxi.isFree()) {
                int dist = taxi.distanceTo(request.getFromX(), request.getFromY());
                if (dist < bestDist) {
                    bestDist = dist;
                    best = taxi;
                }
            }
        }
        return best;
    }
}