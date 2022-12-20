import java.util.ArrayList;

public class GlobalNet
{
    //creates a global network 
    //O : the original graph
    //regions: the regional graphs
    public static Graph run(Graph O, Graph[] regions) 
    {

        //Create new global network:
        Graph globalNetwork = createBlankNetwork(O, regions);

        //Run Dijkstra's using every region as a starting node:
        shortestPath(O, regions, globalNetwork);
        
        return globalNetwork;
    }

    //Runs Dijkstra's using a source vertex from every region
    //Updates the global network with the shortest paths between regions
    public static void shortestPath(Graph originalGraph, Graph[] regions, Graph globalNetwork) {
        int[] prev = new int[originalGraph.V()];
        Integer[] dist = new Integer[originalGraph.V()];
        int source;
        int destination;
        Graph currentRegion;
        Graph nextRegion;

        for (int i = 0; i < regions.length; i++) {
            currentRegion = regions[i];

            //The source should be the index of the first vertex in the current region
            source = originalGraph.index(currentRegion.getCode(0));
            dijkstra(originalGraph, source, currentRegion, prev, dist);

            //Use prev[] to find the shortest path from the current region to all other regions:
            for (int j = i + 1; j < regions.length; j++) {
                nextRegion = regions[j];
                destination = originalGraph.index(nextRegion.getCode(0));

                //Find the vertex in nextRegion with the smallest dist:
                for (int v = 1; v < nextRegion.V(); v++) {
                    int vertexNumber = originalGraph.index(nextRegion.getCode(v));
                    if (dist[vertexNumber] < dist[destination]) {
                        destination = vertexNumber;
                    }
                }

                buildPath(globalNetwork, originalGraph, prev, source, destination);

            }
        }
    }

    public static void buildPath(Graph globalNetwork, Graph originalGraph, int[] prev, int source, int destination) {
        //Start from destination and work backwards towards source
        while (prev[destination] != -1) {
            //Build an edge based off the current vertex and its previous vertex:
            //The next two lines will only add edges between regions to the global network, since the edges within
            //the regions have already been initialized
            int distance = originalGraph.getEdgeWeight(prev[destination], destination);
            globalNetwork.addEdge(prev[destination], destination, distance);

            //Go to the previous vertex and repeat:
            destination = prev[destination];
        }
    }

    public static int[] dijkstra(Graph originalGraph, int source, Graph currentRegion, int[] prev, Integer[] dist) {
        //Declare variables:
        int currentVertex;
        int distance;

        DistQueue queue = new DistQueue(originalGraph.V());

        //Initialize array values:
        for (int i = 0; i < originalGraph.V(); i++) {
            dist[i] = Integer.MAX_VALUE;
            prev[i] = -1;
            queue.insert(i, dist[i]);
        }
        //Update array values of the current region:
        for (int i = 0; i < currentRegion.V(); i++) {
            String vertexCode = currentRegion.getCode(i);
            int vertexNumber = originalGraph.index(vertexCode);

            dist[vertexNumber] = 0;
            queue.set(vertexNumber, 0);
        }

        //Run shortest path search:
        while(!queue.isEmpty()) {
            currentVertex = queue.delMin();

            //For each adjacent vertex to the current vertex
            for (int adjacentVertex : originalGraph.adj(currentVertex)) {
                if (queue.inQueue(adjacentVertex)) { //Check if vertex is still in queue
                    //Calculate the relaxed edge weight
                    distance = dist[currentVertex] + originalGraph.getEdgeWeight(currentVertex, adjacentVertex);

                    if (distance < dist[adjacentVertex]) { //If the distance is shorter, update
                        dist[adjacentVertex] = distance;
                        prev[adjacentVertex] = currentVertex;
                        //update queue
                        queue.set(adjacentVertex, distance);
                    }
                }
            }
        }
        return prev;
    }

    //Creates a blank global network with only regional edges
    public static Graph createBlankNetwork(Graph O, Graph[] regions) {
        Graph globalNetwork = new Graph(O.V());
        globalNetwork.setCodes(O.getCodes());

        //Initialize regional edges:
        for (Graph region : regions) {
            for (Edge regionEdge : region.edges()) {
                globalNetwork.addEdge(regionEdge.u, regionEdge.v, regionEdge.w);
            }
        }

        return globalNetwork;
    }
}
    
    
    