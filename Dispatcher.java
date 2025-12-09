// Dispatcher.java
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Dispatcher implements Runnable {
    private final BlockingQueue<RideRequest> queue;
    private final List<Taxi> taxis;

    private volatile boolean running = true;
    public Dispatcher(BlockingQueue<RideRequest> queue, List<Taxi> taxis) {
        this.queue = queue;
        this.taxis = taxis;
    }

    public void stopDispatcher() {
        running = false;
    }
    // Такси вызывает после завершения поездки
    public void onRideCompleted(Taxi taxi, RideRequest request, long rideMillis) {
        System.out.printf("[Dispatcher] Такси #%d завершило %s, время в пути ~%d мс%n",
                taxi.getId(), request, rideMillis);
    }
    @Override
    public void run() {
        System.out.println("[Dispatcher] запущен");
        while (running) {
            try {
                // Берем следующий заказ из очереди и (блокируется, если заказов нет)
                RideRequest request = queue.take();
                System.out.println("[Dispatcher] Новый заказ из очереди: " + request);
                Taxi taxi = findBestTaxi(request);
                if (taxi == null) {
                    // Если нет свободных такси, возвращаем заказ обратно и немного ждем
                    System.out.println("[Dispatcher] нет свободных такси, возвращаю заказ обратно в очередь");
                    queue.put(request);
                    Thread.sleep(200);
                } else {
                    System.out.println("[Dispatcher] Назначаю " + request + " для такси #" + taxi.getId());
                    taxi.assignRequest(request);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
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
