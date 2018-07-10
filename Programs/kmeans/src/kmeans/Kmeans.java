package kmeans;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexis Varsava <av11sl@brocku.ca>
 * @version 2
 * @since 2016-03-10
 * 
 * Performs the K-Means Clustering Algorithm on a provided data set.
 * Contains code lifted from TestData.java from my neural net.
 * 
 * Removes choice of distance measure for the project.
 */


public class Kmeans {
    /**
     * Number of times to run clustering algorithm.
     */
    private final int NUM_RUNS;
    
    /**
     * Data taken in from the input file.
     */
    private List<Point> D;
    
    /**
     * Set of Clusters placed on the graph.
     */
    private List<Cluster> V;
    
    /**
     * Copy of V, for comparing to so we can stop eventually.
     */
    private List<Cluster> oldV;
    
    /**
     * Files to read from and write to, respectively.
     */
    private File input, output;
    
    /**
     * Random Number Generator.
     */
    private RNG generator;
    
    /**
     * Facilitates reading from console and file, respectively.
     */
    private static BufferedReader inputReader, fileReader;
    
    /**
     * Writes to output file.
     */
    private static BufferedWriter fileWriter;
    
    /**
     * Number of clusters to generate, and which distance measure to use.
     */
    private int k;
    
    /**
     * Keeps track of which points belong to which clusters
     */
    private HashMap<Point, Cluster> dict;
    
    /**
     * Default constructor
     * 
     * Creates blank data and cluster lists, initializes readers and RNG,
     * loads data, then begins clustering.
     */
    public Kmeans(){
        NUM_RUNS = 30;
        D = new LinkedList<>();
        V = new LinkedList<>();
        oldV = new LinkedList<>();
        dict = new HashMap<>();
        inputReader = new BufferedReader(new InputStreamReader(System.in));
        output = new File("outputs\\z.txt");
        try {
            fileWriter = new BufferedWriter(new FileWriter(output));
        } catch (IOException ex) {
            System.err.println("Failed to create z.txt");
        }
        setFileReader();
        setD();
        setK();
        initCluster();
        generator = new RNG(0, D.size());
        for(int i = 0; i < NUM_RUNS; i++){
            randCluster();
            cluster();
        }
        try{
            inputReader.close();
            fileReader.close();
            fileWriter.close();
        } catch(Exception e){
            System.err.println("Closing something failed.");
        }
        
        output.renameTo(new File("outputs\\" + input.getName().split("\\.")[0]
                + "_" + k + ".txt"));
    }
    
    /**
     * Allows the user to specify which file will be read.
     */
    private void setFileReader() {
        String path = null;

        System.out.println("Please enter the path of the data set.");
        while (true) {
            try {
                System.out.print("> ");
                path = inputReader.readLine();
                input = new File(path);
                fileReader = new BufferedReader(new FileReader(input));
                fileReader.mark((int) input.length());
                break;
            } catch (FileNotFoundException fnf) {
                System.err.println("Error: File not found");
            } catch (NullPointerException np) {
                System.err.println("Error: null file");
            } catch (Exception e) {
                System.err.println("Error reading filepath.");
            }
        }
    }

    /**
     * Load data from the file to the list
     * 
     * @return list of points from the file  
     */
    private void setD(){
        String str;
        String[] strarr;
        
        while(true){
            try{
                str = fileReader.readLine();
                str = str.trim();
                strarr = str.split("\\s+");
                D.add(new Point(Integer.parseInt(strarr[0]), 
                        Integer.parseInt(strarr[1])));
            }catch(Exception e){
                break;
            }
        }
    }
    
    /**
     * Allows the user to set the number of clusters used.
     */
    private void setK(){
        k = 0;
        do{
            try{
                System.out.println("Please enter the number of clusters (K).");
                System.out.print("> ");
                k = Integer.parseInt(inputReader.readLine());
                if(k<0) throw new Exception();
            }catch(Exception e){
                System.err.println("Please enter a positive number");
            }
        }while(k<=0);
    }
    
    /**
     * Creates k blank clusters.
     */
    private void initCluster(){
        for(int i = 0; i < k; i++){
            V.add(new Cluster());
        }
    }
    
    /**
     * Assigns the cluster centres initially to be the same as 
     * randomly selected data points.
     */
    private void randCluster(){
        Point data;
        int rand;
        List<Point> added = new LinkedList<>();
        for(Cluster c : V){
            while(true){
                rand = generator.getIntInRange(D.size());
                data = D.get(rand);
                if(!added.contains(data)){
                    c.setLoc(data);
                    added.add(data);
                    break;
                }
            }
            
        }
    }
    
    /**
     * Run loop for clustering.
     */
    private void cluster(){
        while(true){
            dict.clear();
            for(Point dataPoint : D){
                addToClosestCluster(dataPoint);
            }
            
            for(Cluster c : V){
                if(!c.getNeighbourhood().isEmpty()){
                    c.calcNewLoc();
                }
                c.clearNeighbourhood();
            }
            if(compareSets(V, oldV)) break;
            
            oldV = cloneV();
        }
        
        for(Point dataPoint : D){
            addToClosestCluster(dataPoint);
        }
        
        printClusters();
    }
    
