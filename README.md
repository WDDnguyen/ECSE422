# ECSE422 Communication Network Designer

## Requirements
Java 1.8 <br/>
Terminal/Command line or IDE to compile and run the program. <br/>

## How to run program
In src folder  <br/> 
run command : javac Network.java </br>
run command : java Network arg1 arg2 arg3 <br/>

Program takes minimum 1 argument and maximum of 3 arguments <br/>
Necessary input : input.txt file with correct format <br/>

## Format for input file

Input should only have 3 lines containing no # <br/>

1- number of nodes <br/>
2- reliability major matrix <br/>
3- cost major matrix <br/>

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

Based on provided arguments, output and behavior of program changes
If reliability argument given, only display edges and reliability
If cost argument given, calculate the maximum reliability until cost contraint is surpassed.
If reliability and cost argument given, try to find a possible solution with given reliability with cost. Possible of having no solution depending on the 2 arguments.

SHOW IMAGE 

# Description of program
Program is designed to create a network with all-to-all reliability.

From input text, the program sort the edges depending on reliability or cost and create a spanning tree using kruskal.
Given the constraint, the network graph spanning tree add edges with the highest relialibity or lowest cost will be added. This calculate the new reliability, if edge doesn't attain the reliability then keep adding additional edge. To calculate the reliability, using exhaustive method to calculate the graph.

When graph maxed out constraint and obtained target then network will output  all edges and 





