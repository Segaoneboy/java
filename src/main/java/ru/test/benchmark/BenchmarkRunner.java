package ru.test.benchmark;

import ru.test.domain.UserProfile;
import ru.test.domain.datastructure.BFSResult;
import ru.test.domain.datastructure.Connections;
import ru.test.domain.datastructure.Graph;

import java.util.*;
import java.util.concurrent.*;

public class BenchmarkRunner {

    static class StdGraph {
        private final Map<Integer, List<Integer>> adj = new HashMap<>();

        public synchronized void addVertex(int id) {
            adj.putIfAbsent(id, new ArrayList<>());
        }

        public synchronized void addEdge(int u, int v) {
            adj.computeIfAbsent(u, k -> new ArrayList<>()).add(v);
            adj.computeIfAbsent(v, k -> new ArrayList<>()).add(u);
        }

        public synchronized boolean findVertex(int id) {
            return adj.containsKey(id);
        }

        public synchronized boolean removeVertex(int id) {
            if (!adj.containsKey(id)) return false;
            adj.remove(id);
            for (List<Integer> neighbors : adj.values()) {
                neighbors.remove(Integer.valueOf(id));
            }
            return true;
        }

        public synchronized boolean bfs(int start, int target) {
            if (!adj.containsKey(start) || !adj.containsKey(target)) return false;
            Queue<Integer> queue = new ArrayDeque<>();
            Set<Integer> visited = new HashSet<>();
            queue.add(start);
            visited.add(start);

            while (!queue.isEmpty()) {
                int curr = queue.poll();
                if (curr == target) return true;
                List<Integer> neighbors = adj.get(curr);
                if (neighbors != null) {
                    for (int n : neighbors) {
                        if (visited.add(n)) {
                            queue.add(n);
                        }
                    }
                }
            }
            return false;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("===============================================================================");
        System.out.println("               ЛАБОРАТОРНАЯ РАБОТА № 2: ТЕСТИРОВАНИЕ И ЗАМЕРЫ                  ");
        System.out.println("===============================================================================");

        runConcurrencyExperiment();

        System.out.println("\n===============================================================================");
        System.out.println("                     СРАВНИТЕЛЬНЫЙ БЕНЧМАРК (10^4 и 10^5)                      ");
        System.out.println("===============================================================================");
        runBenchmarkForSize(10_000);
        runBenchmarkForSize(100_000);
    }

    private static void runConcurrencyExperiment() throws InterruptedException {
        System.out.println("\n[1] ДЕМОНСТРАЦИЯ СОСТОЯНИЯ ГОНКИ (DATA RACE)");
        int threadCount = 10;
        int itemsPerThread = 1_000;
        int expectedTotal = threadCount * itemsPerThread;

        Graph unsafeGraph = new Graph(expectedTotal);
        ExecutorService unsafePool = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            final int baseId = t * itemsPerThread;
            unsafePool.submit(() -> {
                for (int i = 0; i < itemsPerThread; i++) {
                    int id = baseId + i + 1;
                    unsafeGraph.unsafeAddVertex(new UserProfile(id, "User_" + id, "Город", 2000, new Connections[0]));
                }
            });
        }
        unsafePool.shutdown();
        unsafePool.awaitTermination(10, TimeUnit.SECONDS);

        int unsafeSaved = unsafeGraph.getVertexCount();
        System.out.printf("  Незащищенная запись: ожидалось = %d, фактически = %d (Потеряно: %d записей)\n",
                expectedTotal, unsafeSaved, (expectedTotal - unsafeSaved));

        Graph safeGraph = new Graph(expectedTotal);
        ExecutorService safePool = Executors.newFixedThreadPool(threadCount);
        for (int t = 0; t < threadCount; t++) {
            final int baseId = t * itemsPerThread;
            safePool.submit(() -> {
                for (int i = 0; i < itemsPerThread; i++) {
                    int id = baseId + i + 1;
                    safeGraph.addVertex(new UserProfile(id, "User_" + id, "Город", 2000, new Connections[0]));
                }
            });
        }
        safePool.shutdown();
        safePool.awaitTermination(10, TimeUnit.SECONDS);

        int safeSaved = safeGraph.getVertexCount();
        System.out.printf("  Синхронизированная запись: ожидалось = %d, фактически = %d (Потери: 0 записей)\n",
                expectedTotal, safeSaved);
    }

    private static void runBenchmarkForSize(int n) {
        System.out.printf("\n>>> Тестирование на объеме N = %d элементов <<<\n", n);

        Graph customGraph = new Graph(n + 10);

        long tStart = System.currentTimeMillis();
        for (int i = 1; i <= n; i++) {
            customGraph.addVertex(new UserProfile(i, "U_" + i, "City", 1995, new Connections[0]));
            if (i > 1 && i % 2 == 0) {
                customGraph.addEdge(i, i - 1, 5);
            }
        }
        long customInsertTime = System.currentTimeMillis() - tStart;

        tStart = System.currentTimeMillis();
        for (int i = 1; i <= 1_000; i++) {
            customGraph.findVertexById(i * (n / 1000));
        }
        long customSearchTime = System.currentTimeMillis() - tStart;

        tStart = System.currentTimeMillis();
        BFSResult bfsRes = customGraph.findShortestPath(1, Math.min(n, 1000));
        long customBfsTime = System.currentTimeMillis() - tStart;

        tStart = System.currentTimeMillis();
        for (int i = 1; i <= 500; i++) {
            customGraph.removeVertex(i);
        }
        long customDeleteTime = System.currentTimeMillis() - tStart;

        StdGraph stdGraph = new StdGraph();

        tStart = System.currentTimeMillis();
        for (int i = 1; i <= n; i++) {
            stdGraph.addVertex(i);
            if (i > 1 && i % 2 == 0) {
                stdGraph.addEdge(i, i - 1);
            }
        }
        long stdInsertTime = System.currentTimeMillis() - tStart;

        tStart = System.currentTimeMillis();
        for (int i = 1; i <= 1_000; i++) {
            stdGraph.findVertex(i * (n / 1000));
        }
        long stdSearchTime = System.currentTimeMillis() - tStart;

        tStart = System.currentTimeMillis();
        stdGraph.bfs(1, Math.min(n, 1000));
        long stdBfsTime = System.currentTimeMillis() - tStart;

        tStart = System.currentTimeMillis();
        for (int i = 1; i <= 500; i++) {
            stdGraph.removeVertex(i);
        }
        long stdDeleteTime = System.currentTimeMillis() - tStart;

        System.out.println("+-------------------+----------------------+----------------------+");
        System.out.printf("| %-17s | %-20s | %-20s |\n", "Операция", "Собственный граф (мс)", "java.util граф (мс)");
        System.out.println("+-------------------+----------------------+----------------------+");
        System.out.printf("| %-17s | %-20d | %-20d |\n", "Вставка", customInsertTime, stdInsertTime);
        System.out.printf("| %-17s | %-20d | %-20d |\n", "Поиск (1000 шт)", customSearchTime, stdSearchTime);
        System.out.printf("| %-17s | %-20d | %-20d |\n", "BFS путь", customBfsTime, stdBfsTime);
        System.out.printf("| %-17s | %-20d | %-20d |\n", "Удаление (500 шт)", customDeleteTime, stdDeleteTime);
        System.out.println("+-------------------+----------------------+----------------------+");
    }
}