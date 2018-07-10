package xmeans;

/**
 * @author Alexis Varsava <av11sl@brocku.ca>
 * @version 1
 * @since 2016-04-24
 * 
 * Helps form a list of Clusters as well as their dmax score
 */
public class ClusterNode implements Comparable<ClusterNode>{
    /**
     * Cluster this node represents
     */
    private Cluster clust;
    
    /**
     * DMax score associated with this cluster
     */
    private double dmax;
    
    /**
     * Creates a new ClusterNode with a user-defined cluster and dmax score
     * @param c Cluster
     * @param d DMax score
     */
    public ClusterNode(Cluster c, double d){
        clust = c;
        dmax = d;
    }

    /**
     * Getter for cluster
     * @return Cluster
     */
    public Cluster getCluster(){
        return clust;
    }
    
    /**
     * Getter for DMax score
     * @return DMax score associated with this cluster
     */
    public double getDMax(){
        return dmax;
    }
    
    /**
     * Compares two ClusterNodes to order them by dmax score, highest to 
     * lowest
     * 
     * @param o other ClusterNode to compare to
     * @return -1 if this one is greater, 1 if this one is lower, 0 if equal
     */
    @Override
    public int compareTo(ClusterNode o) {
        if (this.getDMax() > o.getDMax()){
            return -1;
        } else if(this.getDMax() < o.getDMax()){
            return 1;
        } else{
            return 0;
        }
    }
    
    /**
     * Creates a string describing the cluster node
     * @return string
     */
    @Override
    public String toString(){
        return "Cluster of " + Integer.toString(clust.getNeighbourhood().size())
                + " with dMax of " + Double.toString(dmax);
    }
}
