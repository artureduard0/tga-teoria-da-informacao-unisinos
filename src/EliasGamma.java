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
                    break;
                }

                expoente++;
            }

            //com o valor de N encontrado, codificar N de forma unária, com N zeros como prefixo.
            indexBit += expoente;

            //sufixo: resto da divisão por 2ⁿ
            int restoDivisao = (int) (simbolo % Math.pow(2, expoente));
            
            //stop bit
            bitsCodificados.set(indexBit++, true);

            // concatenar o sufixo ao stop bit
            // o comprimento do sufixo é igual ao do prefixo
            String sufixo = Integer.toBinaryString(restoDivisao);

            for (int i = 0; i < sufixo.length(); i++) {
                bitsCodificados.set(indexBit++, sufixo.charAt(i) == '1' ? true : false);
            }
        }

        // printar bits
        // System.out.println("String a ser escrita encoder: ");
        // for (int i = 0; i < indexBit; i++) {
        // System.out.print(bitsCodificados.get(i) == true ? "1" : "0");
        // }
        // System.out.println();

        return bitsCodificados;
    }

    @Override
    public ArrayList<String> decode(byte[] data) {
        BitSet bits = BitSet.valueOf(data);

        ArrayList<String> simbolosDecodificados = new ArrayList<>();
        boolean stopBitEncontrado = false;
        String sufixo = "";
        int prefixoBits = 0;
        int sufixoBits = 0;
        
        for (int i = 0; i < bits.size(); i++) {
            if (!stopBitEncontrado) {
                if (bits.get(i) == true) {
                    stopBitEncontrado = true;
                } else {
                    prefixoBits++;
                }
            } else { 
                sufixo += bits.get(i) == true ? "1" : "0";
                sufixoBits++;

                if (sufixoBits == prefixoBits) { 
                    int simboloNum = (int) Math.pow(2, prefixoBits) + Integer.parseUnsignedInt(sufixo, 2);
                    String simbolo = Character.toString((char) simboloNum);
                    simbolosDecodificados.add(simbolo);
    
                    sufixo = "";
                    sufixoBits = 0;
                    prefixoBits = 0;
                    stopBitEncontrado = false;
                }
            }
        }

        return simbolosDecodificados;
    }
}
