import java.util.BitSet;

public class TratamentoErros {
    public byte[] encode(byte[] encodedBits) {
        BitSet crc8 = crc8(encodedBits);

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
        BitSet hammingBits = bits.get(16, bits.size());

        // Aplica a codificação hamming nos codewords previamente codificados
        BitSet withHamming = hamming(hammingBits);

        // Junta a codificação hamming ao bitset do cabeçalho
        for (int i = 0; i < withHamming.size(); i++)
            tratErro.set(index++, withHamming.get(i));

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

    // Hamming encoder
    private BitSet hamming(BitSet hammingBits) {
        // Matriz Hamming com os valores utilizados pelo xor para gerar as codewords
        int hammingMatrix[] = { 5, 6, 7, 3 };
        int index = 0;

        BitSet withHamming = new BitSet();

        // Gera um codeword de 3 bits para cada 4 bits de acordo com o algoritmo Hamming
        for (int i = 0; i < hammingBits.size(); i += 4) {

            // XOR entre as posições da matriz de acordo com o codeword lido
            int redundancy = 0;
            if (hammingBits.get(i))
                redundancy ^= hammingMatrix[0];
            if (hammingBits.get(i + 1))
                redundancy ^= hammingMatrix[1];
            if (hammingBits.get(i + 2))
                redundancy ^= hammingMatrix[2];
            if (hammingBits.get(i + 3))
                redundancy ^= hammingMatrix[3];

            // Copia os 4 bits originais ao novo Bitset
            for (int j = 0; j < 4; j++) {
                if (hammingBits.get(i + j))
                    withHamming.set(index);
                index++;
            }
            String redundancyStr = Integer.toBinaryString(redundancy);
            while (redundancyStr.length() < 3)
                redundancyStr = "0" + redundancyStr;

            // Adiciona os 3 bits de redundância ao bitset
            for (int k = 0; k < redundancyStr.length(); k++) {
                withHamming.set(index, redundancyStr.charAt(k) == '1');
                index++;
            }
        }
        return withHamming;
    }

    // Hamming decoder
    public byte[] checkHamming(byte[] data) {
        BitSet tratErro = BitSet.valueOf(data);

        // Lê o valor de cada um dos 7 bits da palavra Hamming
        for (int i = 24; i < tratErro.size(); i += 7) {
            int b0 = tratErro.get(i) ? 1 : 0;
            int b1 = tratErro.get(i + 1) ? 1 : 0;
            int b2 = tratErro.get(i + 2) ? 1 : 0;
            int b3 = tratErro.get(i + 3) ? 1 : 0;
            int b4 = tratErro.get(i + 4) ? 1 : 0;
            int b5 = tratErro.get(i + 5) ? 1 : 0;
            int b6 = tratErro.get(i + 6) ? 1 : 0;

            // Checa a validade de cada um dos 3 bits de paridade
            boolean checkB4 = checkHammingCodeword(b0, b1, b2, b4);
            boolean checkB5 = checkHammingCodeword(b1, b2, b3, b5);
            boolean checkB6 = checkHammingCodeword(b0, b2, b3, b6);

            // De acordo com os valores de checagem dos bits de paridade, descobre qual dos
            // 7 bits está comprometido, informa no console e corrige a situação
            if (checkB4 && checkB5 && checkB6)
                continue;
            else if (!checkB4 && checkB5 && !checkB6) {
                System.out.println("Verificação falhou no bit " + i);
                tratErro.flip(i);
            } else if (!checkB4 && !checkB5 && checkB6) {
                System.out.println("Verificação falhou no bit " + i);
                tratErro.flip(i + 1);
            } else if (!checkB4 && !checkB5 && !checkB6) {
                System.out.println("Verificação falhou no bit " + i);
                tratErro.flip(i + 2);
            } else if (checkB4 && !checkB5 && !checkB6) {
                System.out.println("Verificação falhou no bit " + i);
                tratErro.flip(i + 3);
            } else if (!checkB4 && checkB5 && checkB6) {
                System.out.println("Verificação falhou no bit " + i);
                tratErro.flip(i + 4);
            } else if (checkB4 && !checkB5 && checkB6) {
                System.out.println("Verificação falhou no bit " + i);
                tratErro.flip(i + 5);
            } else if (checkB4 && checkB5 && !checkB6) {
                System.out.println("Verificação falhou no bit " + i);
                tratErro.flip(i + 6);
            }

        }
        BitSet withoutRedundancy = removeRedundancy(tratErro);
        return withoutRedundancy.toByteArray();
    }

    // Checa a soma de 3 bits com 1 bit de redundância, caso o bit de redundância =
    // 0 a soma deve ser par, caso = 1, deve ser ímpar
    private boolean checkHammingCodeword(int b1, int b2, int b3, int redundancyB) {
        boolean check = redundancyB == (b1 ^ b2 ^ b3) ? true : false;
        return check;
    }

    // Remove os bits de redundância do bitset
    private BitSet removeRedundancy(BitSet withRedundancy) {
        BitSet tratErro = new BitSet();

        int index = 0;
        for (int i = 0; i < 16; i++) {
            if (withRedundancy.get(i))
                tratErro.set(index);
            index++;
        }

        int count = 0;
        for (int i = 24; i < withRedundancy.size(); i++) {
            if (count < 4) {
                if (withRedundancy.get(i))
                    tratErro.set(index);
                count++;
                index++;
            } else {
                i += 2;
                count = 0;
            }
        }
        return tratErro;
    }
}