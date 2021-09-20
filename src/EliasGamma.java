import java.util.ArrayList;
import java.util.BitSet;

public class EliasGamma implements IEncoder {

    @Override
    public BitSet encode(byte[] data) {
        BitSet bitsCodificados = new BitSet();
        int indexBit = 0; // index dos bits no BitSet

        for (byte b: data) {
            int simbolo = b;

            //prefixo: encontrar o valor N que, elevado à potência de dois, fica o mais próximo do número a ser codificado.
            boolean encontrouExpoente = false;
            int expoente = 0;

            while (!encontrouExpoente) {
                double potenciaAtual = Math.pow(2, expoente);
                double potenciaSeguinte = Math.pow(2, expoente + 1);

                if (potenciaAtual <= simbolo && potenciaSeguinte > simbolo) {
                    encontrouExpoente = true;
                    continue;
                }

                expoente++;
            }

            //com o valor de N encontrado, codificar N de forma unária, com N zeros como prefixo.
            indexBit += expoente;

            //sufixo: resto da divisão por 2ⁿ
            int restoDivisao = (int) (simbolo % Math.pow(2, expoente));

            // left shifting de N sobre o stop bit para fazer com que o sufixo tenha o mesmo comprimento do prefixo
            // or exclusivo pra substituir os 0s pelo resto da equação
            String sufixo = Integer.toBinaryString((1 << expoente) | restoDivisao);

            for (int i = 0; i < sufixo.length(); i++) {
                bitsCodificados.set(indexBit++, sufixo.charAt(i) == '1');
            }
        }

        return bitsCodificados;
    }

    @Override
    public ArrayList<String> decode(byte[] data) {
        BitSet bits = BitSet.valueOf(data);

        ArrayList<String> simbolosDecodificados = new ArrayList<>();
        boolean stopBitEncontrado = false;
        StringBuilder sufixo = new StringBuilder();
        StringBuilder prefixo = new StringBuilder();
        int sufixoBits = 0;

        for (int i = 0; i < bits.size(); i++) {
            if (!stopBitEncontrado) {
                if (bits.get(i)) {
                    stopBitEncontrado = true;
                } else {
                    prefixo.append(bits.get(i) ? "1" : "0");
                }
            } else {
                sufixo.append(bits.get(i) ? "1" : "0");
                sufixoBits++;

                int comprimentoPrefixo = prefixo.length();
                if (sufixoBits == comprimentoPrefixo) {
                    int simboloNum = (int) Math.pow(2, comprimentoPrefixo) + Integer.parseUnsignedInt(sufixo.toString(), 2);
                    String simbolo = Character.toString((char) simboloNum);
                    simbolosDecodificados.add(simbolo);

                    sufixo = new StringBuilder();
                    prefixo = new StringBuilder();
                    sufixoBits = 0;
                    stopBitEncontrado = false;
                }
            }
        }

        return simbolosDecodificados;
    }
}
