
import java.util.BitSet;

// Dynamic programming solver
public class KnapsackDPSolver extends KnapsackBFSolver {
	private int[][] dp;
	private static final long DP_MEMORY_SAFETY_DIVISOR = 3L;

	private void FindSol() {
		int n = inst.GetItemCnt();
		int cap = inst.GetCapacity();
		dp = new int[n + 1][cap + 1];

		for (int i = 1; i <= n; i++) {
			int wi = inst.GetItemWeight(i);
			int vi = inst.GetItemValue(i);
			int[] prev = dp[i - 1];
			int[] row = dp[i];
			if (wi > cap) {
				System.arraycopy(prev, 0, row, 0, cap + 1);
			} else {
				System.arraycopy(prev, 0, row, 0, wi);
				for (int w = wi; w <= cap; w++) {
					int take = prev[w - wi] + vi;
					row[w] = prev[w] > take ? prev[w] : take;
				}
			}
		}

		reconstruct(n, cap);
		bestSoln.ComputeValue();
	}

	/**
	 * Memory-light DP:
	 * - keeps only one value row (O(cap) ints)
	 * - stores take decisions as bitsets (O(n*cap) bits) for reconstruction
	 */
	private void FindSolCompressed() {
		int n = inst.GetItemCnt();
		int cap = inst.GetCapacity();
		int[] bestAtWeight = new int[cap + 1];
		BitSet[] takeBits = new BitSet[n + 1];
		for (int i = 1; i <= n; i++) {
			takeBits[i] = new BitSet(cap + 1);
			int wi = inst.GetItemWeight(i);
			int vi = inst.GetItemValue(i);
			for (int w = cap; w >= wi; w--) {
				int take = bestAtWeight[w - wi] + vi;
				if (take > bestAtWeight[w]) {
					bestAtWeight[w] = take;
					takeBits[i].set(w);
				}
			}
		}

		int w = cap;
		for (int i = n; i >= 1; i--) {
			if (takeBits[i].get(w)) {
				bestSoln.TakeItem(i);
				w -= inst.GetItemWeight(i);
			} else {
				bestSoln.DontTakeItem(i);
			}
		}
		bestSoln.ComputeValue();
	}

	private boolean canUseFullTable(int n, int cap) {
		long rows = (long) n + 1L;
		long cols = (long) cap + 1L;
		if (rows <= 0 || cols <= 0) {
			return false;
		}
		long cells = rows * cols;
		if (cells <= 0) {
			return false;
		}
		long rawIntBytes = cells * Integer.BYTES;
		long maxHeap = Runtime.getRuntime().maxMemory();
		return rawIntBytes > 0 && rawIntBytes <= maxHeap / DP_MEMORY_SAFETY_DIVISOR;
	}

	private void reconstruct(int n, int cap) {
		int w = cap;
		for (int i = n; i >= 1; i--) {
			if (dp[i][w] != dp[i - 1][w]) {
				bestSoln.TakeItem(i);
				w -= inst.GetItemWeight(i);
			} else {
				bestSoln.DontTakeItem(i);
			}
		}
	}

	@Override
	public void close() {
		dp = null;
		super.close();
	}

	@Override
	public void Solve(KnapsackInstance inst_, KnapsackSolution soln_) {
		inst = inst_;
		bestSoln = soln_;
		int n = inst.GetItemCnt();
		int cap = inst.GetCapacity();
		if (canUseFullTable(n, cap)) {
			FindSol();
		} else {
			FindSolCompressed();
		}
	}
}
