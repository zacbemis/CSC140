// Backtracking solver
public class KnapsackBTSolver extends KnapsackBFSolver {

	private void FindSolns(int itemNum, int curWeight) {
		int itemCnt = inst.GetItemCnt();

		if (itemNum == itemCnt + 1) {
			CheckCrntSoln();
			return;
		}
		crntSoln.DontTakeItem(itemNum);
		FindSolns(itemNum + 1, curWeight);
		int w = inst.GetItemWeight(itemNum);
		if (curWeight + w <= inst.GetCapacity()) {
			crntSoln.TakeItem(itemNum);
			FindSolns(itemNum + 1, curWeight + w);
		}
	}

	public KnapsackBTSolver() {
		super();
	}

	public void close() {
		super.close();
	}

	public void Solve(KnapsackInstance inst_, KnapsackSolution soln_) {
		inst = inst_;
		bestSoln = soln_;
		crntSoln = new KnapsackSolution(inst);
		FindSolns(1, 0);
	}
}