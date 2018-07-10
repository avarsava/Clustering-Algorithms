package kmedians;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Alexis Varsava <av11sl@brocku.ca>
 * @version 2
 * @since 2016-04-18
 * 
 * Represents a cluster centre.
 */


public class Cluster {
    /**
     * Location on the graph of the cluster centre.
     */
    private Point location;
    
    /**
     * The Points from the read-in data considered to be associated with
     * this cluster.
     */
    private List<Point> neighbours;
    
    /**
     * Default constructor.
     * 
     * Creates an unplaced cluster with no neighbourhood.
     */
    public Cluster(){
        location = null;
        neighbours = new LinkedList<>();
    }
    
    /**
     * Clone constructor. 
     * 
     * Clones an existing cluster to a new one.
     * 
     * @param c Cluster to clone.
     */
    public Cluster(Cluster c){
        location = new Point(c.getLoc());
        neighbours = new LinkedList<>(c.getNeighbourhood());
    }
    
    /**
     * Returns this cluster's location.
     * @return this cluster's location.
     */
    public Point getLoc(){
        return location;
    }
    
    /**
     * Sets location to be Point p
     * 
     * Clones Point p for safety's sake.
     * 
     * @param p Point to clone to be new centre location 
     */
    public void setLoc(Point p){
        location = new Point(p);
    }
    
    /**
     * Sets location to (x, y)
     * 
     * @param x x-coordinate of new location
     * @param y y-coordinate of new location
     */
    public void setLoc(int x, int y){
        location = new Point(x,y);
    }
    
    /**
     * Takes median the x and y coordinates of the neighbourhood and sets
     * the new locations to that.
     */
    public void calcNewLoc(){
        List<Integer> xs = new LinkedList<>();
        List<Integer> ys = new LinkedList<>();
        
        int middle;
        double nx, ny;
        
        for(Point p : neighbours){
            xs.add((int)p.getX());
            ys.add((int)p.getY());
        }
        
        xs.sort(null);
        ys.sort(null);
        
        middle = neighbours.size()/2;
        
        if(neighbours.size() % 2 == 1){
            //set as middle items
            nx = xs.get(middle);
            ny = ys.get(middle);
            setLoc((int)nx, (int)ny);
        } else {
            //set as avg of middle 2 items
            nx = (xs.get(middle-1) + xs.get(middle))/2;
            ny = (ys.get(middle-1) + ys.get(middle))/2;
            setLoc((int)nx, (int)ny);
        }
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
     * Returns neighbourhood of this cluster
     * @return neighbourhood of this cluster
     */
    public List<Point> getNeighbourhood(){
        return neighbours;
    }
    
    /**
     * Sets the neighbourhood of this cluster to a list of points
     * @param l list of Points to assign as neighbourhood
     */
    public void setNeighbourhood(List<Point> l){
        neighbours = l;
    }
    
    /**
     * Adds a point to the cluster's neighbourhood.
     * 
     * @param p Point to add to the neighbourhood. 
     */
    public void addNeighbour(Point p){
        neighbours.add(p);
    }
    
    /**
     * Replaces the neighbourhood with a fresh LinkedList.
     */
    public void clearNeighbourhood(){
        neighbours = new LinkedList<>();
    }
    
    /**
     * Prints x,y coordinates of cluster location.
     * @return String of cluster location.
     */
    @Override
    public String toString(){
        return Double.toString(location.getX())+" "
                +Double.toString(location.getY());
    }
    
    
    /**
     * Compares if two Clusters are equal in location only, neighbourhood 
     * is ignored.
     * 
     * @param o object to compare cluster to
     * @return true if clusters are in same position, false otherwise
     */
    @Override
    public boolean equals(Object o){
        if(o instanceof Cluster){
            if (this.getLoc().equals(((Cluster)o).getLoc())) return true;
        }
        return false;
    }
}
