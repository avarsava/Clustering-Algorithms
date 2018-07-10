package xmedians;

import java.awt.Point;
import java.io.*;
import java.util.*;

/**
 * @author Alexis Varsava <av11sl@brocku.ca>
 * @version 3
 * @since 2016-03-10
 *
 * Performs the X-Means Clustering Algorithm on a provided data set. Based
 * directly off kmeans.java
 */
public class Xmedians {

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
     * Keeps copies of all the previous runs, so we can determine which is best.
     */
    private List<OldRun> oldRuns;

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
     * Number of clusters to generate.
     */
    private int k;

    /**
     * Max number of clusters to generate
     */
    private int maxK;

    /**
     * Keeps track of which points belong to which clusters
     */
    private HashMap<Point, Cluster> dict;

    /**
     * Default constructor
     *
     * Creates blank data and cluster lists, initializes readers and RNG, loads
     * data, then begins clustering.
     */
    public Xmedians() {
        NUM_RUNS = 30;
        D = new LinkedList<>();
        V = new LinkedList<>();
        oldV = new LinkedList<>();
        dict = new HashMap<>();
        oldRuns = new LinkedList<>();
        inputReader = new BufferedReader(new InputStreamReader(System.in));
        output = new File("outputs", "z.txt");
        try {
            fileWriter = new BufferedWriter(new FileWriter(output));
        } catch (IOException ex) {
            System.err.println("Failed to create z.txt");
        }
        setFileReader();
        setD();
        setMaxK();
        generator = new RNG(0, D.size(), inputReader);

        for (int i = 0; i < NUM_RUNS; i++) {
            k = 2;
            initCluster();
            randCluster(V, D);
            cluster();
            oldRuns = new LinkedList<>();
        }
        try {
            inputReader.close();
            fileReader.close();
            fileWriter.close();
        } catch (Exception e) {
            System.err.println("Closing something failed.");
        }

        output.renameTo(new File("outputs", Long.toString(generator.getSeed())));
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
    private void setD() {
        String str;
        String[] strarr;

        while (true) {
            try {
                str = fileReader.readLine();
                str = str.trim();
                strarr = str.split("\\s+");
                D.add(new Point(Integer.parseInt(strarr[0]),
                        Integer.parseInt(strarr[1])));
            } catch (Exception e) {
                break;
            }
        }
    }

    /**
     * Allows the user to set the maximum number of clusters used.
     */
    private void setMaxK() {
        maxK = 0;
        do {
            try {
                System.out.println("Please enter the max number of clusters.");
                System.out.print("> ");
                maxK = Integer.parseInt(inputReader.readLine());
                if (maxK < 0) {
                    throw new IndexOutOfBoundsException();
                } else if (maxK > D.size()) {
                    throw new IllegalArgumentException();
                }

            } catch (IndexOutOfBoundsException i) {
                System.err.println("Please enter a positive number");
            } catch (IllegalArgumentException e) {
                System.err.println(
                        "More clusters than data, please try again.");
            } catch (IOException ex) {
                System.err.println("Failed to read input, please try again.");
            }
        } while (maxK <= 0 || maxK > D.size());
    }

    /**
     * Creates k blank clusters.
     */
    private void initCluster() {
        if (!V.isEmpty()) {
            V.clear();
        }
        for (int i = 0; i < k; i++) {
            V.add(new Cluster());
        }
    }

    /**
     * Assigns the cluster centres initially to be the same as randomly selected
     * data points.
     */
    private void randCluster(List<Cluster> clusterSet, List<Point> dataSet) {
        Point data;
        int rand;
        List<Point> added = new LinkedList<>();
        for (Cluster c : clusterSet) {
            while (true) {
                rand = generator.getIntInRange(dataSet.size());
                data = dataSet.get(rand);
                if (!added.contains(data) || dataSet.size() == 1) {
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
    private void cluster() {

        while (k <= maxK) {
            //run kMeans til convergence
            while (true) {
                dict.clear();
                for (Point dataPoint : D) {
                    addToClosestCluster(dataPoint);
                }

                for (Cluster c : V) {
                    if (!c.getNeighbourhood().isEmpty()) {
                        c.calcNewLoc();
                    }
                    c.clearNeighbourhood();
                }

                if (compareSets(V, oldV)) {
                    break;
                }
                oldV = cloneV();
            }

            for (Point dataPoint : D) {
                addToClosestCluster(dataPoint);
            }
            oldV.clear();

            //add to list of old runs
            oldRuns.add(new OldRun(k, cloneV(), getDunnIndex()));
            
            //split half the clusters
            splitV();
        }
        printOldRuns();
    }

    /**
     * Prints best of the old runs to screen and to file
     */
    public void printOldRuns() {
        Collections.sort(oldRuns);
        System.out.println(oldRuns.get(0));
        try {
            fileWriter.write(oldRuns.get(0).toString());
        } catch (IOException ex) {
            System.err.println("Failed to write line to file");
        }
        try {
            fileWriter.newLine();
        } catch (IOException ex) {
            System.err.println("Somehow, writing a newline failed.");
        }
    }

    /**
     * Splits worst clusters into two new clusters
     */
    public void splitV() {
        LinkedList<ClusterNode> clusterList = new LinkedList<>();
        Cluster toSplit;

        for (Cluster c : V) {
            clusterList.add(new ClusterNode(c, getDMax(c)));
        }

        Collections.sort(clusterList);
        int mid = (clusterList.size() / 2) - 1;
        //split clusters into 2 new ones
        for (int i = 0; i <= mid; i++) {
            LinkedList<Cluster> twoNewClusters = new LinkedList<>();
            twoNewClusters.add(new Cluster());
            twoNewClusters.add(new Cluster());
            toSplit = findClusterFromPoint(
                    clusterList.get(i).getCluster().getLoc());
            randCluster(twoNewClusters, toSplit.getNeighbourhood());
            V.remove(toSplit);
            V.addAll(twoNewClusters);
            k++;
        }

    }

    /**
     * Not sure if regular copying of a LinkedList is deep enough for my
     * purposes, so i'll just do it manually
     *
     * @return clone of V
     */
    private LinkedList<Cluster> cloneV() {
        LinkedList<Cluster> newList = new LinkedList<>();
        for (Cluster c : V) {
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
    private boolean compareSets(List<Cluster> l1, List<Cluster> l2) {
        if (l1.isEmpty() || l2.isEmpty()) {
            return false;
        }

        for (int i = 0; i < Math.max(l1.size(), l2.size()); i++) {
            if (!l1.get(i).equals(l2.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Takes a point and adds it to the neighbourhood of the cluster it is
     * closest to.
     *
     * Achieves this by calculating Euclidean distance.
     *
     * @param p Data Point to associate with a cluster.
     */
    private void addToClosestCluster(Point p) {
        Point cen, shortP = null;
        double ed, shortL = Double.POSITIVE_INFINITY;
        Cluster clus;

        //Calculate the distance between the point and each cluster centre and
        //keep the shortest one
        for (Cluster c : V) {
            cen = c.getLoc();
            ed = calcDist(cen, p);
            if (ed < shortL) {
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
    private Cluster findClusterFromPoint(Point p) {
        for (Cluster c : V) {
            if (c.getLoc().equals(p)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Calculates Dunn Index of the current set of clusters
     *
     * @return Dunn index
     */
    private double getDunnIndex() {
        if (V.size() == 1) {
            return 0.0;
        }
        double dmin, dmax;

        dmin = getDMin();
        dmax = getDMax();

        if (dmax == 0) {
            return 0.0;
        }
        return dmin / dmax;
    }

    /**
     * Calculates first term for Dunn Index
     *
     * @return smallest distance between objects from 2 different clusters
     */
    private double getDMin() {
        Cluster a = null, b = null;
        double shortest = Double.POSITIVE_INFINITY, dist = 0.0;
        for (Point p1 : D) {
            for (Point p2 : D) {
                if (!p1.equals(p2)) {
                    a = getCluster(p1);
                    b = getCluster(p2);
                    if (!a.equals(b)) {
                        //dist = distanceCache.getDistance(p1, p2);
                        dist = calcDist(p1, p2);
                        if (dist < shortest) {
                            shortest = dist;
                        }
                    }
                }
            }
        }
        if (shortest == Double.POSITIVE_INFINITY) {
            System.out.println("say whaat");
        }
        return shortest;
    }

    /**
     * Gets the cluster centred at a Point.
     *
     * @param p point to find cluster at
     * @return cluster at point p
     */
    private Cluster getCluster(Point p) {
        return dict.get(p);
    }

    /**
     * Calculates second term for Dunn Index
     *
     * @return largest distance between 2 objects in the same cluster
     */
    private double getDMax() {
        List<Double> dmaxes = new LinkedList<>();
        for (Cluster c : V) {
            dmaxes.add(getDMax(c));
        }
        dmaxes.sort(null);
        return dmaxes.get(dmaxes.size() - 1);
    }

    private double getDMax(Cluster c) {
        double largest = 0.0, dist;

        for (Point p1 : c.getNeighbourhood()) {
            for (Point p2 : c.getNeighbourhood()) {
                //dist = distanceCache.getDistance(p1, p2);
                dist = calcDist(p1, p2);
                if (dist > largest) {
                    largest = dist;
                }
            }
        }

        return largest;
    }

    /**
     * Calculates a different distance based on which the user has selected
     *
     * @param a First point to find distance from
     * @param b Second point to find distance to
     * @return Distance between two points, calculated some way
     */
    private double calcDist(Point a, Point b) {
        return manDist(a, b);
    }

    /**
     * Calculates Manhattan distance between two points
     * 
     * @see https://reference.wolfram.com/language/ref/ManhattanDistance.html
     * @param a Point a
     * @param b Point b
     * @return  Manhattan distance between Points a and b
     */
    private double manDist(Point a, Point b){
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    /**
     * Main method; launches application
     *
     * @param args unused
     */
    public static void main(String[] args) {
        Xmedians k = new Xmedians();
    }

}
