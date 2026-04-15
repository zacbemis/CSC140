import java.util.Arrays;
import java.util.Comparator;

// Branch-and-Bound solver
public class KnapsackBBSolver extends KnapsackBFSolver {
	protected UPPER_BOUND ub;
	private int totalAllValues;
	private int[] orderByRatio;
	private UpperBoundFn upperBound;

	@FunctionalInterface
	private interface UpperBoundFn {
		double apply(int itemNum, int curWeight, int curValue, int sumRefusedValues);
	}

	private void preprocessFor(UPPER_BOUND kind) {
		if (kind == UPPER_BOUND.UB1) {
			computeTotalValueSum();
		} else if (kind == UPPER_BOUND.UB3 || kind == UPPER_BOUND.UB3_EC) {
			buildRatioOrder();
		}
	}

	private void computeTotalValueSum() {
		int n = inst.GetItemCnt();
		totalAllValues = 0;
		for (int i = 1; i <= n; i++) {
			totalAllValues += inst.GetItemValue(i);
		}
	}

	private void buildRatioOrder() {
		int n = inst.GetItemCnt();
		Integer[] ord = new Integer[n];
		for (int i = 0; i < n; i++) {
			ord[i] = i + 1;
		}
		Arrays.sort(ord, new Comparator<Integer>() {
			@Override
			public int compare(Integer a, Integer b) {
				long va = inst.GetItemValue(a);
				long wa = inst.GetItemWeight(a);
				long vb = inst.GetItemValue(b);
				long wb = inst.GetItemWeight(b);
				return Long.compare(vb * wa, va * wb);
			}
		});
		orderByRatio = new int[n];
		for (int i = 0; i < n; i++) {
			orderByRatio[i] = ord[i];
		}
	}

	private void bindUpperBound(UPPER_BOUND kind) {
		if (kind == UPPER_BOUND.UB1) {
			upperBound = (itemNum, curWeight, curValue, sumRefusedValues) -> ub1(sumRefusedValues);
		} else if (kind == UPPER_BOUND.UB2) {
			upperBound = (itemNum, curWeight, curValue, sumRefusedValues) -> ub2(itemNum, curWeight, curValue);
		} else if (kind == UPPER_BOUND.UB3) {
			upperBound = (itemNum, curWeight, curValue, sumRefusedValues) -> ub3(itemNum, curWeight, curValue);
		} else {
			upperBound = null;
		}
	}

	private int ub1(int sumRefusedValues) {
		return totalAllValues - sumRefusedValues;
	}

	private int ub2(int itemNum, int curWeight, int curValue) {
		int rem = inst.GetCapacity() - curWeight;
		int b = curValue;
		for (int j = itemNum; j <= inst.GetItemCnt(); j++) {
			if (inst.GetItemWeight(j) <= rem) {
				b += inst.GetItemValue(j);
			}
		}
		return b;
	}

