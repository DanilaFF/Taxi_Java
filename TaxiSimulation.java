// TaxiSimulation.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TaxiSimulation {
    public static void main(String[] args) throws InterruptedException {
        // Очередь заявок -потокобезопасная
        BlockingQueue<RideRequest> queue = new LinkedBlockingQueue<>();

        // Список такси
        List<Taxi> taxis = new ArrayList<>();
        int taxiCount = 3; // можно поменять количество машин
        // Диспетчер
        Dispatcher dispatcher = new Dispatcher(queue, taxis);
        Thread dispatcherThread = new Thread(dispatcher, "Dispatcher");
        // Такси
        List<Thread> taxiThreads = new ArrayList<>();
        for (int i = 1; i <= taxiCount; i++) {
            Taxi taxi = new Taxi(i, 0, 0, dispatcher);
            taxis.add(taxi);
            Thread taxiThread = new Thread(taxi, "Taxi-" + i);
            taxiThreads.add(taxiThread);
        }
        // Генератор клиентов
        ClientGenerator generator = new ClientGenerator(queue);
        Thread generatorThread = new Thread(generator, "ClientGenerator");
        // Стартуем все потоки
        dispatcherThread.start();
        for (Thread t : taxiThreads) {
            t.start();
        }
        generatorThread.start();
        // Пусть симуляция поработает какое-то время - 15 секунд
        Thread.sleep(15_000);
        // Аккуратно останавливаем систему
        System.out.println("=== Останавливаем симуляцию ===");
        generator.stopGenerator();
        dispatcher.stopDispatcher();
        for (Taxi taxi : taxis) {
            taxi.stopTaxi();
        }
        // Будим потоки если они ждут
        generatorThread.interrupt();
        dispatcherThread.interrupt();
        for (Thread t : taxiThreads) {
            t.interrupt();
        }
        // Ждем завершения
        generatorThread.join();
        dispatcherThread.join();
        for (Thread t : taxiThreads) {
            t.join();
        }
        System.out.println("=== Симуляция завершена ===");
    }
}
