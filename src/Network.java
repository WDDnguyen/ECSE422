import com.sun.deploy.util.ArrayUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class Network {

    private static int numberOfCities;
    private static ArrayList<Double> reliabilityMajorMatrix;
    private static ArrayList<Integer> costMajorMatrix;

    public static void main (String[] args){
        Graph networkGraph = new Graph();
        reliabilityMajorMatrix = new ArrayList<Double>();
        costMajorMatrix = new ArrayList<Integer>();
        readInputFile();

        ArrayList<Edge> edges = new ArrayList<>();

        initializeVertices(networkGraph);
        createEdges(edges, networkGraph);
        System.out.println("-----------------------------------");

        Collections.sort(edges);

        for(Edge edge: edges){
            System.out.println("EDGE SOURCE ID : " + edge.source.getId() + " DEST ID : " + edge.destination.getId() + " RELIABILITY : " + edge.reliability + " COST : " + edge.cost );
        }

        addEdgeToGraph(networkGraph, edges);
        double spanningReliability = calculateSpanningTreeReliability(networkGraph);
        double targetReliability = 0.85;

        if (spanningReliability < targetReliability){
            System.out.println("SPANNING TREE RELIABILITY IS : " + spanningReliability);

            Edge additionalEdge = findEdge(networkGraph, edges);
            updateGraph(networkGraph, additionalEdge);

            // ENUMERATION

            ArrayList<Integer[]> combinationList = new ArrayList<>();
            findCombination(networkGraph, combinationList);
            double totalReliability = calculateTotalReliability(networkGraph, combinationList);

            if (totalReliability >= targetReliability){
                System.out.println("OBTAINED TARGET RELIABILITY");
            }
        }


    }

    public static void initializeVertices (Graph networkGraph){
        for (int i = 0; i < numberOfCities; i++){
            networkGraph.addVertex(i);
        }
        System.out.println("GRAPH INITIALIZED WITH " + networkGraph.vertexMap.size() + " VERTICES");
    }

    public static void createEdges(ArrayList<Edge> edges, Graph networkGraph){
        int matrixIndex = 0;
        for (int i = 0 ; i < numberOfCities; i++){
            int sourceId = i;
            for (int j = sourceId + 1 ; j < numberOfCities; j++){
                int destinationId = j;
                //System.out.println("ADDED EDGE WITH SOURCE : " + sourceId + " DESTINATION ID " + destinationId + " ACCESS VALUE OF MATRIX " + matrixIndex);
                edges.add(new Edge(networkGraph.getVertex(sourceId), networkGraph.getVertex(destinationId), costMajorMatrix.get(matrixIndex), reliabilityMajorMatrix.get(matrixIndex)));
                matrixIndex++;
            }
        }
    }

    public static ArrayList<Edge> getPath (HashMap<Vertex,Vertex> parents, Vertex source, Vertex destination){
        ArrayList<Vertex> path = new ArrayList<>();
        ArrayList<Edge> G2Edges = new ArrayList<>();

        path.add(destination);
        Vertex currentNode = destination;
        Vertex parent = parents.get(currentNode);
        System.out.println("CURRENT NODE " + currentNode.getId() + 1);
        System.out.println("PARENT NODE : " + (parent.getId() + 1));
        while(parent != source){
            path.add(parent);
            G2Edges.add(currentNode.getEdge(parent));
            System.out.println("G2 EDGE ADDED " + (currentNode.getEdge(parent).source.getId() + 1) + " DEST " + (currentNode.getEdge(parent).destination.getId() + 1));

            currentNode = parent;
            parent = parents.get(currentNode);
          //  System.out.println("ADDED VERTEX ID : " + parent.getId());
        }

        // add last element
        path.add(parent);
        G2Edges.add(currentNode.getEdge(parent));
        System.out.println("G2 EDGE ADDED " + (currentNode.getEdge(parent).source.getId() + 1) + " DEST " + (currentNode.getEdge(parent).destination.getId() + 1));

        System.out.println("G2 Edge " + G2Edges.size());

        System.out.println("DONE FINDING PATH");


       // return path;
        return G2Edges;
    }

    // prints BFS traversal from a given source
    public static ArrayList<Edge> BFS(Graph networkGraph, Vertex source, Vertex destination)
    {
        HashMap<Vertex, Vertex> parents = new HashMap<>();
        Vertex parent = source;
       Queue<Vertex> queue = new LinkedList<>();
       ArrayList<Vertex> visitedVertices = new ArrayList<>();

       queue.add(source);
       visitedVertices.add(source);

       while(!queue.isEmpty()){
           Vertex v = queue.remove();
           if (v.equals(destination)){
               System.out.println("LAST NODE");
               return getPath(parents, source, destination);

           }
           for (Vertex neighbor : networkGraph.vertexMap.get(v)){
               if (!visitedVertices.contains(neighbor)){
                   queue.add(neighbor);
                   visitedVertices.add(neighbor);
                   parent = v;
                  // System.out.println("ADDED VERTEX " + (neighbor.getId() + 1));
                   parents.put(neighbor, parent);
                  // System.out.println("PARENT ID " + (parent.getId() + 1));
               }
           }
       }
       return null;
    }

    public static void readInputFile(){

        int counter = 0;
        try {
            File f = new File("./input.txt");
            BufferedReader br = new BufferedReader(new FileReader(f));
            String readLine = "";

            while ((readLine = br.readLine()) != null){
                if (!readLine.contains("#")){
                    System.out.println(readLine);
                    if (counter == 0){
                        System.out.println("NUMBER OF CITIES READ");
                        numberOfCities = Integer.valueOf(readLine);
                    } else if (counter == 1){
                        System.out.println("RELIABILITY MAJOR MATRIX READ");
                        String[] reliabilityValues = readLine.split(" ");
                        for (String value : reliabilityValues){
                            Double dnum = Double.valueOf(value);
                            reliabilityMajorMatrix.add(dnum);
                        }
                    } else if (counter == 2){
                        System.out.println("COST MAJOR MATRIX READ");
                        String[] costValues = readLine.split(" ");
                        for (String value : costValues){
                            costMajorMatrix.add(Integer.valueOf(value));
                        }
                    } else {
                        System.out.println("ERROR TOO MANY LINES");
                    }
                    counter++;
                    }
                }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void combination(int arr[], int data[], int start, int end, int index, int r, ArrayList<Integer[]> combinationValue){
        if(index == r){
            Integer[] combinationList = Arrays.stream(data).boxed().toArray(Integer[]::new);
            combinationValue.add(combinationList);
            return;
        }

        for (int i = start; i <= end && end-i+1 >= r-index; i++){
            data[index] = arr[i];
            combination(arr, data, i+1, end, index+1, r, combinationValue);
        }

    }

    public static void getCombinations(int arr[], int n, int r, ArrayList<Integer[]> combinationValue){
        int data[] = new int[r];
        combination(arr, data, 0 , n-1, 0, r, combinationValue);
    }

    public static void addEdgeToGraph(Graph networkGraph, ArrayList<Edge> edges){
        for(Edge edge : edges){

            if(networkGraph.vertexMap.get(edge.source).size() == 1 || networkGraph.vertexMap.get(edge.destination).size() == 1){

                // Source Vertex
                edge.source.addEdge(edge);
                // add itself vertex, destination to source
                HashSet<Vertex> hs = networkGraph.vertexMap.get(edge.source);
                hs.add(edge.destination);
                hs.add(edge.source);
                networkGraph.vertexMap.put(edge.source, hs);
                edge.source.addEdge(edge);

                // add destination vertex itself, source to destination

                HashSet<Vertex> hs2 = networkGraph.vertexMap.get(edge.destination);
                hs2.add(edge.destination);
                hs2.add(edge.source);
                networkGraph.vertexMap.put(edge.destination, hs2);
                edge.destination.addEdge(edge);

                System.out.println("ADDED EDGE SOURCE ID : " + (edge.source.getId() + 1) + " DEST ID : " + (edge.destination.getId() + 1) + " RELIABILITY : " + edge.reliability + " COST : " + edge.cost );

                networkGraph.addEdge(edge);
            }
        }
    }

    public static double calculateSpanningTreeReliability(Graph networkGraph){

        double spanningTreeReliability = 1;
        for(Edge edge : networkGraph.getNetworkEdges()){
            spanningTreeReliability *= edge.getReliability();
        }

        System.out.println("CURRENT TOTAL RELIABILITY : " + spanningTreeReliability);
        return spanningTreeReliability;
    }

    public static Edge findEdge(Graph networkGraph, ArrayList<Edge> edges){
        // add new edge base on the 2 lowest reliable edges
        System.out.println("LOWEST EDGE : " + networkGraph.getLowestReliableEdge().getReliability() + " SECOND LOWEST EDGE : " + networkGraph.getSecondLowestEdge().getReliability());
        Vertex vertex1 = networkGraph.getLowestReliableEdge().destination;
        Vertex vertex2 = networkGraph.getSecondLowestEdge().destination;

        Vertex edgeSource;
        Vertex edgeDestination;
        if (vertex1.getId() > vertex2.getId()){
            edgeSource = vertex2;
            edgeDestination = vertex1;
        }else {
            edgeSource = vertex1;
            edgeDestination = vertex2;
        }

        // find edge with source and destination

        Edge newEdge = null;
        for (Edge edge : edges){
            if (edge.source.equals(edgeSource) && edge.destination.equals(edgeDestination)){
                newEdge = edge;
                System.out.println("NEW EDGE TO BE ADDED SOURCE ID : " + (edge.source.getId() + 1) + " DEST ID : " + (edge.destination.getId() + 1) + " RELIABILITY : " + edge.getReliability());
            }
        }

        return newEdge;
    }

    public static void findCombination(Graph networkGraph, ArrayList<Integer[]> combinationList){

        int arr[] = new int[networkGraph.getNetworkEdges().size()];
        for (int i = 0; i < arr.length; i++){
            arr[i] = i;
        }

        int r = numberOfCities - 1;
        int n = arr.length;

        for(int i = r; i <= n; i++){
            getCombinations(arr, n, i, combinationList);
        }
    }

    public static double calculateTotalReliability(Graph networkGraph, ArrayList<Integer[]> combinationList){
        double totalReliability = 0;

        for (Integer[] combination : combinationList){
            ArrayList<Edge> edges = convertCombinationToEdges(networkGraph, combination);
            if (checkConnectivity(networkGraph,edges)){
                double reliability = 1;

                for(Edge edge : edges){
                    reliability *= edge.getReliability();
                }

                for (int i = 0; i < networkGraph.getNetworkEdges().size(); i++){
                    if (Arrays.binarySearch(combination, i) < 0){
                        reliability *= (1 - networkGraph.getNetworkEdges().get(i).getReliability());
                    }
                }
                System.out.println("Adding reliability " + reliability);
                totalReliability += reliability;
            }
        }

        System.out.println("TOTAL RELIABILITY : " + totalReliability);
        return totalReliability;
    }

    public static void updateGraph(Graph networkGraph, Edge edge){
        Vertex source = edge.source;
        Vertex destination = edge.destination;

        source.addEdge(edge);
        HashSet<Vertex> hs = networkGraph.vertexMap.get(source);
        hs.add(destination);
        networkGraph.vertexMap.put(source, hs);


        destination.addEdge(edge);
        HashSet<Vertex> hs2 = networkGraph.vertexMap.get(edge.destination);
        hs2.add(destination);
        networkGraph.vertexMap.put(destination, hs2);

        System.out.println("UPDATE : ADD EDGE SOURCE ID : " + (source.getId() + 1) + " DEST ID : " + (destination.getId() + 1) + " RELIABILITY : " + edge.getReliability() + " COST : " + edge.getReliability());

        networkGraph.addEdge(edge);
    }

    public static boolean checkConnectivity(Graph networkGraph, ArrayList<Edge> edges) {
        boolean[] visitedVertices = new boolean[networkGraph.vertexMap.keySet().size()];
        Arrays.fill(visitedVertices, Boolean.FALSE);

        // verify if every node is connected
        for (Edge edge : edges) {
            if (!visitedVertices[edge.source.getId()]) {
                visitedVertices[edge.source.getId()] = true;
            }

            if (!visitedVertices[edge.destination.getId()]) {
                visitedVertices[edge.destination.getId()] = true;
            }

        }

        for (boolean visited : visitedVertices){
            if (!visited){
                System.out.println("GRAPH NOT CONNECTED WITH THIS COMBINATION");
                return false;
            }
        }
        return true;
    }

    public static ArrayList<Edge> convertCombinationToEdges(Graph networkGraph, Integer[] combinationList){
        ArrayList<Edge> edges = new ArrayList<>();
        for (Integer index : combinationList){
            edges.add(networkGraph.getNetworkEdges().get(index));
        }
        return edges;
    }
}

