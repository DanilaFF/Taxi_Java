// TaxiSimulation.java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 Главный класс симуляции парка беспилотных такси
 Запускает и координирует работу всех компонентов системы
 */
public class TaxiSimulation {
    private static final int TAXI_COUNT = 3;
    private static final long SIMULATION_TIME_MS = 15_000;
    
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Запуск симуляции парка такси ===");
        System.out.println("Количество такси: " + TAXI_COUNT);
        System.out.println("Время работы: " + (SIMULATION_TIME_MS / 1000) + " секунд");
        System.out.println();
        // Очередь заявок -потокобезопасная
        BlockingQueue<RideRequest> queue = new LinkedBlockingQueue<>();

        // Список такси
        List<Taxi> taxis = new ArrayList<>();
        
        // Диспетчер
        Dispatcher dispatcher = new Dispatcher(queue, taxis);
        Thread dispatcherThread = new Thread(dispatcher, "Dispatcher");
        
        // Такси
        List<Thread> taxiThreads = new ArrayList<>();
        for (int i = 1; i <= TAXI_COUNT; i++) {
            Taxi taxi = new Taxi(i, 0, 0, dispatcher);
            taxis.add(taxi);
            Thread taxiThread = new Thread(taxi, "Taxi-" + i);
            taxiThreads.add(taxiThread);
        }
        
        // Генератор клиентов
        ClientGenerator generator = new ClientGenerator(queue);
        Thread generatorThread = new Thread(generator, "ClientGenerator");
        
        // стартуем все потоки
        dispatcherThread.start();
        for (Thread t : taxiThreads) {
            t.start();
        }
        generatorThread.start();
        //Пусть симуляция поработает какое-то время
        Thread.sleep(SIMULATION_TIME_MS);
        
        // Аккуратно останавливаем систему
        System.out.println();
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