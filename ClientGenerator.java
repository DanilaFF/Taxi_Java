// ClientGenerator.java
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class ClientGenerator implements Runnable {
    private final BlockingQueue<RideRequest> queue;
    private final Random random = new Random();
    private volatile boolean running = true;
    private int nextId = 1;
    // модель города: координаты от 0 до MAX_COORD
    private static final int MAX_COORD = 10;
    // Константы для времени генерации
    private static final long BASE_GENERATION_DELAY_MS = 500;
    private static final long RANDOM_GENERATION_DELAY_MS = 1000;

    public ClientGenerator(BlockingQueue<RideRequest> queue) {
        this.queue = queue;
    }
    public void stopGenerator() {
        running = false;
    }
    @Override
    public void run() {
        System.out.println("[ClientGenerator] запущен");
        while (running) {
            int fromX = random.nextInt(MAX_COORD + 1);
            int fromY = random.nextInt(MAX_COORD + 1);
            int toX = random.nextInt(MAX_COORD + 1);
            int toY = random.nextInt(MAX_COORD + 1);

            RideRequest request = new RideRequest(
                    nextId++,
                    fromX,
                    fromY,
                    toX,
                    toY,
                    System.currentTimeMillis()
            );
            // Проверяем что маршрут не пустой
            if (!request.isValidTrip()) {
                System.out.println("[ClientGenerator] Пропускаю заказ " + request.getId() + " (from == to)");
                continue;
            }
            
            try {
                queue.put(request);
                System.out.println("[ClientGenerator] Создан " + request);
                Thread.sleep(BASE_GENERATION_DELAY_MS + random.nextInt((int)RANDOM_GENERATION_DELAY_MS)); // генерация не слишком частая
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        System.out.println("[ClientGenerator] остановлен");
    }
}