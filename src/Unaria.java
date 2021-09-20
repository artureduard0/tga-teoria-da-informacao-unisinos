import java.util.ArrayList;
import java.util.BitSet;

public class Unaria implements IEncoder {
  public BitSet encode(byte[] data) {
    BitSet encodedBits = new BitSet();

    int index = 0;

    for (byte b : data) {

      if (index == 0)
        index = index + b;
      else
        index = index + (b + 1);

      encodedBits.set(index);
    }

    // System.out.println(encodedBits);
    return encodedBits;
  }

  public ArrayList<String> decode(byte[] data) {
    BitSet bits = BitSet.valueOf(data);

    ArrayList<String> simbolosDecodificados = new ArrayList<>();
    int symbol = 0;
    for (int i = 0; i < bits.size(); i++) {
      if (bits.get(i)) {
        String simbolo = Character.toString((char) symbol);
        simbolosDecodificados.add(simbolo);
        symbol = 0;
      } else
        symbol++;
    }

    return simbolosDecodificados;
  }
}