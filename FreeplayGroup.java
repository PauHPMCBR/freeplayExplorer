import java.util.List;

public class FreeplayGroup {
    //List<BloonEmission> bloonEmissions;
    double score;
    List<Bounds> bounds;
    Group group;


    /*static class BloonEmission {
        String bloon;
        double time;
        int emissionIndex;
        String name;
        long objectClass;
        long pointer;
    }*/

    static class Group {
        String bloon;
        //double start;
        double end;
        int count;
        //String name;
        //boolean wasCollected;
    }

    static class Bounds {
        int lowerBounds;
        int upperBounds;
    }
}
