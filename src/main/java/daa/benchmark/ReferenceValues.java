package daa.benchmark;

import java.util.List;

/** Expected outputs are assertions; the algorithms calculate results independently. */
public final class ReferenceValues {
    private ReferenceValues() { }
    public record Expected(int n, int m, long mst, int scc, long distanceSum) { }
    public static final List<Expected> TABLE = List.of(
        new Expected(1000, 4000, 147970, 27, 396274),
        new Expected(1000, 20000, 30363, 1, 252411),
        new Expected(3162, 12648, 468911, 137, 1271911),
        new Expected(3162, 63240, 97589, 1, 723157),
        new Expected(10000, 40000, 1486139, 373, 3941828),
        new Expected(10000, 200000, 309550, 1, 2381342),
        new Expected(31623, 126492, 4715515, 1188, 12551891),
        new Expected(31623, 632460, 968517, 1, 7376612),
        new Expected(100000, 400000, 15012094, 3923, 40096539),
        new Expected(100000, 2000000, 3056681, 1, 23512087)
    );
    public static void require(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
