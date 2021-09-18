import java.util.ArrayList;
import java.util.BitSet;

public class Golomb implements IEncoder {
    private int k;

    public Golomb(int k) {
        this.k = k;
    }

    @Override
    public BitSet encode(byte[] data) {
        BitSet encodedBits = new BitSet();

        int tamanhoSufixo = (int) (Math.log(this.k) / Math.log(2)); // sempre será igual
        int indexBit = 0; // index dos bits no BitSet

        for (byte b : data) {
            int simbolo = b;
            // System.out.println(simbolo);

            // prefixo: repetir zeros conforme o número do resultado da divisão do int atual
            // pelo divisor
            int tamanhoPrefixo = Math.floorDiv(simbolo, k);
            indexBit += tamanhoPrefixo;

            // sufixo: resto da divisão convertido em binário de tamanho igual ao log base 2
            // do divisor k
            int restoDivisao = (simbolo % k);
            // concatenar o sufixo ao stop bit
            String sufixo = Integer.toBinaryString((1 << tamanhoSufixo) | restoDivisao);

            for (int i = 0; i < sufixo.length(); i++) {
                encodedBits.set(indexBit++, sufixo.charAt(i) == '1' ? true : false);
            }
        }

        // printar bits
        // System.out.println("String a ser escrita encoder: ");
        // for (int i = 0; i < indexBit; i++) {
        // System.out.print(encodedBits.get(i) == true ? "1" : "0");
        // }
        // System.out.println();

        return encodedBits;
    }

    @Override
    public ArrayList<String> decode(byte[] data) {
        BitSet bits = BitSet.valueOf(data);

        // System.out.println("String lida decoder: ");
        // for (int i = 0; i < bits.size(); i++) {
        // System.out.print(bits.get(i) == true ? "1" : "0");
        // }
        // System.out.println();

        ArrayList<String> simbolosDecodificados = new ArrayList<>();

        // Primeiro procurar o algoritmo e o divisor. Depois decodificar.
        int tamanhoSufixoRec = (int) (Math.log(this.k) / Math.log(2)); // sempre será igual
        String prefixo = "";
        String sufixo = "";
        int sufixoBits = 0;
        boolean stopBitEncontrado = false;

        for (int i = 0; i < bits.size(); i++) {
            if (!stopBitEncontrado) {
                if (bits.get(i) == true) {
                    stopBitEncontrado = true;
                } else {
                    prefixo += bits.get(i) == true ? "1" : "0";
                }
            } else {
                sufixo += bits.get(i) == true ? "1" : "0";
                sufixoBits++;

                if (sufixoBits >= tamanhoSufixoRec) {
                    int simboloNum = (prefixo.length() * this.k) + Integer.parseUnsignedInt(sufixo, 2);
                    // System.out.println(simboloNum);
                    String simbolo = Character.toString((char) simboloNum);
                    simbolosDecodificados.add(simbolo);

                    prefixo = "";
                    sufixo = "";
                    sufixoBits = 0;
                    stopBitEncontrado = false;
                }
            }
        }

        return simbolosDecodificados;
    }
}