    /**
     * Not sure if regular copying of a LinkedList is deep enough for my 
     * purposes, so i'll just do it manually
     * 
     * @return clone of V
     */
    private LinkedList<Cluster> cloneV(){
        LinkedList<Cluster> newList = new LinkedList<>();
        for(Cluster c : V){
            newList.add(new Cluster(c));
        }
        return newList;
    }
    
    /**
     * Compare two lists of Clusters for equivalence
     * 
     * @param l1 first list of clusters
     * @param l2 second list of clusters
     * @return true if equivalent, false if not
     */
    private boolean compareSets(List<Cluster> l1, List<Cluster> l2)
    {
        if(l1.isEmpty() || l2.isEmpty()){
            return false;
        }
        
        for(int i = 0; i < Math.max(l1.size(), l2.size()); i++){
            if (!l1.get(i).equals(l2.get(i))){
                return false;
            }
        }
        return true;
    }
    
    /**
     * Takes a point and adds it to the neighbourhood of the cluster
     * it is closest to.
     * 
     * Achieves this by calculating Euclidean distance.
     * 
     * @param p Data Point to associate with a cluster.
     */
    private void addToClosestCluster(Point p){
        Point cen, shortP = null;
        double ed,  shortL = Double.POSITIVE_INFINITY;
        Cluster clus;
        
        //Calculate the distance between the point and each cluster centre and
        //keep the shortest one
        for(Cluster c : V){
            cen = c.getLoc();
            ed = calcDist(cen, p);
            
            if(ed < shortL){
                shortP = new Point(cen);
                shortL = ed;
            }
        }
        clus = findClusterFromPoint(shortP);
        clus.addNeighbour(p);
        dict.put(p, clus);
    }
    
    
    
    /**
     * Gets the cluster centred at a Point.
     * 
     * @param p point to find cluster at
     * @return cluster at point p
     */
    private Cluster findClusterFromPoint(Point p){
        for(Cluster c : V){
            if (c.getLoc().equals(p)) {
                return c;
            }
        }
        return null;
    }
    
    /**
     * Prints details of cluster to the console and to the output file
     */
    private void printClusters(){
        for(Cluster c : V){
            System.out.print(c.toString() + " ");
            try {
                fileWriter.write(c.toString() + " ");
            } catch (IOException ex) {
                System.err.println("Failed to write line to file");
            }
        }
        System.out.print(getDunnIndex());
        try {
                fileWriter.write(Double.toString(getDunnIndex()));
            } catch (IOException ex) {
                System.err.println("Failed to write line to file");
            }
        System.out.println();
        try {
            fileWriter.newLine();
        } catch (IOException ex) {
            System.err.println("Somehow, writing a newline failed.");
        }
    }
    
    /**
     * Calculates Dunn Index of the current set of clusters
     * @return Dunn index
     */
    private double getDunnIndex(){
        double dmin, dmax;
        
        dmin = getDMin();
        dmax = getDMax();
        
        if(dmax == 0) return 0.0;
        return dmin/dmax;
    }
    
    
    /**
     * Calculates first term for Dunn Index
     * @return smallest distance between objects from 2 different clusters
     */
    private double getDMin(){
        Cluster a = null, b = null;
        double shortest = Double.POSITIVE_INFINITY, dist = 0.0;
        for(Point p1 : D){
            for(Point p2 : D){
                if(!p1.equals(p2)){
                    a = getCluster(p1);
                    b = getCluster(p2);
                    if (!a.equals(b)){
                        dist = calcDist(a.getLoc(), b.getLoc());
                    
                        if(dist < shortest){
                            shortest = dist;
                        }
                    }
                }
            }
        }
        if(shortest == Double.POSITIVE_INFINITY) {
            System.out.println("say whaat");
        }
        return shortest;
    }
    
    private Cluster getCluster(Point p){
        return dict.get(p);
    }
    
    /**
     * Calculates second term for Dunn Index
     * @return largest distance between 2 objects in the same cluster
     */
    private double getDMax(){
        double largest = 0.0, dist;
        
        for(Cluster c : V){
            for(Point p1 : c.getNeighbourhood()){
                for(Point p2 : c.getNeighbourhood()){
                    dist = calcDist(p1, p2);
                    
                    if(dist > largest){
                        largest = dist;
                    }
                }
            }
        }
        
        return largest;
    }
    
    /**
     * Calculates a different distance based on which the user has selected
     * @param a First point to find distance from
     * @param b Second point to find distance to
     * @return Distance between two points, calculated some way
     */
    private double calcDist(Point a, Point b){
        return euDist(a, b); 
    }
    
    /**
     * Calculates Euclidean distances between two points
     * 
     * @param a Point a
     * @param b Point b
     * @return Euclidean distance between Points a and b
     */
    private double euDist(Point a, Point b){
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) 
                + Math.pow(a.getY() - b.getY(), 2));
    }
    
    
    /**
     * Main method; launches application
     * @param args unused
     */
    public static void main(String[] args) {
        Kmeans k = new Kmeans();
    }
    
}
