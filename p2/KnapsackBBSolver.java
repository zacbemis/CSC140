import java.util.Arrays;
import java.util.Comparator;

// Branch-and-Bound solver
public class KnapsackBBSolver extends KnapsackBFSolver {
	protected UPPER_BOUND ub;
	private int totalAllValues;
	private int[] orderByRatio;
	private int totalFrac;
	private int[] fracContrib;
	private int[] fracWeight;
	private int[] orderIndex;
	private int[] precomputedRefusalDeltas;
	private int[] precomputedTakeDeltas;
	private long[] ratioPrefixWeight;
	private long[] ratioPrefixValue;
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
			if (kind == UPPER_BOUND.UB3_EC) {
				computeTotalFrac();
			}
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

	private void computeTotalFrac() {
		int n = inst.GetItemCnt();
		fracContrib = new int[n + 1];
		fracWeight = new int[n + 1];
		orderIndex = new int[n + 1];
		precomputedRefusalDeltas = new int[n + 1];
		precomputedTakeDeltas = new int[n + 1];
		for (int k = 0; k < orderByRatio.length; k++) {
			orderIndex[orderByRatio[k]] = k;
		}
		buildRatioPrefixSums();
		int capLeft = inst.GetCapacity();
		int sum = 0;
		for (int k = 0; k < orderByRatio.length; k++) {
			int idx = orderByRatio[k];
			int w = inst.GetItemWeight(idx);
			int v = inst.GetItemValue(idx);
			if (w <= capLeft) {
				sum += v;
				fracContrib[idx] = v;
				fracWeight[idx] = w;
				capLeft -= w;
			} else {
				int use = capLeft;
				sum += v * use / w;
				fracContrib[idx] = v * use / w;
				fracWeight[idx] = use;
				break;
			}
		}
		totalFrac = sum;
		for (int i = 1; i <= n; i++) {
			int k = orderIndex[i];
			precomputedRefusalDeltas[i] = -fracContrib[i] + fillFrom(k + 1, fracWeight[i]);
			int extraWeightNeeded = inst.GetItemWeight(i) - fracWeight[i];
			precomputedTakeDeltas[i] = inst.GetItemValue(i) - fracContrib[i] - fillFrom(k + 1, extraWeightNeeded);
		}
	}

	private void buildRatioPrefixSums() {
		int n = orderByRatio.length;
		ratioPrefixWeight = new long[n + 1];
		ratioPrefixValue = new long[n + 1];
		for (int k = 0; k < n; k++) {
			int idx = orderByRatio[k];
			ratioPrefixWeight[k + 1] = ratioPrefixWeight[k] + inst.GetItemWeight(idx);
			ratioPrefixValue[k + 1] = ratioPrefixValue[k] + inst.GetItemValue(idx);
		}
	}

	private int fillFrom(int startK, int cap) {
		int n = orderByRatio.length;
		if (cap <= 0 || startK >= n) {
			return 0;
		}
		long targetWeight = ratioPrefixWeight[startK] + cap;
		int lo = startK;
		int hi = n;
		while (lo < hi) {
			int mid = lo + (hi - lo + 1) / 2;
			if (ratioPrefixWeight[mid] <= targetWeight) {
				lo = mid;
			} else {
				hi = mid - 1;
			}
		}
		int fullEnd = lo;
		long fullValue = ratioPrefixValue[fullEnd] - ratioPrefixValue[startK];
		if (fullEnd == n) {
			return (int) fullValue;
		}
		long usedWeight = ratioPrefixWeight[fullEnd] - ratioPrefixWeight[startK];
		long rem = cap - usedWeight;
		if (rem <= 0) {
			return (int) fullValue;
		}
		int idx = orderByRatio[fullEnd];
		long w = inst.GetItemWeight(idx);
		long v = inst.GetItemValue(idx);
		return (int) (fullValue + (v * rem / w));
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
	private void FindSol(int itemNum, int curWeight, int curValue, int sumRefusedValues) {
		int itemCnt = inst.GetItemCnt();
		if (itemNum == itemCnt + 1) {
			CheckCrntSoln();
			return;
		}
		if (upperBound.apply(itemNum, curWeight, curValue, sumRefusedValues) <= bestSoln.GetValue()) {
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

	private void FindSolUb3Ec(int itemNum, int curWeight, int currentBound) {
		int itemCnt = inst.GetItemCnt();
		if (itemNum == itemCnt + 1) {
			CheckCrntSoln();
			return;
		}
		if (currentBound <= bestSoln.GetValue()) {
			return;
		}
		crntSoln.DontTakeItem(itemNum);
		FindSolUb3Ec(itemNum + 1, curWeight, currentBound + precomputedRefusalDeltas[itemNum]);
		int w = inst.GetItemWeight(itemNum);
		if (curWeight + w <= inst.GetCapacity()) {
			crntSoln.TakeItem(itemNum);
			FindSolUb3Ec(itemNum + 1, curWeight + w,
					currentBound + precomputedTakeDeltas[itemNum]);
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
		fracContrib = null;
		fracWeight = null;
		orderIndex = null;
		precomputedRefusalDeltas = null;
		precomputedTakeDeltas = null;
		ratioPrefixWeight = null;
		ratioPrefixValue = null;
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
			FindSolUb3Ec(1, 0, totalFrac);
		} else {
			FindSol(1, 0, 0, 0);
		}
	}
}
