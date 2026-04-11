
// Dynamic programming solver
public class KnapsackDPSolver implements java.io.Closeable {
	private KnapsackInstance inst;
	private KnapsackSolution soln;
	private int[][] dp;

	public KnapsackDPSolver() {
	}

	public void close() {
		dp = null;
	}

	public void Solve(KnapsackInstance inst_, KnapsackSolution soln_) {
		inst = inst_;
		soln = soln_;
		int n = inst.GetItemCnt();
		int cap = inst.GetCapacity();
		dp = new int[n + 1][cap + 1];

		for (int i = 1; i <= n; i++) {
			int wi = inst.GetItemWeight(i);
			int vi = inst.GetItemValue(i);
			for (int w = 0; w <= cap; w++) {
				int best = dp[i - 1][w];
				if (w >= wi) {
					int take = dp[i - 1][w - wi] + vi;
					if (take > best) {
						best = take;
					}
				}
				dp[i][w] = best;
			}
		}

		reconstruct(n, cap);
		soln.ComputeValue();
	}

	private void reconstruct(int n, int cap) {
		int w = cap;
		for (int i = n; i >= 1; i--) {
			if (dp[i][w] != dp[i - 1][w]) {
				soln.TakeItem(i);
				w -= inst.GetItemWeight(i);
			} else {
				soln.DontTakeItem(i);
			}
		}
	}
}
