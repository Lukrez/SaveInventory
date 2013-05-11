package saveinventory;

public class Quicksort {
	private String[] numbers;
	private int number;

	public String[] sort(String[] values) {
		// Check for empty or null array
		if (values == null || values.length == 0) {
			return null;
		}
		this.numbers = values;
		number = values.length;
		quicksort(0, number - 1);
		return numbers;
	}
	
	private int[] toIntArray(String[] x) {
		int[] y = new int[x.length];
		for (int i = 0; i < x.length; i++) {
			y[i] = Integer.parseInt(x[i]);
		}
		return y;
	}
	
	private boolean isNewerInventory(String a, String b) {

		int[] spA = toIntArray(a.split("_"));
		if (spA.length != 5) {
			return true;
		}
		int[] spB = toIntArray(b.split("_"));
		if (spB.length != 5) {
			return false;
		}
		for (int i = 0; i < 5; i++) {
			if (spA[i] > spB[i]) {
				return true;
			} else if (spA[i] < spB[i]){
				return false;
			}
		}
		System.out.println(a + " < " + b);
		return false;
	}

	private void quicksort(int low, int high) {
		int i = low, j = high;
		// Get the pivot element from the middle of the list
		String pivot = numbers[low + (high - low) / 2];

		// Divide into two lists
		while (i <= j) {
			// If the current value from the left list is smaller then the pivot
			// element then get the next element from the left list
			while (isNewerInventory(numbers[i], pivot)) {
				i++;
			}
			// If the current value from the right list is larger then the pivot
			// element then get the next element from the right list
			while (!isNewerInventory(numbers[i], pivot)) {
				j--;
			}

			// If we have found a values in the left list which is larger then
			// the pivot element and if we have found a value in the right list
			// which is smaller then the pivot element then we exchange the
			// values.
			// As we are done we can increase i and j
			if (i <= j) {
				exchange(i, j);
				i++;
				j--;
			}
		}
		// Recursion
		if (low < j)
			quicksort(low, j);
		if (i < high)
			quicksort(i, high);
	}

	private void exchange(int i, int j) {
		String temp = numbers[i];
		numbers[i] = numbers[j];
		numbers[j] = temp;
	}
}