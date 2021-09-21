import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;

public class Delta implements IEncoder {
  public BitSet encode(byte[] data) {
    BitSet encodedBits = new BitSet();

    int indexBit = 0;
    int previousByte = 0;
    int CODEWORD_SIZE = 8; // tamanho em bits da primeira codeword do encoder

    int maxDelta = findMaxDelta(data);
    int deltaInBits = findDeltaInBits(maxDelta);

    for (byte b : data) {
      System.out.println("byte: " + b);

      if (indexBit == 0) { // Na primeira iteração, adiciona o símbolo em binário no início do arquivo
        String symbol = Integer.toBinaryString(b);
        while (symbol.length() < CODEWORD_SIZE) // Prepara o primeiro codeword à ser gravado
          symbol = "0" + symbol;

        for (int i = 0; i < symbol.length(); i++)
          encodedBits.set(indexBit++, symbol.charAt(i) == '1' ? true : false);

        System.out.println(symbol);

        previousByte = b;
      } else {

        if (b == previousByte) { // Caso seja igual ao último bit escrito, grava 0 no arquivo
          encodedBits.set(indexBit++, false);
          System.out.println(0);

        } else { // Caso mude o símbolo, grava 1 de stopbit + a variação em 8 bits
          encodedBits.set(indexBit++, true);
          System.out.println(1);

          int delta = b - previousByte;
          int signal = 0;

          if (delta < 0) { // seta o signal para 1 caso o delta seja negativo
            signal = 1;
          }

          String deltaS = Integer.toBinaryString(Math.abs(delta)); // Prepara o primeiro codeword à ser gravado
          while (deltaS.length() < deltaInBits)
            deltaS = "0" + deltaS;

          deltaS = signal + deltaS;

          for (int i = 0; i < deltaS.length(); i++) {
            encodedBits.set(indexBit++, deltaS.charAt(i) == '1' ? true : false);
          }
          System.out.println(deltaS);

        }
        previousByte = b;

      }
    }
    // encodedBits.set(indexBit++, true);
    return encodedBits;
  }

  public ArrayList<String> decode(byte[] data) {
    ArrayList<String> simbolosDecodificados = new ArrayList<>();
    return simbolosDecodificados;
  }

  private int findMaxDelta(byte[] data) {
    byte[] dataCopy = Arrays.copyOf(data, data.length);
    Arrays.sort(dataCopy);
    return dataCopy[dataCopy.length - 1] - dataCopy[0];
  }

  private int findDeltaInBits(int delta) {
    return ((int) Math.ceil((int) (Math.log(delta) / Math.log(2)))) + 1;
  }
}