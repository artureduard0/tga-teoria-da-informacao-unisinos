import java.util.ArrayList;
import java.util.BitSet;

public interface IEncoder {
    public BitSet encode(byte[] data);

    public ArrayList<String> decode(byte[] data);
}
