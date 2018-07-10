package xmeans;

import java.io.*;
import java.util.Random;

/**
 * @author Alexis Varsava <av11sl@brocku.ca>
 * @version 2
 * @since 2015-11-27
 * 
 * Pseudo-Random Number Generator, offers a few controls needed for the ANN.
 * 
 * Version 2: Added getRandom();
 * Version 3: Passes in reader for automation
 */
public class RNG {

    private double min, max;
    private Random rng;
    private long seed;
    private BufferedReader reader;

    /**
     * Constructor. Allows for the specification of a min and max for double
     * generation. Allows for user to enter their own seed.
     * 
     * @param minimum minimum value for double generation
     * @param maximum maximum value for double generation
     * @param rdr input reader to accept input from user
     */
    public RNG(double minimum, double maximum, BufferedReader rdr) {
        reader = rdr;
        min = minimum;
        max = maximum;
        setSeed();
        rng = new Random(seed);
    }

    /**
     * Allows for a standard user interface for selecting new or old seeds.
     */
    private void setSeed() {
        String s = "";
        int option = 0;
        boolean tryAgain = true;

        do {
            System.out.println("Please select an option:");
            System.out.println("(1) Generate new seed. (2) Enter seed.");
            System.out.print("> ");
            try {
                s = reader.readLine();
            } catch (IOException ex) {
                System.err.println("Failed to read input.");
            }
            try {
                option = Integer.parseInt(s);
            } catch (Exception e) {
                System.err.println("Please enter 1 or 2.");
            }
        } while (option != 1 && option != 2);

        if (option == 1) {
            seed = System.currentTimeMillis();
        } else if (option == 2) {
            do {
                System.out.println("Please enter the seed.");
                System.out.print("> ");
                try{
                    s = reader.readLine();
                } catch(IOException ex){
                    System.err.println("Failed to read input");
                }
                try{
                    seed = Long.parseLong(s, 10);
                    tryAgain = false;
                } catch (Exception e){
                    System.err.println("Please enter a valid long.");
                }
            } while (tryAgain == true);
        }
        
        System.out.println("Here's the RNG seed:");
        System.out.println(seed);
    }
    
    /**
     * Getter for seed
     * 
     * @return seed
     */
    public long getSeed(){
        return seed;
    }

    /**
     * Generates a random double between the assigned max and min.
     * 
     * @return randomly generated double  
     */
    public double getRandom(){
        return rng.nextDouble() * (max-min) + min;
    }
    
    /**
     * Will keep attempting to generate a random double until one that is not
     * 0 is generated.
     * 
     * @return random double != 0.0
     */
    public double getRandomNotZero() {
        double rand = 0;

        //If we accidentally generate 0, repeat until we don't
        do {
            rand = rng.nextDouble() * (max - min) + min;
        } while (rand == 0);
        return rand;
    }
    
    /**
     * Returns random integer between 0 and specified maximum
     * 
     * @param imax maximum allowed generated int
     * @return random int between 0 and imax
     */
    public int getIntInRange(int imax){
        return rng.nextInt(imax);
    }

    /**
     * Returns random integer between specified minimum and maximum
     * 
     * @param imin minimum allowed generated int
     * @param imax maximum allowed generated int
     * @return random int between imin and imax
     */
        public int getIntInRange(int imin, int imax){
        return rng.nextInt((imax-imin)+1)+imin;
    }
}
