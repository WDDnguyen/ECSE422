import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Network {

    private static int numberOfCities;
    private static ArrayList<Double> reliabilityMajorMatrix;
    private static ArrayList<Integer> costMajorMatrix;
    private static String fileName;
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

        readInputFile(fileName);

        initializeVertices(networkGraph);
        createEdges(edges, networkGraph);
       // System.out.println("-----------------------------------");

         if (reliabilityConstraint == 1){
             Comparator<Edge> reliabilityOrder = Comparator.comparing(Edge::getReliability, Comparator.reverseOrder()).thenComparing(Edge::getCost);
             Collections.sort(edges, reliabilityOrder);
         }
         else if (reliabilityConstraint == 2 || reliabilityConstraint == 3){
            Comparator<Edge> costOrder = Comparator.comparing(Edge::getCost).thenComparing(Edge::getReliability, Comparator.reverseOrder());
            Collections.sort(edges, costOrder);
        }

//        for(Edge edge : edges){
//            System.out.println("SORTED EDGE WITH CITY " + (edge.source.getId() + 1)
//                    + " AND CITY : " + (edge.destination.getId() + 1)
//                    + " EDGE RELIABILITY : " + edge.getReliability()
//                    + " EDGE COST : " + edge.getCost());
//        }

        //addEdgeToGraph(networkGraph, edges);
        kruskalMST(networkGraph, edges);
        amountOfSpanningEdges = networkGraph.getNetworkEdges().size();
        spanningReliability = calculateSpanningTreeReliability(networkGraph);
        spanningCost = calculateSpanningTreeCost(networkGraph);

        // value changes based on contraints
        // 1 - Only need to get largest target
        // 2 - Need to get reliability based on cost constraint
        // 3 - maximize reliability

        // TO DO MAKE SURE IF EDGE DOESNT INCREASE RELIABILITY, PICK A NEW ONE.

        double currentReliability = spanningReliability;
        int currentCost = spanningCost;

        // meet target reliability
        if (reliabilityConstraint == 1){
            while (currentReliability <= targetReliability){
               // System.out.println("CURRENT RELIABILITY IS : " + currentReliability);

                Edge additionalEdge = getHighestAvailableEdge(networkGraph, edges);
                // no more edges to enhance
                if(additionalEdge == null){
                    System.out.println("NO DESIGN POSSIBLE WITH TARGET : " + targetReliability + " CURRENT RELIABILITY : " + currentReliability);
                    break;
                }
                updateGraph(networkGraph, additionalEdge);

                // ENUMERATION
                ArrayList<Integer[]> combinationList = new ArrayList<>();
                findCombination(networkGraph, combinationList);
                currentReliability = calculateTotalReliability(networkGraph, combinationList);

                }
               // System.out.println("OBTAINED TARGET RELIABILITY");
                totalReliability = currentReliability;
                displayGraphInformation(networkGraph);
        }
        //  meet constraint reliability
        else if (reliabilityConstraint == 2){
            ArrayList<Edge> additionalEdges = new ArrayList<>();
            while (currentCost < targetCost && currentReliability < targetReliability){

                if(additionalEdges.isEmpty()){
                    // Grab next set of available edges
                    additionalEdges = getLowestAvailableCostEdges(networkGraph, edges);
                    // VERIFIY IF THIS BREAK ANYTHING
                    if(additionalEdges == null){
                        // make sure if no more edges
                        break;
                    }
                }

                // only update with the first available low cost edge.
                updateGraph(networkGraph, additionalEdges.get(0));

                // ENUMERATION
                ArrayList<Integer[]> combinationList = new ArrayList<>();
                findCombination(networkGraph, combinationList);
                currentReliability = calculateTotalReliability(networkGraph, combinationList);
                currentCost = calculateTotalCost(networkGraph);

                additionalEdges.remove(0);

            }

            if (currentReliability >= targetReliability && currentCost <= targetCost){
               // System.out.println("OBTAINED TARGET RELIABILITY WITH COST CONSTRAINT");
                totalReliability = currentReliability;
                totalCost = currentCost;
                displayGraphInformation(networkGraph);
            } else {
                // remove
                networkGraph.getNetworkEdges().remove(networkGraph.getNetworkEdges().size() - 1);

                // if the first edge fails then try every other edges with the same cost. If no edges give high reliability then no design possible.
                for (Edge edge : additionalEdges){

                    // only update with the first available low cost edge.
                    updateGraph(networkGraph, edge);

                    // ENUMERATION
                    ArrayList<Integer[]> combinationList = new ArrayList<>();
                    findCombination(networkGraph, combinationList);
                    currentReliability = calculateTotalReliability(networkGraph, combinationList);
                    currentCost = calculateTotalCost(networkGraph);

                    if (currentReliability >= targetReliability && currentCost <= targetCost) {
                      //  System.out.println("OBTAINED TARGET RELIABILITY WITH COST CONSTRAINT");
                        totalReliability = currentReliability;
                        totalCost = currentCost;
                        displayGraphInformation(networkGraph);
                    }

                    networkGraph.getNetworkEdges().remove(edge);

                }
                System.out.println("CANNOT FIND DESIGN WITH TARGET RELIABILITY : " + targetReliability + " AND COST : " + targetCost);
            }

        } else if (reliabilityConstraint == 3){

            double newReliability;
            int newCost;

            ArrayList<Edge> additionalEdges = new ArrayList<>();
            while (currentCost < targetCost){

                if(additionalEdges.isEmpty()){
                    // Grab next set of available edges
                    additionalEdges = getLowestAvailableCostEdges(networkGraph, edges);
                    if(additionalEdges == null){
                        // make sure if no more edges
                        break;
                    }
                }

                // only update with the first available low cost edge.
                updateGraph(networkGraph, additionalEdges.get(0));

                // ENUMERATION
                ArrayList<Integer[]> combinationList = new ArrayList<>();
                findCombination(networkGraph, combinationList);

                newReliability = calculateTotalReliability(networkGraph, combinationList);
                newCost = calculateTotalCost(networkGraph);

                if (newCost > targetCost){
                    removeNetworkEdge(networkGraph, additionalEdges.get(0));
                    break;
                } else {
                    currentReliability = newReliability;
                    currentCost = newCost;
                }

                additionalEdges.remove(0);

            }


         //   System.out.println("MAXIMIZED RELIABILITY WITH COST CONSTRAINT");
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
                edges.add(new Edge(networkGraph.getVertex(sourceId), networkGraph.getVertex(destinationId), costMajorMatrix.get(matrixIndex), reliabilityMajorMatrix.get(matrixIndex)));
                matrixIndex++;
            }
        }
    }

    public static void readInputFile(String fileName){

        int counter = 0;
        try {
            File f = new File(fileName);
            BufferedReader br = new BufferedReader(new FileReader(f));
            String readLine = "";

            while ((readLine = br.readLine()) != null){
                if (!readLine.contains("#")){
                    System.out.println(readLine);
                    if (counter == 0){
                      //  System.out.println("NUMBER OF CITIES READ");
                        numberOfCities = Integer.valueOf(readLine);
                    } else if (counter == 1){
                      //  System.out.println("RELIABILITY MAJOR MATRIX READ");
                        String[] reliabilityValues = readLine.split(" ");
                        for (String value : reliabilityValues){
                            Double dnum = Double.valueOf(value);
                            reliabilityMajorMatrix.add(dnum);
                        }
                    } else if (counter == 2){
                      //  System.out.println("COST MAJOR MATRIX READ");
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


    // using BFS to check for any cycles
    public static boolean hasCycle(Graph networkGraph){
        HashMap<Vertex, Vertex> parents = new HashMap<>();
        Queue<Vertex> queue = new LinkedList<>();
        ArrayList<Vertex> visitedVertices = new ArrayList<>();

        Vertex source = networkGraph.getVertex(networkGraph.getNetworkEdges().get(0).source.getId());
        Vertex parent = source;
        queue.add(source);
        visitedVertices.add(source);
        parents.put(source,parent);

        boolean firstTime = true;

        while(!queue.isEmpty()){
            Vertex v = queue.remove();
            parent = v;

            for(Vertex neighbor : networkGraph.vertexMap.get(v)){
                if (!(parents.get(v).equals(neighbor))){

                    if(visitedVertices.contains(neighbor) && !firstTime){
                        return true;
                    }

                    visitedVertices.add(neighbor);
                    queue.add(neighbor);
                    parents.put(neighbor, parent);
                }
            }
            firstTime = false;
        }

        return false;
    }

    public static void kruskalMST(Graph networkGraph, ArrayList<Edge> edges){
        //System.out.println("----- SPANNING EDGES -----");
        for(Edge edge : edges){
            networkGraph.addEdge(edge);
            addNetworkEdge(networkGraph, edge);
            if(networkGraph.getNetworkEdges().size() != 1){
                if(hasCycle(networkGraph)){
                    removeNetworkEdge(networkGraph,edge);
                } else {
                   // System.out.println("ADDED EDGE SOURCE ID : " + (edge.source.getId() + 1) + " DEST ID : " + (edge.destination.getId() + 1) + " RELIABILITY : " + edge.reliability + " COST : " + edge.cost);
                    // maximum number of edges of spanning tree done.
                    if (networkGraph.getNetworkEdges().size() == (networkGraph.vertexMap.keySet().size() - 1)) {
                        break;
                    }
                }
            } else {
             //   System.out.println("ADDED EDGE SOURCE ID : " + (edge.source.getId() + 1) + " DEST ID : " + (edge.destination.getId() + 1) + " RELIABILITY : " + edge.reliability + " COST : " + edge.cost);
            }
        }
       // System.out.println("----- SPANNING EDGES DONE -----");

    }

    public static void addNetworkEdge(Graph networkGraph, Edge edge){
        // Source Vertex
        edge.source.addEdge(edge);
        // add , destination to source
        HashSet<Vertex> hs = networkGraph.vertexMap.get(edge.source);
        hs.add(edge.destination);
        networkGraph.vertexMap.put(edge.source, hs);
        edge.source.addEdge(edge);

        // add , source to destination

        HashSet<Vertex> hs2 = networkGraph.vertexMap.get(edge.destination);
        hs2.add(edge.source);
        networkGraph.vertexMap.put(edge.destination, hs2);
        edge.destination.addEdge(edge);
    }

    public static void removeNetworkEdge(Graph networkGraph, Edge edge){

        // add , destination to source
        HashSet<Vertex> hs = networkGraph.vertexMap.get(edge.source);
        hs.remove(edge.destination);
        networkGraph.vertexMap.put(edge.source, hs);
        edge.source.removeEdge(edge);

        // add , source to destination

        HashSet<Vertex> hs2 = networkGraph.vertexMap.get(edge.destination);
        hs2.remove(edge.source);
        networkGraph.vertexMap.put(edge.destination, hs2);
        edge.destination.removeEdge(edge);

        networkGraph.getNetworkEdges().remove(edge);

    }

    public static double calculateSpanningTreeReliability(Graph networkGraph){

        double spanningTreeReliability = 1;
        for(Edge edge : networkGraph.getNetworkEdges()){
            spanningTreeReliability *= edge.getReliability();
        }

       // System.out.println("CURRENT TOTAL RELIABILITY : " + spanningTreeReliability);
        return spanningTreeReliability;
    }

    public static int calculateSpanningTreeCost(Graph networkGraph){
        int cost = 0;
        for(Edge edge : networkGraph.getNetworkEdges()){
            cost += edge.getCost();
        }

        //System.out.println("CURRENT COST : " + cost);
        return cost;

    }

    public static Edge getHighestAvailableEdge(Graph networkGraph, ArrayList<Edge> edges) {
        for (Edge edge : edges) {
            if (!networkGraph.getNetworkEdges().contains(edge)) {
                return edge;
            }
        }
        return null;
    }

    public static Edge findEdge(Graph networkGraph, ArrayList<Edge> edges){
        // add new edge base on the 2 lowest reliable edges
       // System.out.println("LOWEST EDGE : " + networkGraph.getLowestReliableEdge().getReliability() + " SECOND LOWEST EDGE : " + networkGraph.getSecondLowestEdge().getReliability());
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
              //  System.out.println("NEW EDGE TO BE ADDED SOURCE ID : " + (edge.source.getId() + 1) + " DEST ID : " + (edge.destination.getId() + 1) + " RELIABILITY : " + edge.getReliability());
            }
        }

        return newEdge;
    }


    public static ArrayList<Edge> getLowestAvailableCostEdges(Graph networkGraph, ArrayList<Edge> edges){
        ArrayList<Edge>  lowestAvailableCostEdges = new ArrayList<>();
        boolean firstAvailableEdge = true;
        int minimumAvailableCost = 0;
        for(Edge edge : edges){
            if(!networkGraph.getNetworkEdges().contains(edge)){

                if(firstAvailableEdge){
                    lowestAvailableCostEdges.add(edge);
                   minimumAvailableCost = edge.getCost();
                   firstAvailableEdge = false;
                }

                else if (edge.getCost() == minimumAvailableCost){
                    lowestAvailableCostEdges.add(edge);
                }
            }
        }
        return lowestAvailableCostEdges;
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
               // System.out.println("Adding reliability " + reliability);
                totalReliability += reliability;
            }
        }

      //  System.out.println("TOTAL RELIABILITY : " + totalReliability);
        return totalReliability;
    }

    public static int calculateTotalCost(Graph networkGraph){
        int totalCost = 0;

        for(Edge edge : networkGraph.getNetworkEdges()){
            totalCost += edge.getCost();
        }

      //  System.out.println("TOTAL cost : " + totalCost);
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

     //   System.out.println("UPDATE : ADD EDGE SOURCE ID : " + (source.getId() + 1) + " DEST ID : " + (destination.getId() + 1) + " RELIABILITY : " + edge.getReliability() + " COST : " + edge.getCost());

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
                //System.out.println("GRAPH NOT CONNECTED WITH THIS COMBINATION");
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
            fileName = args[0];
            // if args 3  then reliabilty and cost given
            // if arg 2 -> cost or reliability
            if (args.length == 3){

                System.out.println("BOTH RELIABILITY AND COST");
                targetReliability = Double.parseDouble(args[1]);
                targetCost = Integer.parseInt(args[2]);
                reliabilityConstraint = 2;

            } else if (args.length == 2){
                double value = Double.parseDouble(args[1]);
                if (value < 1){
                    System.out.println("ONLY RELIABILITY");
                    targetReliability = Double.parseDouble(args[1]);
                    reliabilityConstraint = 1;
                } else {
                    System.out.println("ONLY COST");
                    targetCost = Integer.parseInt(args[1]);
                    reliabilityConstraint = 3;
                }
            }
        } else {
            // default values
            targetReliability = 0.85;
            targetCost = 70;
            reliabilityConstraint = 2;
            fileName = "./input.txt";
        }
    }

    public static void displayGraphInformation(Graph networkGraph){
        ArrayList<Edge> finalEdges = networkGraph.getNetworkEdges();

        System.out.println("----- NETWORK DESIGN -----");
        System.out.println("----- EDGES -----");
        System.out.println("Connected edges : ");
        for(Edge edge : finalEdges){
            System.out.println(" [" + (edge.source.getId() + 1) + ", " + (edge.destination.getId() + 1) + "]");
//            System.out.println("NETWORK HAS EDGE WITH CITY " + (edge.source.getId() + 1)
//            + " AND CITY : " + (edge.destination.getId() + 1)
//            + " EDGE RELIABILITY : " + edge.getReliability()
//            + " EDGE COST : " + edge.getCost());
        }
        System.out.println("NETWORK HAS TOTAL EDGE : " + networkGraph.getNetworkEdges().size());
        System.out.println("----- RELIABILITY -----");
        System.out.println("DESIGN SPANNING RELIABILITY : " + spanningReliability);
        if (reliabilityConstraint == 1 || reliabilityConstraint == 2){
            System.out.println("DESIGN TARGET RELIABILITY : " + targetReliability);
        }

        if (reliabilityConstraint == 3){
            System.out.println("DESIGN MAX RELIABILITY : " + totalReliability);
        } else if (reliabilityConstraint == 1 || reliabilityConstraint == 2) {
            System.out.println("DESIGN OPTIMIZED RELIABILITY : " + totalReliability);
        }

        if (reliabilityConstraint == 2 || reliabilityConstraint == 3){
            System.out.println("----- COST -----");
            System.out.println("DESIGN SPANNING COST : " + spanningCost);
            System.out.println("DESIGN TARGET COST : " + targetCost);
            System.out.println("DESIGN OPTIMIZED COST : " + totalCost);
        }
        System.out.println("----- END -----");
        System.exit(0);
    }
}

