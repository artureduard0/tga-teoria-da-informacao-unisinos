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
    int deltaSizeInBits = findDeltaSizeInBits(maxDelta);

    String deltaSizeInBitsS = Integer.toBinaryString(deltaSizeInBits);
    while (deltaSizeInBitsS.length() < CODEWORD_SIZE) // Prepara o primeiro codeword à ser gravado
      deltaSizeInBitsS = "0" + deltaSizeInBitsS;

    for (int i = 0; i < deltaSizeInBitsS.length(); i++)
      encodedBits.set(indexBit++, deltaSizeInBitsS.charAt(i) == '1' ? true : false);

    for (byte b : data) {

      if (indexBit == 8) { // Na primeira iteração, adiciona o símbolo em binário no início do arquivo
        String symbol = Integer.toBinaryString(b);
        while (symbol.length() < CODEWORD_SIZE) // Prepara o primeiro codeword à ser gravado
          symbol = "0" + symbol;

        for (int i = 0; i < symbol.length(); i++)
          encodedBits.set(indexBit++, symbol.charAt(i) == '1' ? true : false);

        previousByte = b;
      } else {

        if (b == previousByte) { // Caso seja igual ao último bit escrito, grava 0 no arquivo
          encodedBits.set(indexBit++, false);

        } else { // Caso mude o símbolo, grava 1 de stopbit + a variação em 8 bits
          encodedBits.set(indexBit++, true);

          int delta = b - previousByte;
          int signal = 0;

          if (delta < 0) { // seta o signal para 1 caso o delta seja negativo
            signal = 1;
          }

          String deltaS = Integer.toBinaryString(Math.abs(delta)); // Prepara o primeiro codeword à ser gravado
          while (deltaS.length() < deltaSizeInBits)
            deltaS = "0" + deltaS;

          deltaS = signal + deltaS;

          for (int i = 0; i < deltaS.length(); i++) {
            encodedBits.set(indexBit++, deltaS.charAt(i) == '1' ? true : false);
          }

        }
        previousByte = b;

      }

    }
    encodedBits.set(indexBit++, true);
    return encodedBits;
  }

  public ArrayList<String> decode(byte[] data) {
    BitSet bits = BitSet.valueOf(data);
    ArrayList<String> simbolosDecodificados = new ArrayList<>();

    String s = "";

    for (int i = 0; i < 16; i++) { // Transforma os dois primeiros bytes em string
      if (bits.get(i))
        s = s + '1';
      else
        s = s + '0';
    }

    String deltaSizeS = s.substring(0, 8); // Separa o primeiro byte que contém o tamanho do delta
    int deltaSize = Integer.parseInt(deltaSizeS, 2) + 1; // Delta + 1 do signal

    String symbolS = s.substring(8, 16); // Lê, converte e adiciona primeiro símbolo
    int symbol = Integer.parseInt(symbolS, 2);
    String simbolo = Character.toString((char) symbol);
    simbolosDecodificados.add(simbolo);

    boolean isStopBit = true;
    boolean isSignalBit = false;
    boolean signal = false;

    String delta = "";

    for (int i = 16; i < bits.length(); i++) {

      if (isStopBit) {
        if (!bits.get(i)) { // Caso o bit seja 0, reescreve o símbolo
          simbolosDecodificados.add(simbolo);
          continue;
        }
        isStopBit = false;
        isSignalBit = true;
        continue;
      }

      if (isSignalBit) {
        signal = bits.get(i); // True para negativo
        isSignalBit = false;
        continue;
      }

      if (delta.length() < deltaSize - 1) {
        delta += bits.get(i) == true ? "1" : "0";
        continue;
      }
      symbolS = delta;
      if (signal) // Primeiro símbolo do delta = 1 = negativo
        symbol = symbol - Integer.parseInt(symbolS, 2);
      else
        symbol = symbol + Integer.parseInt(symbolS, 2);

      simbolo = Character.toString((char) symbol);
      simbolosDecodificados.add(simbolo);

      i--;
      isStopBit = true;
      delta = "";

    }
    return simbolosDecodificados;

  }

  private int findMaxDelta(byte[] data) {
    byte[] dataCopy = Arrays.copyOf(data, data.length);
    Arrays.sort(dataCopy);
    return dataCopy[dataCopy.length - 1] - dataCopy[0];
  }

  private int findDeltaSizeInBits(int delta) {
    return delta == 0 ? 0 : ((int) Math.ceil((int) (Math.log(delta) / Math.log(2)))) + 1;
  }
}