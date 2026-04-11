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
			upperBound = (itemNum, curWeight, curValue, sumRefusedValues) -> ub3Ec(itemNum, curWeight, curValue);
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
	private int ub3Ec(int itemNum, int curWeight, int curValue) {

	}

	private boolean shouldPrune(int itemNum, int curWeight, int curValue, int sumRefusedValues) {
		return upperBound.apply(itemNum, curWeight, curValue, sumRefusedValues) <= bestSoln.GetValue();
	}

	private void FindSol(int itemNum, int curWeight, int curValue, int sumRefusedValues) {
		int itemCnt = inst.GetItemCnt();
		if (itemNum == itemCnt + 1) {
			CheckCrntSoln();
			return;
		}
		if (shouldPrune(itemNum, curWeight, curValue, sumRefusedValues)) {
			return;
		}
		crntSoln.DontTakeItem(itemNum);
		FindSol(itemNum + 1, curWeight, curValue, sumRefusedValues + inst.GetItemValue(itemNum));
		int w = inst.GetItemWeight(itemNum);
		if (curWeight + w <= inst.GetCapacity()) {
			crntSoln.TakeItem(itemNum);
			FindSol(itemNum + 1, curWeight + w, curValue + inst.GetItemValue(itemNum), sumRefusedValues);
		}
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
		crntSoln = new KnapsackSolution(inst);
		preprocessFor(ub);
		bindUpperBound(ub);
		FindSol(1, 0, 0, 0);
	}
}
