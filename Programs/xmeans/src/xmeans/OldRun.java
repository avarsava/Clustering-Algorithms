package xmeans;

import java.util.LinkedList;

/**
 * @author Alexis Varsava <av11sl@brocku.ca>
 * @version 1
 * @since 2016-04-22
 * 
 * Stores an old run of the k-means clustering algorithm
 */
public class OldRun implements Comparable<OldRun> {
    /**
     * K value associated with this run
     */
    private int k;
    
    /**
     * Dunn Index score this run achieved
     */
    private double dunn;
    
    /**
     * Final set of clusters in the run
     */
    private LinkedList<Cluster> V;
    
    /**
     * Creates a new OldRun
     * @param n new value for k
     * @param l new list of clusters
     * @param d new dunn index score
     */
    public OldRun (int n, LinkedList<Cluster> l, double d){
        k = n;
        V = new LinkedList<>(l);
        dunn = d;
    }
    
    /**
     * Getter for this run's k value
     * @return k
     */
    public int getK(){
        return k;
    }
    
    /**
     * Getter for this run's Dunn Index score
     * @return Dunn Index
     */
    public double getDunn(){
        return dunn;
    }
    
    /**
     * Getter for this run's set of clusters
     * @return list of clusters
     */
    public LinkedList<Cluster> getV(){
        return V;
    }
    
    @Override
    public String toString(){
        return "K: "+ Integer.toString(k) + " V: " + V.toString() 
                + " Dunn Index: " + Double.toString(dunn);
    }

    @Override
    public int compareTo(OldRun o) {
        if (this.getDunn() > o.getDunn()){
            return -1;
        } else if(this.getDunn() < o.getDunn()){
            return 1;
        } else{
            return 0;
        }
    }
}
