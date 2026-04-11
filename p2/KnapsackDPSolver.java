
// Dynamic programming solver
public class KnapsackDPSolver extends KnapsackBFSolver {
	private int[][] dp;

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
		FindSol();
	}
}
