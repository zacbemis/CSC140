
// Brute-force solver
public class KnapsackBFSolver implements java.io.Closeable {
	protected KnapsackInstance inst;
	protected KnapsackSolution crntSoln;
	protected KnapsackSolution bestSoln;

	public void FindSolns(int itemNum) {
		int itemCnt = inst.GetItemCnt();

		if (itemNum == itemCnt + 1) {
			CheckCrntSoln();
			return;
		}
		crntSoln.DontTakeItem(itemNum);
		FindSolns(itemNum + 1);
		crntSoln.TakeItem(itemNum);
		FindSolns(itemNum + 1);
	}

	public int getTotalWeight(KnapsackInstance inst) {
		int totalWeight = 0;
		for (int i = 0; i <= inst.GetItemCnt(); i++) {
			totalWeight += inst.GetItemWeight(i);
		}
		return totalWeight;
	}

	public void CheckCrntSoln() {
		int crntVal = crntSoln.ComputeValue();
		if (KnapSack.VERBOSE_SOLVER_TRACE) {
			System.out.print("\nChecking solution ");
			crntSoln.Print(" ");
		}

		if (crntVal == DefineConstants.INVALID_VALUE) {
			return;
		}

		if (bestSoln.GetValue() == DefineConstants.INVALID_VALUE) // The first solution is initially the best solution
		{
			bestSoln.Copy(crntSoln);
		} else {
			if (crntVal > bestSoln.GetValue()) {
				bestSoln.Copy(crntSoln);
			}
		}
	}

	public KnapsackBFSolver() {
		crntSoln = null;
	}

	public void close() {
		if (crntSoln != null) {
			crntSoln = null;
		}
	}

	public void Solve(KnapsackInstance inst_, KnapsackSolution soln_) {
		inst = inst_;
		bestSoln = soln_;
		crntSoln = new KnapsackSolution(inst);
		FindSolns(1);
	}
}