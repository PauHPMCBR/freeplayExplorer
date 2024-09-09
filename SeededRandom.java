public class SeededRandom {
    int initialSeed;
    long currentSeed;

    public SeededRandom(int seed) {
        this.initialSeed = seed;
        this.currentSeed = seed;
    }

    public float getNext() {
        this.currentSeed = (this.currentSeed * 0x41a7) % 0x7FFFFFFF;
        return (float) (this.currentSeed * 4.656613e-10);
    }

    public double getNextDouble() {
        this.currentSeed = (this.currentSeed * 0x41a7) % 0x7FFFFFFF;
        return this.currentSeed / 2147483646.0;
    }
}
