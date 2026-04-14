import java.util.Arrays;
import java.util.Comparator;

// Branch-and-Bound solver
public class KnapsackBBSolver extends KnapsackBFSolver {
	private static final long MAX_SUFFIX_TABLE_CELLS = 8_000_000L;

	protected UPPER_BOUND ub;
	private int totalAllValues;
	private int[] orderByRatio;
	private long[] ratioPrefixWeight;
	private long[] ratioPrefixValue;
	private int[][] suffixFracBound;
	private boolean useSuffixFracTable;
	private UpperBoundFn upperBound;

	@FunctionalInterface
	private interface UpperBoundFn {
		double apply(int itemNum, int curWeight, int curValue, int sumRefusedValues);
	}

	private void preprocessFor(UPPER_BOUND kind) {
		suffixFracBound = null;
		useSuffixFracTable = false;
		if (kind == UPPER_BOUND.UB1) {
			computeTotalValueSum();
		} else if (kind == UPPER_BOUND.UB3 || kind == UPPER_BOUND.UB3_EC) {
			buildRatioOrder();
			if (kind == UPPER_BOUND.UB3_EC) {
				buildRatioPrefixSums();
				if (shouldBuildSuffixFracBoundTable()) {
					buildSuffixFracBoundTable();
					useSuffixFracTable = true;
				}
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
		if (cap < 0 || startK >= n) {
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

	private void buildSuffixFracBoundTable() {
		int n = orderByRatio.length;
		int cap = inst.GetCapacity();
		suffixFracBound = new int[n + 1][cap + 1];
		for (int level = 0; level <= n; level++) {
			int nextK = level;
			int usedWeight = 0;
			int usedValue = 0;
			for (int rem = 0; rem <= cap; rem++) {
				while (nextK < n) {
					int idx = orderByRatio[nextK];
					int w = inst.GetItemWeight(idx);
					if (usedWeight + w <= rem) {
						usedWeight += w;
						usedValue += inst.GetItemValue(idx);
						nextK++;
					} else {
						break;
					}
				}
				if (nextK == n) {
					suffixFracBound[level][rem] = usedValue;
				} else {
					int idx = orderByRatio[nextK];
					int w = inst.GetItemWeight(idx);
					int v = inst.GetItemValue(idx);
					int frac = (w == 0) ? v : (int) ((long) v * (rem - usedWeight) / w);
					suffixFracBound[level][rem] = usedValue + frac;
				}
			}
		}
	}

	private boolean shouldBuildSuffixFracBoundTable() {
		long nStates = (long) orderByRatio.length + 1L;
		long capStates = (long) inst.GetCapacity() + 1L;
		if (nStates <= 0 || capStates <= 0) {
			return false;
		}
		long cells = nStates * capStates;
		return cells <= MAX_SUFFIX_TABLE_CELLS;
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

	private void initializeGreedyBestSoln() {
		bestSoln.ClearForInstance(inst);
		int capLeft = inst.GetCapacity();
		for (int k = 0; k < orderByRatio.length; k++) {
			int idx = orderByRatio[k];
			int w = inst.GetItemWeight(idx);
			if (w <= capLeft) {
				bestSoln.TakeItem(idx);
				capLeft -= w;
			}
		}
		bestSoln.ComputeValue();
	}

	private void FindSolUb3EcTable(int level, int curWeight, int curValue) {
		if (level == orderByRatio.length) {
			CheckCrntSoln();
			return;
		}
		int rem = inst.GetCapacity() - curWeight;
		int bound = curValue + suffixFracBound[level][rem];
		if (bound <= bestSoln.GetValue()) {
			return;
		}
		int idx = orderByRatio[level];
		crntSoln.DontTakeItem(idx);
		FindSolUb3EcTable(level + 1, curWeight, curValue);
		int w = inst.GetItemWeight(idx);
		if (curWeight + w <= inst.GetCapacity()) {
			crntSoln.TakeItem(idx);
			FindSolUb3EcTable(level + 1, curWeight + w, curValue + inst.GetItemValue(idx));
		}
	}

	private void FindSolUb3EcBinary(int level, int curWeight, int curValue) {
		if (level == orderByRatio.length) {
			CheckCrntSoln();
			return;
		}
		int rem = inst.GetCapacity() - curWeight;
		int bound = curValue + fillFrom(level, rem);
		if (bound <= bestSoln.GetValue()) {
			return;
		}
		int idx = orderByRatio[level];
		crntSoln.DontTakeItem(idx);
		FindSolUb3EcBinary(level + 1, curWeight, curValue);
		int w = inst.GetItemWeight(idx);
		if (curWeight + w <= inst.GetCapacity()) {
			crntSoln.TakeItem(idx);
			FindSolUb3EcBinary(level + 1, curWeight + w, curValue + inst.GetItemValue(idx));
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
		ratioPrefixWeight = null;
		ratioPrefixValue = null;
		suffixFracBound = null;
		useSuffixFracTable = false;
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
			initializeGreedyBestSoln();
			crntSoln.ClearForInstance(inst);
			if (useSuffixFracTable) {
				FindSolUb3EcTable(0, 0, 0);
			} else {
				FindSolUb3EcBinary(0, 0, 0);
			}
		} else {
			FindSol(1, 0, 0, 0);
		}
	}
}
