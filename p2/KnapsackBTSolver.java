// Backtracking solver
public class KnapsackBTSolver extends KnapsackBFSolver {
	private int cap;
	private int nItems;
	private int bestValue;

	private void FindSol(int itemNum, int curWeight, int curValue) {
		if (itemNum == nItems + 1) {
			if (bestValue == DefineConstants.INVALID_VALUE || curValue > bestValue) {
				bestValue = curValue;
				bestSoln.Copy(crntSoln);
			}
			return;
		}

		crntSoln.DontTakeItem(itemNum);
		FindSol(itemNum + 1, curWeight, curValue);

		int w = inst.GetItemWeight(itemNum);
		if (curWeight + w <= cap) {
			crntSoln.TakeItem(itemNum);
			FindSol(itemNum + 1, curWeight + w, curValue + inst.GetItemValue(itemNum));
		}
	}

	@Override
	public void Solve(KnapsackInstance inst_, KnapsackSolution soln_) {
		inst = inst_;
		bestSoln = soln_;
		crntSoln = new KnapsackSolution(inst);
		nItems = inst.GetItemCnt();
		cap = inst.GetCapacity();a
		bestValue = DefineConstants.INVALID_VALUE;
		FindSol(1, 0, 0);
		bestSoln.ComputeValue();
	}
}
