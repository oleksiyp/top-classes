package extraction;

public class HashedStringCounter {
    HashEntry[]table = new HashEntry[1024];
    int bits = 10;
    int bucketsFilled;

    long totalCounter;

    public long getTotal() {
        return totalCounter;
    }

    public void add(ByteString str) {
        if (bucketsFilled * 3 > table.length) {
            rehash();
        }
        add(str, 1);
    }

    private void add(ByteString str, long val) {
        for (int n = 0; n < table.length; n++) {
            int item = openAddressItem(str, n);

            HashEntry entry = table[item];

            if (entry == null) {
                table[item] = new HashEntry(str.copyOf(), val);

                totalCounter += val;
                bucketsFilled++;

                break;
            }

            if (entry.key.equals(str)) {

                entry.add(val);
                totalCounter += val;

                break;
            }
        }
    }

    public long lookup(ByteString str) {
        for (int n = 0; n < table.length; n++) {
            int item = openAddressItem(str, n);

            HashEntry entry = table[item];
            if (entry == null) {
                break;
            }
            if (entry.key.equals(str)) {
                return entry.value;
            }
        }
        return 0;
    }

    private void rehash() {
        HashEntry[] oldTable = table;
        table = new HashEntry[oldTable.length * 2];
        bits++;

        bucketsFilled = 0;
        totalCounter = 0;

        for (HashEntry entry : oldTable) {
            if (entry == null) {
                continue;
            }

            add(entry.key, entry.value);
        }

        System.out.println(bucketsFilled + " distinct elements " +
                           totalCounter + " total counter ");
    }

    private int openAddressItem(ByteString str, int nHash) {
        return (str.hashCode() + nHash) & ((1 << bits) - 1);
    }

    public HashEntry[] allEntries() {
        HashEntry[] entries = new HashEntry[bucketsFilled];
        int n = 0;
        for (HashEntry entry : table) {
            if (entry != null) {
                entries[n++] = entry;
            }
        }
        return entries;
    }
}
