import java.util.ArrayList;
import java.util.Arrays;

public class RegNet
{
    public static Graph originalGraph;
    public static int max;
    //creates a regional network
    //G: the original graph
    //max: the budget
    public static Graph run(Graph G, int max) 
    {
        originalGraph = G;
        RegNet.max = max;
        //Find the MST of G:
        Graph MST = findMST(originalGraph);

        //Ensure that the MST is under budget:
        trim(MST, max);
        MST = MST.connGraph();

        //Use DFS to calculate # stops between pairs of airports
        VPair[] unsortedPairs = makePairs(MST);

        //Sort the pairs of airports by priority (#Stops * total weight - edge weight)
        ArrayList<VPair> sortedPairs = sortEdgeArray(unsortedPairs);

        //Try to add in more edges that connect far away vertices.
        addConnectingFlights(MST, sortedPairs);


        return MST;
    }

    public static void addConnectingFlights(Graph g, ArrayList<VPair> sortedPairs) {
        int currentEdgeWeight;
        int ui;
        int vi;
        String u;
        String v;
        for (int i = sortedPairs.size() - 1; i >= 0; i--) {
            u = sortedPairs.get(i).u;
            v = sortedPairs.get(i).v;

            ui = originalGraph.index(u);
            vi = originalGraph.index(v);

            currentEdgeWeight = originalGraph.getEdgeWeight(ui, vi);

            //Add the edge if it doesn't go over budget
            if (g.totalWeight() + currentEdgeWeight <= max) {
                g.addEdge(originalGraph.getEdge(ui,vi));
            }
        }
    }

    public static ArrayList<VPair> sortEdgeArray(VPair[] pairs) {
        ArrayList<VPair> pairList= new ArrayList<>(pairs.length);

        for (int i = 0; i < pairs.length; i++) {
            pairList.add(pairs[i]);
        }

        pairList.sort(new EdgeSort());

        return pairList;
    }

    public static void swap(Object[] array, int i1, int i2) {
        Object temp = array[i1];
        array[i1] = array[i2];
        array[i2] = temp;
    }

    public static VPair[] makePairs(Graph g) {
        VPair[] distStops = new VPair[g.V()];

        VPair[] pairs = new VPair[sumUp(g.V())];
        int idx = 0;

        for (int i = 0; i < g.V(); i++) {
            distStops = new VPair[g.V()];

            DFS(g, i, i, 0, distStops);

            for (int j = i; j < g.V(); j++) {
                if (distStops[j] != null && distStops[j].getStops() > 0) {
                    pairs[idx++] = distStops[j];
                }
            }
            distStops = null;
            g.unMark();
        }

        //Remove null values from array
        VPair[] pairs2 = new VPair[idx];
        for (int i = 0; i < idx; i++) {
            pairs2[i] = pairs[i];
        }

        return pairs2;

    }

    // int u - starting node for dfs
    // int start, the node to build the path from
    // int currentDist - keeps track of distance from start
    public static void DFS(Graph g, int u, int start, int stops,  VPair[] distStops) {
        ArrayList<Integer> adjacent = g.adj(u);
        g.mark(u);

        for (int i = 0; i < adjacent.size(); i++) { //For each adjacent vertex:

            int v = adjacent.get(i);
            if (!g.isMarked(v)) { //If vertex hasn't been visited

                //Calculate the weight including stops for the VPair
                int weight = stops * g.totalWeight() - originalGraph.getEdgeWeight(start, v);
                distStops[v] = new VPair(g.getCode(start), g.getCode(v), weight, start, v, stops++);

                DFS(g, v, start, stops--, distStops);
            }
        }
    }

    //Returns the sum of 1 + 2 + 3 +.... sumTo
    public static int sumUp(int sumTo) {
        int sum = 0;
        for (int i = 1; i < sumTo; i++) {
            sum += i;
        }
        return sum;
    }

    public static void trim(Graph g, int max) {
        Edge nextEdge;
        int idx;

        while (g.totalWeight() > max) {
            idx = g.edges().size() - 1;
            nextEdge = g.edges().get(idx);

            while (!tryRemove(g, nextEdge)) { //Edge was not removed
                idx--;
                nextEdge = g.edges().get(idx);
            }
        }
    }

    //Method to try removing an edge. If the process creates a disconnected graph, then add the edge back and return
    //false. Otherwise, remove the edge and return true
    public static boolean tryRemove(Graph g, Edge e) {
        int strays = g.getStrayCount();
        g.removeEdge(e);

        if (strays == g.getStrayCount() - 1) { //If deleting edge leaves one stray vertex, then graph is connected
            return true;
        }
        else { //Add back edge if graph is disconnected
            g.addEdge(e);
            return false;
        }
    }

    public static Graph findMST(Graph g) {

        //Create a new graph with all vertices and no edges.
        Graph MST = new Graph(g.V());
        MST.setCodes(g.getCodes());
        ArrayList<Edge> sortedEdges = g.sortedEdges();

        //Create a union find to help implement MST:
        UnionFind uf = new UnionFind(MST.V());

        while (MST.E() < MST.V() - 1) {
            findNextEdge(MST, uf, sortedEdges);
        }

        return MST;
    }

    public static void findNextEdge(Graph MST, UnionFind uf, ArrayList<Edge> sortedEdges) {
        Edge nextEdge = sortedEdges.get(0);
        sortedEdges.remove(nextEdge);

        int u = nextEdge.ui();
        int v = nextEdge.vi();

        if (!uf.connected(u,v)) { //If the two components are not connected
            MST.addEdge(nextEdge);
            uf.union(u,v);
        }


    }
}