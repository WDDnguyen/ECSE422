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
    private static double targetReliability;
    private static int targetCost;

    private static double spanningReliability;
    private static int spanningCost;

    private static double totalReliability;
    private static int totalCost;
    private static int reliabilityConstraint;
    private static int amountOfSpanningEdges;

    public static void main (String[] args){

        parseArguments(args);
        Graph networkGraph = new Graph();
        reliabilityMajorMatrix = new ArrayList<>();
        costMajorMatrix = new ArrayList<>();
        ArrayList<Edge> edges = new ArrayList<>();

        readInputFile();

        initializeVertices(networkGraph);
        createEdges(edges, networkGraph);
        System.out.println("-----------------------------------");

         if (reliabilityConstraint == 1){
             Comparator<Edge> reliabilityOrder = Comparator.comparing(Edge::getReliability, Comparator.reverseOrder()).thenComparing(Edge::getCost);
             Collections.sort(edges, reliabilityOrder);
         }
         else if (reliabilityConstraint == 2 || reliabilityConstraint == 3){
            Comparator<Edge> costOrder = Comparator.comparing(Edge::getCost).thenComparing(Edge::getReliability, Comparator.reverseOrder());
            Collections.sort(edges, costOrder);
        }
         
        for(Edge edge : edges){
            System.out.println("SORTED EDGE WITH CITY " + (edge.source.getId() + 1)
                    + " AND CITY : " + (edge.destination.getId() + 1)
                    + " EDGE RELIABILITY : " + edge.getReliability()
                    + " EDGE COST : " + edge.getCost());
        }

        addEdgeToGraph(networkGraph, edges);
        amountOfSpanningEdges = networkGraph.getNetworkEdges().size();
        spanningReliability = calculateSpanningTreeReliability(networkGraph);
        spanningCost = calculateSpanningTreeCost(networkGraph);

        // value changes based on contraints
        // 1 - Only need to get largest target
        // 2 - Need to get reliability based on cost constraint
        // 3 - maximize reliability

        double currentReliability = spanningReliability;
        int currentCost = spanningCost;

        // meet target reliability
        if (reliabilityConstraint == 1){
            while (currentReliability <= targetReliability){
                System.out.println("CURRENT RELIABILITY IS : " + currentReliability);

                // TO DO : NEED TO VERIFY IF LOWEST EDGES DOESNT WORK.
                Edge additionalEdge = findEdge(networkGraph, edges);
                updateGraph(networkGraph, additionalEdge);

                // ENUMERATION
                ArrayList<Integer[]> combinationList = new ArrayList<>();
                findCombination(networkGraph, combinationList);
                currentReliability = calculateTotalReliability(networkGraph, combinationList);

                }
                System.out.println("OBTAINED TARGET RELIABILITY");
                totalReliability = currentReliability;
                displayGraphInformation(networkGraph);
        }
        //  meet constraint reliability
        else if (reliabilityConstraint == 2){
            while (currentCost < targetCost && currentReliability < targetReliability){
                Edge additionalEdge = getLowestAvailableCostEdge(networkGraph, edges);
                updateGraph(networkGraph, additionalEdge);

                // ENUMERATION
                ArrayList<Integer[]> combinationList = new ArrayList<>();
                findCombination(networkGraph, combinationList);
                currentReliability = calculateTotalReliability(networkGraph, combinationList);
                currentCost = calculateTotalCost(networkGraph);

            }

            if (currentReliability >= targetReliability && currentCost <= targetCost){
                System.out.println("OBTAINED TARGET RELIABILITY WITH COST CONSTRAINT");
                totalReliability = currentReliability;
                totalCost = currentCost;
                displayGraphInformation(networkGraph);
            } else {
                System.out.println("CANNOT FIND DESIGN WITH TARGET RELIABILITY : " + targetReliability + " AND COST : " + targetCost);
            }

        } else if (reliabilityConstraint == 3){
            while (currentCost < targetCost){
                Edge additionalEdge = getLowestAvailableCostEdge(networkGraph, edges);
                updateGraph(networkGraph, additionalEdge);

                // ENUMERATION
                ArrayList<Integer[]> combinationList = new ArrayList<>();
                findCombination(networkGraph, combinationList);
                currentReliability = calculateTotalReliability(networkGraph, combinationList);
                currentCost = calculateTotalCost(networkGraph);

            }

            System.out.println("MAXIMIZED RELIABILITY WITH COST CONSTRAINT");
            totalReliability = currentReliability;
            totalCost = currentCost;
            displayGraphInformation(networkGraph);

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

    public static int calculateSpanningTreeCost(Graph networkGraph){
        int cost = 0;
        for(Edge edge : networkGraph.getNetworkEdges()){
            cost += edge.getCost();
        }

        System.out.println("CURRENT COST : " + cost);
        return cost;

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

    public static Edge getLowestAvailableCostEdge(Graph networkGraph, ArrayList<Edge> edges){
        for(Edge edge : edges){
            if(!networkGraph.getNetworkEdges().contains(edge)){
                return edge;
            }
        }
        return null;
    }

    public static void findCombination(Graph networkGraph, ArrayList<Integer[]> combinationList){

        int arr[] = new int[networkGraph.getNetworkEdges().size()];
        for (int i = 0; i < arr.length; i++){
            arr[i] = i;
        }

        int n = arr.length;

        for(int i = amountOfSpanningEdges; i <= n; i++){
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

    public static int calculateTotalCost(Graph networkGraph){
        int totalCost = 0;

        for(Edge edge : networkGraph.getNetworkEdges()){
            totalCost += edge.getCost();
        }

        System.out.println("TOTAL cost : " + totalCost);
        return totalCost;
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

    public static void parseArguments(String[] args){
        if (args.length > 0){
            // if args 2  then reliabilty and cost given
            // if arg 1 -> cost or reliability
            if (args.length == 3){
                targetReliability = Double.parseDouble(args[2]);
                targetCost = Integer.parseInt(args[3]);
                reliabilityConstraint = 2;

            } else if (args.length == 2){
                if (Double.valueOf(args[1]) != null){
                    targetReliability = Double.parseDouble(args[2]);
                    reliabilityConstraint = 1;
                } else {
                    targetCost = Integer.parseInt(args[2]);
                    reliabilityConstraint = 3;
                }
            }
        } else {
            targetReliability = 0.85;
            targetCost = 110;
            reliabilityConstraint = 2;
        }
    }

    public static void displayGraphInformation(Graph networkGraph){
        ArrayList<Edge> finalEdges = networkGraph.getNetworkEdges();

        System.out.println("----- NETWORK DESIGN -----");
        System.out.println("----- EDGES -----");
        for(Edge edge : finalEdges){
            System.out.println("CONNECTED EDGE WITH CITY " + (edge.source.getId() + 1)
            + " AND CITY : " + (edge.destination.getId() + 1)
            + " EDGE RELIABILITY : " + edge.getReliability()
            + " EDGE COST : " + edge.getCost());
        }
        System.out.println("----- RELIABILITY -----");
        System.out.println("DESIGN SPANNING RELIABILITY : " + spanningReliability);
        if (reliabilityConstraint == 1 || reliabilityConstraint == 2){
            System.out.println("DESIGN TARGET RELIABILITY : " + targetReliability);
        }
        System.out.println("DESIGN OPTIMIZED RELIABILITY : " + totalReliability);
        if (reliabilityConstraint == 2 || reliabilityConstraint == 3){
            System.out.println("----- COST -----");
            System.out.println("DESIGN SPANNING COST : " + spanningCost);
            if (reliabilityConstraint == 2){
                System.out.println("DESIGN TARGET COST : " + targetCost);
            }
            System.out.println("DESIGN OPTIMIZED COST : " + totalCost);
        }
        System.out.println("----- END -----");
    }
}

