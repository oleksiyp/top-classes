package extraction;

import java.io.PrintStream;
import java.util.Arrays;

public class TopReporter {
    final PrintStream out;
    final int itemsLimit;

    private TopReporter(PrintStream out, int itemsLimit) {
        this.out = out;
        this.itemsLimit = itemsLimit;
    }

    public void report(HashedStringCounter counter) {
        HashEntry[] entries = counter.allEntries();

        Arrays.sort(entries);

        int item = itemsLimit;

        for (HashEntry entry : entries) {
            out.printf("%7d %s%n",
                       entry.value,
//                     %5.1f%%   100.0 * getRelativeValue(counter, entry),
                       entry.key.toString().replace('/', '.'));

            if (item-- <= 0) {
                break;
            }
        }
    }

    private int compareRelatively(HashedStringCounter counter,
                                  HashEntry o1,
                                  HashEntry o2) {
        long v1 = o1.value * getParentCount(counter, o2);
        long v2 = o2.value * getParentCount(counter, o1);
        return -Long.compare(v1, v2);
    }

    private double getRelativeValue(HashedStringCounter counter, HashEntry entry) {
        double value = entry.value;
        return value / getParentCount(counter, entry);
    }

    private long getParentCount(HashedStringCounter counter, HashEntry entry) {
        long val;
        ByteString parent = entry.key;
        do {
            parent = parentOf(parent);
            if (parent.isEmpty())  {
                return counter.getTotal();
            }
            val = counter.lookup(parent);
        } while (val == entry.value);

        return val;
    }

    public ByteString parentOf(ByteString classOrPackage) {
        int idx = classOrPackage.lastIndexOf((byte) '/');
        if (idx == -1) {
            return ByteString.EMPTY;
        }
        return classOrPackage.subString(0, idx);
    }

    public static TopReporter to(PrintStream out) {
        return new TopReporter(out, Integer.MAX_VALUE);
    }

    public TopReporter onlyFirst(int items) {
        return new TopReporter(out, items);
    }
}
