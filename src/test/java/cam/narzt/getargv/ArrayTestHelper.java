package cam.narzt.getargv;

import java.util.Arrays;
import java.util.ArrayList;

class ArrayHelper {
    // These are inefficient and badly named, so only use in tests
    public static <T> T[] push(T[] arr, T item) {
        T[] tmp = Arrays.copyOf(arr, arr.length + 1);
        tmp[tmp.length - 1] = item;
        return tmp;
    }

    public static <T> T[] unshift(T[] arr, T item) {
        ArrayList<T> tmp = new ArrayList<T>(Arrays.asList(arr));
        tmp.add(0, item);
        return tmp.toArray(arr);
    }

    public static <T> T[] pop(T[] arr) {
        T[] tmp = Arrays.copyOf(arr, arr.length - 1);
        return tmp;
    }

    public static <T> T[] shift(T[] arr) {
        T[] tmp = Arrays.copyOfRange(arr, 1, arr.length);
        return tmp;
    }
}
