package p1;
// Zac Bemis
// CSC140-02
// Assignment 3
// 03-09-26
import java.util.Random;
import java.util.Scanner;
import java.util.Arrays;

public class Sorting {
	final int MAX_SIZE = 10000000;

	// Set this to true if you wish the arrays to be printed.
	final static boolean OUTPUT_DATA = false;

	private static final Random RNG = new Random();

	public static String sortAlg = null;
	public static int size = 10;

	public static void main(String[] args) {
		readInput();
		int[] data = new int[size];
		GenerateSortedData(data, size);
		Sort(data, size, "Sorted Data");

		GenerateNearlySortedData(data, size);
		Sort(data, size, "Nearly Sorted Data");

		GenerateReverselySortedData(data, size);
		Sort(data, size, "Reversely Sorted Data");

		GenerateRandomData(data, size);
		Sort(data, size, "Random Data");

		System.out.println("\nProgram Completed Successfully.");

	}

	@SuppressWarnings("resource")
	public static void readInput() {
		System.out.println("  I:\tInsertion Sort");
		System.out.println("  M:\tMergeSort");
		System.out.println("  Q:\tQuickSort");
		System.out.println("  QD:\tQuickSort with Depth Counting");
		System.out.println("  IS:\tImproved QuickSort");
		System.out.println("  ISD:\tImproved QuickSort with Depth Counting");
		System.out.println("  S:\tSTLSort");
		System.out.println("Enter sorting algorithm:");
		Scanner reader = new Scanner(System.in);
		sortAlg = reader.next();
		System.out.println(sortAlg);
		String sortAlgName = "";

		if (sortAlg.equals("I"))
			sortAlgName = "Insertion Sort";
		else if (sortAlg.equals("M"))
			sortAlgName = "MergeSort";
		else if (sortAlg.equals("Q"))
			sortAlgName = "QuickSort";
		else if (sortAlg.equals("QD"))
			sortAlgName = "QuickSort with Depth Counting";
		else if (sortAlg.equals("IS"))
			sortAlgName = "Improved QuickSort";
		else if (sortAlg.equals("ISD"))
			sortAlgName = "Improved QuickSort with Depth Counting";
		else if (sortAlg.equals("S"))
			sortAlgName = "STLSort";
		else {
			System.out.println("Unrecognized sorting algorithm Code:" + sortAlg);
			System.exit(1);
		}
		System.out.println("Enter input size: ");
		size = reader.nextInt();
		System.out.println("\nSorting Algorithm: " + sortAlgName);
		System.out.println("\nInput Size = " + size);
	}

	/******************************************************************************/

	public static void GenerateSortedData(int data[], int size) {
		int i;

		for (i = 0; i < size; i++)
			data[i] = i * 3 + 5;
	}

	/*****************************************************************************/
	public static void GenerateNearlySortedData(int data[], int size) {
		int i;

		GenerateSortedData(data, size);

		for (i = 0; i < size; i++)
			if (i % 10 == 0)
				if (i + 1 < size)
					data[i] = data[i + 1] + 7;
	}

	/*****************************************************************************/

	public static void GenerateReverselySortedData(int data[], int size) {
		int i;

		for (i = 0; i < size; i++)
			data[i] = (size - i) * 2 + 3;
	}

	/*****************************************************************************/

	public static void GenerateRandomData(int data[], int size) {
		int i;
		for (i = 0; i < size; i++)
			data[i] = new Random().nextInt(10000000);
	}

	/*****************************************************************************/

	public static void Sort(int[] data, int size, String string) {

		System.out.print("\n" + string + ":");
		if (OUTPUT_DATA) {
			printData(data, size, "Data before sorting:");
		}

		// Sorting is about to begin ... start the timer!
		long start_time = System.nanoTime();
		int[] maxDepthHolder = null;
		if (sortAlg.equals("I")) {
			InsertionSort(data, size);
		} else if (sortAlg.equals("M")) {
			MergeSort(data, 0, size - 1);
		} else if (sortAlg.equals("Q")) {
			QuickSort(data, 0, size - 1);
		} else if (sortAlg.equals("QD")) {
			maxDepthHolder = new int[] { 0 };
			QuickSort(data, 0, size - 1, 1, maxDepthHolder, PivotStrategy.RANDOM);
		} else if (sortAlg.equals("S")) {
			STLSort(data, size);
		} else if (sortAlg.equals("IS")) {
			ImprovedQS(data, 0, size - 1);
		} else if (sortAlg.equals("ISD")) {
			maxDepthHolder = new int[] { 0 };
			ImprovedQS(data, 0, size - 1, PivotStrategy.MEDIAN_OF_THREE, maxDepthHolder);
		} else {
			System.out.print("Invalid sorting algorithm!");
			System.out.print("\n");
			System.exit(1);
		}

		// Sorting has finished ... stop the timer!

		double elapsed = System.nanoTime() - start_time;
		elapsed = elapsed / 1000000;

		if (OUTPUT_DATA) {
			printData(data, size, "Data after sorting:");
		}

		if (IsSorted(data, size)) {
			System.out.print("\nCorrectly sorted ");
			System.out.print(size);
			System.out.print(" elements in ");
			System.out.print(elapsed);
			System.out.print("ms");
			if ((sortAlg.equals("QD") || sortAlg.equals("ISD")) && maxDepthHolder != null) {
				System.out.print(" with recursion depth: ");
				System.out.print(maxDepthHolder[0]);
			}
		} else {
			System.out.print("ERROR!: INCORRECT SORTING!");
			System.out.print("\n");
		}
		System.out.print("\n-------------------------------------------------------------\n");
	}

