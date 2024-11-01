package translations;


import java.util.ArrayList;

public class SortManager {

    private static Object rowPivot;

    public static <T extends Comparable<? super T>> void quickSort(ArrayList<T> list) {
        quickSort(list, 0, list.size() - 1);
    }

    private static <T extends Comparable<? super T>> void quickSort(ArrayList<T> list, int low, int high) {
        if(low == high) return;
        int i = low, j = high;

        // Get the pivot element from the middle of the list
        T rowPivot = list.get(low + (high - low) / 2);

        // Divide into two lists
        while (i <= j) {
            // If the current value from the left list is smaller than the pivot
            // element then get the next element from the left list
            while ((list.get(i).compareTo(rowPivot) < 0)) {
                i++;
            }
            // If the current value from the right list is larger than the pivot
            // element then get the next element from the right list
            while ((list.get(j).compareTo(rowPivot) > 0)) {
                j--;
            }

            // If we have found a value in the left list which is larger than
            // the pivot element and if we have found a value in the right list
            // which is smaller than the pivot element then we exchange the
            // values.
            // As we are done we can increase i and j
            if (i <= j) {
                exchange(list, i, j);
                i++;
                j--;
            }
        }
        // Recursion
        if (low < j) quickSort(list, low, j);
        if (i < high) quickSort(list, i, high);
    }

    private static <T extends Comparable<? super T>> void exchange(ArrayList<T> list, int a, int b) {
        T temp = list.get(a);
        list.set(a, list.get(b));
        list.set(b, temp);
    }
}
