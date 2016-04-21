package extraction;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public class ByteString {
    public static final Charset CHARSET = Charset.forName("UTF-8");
    public static final ByteString EMPTY = new ByteString(new byte[0], 0, 0);

    private final byte[] buffer;
    private final int offset;
    private final int length;

    public ByteString(byte[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }

    public ByteString copyOf() {
        byte[] bufCopy = Arrays.copyOfRange(this.buffer,
                                            offset,
                                            offset + length);

        return new ByteString(bufCopy, 0, length);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ByteString that = (ByteString) o;

        if (length != that.length) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (buffer[i + offset] != that.buffer[i + that.offset]) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + length;
        for (int i = offset; i < offset + length; i++) {
            result = 31 * result + buffer[i];
        }
        return result;
    }

    @Override
    public String toString() {
        return new String(buffer, offset, length, CHARSET);
    }

    public void writeTo(PrintStream out) {
        out.write(buffer, offset, length);
    }

    public int lastIndexOf(byte item) {
        for (int i = offset + length - 1; i >= offset; i--) {
            if (buffer[i] == item) {
                return i;
            }
        }
        return -1;
    }

    public ByteString subString(int from, int to) {
        return new ByteString(buffer, offset + from, to - from);
    }

    public boolean isEmpty() {
        return length == 0;
    }
}