	/*****************************************************************************/

	public static boolean IsSorted(int data[], int size) {
		int i;

		for (i = 0; i < (size - 1); i++) {
			if (data[i] > data[i + 1])
				return false;
		}
		return true;
	}

	/*****************************************************************************/

	public static void InsertionSort(int data[], int size) {
		InsertionSort(data, 0, size - 1);
	}

	// Overload so I can call from improvedQS
	public static void InsertionSort(int data[], int l, int r) {
		for (int i = l + 1; i <= r; i++) {
			int key = data[i];
			int j = i - 1;
			while (j >= l && data[j] > key) {
				data[j + 1] = data[j];
				j--;
			}
			data[j + 1] = key;
		}
	}

	/*****************************************************************************/

	public static void MergeSort(int data[], int l, int r) {
		if (l == r)
			return;
		int m = (l + r) / 2;
		MergeSort(data, l, m);
		MergeSort(data, m + 1, r);
		Merge(data, l, m, r);
	}

	public static void Merge(int data[], int l, int m, int r) {
		int left = m - l + 1;
		int right = r - m;
		int[] L = new int[left + 1];
		int[] R = new int[right + 1];
		for (int i = 0; i < left; i++) {
			L[i] = data[l + i];
		}
		for (int i = 0; i < right; i++) {
			R[i] = data[m + 1 + i];
		}
		L[left] = Integer.MAX_VALUE;
		R[right] = Integer.MAX_VALUE;
		int j = 0, k = 0;
		for (int i = l; i <= r; i++) {
			if (L[j] <= R[k]) {
				data[i] = L[j];
				j++;
			} else {
				data[i] = R[k];
				k++;
			}
		}
	}

	/*****************************************************************************/

	public enum PivotStrategy {
		RANDOM,
		MEDIAN_OF_THREE
	}

	public static void QuickSort(int data[], int l, int r) {
		QuickSort(data, l, r, 1, null, PivotStrategy.RANDOM);
	}

	public static void QuickSort(int data[], int l, int r, PivotStrategy strategy) {
		QuickSort(data, l, r, 1, null, strategy);
	}
	
	public static void QuickSort(int data[], int l, int r, int depth, int[] maxDepth) {
		QuickSort(data, l, r, depth, maxDepth, PivotStrategy.RANDOM);
	}

	private static final int QUICKSORT_INSERTION_THRESHOLD = 40;

	private static void QuickSort(int data[], int l, int r, int depth, int[] maxDepth, PivotStrategy strategy) {
		if (l >= r)
			return;
		if (r - l + 1 <= QUICKSORT_INSERTION_THRESHOLD) {
			InsertionSort(data, l, r);
			return;
		}
		if (maxDepth != null && depth > maxDepth[0])
			maxDepth[0] = depth;
		int p = Partition(data, l, r, strategy);
		QuickSort(data, l, p - 1, depth + 1, maxDepth, strategy);
		QuickSort(data, p + 1, r, depth + 1, maxDepth, strategy);
	}

	public static int Partition(int data[], int l, int r, PivotStrategy strategy) {
		switch (strategy) {
			case RANDOM:
				swap(l + RNG.nextInt(r - l + 1), r, data);
				break;
			case MEDIAN_OF_THREE:
				medianOfThree(data, l, r);
				break;
		}
		int pivot = data[r];
		int i = l - 1;
		for (int j = l; j < r; j++) {
			if (data[j] < pivot) {
				swap(j, i + 1, data);
				i++;
			}
		}
		swap(r, i + 1, data);
		return i + 1;
	}

//	private static void medianOfThree(int data[], int l, int r) {
//		int n = r - l + 1;
//		if (n < 3) return;
//		int i = l + RNG.nextInt(n);
//		int j = l + RNG.nextInt(n);
//		int k = l + RNG.nextInt(n);
//		int medianIdx = i;
//		if (data[i] <= data[j] && data[j] <= data[k] || data[k] <= data[j] && data[j] <= data[i]) {
//			medianIdx = j;
//		} else if (data[i] <= data[k] && data[k] <= data[j] || data[j] <= data[k] && data[k] <= data[i]) {
//			medianIdx = k;
//		}
//		swap(medianIdx, r, data);
//	}

