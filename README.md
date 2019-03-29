# ECSE422 Communication Network Designer

## Requirements
Java 1.8 <br/>
Terminal/Command line or IDE to compile and run the program. <br/>

## How to run program
In src folder  <br/> 
run command : javac Network.java </br>
run command : java Network ["path/filename"] [reliability] [cost] <br/>

Program takes minimum 1 argument and maximum of 3 arguments <br/>
First argument is necessary, input.txt file with the correct format <br/>

# Example of running the program
For part A (Meet reliability goal) <br/>
Reliability goal : 0.85 <br/>
<b> java Network "../input.txt" 0.85 </b> <br/>

For part B (Meet a given reliability goal subject to a given cost constraint) <br/>
Reliability goal : 0.85 <br/>
Cost constraint : 75 <br/>
<b> java Network "../input.txt" 0.85 75 </b> <br/>

For part C (Maximize reliability subject to a given cost constraint) <br/>
Cost constraint : 75 <br/>
<b> java Network "../input.txt" 75 </b> <br/>

## Format for input file

Input should only have 3 lines containing no # <br/>
1- N number of nodes <br/>
2- N(N-1)/2 numbers of reliability values. This represents the reliability major matrix <br/>
3- N(N-1)/2 numbers cost values. This represents the cost major matrix <br/>

### Example
6 <br/>
0.95 0.90 0.95 0.90 0.95 0.9 0.9 0.9 0.95 0.95 0.9 0.9 0.9 0.9 0.95 <br/>
10 10 15 10 10 10 15 15 10 10 10 15 20 10 10 <br/>

Error is produced if file isn't in this format <br/>

### current input files
input.txt : 6 nodes given initially with project <br/>
input2.txt : 10 nodes with random cost between 0-40 and random reliability between 0.9-0.99 for each edges <br/>
input3.txt : 6 nodes with different cost and reliability than input.txt <br/>

## Output

Based on provided arguments, output and behavior of program changes <br/>
If reliability argument given, only display edges and reliability <br/>

![reliabilityOnly](/images/reliabilityOnly.PNG)

If reliability and cost argument given, try to find a possible solution with given reliability with cost. Possible of having no solution depending on the 2 arguments. <br/>

![reliabilityAndConstraint](/images/reliabilityAndConstraint.PNG)

If cost argument given, calculate the maximum reliability until cost contraint is surpassed. <br/>

![maximizeReliability](/images/reliabilityOnly.PNG)

# Description of program
Program is designed to create a network with all-to-all reliability.

From input text, the program initialize a network graph with N number of vertices and N(N-1)/2 edges.
After parsing the edges, the edges are sorted based on either reliability or cost. A spanning tree is produced using kruskal algorithm with the sorted edges.
If the reliability of spanning tree doesn't meet the goals, the spanning tree add an edge with the highest reliability or lowest cost. The reliability and cost is recalculated with the edge in the network graph. If the goal isn't obtained, keep adding additional edge until no more edge can be added. The reliability is calculated using exhaustive method.

When goal is met, the network outputs a design with the number of edges and the link between each link.

## Limitation of program
Program is quick and can calculate up to 18-20 edges before slowing down when applying exhaustive method to find reliability of additional edges.





