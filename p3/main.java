package p3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Scanner;

public class main {

    private static final long INF = Long.MAX_VALUE / 4;

    static final class Edge {
        final int to;
        final int weight;

        Edge(int to, int weight) {
            this.to = to;
            this.weight = weight;
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        String inputFile = args.length > 0 ? args[0] : "input.txt";
        String outputFile = args.length > 1 ? args[1] : "output.txt";

        List<List<Edge>> graph = readGraph(inputFile);
        try (PrintWriter out = new PrintWriter(outputFile)) {
            printOutput(out, dijkstra(graph));
        }
    }

    public static long[] dijkstra(List<List<Edge>> graph) {
        long[] dist = new long[graph.size()];
        Arrays.fill(dist, INF);
        dist[0] = 0;

        PriorityQueue<long[]> pq = new PriorityQueue<>(Comparator.comparingLong(a -> a[1]));
        pq.offer(new long[] { 0, 0 });

        while (!pq.isEmpty()) {
            long[] cur = pq.poll();
            int u = (int) cur[0];
            long d = cur[1];
            if (d != dist[u]) {
                continue;
            }
            for (Edge e : graph.get(u)) {
                long nd = dist[u] + e.weight;
                if (nd < dist[e.to]) {
                    dist[e.to] = nd;
                    pq.offer(new long[] { e.to, nd });
                }
            }
        }
        return dist;
    }

    public static void printOutput(PrintWriter out, long[] dist) {
        StringBuilder vertices = new StringBuilder(String.format("%-8s", "Vertex"));
        StringBuilder distances = new StringBuilder(String.format("%-10s", "Distance"));
        boolean hasInfinity = Arrays.stream(dist).anyMatch(d -> d == INF);

        for (int i = 0; i < dist.length; i++) {
            String distance = dist[i] == INF ? "infinity" : Long.toString(dist[i]);
            int vertexWidth = 4;
            int distanceWidth = hasInfinity ? 4 : distance.length() + 3;

            if (distance.equals("infinity")) {
                vertexWidth = 10 + (i % 2 == 0 ? 1 : 0);
                distanceWidth = vertexWidth;
                vertices.append(String.format("%-" + vertexWidth + "s", "   " + i));
            } else {
                vertices.append(String.format("%-" + vertexWidth + "d", i));
            }
            distances.append(String.format("%-" + distanceWidth + "s", distance));
        }

        out.println(vertices.toString().replaceFirst("\\s+$", ""));
        out.println(distances.toString().replaceFirst("\\s+$", ""));
    }

    public static List<List<Edge>> readGraph(String filename) throws FileNotFoundException {
        try (Scanner scanner = new Scanner(new File(filename))) {
            int vertices = scanner.nextInt();
            int edges = scanner.nextInt();
            List<List<Edge>> graph = new ArrayList<>();

            for (int i = 0; i < vertices; i++) {
                graph.add(new ArrayList<>());
            }

            for (int i = 0; i < edges; i++) {
                int from = scanner.nextInt();
                int to = scanner.nextInt();
                int weight = scanner.nextInt();
                graph.get(from).add(new Edge(to, weight));
            }

            return graph;
        }
    }
}