	private static void medianOfThree(int data[], int l, int r) {
		int n = r - l + 1;
		if (n < 3) return;
		int mid = l + (r - l) / 2;
		int a = data[l], b = data[mid], c = data[r];
		int medianIdx = l;
		if (a <= b && b <= c || c <= b && b <= a)
			medianIdx = mid;
		else if (a <= c && c <= b || b <= c && c <= a)
			medianIdx = r;
		swap(medianIdx, r, data);
	}

	public static int CalcRecursionDepth(int[] data, int l, int r) {
		return CalcRecursionDepth(data, l, r, PivotStrategy.RANDOM);
	}

	public static int CalcRecursionDepth(int[] data, int l, int r, PivotStrategy strategy) {
		if (l >= r)
			return 0;
		int[] copy = Arrays.copyOfRange(data, l, r + 1);
		int[] maxDepth = { 0 };
		QuickSort(copy, 0, copy.length - 1, 1, maxDepth, strategy);
		return maxDepth[0];
	}

	/*****************************************************************************/

	private static final int INSERTION_THRESHOLD = 40;
	private static final int DEPTH_FACTOR = 8;
//	private static final int NEARLY_SORTED_DENOM = 10;

	public static void ImprovedQS(int data[], int l, int r) {
		ImprovedQS(data, l, r, PivotStrategy.MEDIAN_OF_THREE);
	}

	public static void ImprovedQS(int data[], int l, int r, PivotStrategy strategy) {
		ImprovedQS(data, l, r, strategy, null);
	}

	public static void ImprovedQS(int data[], int l, int r, PivotStrategy strategy, int[] maxDepthHolder) {
		int n = r - l + 1;
		int maxDepth = n <= 1 ? 0 : DEPTH_FACTOR * (int) (Math.log(n) / Math.log(2));
		improvedQS(data, l, r, maxDepth, 1, strategy, maxDepthHolder);
	}

	private static void improvedQS(int data[], int l, int r, int maxDepth, int depth, PivotStrategy strategy, int[] maxDepthHolder) {
		while (l < r) {
			int len = r - l + 1;
			if (maxDepthHolder != null && depth > maxDepthHolder[0])
				maxDepthHolder[0] = depth;
			if (len <= INSERTION_THRESHOLD) {
				InsertionSort(data, l, r);
				return;
			}
			if (depth >= maxDepth) {
				MergeSort(data, l, r);
				return;
			}
//			if (isSorted(data, l, r))
//				return;
//			if (isReverseSorted(data, l, r)) {
//				reverse(data, l, r);
//				return;
//			}
//			if (isNearlySorted(data, l, r, len)) {
//				InsertionSort(data, l, r);
//				return;
//			}
			int p = Partition(data, l, r, strategy);
			if (p - l < r - p) {
				improvedQS(data, l, p - 1, maxDepth, depth + 1, strategy, maxDepthHolder);
				l = p + 1;
			} else {
				improvedQS(data, p + 1, r, maxDepth, depth + 1, strategy, maxDepthHolder);
				r = p - 1;
			}
		}
	}


//	private static boolean isSorted(int data[], int l, int r) {
//		for (int i = l; i < r; i++)
//			if (data[i] > data[i + 1])
//				return false;
//		return true;
//	}
//
//	private static boolean isNearlySorted(int data[], int l, int r, int len) {
//		int inversions = 0;
//		int limit = len / NEARLY_SORTED_DENOM;
//		for (int i = l; i < r; i++) {
//			if (data[i] > data[i + 1] && ++inversions > limit)
//				return false;
//		}
//		return true;
//	}
//
//	private static boolean isReverseSorted(int data[], int l, int r) {
//		for (int i = l; i < r; i++)
//			if (data[i] < data[i + 1])
//				return false;
//		return true;
//	}
//
//	private static void reverse(int data[], int l, int r) {
//		while (l < r) {
//			swap(l, r, data);
//			l++;
//			r--;
//		}
//	}

	/*****************************************************************************/

	public static void STLSort(int data[], int size) {
		Arrays.sort(data);
	}

	/*****************************************************************************/

	public static void swap(int x, int y, int data[]) {
		int temp = data[x];
		data[x] = data[y];
		data[y] = temp;
	}

	/*****************************************************************************/

	public static void printData(int[] data, int size, String title) {
		int i;

		System.out.print("\n");
		System.out.print(title);
		System.out.print("\n");
		for (i = 0; i < size; i++) {
			System.out.print(data[i]);
			System.out.print(" ");
			if (i % 10 == 9 && size > 10) {
				System.out.print("\n");
			}
		}
	}

}
