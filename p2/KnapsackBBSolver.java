import java.util.Arrays;

// Branch-and-Bound solver
public class KnapsackBBSolver extends KnapsackBFSolver {
	protected UPPER_BOUND ub;

	private int totalAllValues;
	private int[] orderByRatio;
	/** Cached for this Solve() — avoids repeated GetCapacity / GetItemCnt in hot paths. */
	private int cap;
	private int nItems;
	private int bestValue;

	private void prepare() {
		if (ub == UPPER_BOUND.UB1) {
			totalAllValues = 0;
			for (int i = 1; i <= nItems; i++) {
				totalAllValues += inst.GetItemValue(i);
			}
		} else if (ub == UPPER_BOUND.UB2) {
			orderByRatio = null;
		} else {
			int n = nItems;
			Integer[] ord = new Integer[n];
			for (int i = 0; i < n; i++) {
				ord[i] = i + 1;
			}
			Arrays.sort(ord, (a, b) -> {
				long va = inst.GetItemValue(a);
				long wa = inst.GetItemWeight(a);
				long vb = inst.GetItemValue(b);
				long wb = inst.GetItemWeight(b);
				return Long.compare(vb * wa, va * wb);
			});
			orderByRatio = new int[n];
			for (int i = 0; i < n; i++) {
				orderByRatio[i] = ord[i];
			}
		}
	}

	private int ub2(int itemNum, int curWeight, int curValue) {
		int rem = cap - curWeight;
		int b = curValue;
		for (int j = itemNum; j <= nItems; j++) {
			if (inst.GetItemWeight(j) <= rem) {
				b += inst.GetItemValue(j);
			}
		}
		return b;
	}

	private double ub3(int itemNum, int curWeight, int curValue) {
		int rem = cap - curWeight;
		double add = 0;
		int capLeft = rem;
		for (int k = 0; k < orderByRatio.length; k++) {
			int idx = orderByRatio[k];
			if (idx < itemNum) {
				continue;
			}
			int w = inst.GetItemWeight(idx);
			int v = inst.GetItemValue(idx);
			if (w <= capLeft) {
				add += v;
				capLeft -= w;
			} else {
				add += (double) v * capLeft / w;
				break;
			}
		}
		return curValue + add;
	}

	private boolean shouldPrune(int itemNum, int curWeight, int curValue, int sumRefusedValues) {
		if (bestValue == DefineConstants.INVALID_VALUE) {
			return false;
		}
		if (ub == UPPER_BOUND.UB1) {
			return totalAllValues - sumRefusedValues <= bestValue;
		}
		if (ub == UPPER_BOUND.UB2) {
			return ub2(itemNum, curWeight, curValue) <= bestValue;
		}
		return ub3(itemNum, curWeight, curValue) <= bestValue;
	}

	private void FindSol(int itemNum, int curWeight, int curValue, int sumRefusedValues) {
		if (itemNum == nItems + 1) {
			if (bestValue == DefineConstants.INVALID_VALUE || curValue > bestValue) {
				bestValue = curValue;
				bestSoln.Copy(crntSoln);
			}
			return;
		}
		if (shouldPrune(itemNum, curWeight, curValue, sumRefusedValues)) {
			return;
		}

		int v = inst.GetItemValue(itemNum);
		crntSoln.DontTakeItem(itemNum);
		FindSol(itemNum + 1, curWeight, curValue, sumRefusedValues + v);

		int w = inst.GetItemWeight(itemNum);
		if (curWeight + w <= cap) {
			crntSoln.TakeItem(itemNum);
			FindSol(itemNum + 1, curWeight + w, curValue + v, sumRefusedValues);
		}
	}

	public KnapsackBBSolver(UPPER_BOUND ub_) {
		super();
		ub = ub_;
	}

	@Override
	public void close() {
		orderByRatio = null;
		super.close();
	}

	@Override
	public void Solve(KnapsackInstance inst_, KnapsackSolution soln_) {
		inst = inst_;
		bestSoln = soln_;
		crntSoln = new KnapsackSolution(inst);
		nItems = inst.GetItemCnt();
		cap = inst.GetCapacity();
		bestValue = DefineConstants.INVALID_VALUE;
		prepare();
		FindSol(1, 0, 0, 0);
		bestSoln.ComputeValue();
	}
}
