import java.math.BigInteger;
import java.util.BitSet;

import javax.naming.InitialContext;

public class TratamentoErros {
    public byte[] encode(byte[] encodedBits) {
        BitSet crc8 = crc8(encodedBits);

        for (int i = 0; i < 8; i++) {
            System.out.print(crc8.get(i) == true ? "1" : "0");
        }
        BitSet bits = BitSet.valueOf(encodedBits);
        BitSet tratErro = new BitSet();

        int index = 0;
        for (int i = 0; i < 16; i++) {
            tratErro.set(index++, bits.get(i));
        }

        for (int i = 0; i < 8; i++) {
            tratErro.set(index++, crc8.get(i));
        }

        // contém o restante dos bits do arquivo sem o cabeçalho
        BitSet restoBits = bits.get(index + 1, bits.size());

        return tratErro.toByteArray();
    }

    private BitSet crc8(byte[] encodedBits) {
        StringBuilder builder = new StringBuilder("00000000");
        String algoritmoBin = Integer.toBinaryString(encodedBits[0]);
        builder.replace(8 - algoritmoBin.length(), 8, algoritmoBin);
        String algoritmo = builder.toString();

        String divisorBin = Integer.toBinaryString(encodedBits[1]);
        builder.replace(8 - divisorBin.length(), 8, divisorBin);
        String divisor = builder.toString();

        String bits = algoritmo + divisor + "00000000";
        BitSet bitSet = new BitSet();

        for (int i = 0; i < bits.length(); i++) {
            bitSet.set(i, bits.charAt(i) == '1');
        }

        String polinomio = "100000111";
        int bitMaisSignificativo = 0;
        int bitIndex = 0;

        for (int i = bitIndex; i < 24; i++) {
            if (bitSet.get(i)) {
                bitMaisSignificativo = i;
                break;
            }
        }

        int limite = bitMaisSignificativo + 9;
        BitSet valor = bitSet.get(bitMaisSignificativo, limite);
        BitSet resto = new BitSet(9);
        int nroBitsDescer = 0;

        resto = valor;

        while (true) {
            for (int i = 0; i < 9; i++) {
                if (resto.get(i)) {
                    bitMaisSignificativo = i;
                    break;
                }

                nroBitsDescer++;
            }

            if (nroBitsDescer + limite > limite) {
                int bitsRestantes = 24 - limite;
                int nroBitsResto = 9 - bitMaisSignificativo;
                int nroZeros = 8 - (bitsRestantes + nroBitsResto);

                int index = 0;
                for (int i = 0; i < nroZeros; i++) {
                    resto.set(index++, false);
                }

                for (int i = 0; i < nroBitsResto; i++) {
                    resto.set(index++, resto.get(bitMaisSignificativo + i));
                }

                for (int i = 1; i <= bitsRestantes; i++) {
                    resto.set(index++, bitSet.get(limite + i));
                }

                return resto;
            } else {
                // xor antes de descer os bits
                int indice = 0;
                for (int i = bitMaisSignificativo; i < 9; i++) {
                    resto.set(indice, resto.get(i) == (polinomio.charAt(indice) == '1') ? false : true);
                    indice++;
                }

                // xor descendo os bits
                for (int i = limite; i < limite + nroBitsDescer; i++) {
                    resto.set(indice, bitSet.get(i) == (polinomio.charAt(indice) == '1') ? false : true);
                    indice++;
                }

                limite += nroBitsDescer;
                nroBitsDescer = 0;
            }
        }
    }

    public boolean checkCrc8Decode(byte[] data) {
        BitSet tratErro = BitSet.valueOf(data);

        String crcCabecalho = "";
        for (int i = 16; i < 24; i++) {
            crcCabecalho += tratErro.get(i) == true ? "1" : "0";
        }

        BitSet crc8 = crc8(data);
        String checkCrc = "";
        for (int i = 0; i < 8; i++) {
            checkCrc += crc8.get(i) == true ? "1" : "0";
        }

        return checkCrc.equals(crcCabecalho);
    }
}