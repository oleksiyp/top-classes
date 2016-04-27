package io.github.oleksiyp.top_classes.extraction;

class HashEntry implements Comparable<HashEntry> {
    final ByteString key;
    long value;

    HashEntry(ByteString key, long value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public int compareTo(HashEntry o) {
        return -Long.compare(value, o.value);
    }

    public void add(long val) {
        value += val;
    }
}