	private double ub3(int itemNum, int curWeight, int curValue) {
		int rem = inst.GetCapacity() - curWeight;
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

	private double ub3EcUpperBound(int greedyValue, int tail, int slack) {
		if (tail >= orderByRatio.length || slack == 0) {
			return greedyValue;
		}
		int idx = orderByRatio[tail];
		double ub = greedyValue + ((double) slack * inst.GetItemValue(idx)) / inst.GetItemWeight(idx);
		return Math.nextUp(ub);
	}

	private void FindSol(int itemNum, int curWeight, int curValue, int sumRefusedValues) {
		int itemCnt = inst.GetItemCnt();
		if (itemNum == itemCnt + 1) {
			CheckCrntSoln();
			return;
		}
		if (upperBound.apply(itemNum, curWeight, curValue, sumRefusedValues) <= bestSoln.GetValue()) {
			return;
		}
		int w = inst.GetItemWeight(itemNum);
		if (curWeight + w <= inst.GetCapacity()) {
			crntSoln.TakeItem(itemNum);
			FindSol(itemNum + 1, curWeight + w, curValue + inst.GetItemValue(itemNum), sumRefusedValues);
		}
		crntSoln.DontTakeItem(itemNum);
		FindSol(itemNum + 1, curWeight, curValue, sumRefusedValues + inst.GetItemValue(itemNum));
	}

	private void updateUb3EcIncumbent(int level, int tail, int greedyValue) {
		if (greedyValue <= bestSoln.GetValue()) {
			return;
		}
		bestSoln.ClearForInstance(inst);
		for (int k = 0; k < level; k++) {
			int idx = orderByRatio[k];
			if (crntSoln.IsTaken(idx)) {
				bestSoln.TakeItem(idx);
			}
		}
		for (int k = level; k < tail; k++) {
			bestSoln.TakeItem(orderByRatio[k]);
		}
		bestSoln.ComputeValue();
	}

	private void FindSolUb3EcIncremental(int level, int chosenWeight, int greedyValue, double upperBoundValue, int tail, int slack) {
		if (upperBoundValue <= bestSoln.GetValue()) {
			return;
		}
		updateUb3EcIncumbent(level, tail, greedyValue);
		if (level == orderByRatio.length || upperBoundValue <= greedyValue) {
			return;
		}

		int idx = orderByRatio[level];
		int w = inst.GetItemWeight(idx);
		if (chosenWeight + w <= inst.GetCapacity()) {
			crntSoln.TakeItem(idx);
			FindSolUb3EcIncremental(level + 1, chosenWeight + w, greedyValue, upperBoundValue, tail, slack);
			crntSoln.DontTakeItem(idx);
		}

		int childGreedy;
		int childSlack;
		int childTail;
		if (level < tail) {
			childGreedy = greedyValue - inst.GetItemValue(idx);
			childSlack = slack + w;
			childTail = tail;
		} else {
			childGreedy = greedyValue;
			childSlack = slack;
			childTail = tail + 1;
		}
		while (childTail < orderByRatio.length) {
			int tailIdx = orderByRatio[childTail];
			int tailWeight = inst.GetItemWeight(tailIdx);
			if (tailWeight > childSlack) {
				break;
			}
			childSlack -= tailWeight;
			childGreedy += inst.GetItemValue(tailIdx);
			childTail++;
		}
		double childUpperBound = ub3EcUpperBound(childGreedy, childTail, childSlack);
		if (childUpperBound <= bestSoln.GetValue()) {
			return;
		}
		crntSoln.DontTakeItem(idx);
		FindSolUb3EcIncremental(level + 1, chosenWeight, childGreedy, childUpperBound, childTail, childSlack);
	}

	public KnapsackBBSolver(UPPER_BOUND ub_) {
		super();
		ub = ub_;
	}

	@Override
	public void close() {
		super.close();
		orderByRatio = null;
	}

	@Override
	public void Solve(KnapsackInstance inst_, KnapsackSolution soln_) {
		inst = inst_;
		bestSoln = soln_;
		if (crntSoln == null) {
			crntSoln = new KnapsackSolution(inst);
		} else {
			crntSoln.ClearForInstance(inst);
		}
		preprocessFor(ub);
		bindUpperBound(ub);
		if (ub == UPPER_BOUND.UB3_EC) {
			crntSoln.ClearForInstance(inst);
			int tail = 0;
			int slack = inst.GetCapacity();
			int greedyValue = 0;
			while (tail < orderByRatio.length) {
				int idx = orderByRatio[tail];
				int w = inst.GetItemWeight(idx);
				if (w > slack) {
					break;
				}
				slack -= w;
				greedyValue += inst.GetItemValue(idx);
				tail++;
			}
			double upperBoundValue = ub3EcUpperBound(greedyValue, tail, slack);
			updateUb3EcIncumbent(0, tail, greedyValue);
			FindSolUb3EcIncremental(0, 0, greedyValue, upperBoundValue, tail, slack);
		} else {
			FindSol(1, 0, 0, 0);
		}
	}
}
