package p1;
/**
 * Compares sorting algorithms from Sorting on the same data.
 * Run with optional size argument(s), e.g. java SortingCompare 10000 100000
 */
public class SortingCompare {

	private static final String[] ALGORITHMS = { "I", "M", "Q", "IS", "S" };
	private static final String[] ALG_NAMES = {
			"Insertion", "MergeSort", "QuickSort", "ImprovedQS", "Arrays.sort"
	};
	private static final int[] DEFAULT_SIZES = { 10_000, 100_000, 1_000_000 };

	public static void main(String[] args) {
		int[] sizes = args.length > 0 ? parseSizes(args) : DEFAULT_SIZES;
		System.out.println("Sorting comparison (times in ms)");
		System.out.println("Sizes: " + formatSizes(sizes));
		System.out.println();

		compare("Sorted", sizes, SortingCompare::generateSorted);
		compare("Nearly Sorted", sizes, SortingCompare::generateNearlySorted);
		compare("Reverse Sorted", sizes, SortingCompare::generateReverse);
		compare("Random", sizes, SortingCompare::generateRandom);

		System.out.println("Done.");
	}

	private static void compare(String dataLabel, int[] sizes, DataGenerator gen) {
		System.out.println("--- " + dataLabel + " ---");
		for (int size : sizes) {
			int[] master = new int[size];
			gen.generate(master, size);
			System.out.print("  n=" + String.format("%,d", size) + "\t");
			for (int a = 0; a < ALGORITHMS.length; a++) {
				long time = timeAlgorithm(ALGORITHMS[a], master, size);
				System.out.print(ALG_NAMES[a] + "=" + time + " ms\t");
			}
			System.out.println();
		}
		System.out.println();
	}

	private static long timeAlgorithm(String alg, int[] master, int size) {
		int[] copy = master.clone();
		long start = System.nanoTime();
		switch (alg) {
			case "I":
				Sorting.InsertionSort(copy, size);
				break;
			case "M":
				Sorting.MergeSort(copy, 0, size - 1);
				break;
			case "Q":
				Sorting.QuickSort(copy, 0, size - 1);
				break;
			case "IS":
				Sorting.ImprovedQS(copy, 0, size - 1);
				break;
			case "S":
				Sorting.STLSort(copy, size);
				break;
			default:
				throw new IllegalArgumentException("Unknown algorithm: " + alg);
		}
		long elapsed = (System.nanoTime() - start) / 1_000_000;
		if (!Sorting.IsSorted(copy, size)) {
			System.err.println("ERROR: " + ALG_NAMES[indexOf(alg)] + " did not sort correctly for n=" + size);
		}
		return elapsed;
	}

	private static int indexOf(String alg) {
		for (int i = 0; i < ALGORITHMS.length; i++)
			if (ALGORITHMS[i].equals(alg)) return i;
		return -1;
	}

	private static int[] parseSizes(String[] args) {
		int[] sizes = new int[args.length];
		for (int i = 0; i < args.length; i++) {
			sizes[i] = Integer.parseInt(args[i].replace("_", ""));
		}
		return sizes;
	}

	private static String formatSizes(int[] sizes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < sizes.length; i++) {
			if (i > 0) sb.append(", ");
			sb.append(String.format("%,d", sizes[i]));
		}
		return sb.toString();
	}

	@FunctionalInterface
	private interface DataGenerator {
		void generate(int[] data, int size);
	}

	private static void generateSorted(int[] data, int size) {
		Sorting.GenerateSortedData(data, size);
	}

	private static void generateNearlySorted(int[] data, int size) {
		Sorting.GenerateNearlySortedData(data, size);
	}

	private static void generateReverse(int[] data, int size) {
		Sorting.GenerateReverselySortedData(data, size);
	}

	private static void generateRandom(int[] data, int size) {
		Sorting.GenerateRandomData(data, size);
	}
}
