import java.util.ArrayList;
import java.util.BitSet;

public class Fibonacci implements IEncoder {

    @Override
    public BitSet encode(byte[] data) {
        BitSet bitsCodificados = new BitSet();

        int indexBit = 0; // index dos bits no BitSet

        for (byte b : data) {
            int simbolo = b & 0xff;
            // Encontra o índice máximo da sequência Fibonacci, com resultado menor ou igual
            // ao símbolo
            int indiceMaximoFibonacci = encontrarIndiceFibonacci(simbolo);

            ArrayList<Integer> indicesValidos = new ArrayList<>();
            int soma = 0;

            // Parte do maior índice até 2, decrescendo, somando os valores até fechar
            // exatamente o valor símbolo.
            // Armazena os índices numa lista auxiliar para depois armazenar as informações
            // no BitSet de saída.
            for (int i = indiceMaximoFibonacci; i >= 2; i--) {
                if (soma == simbolo)
                    break;

                int fibonacciIndice = fibonacci(i);

                int somaTemporaria = soma + fibonacciIndice;

                if (somaTemporaria <= simbolo) {
                    soma += fibonacciIndice;
                    indicesValidos.add(i);
                }
            }

            // Partindo de 2 até o índice máximo, já que fibonacci([0, 1]) não contam para o algoritmo, grava
            // 0 ou 1 conforme o índice caso ele seja considerado na soma.
            for (int i = 2; i <= indiceMaximoFibonacci; i++) {
                bitsCodificados.set(indexBit++, indicesValidos.contains(i));
            }

            // adiciona o stop bit ao final da palavra
            bitsCodificados.set(indexBit++, true);
        }
        return bitsCodificados;

    }

    @Override
    public ArrayList<String> decode(byte[] data) {
        ArrayList<String> simbolosDecodificados = new ArrayList<>();
        BitSet bitsRecebidos = BitSet.valueOf(data);
        boolean stopBitEncontrado = false;
        BitSet bitsPalavra = new BitSet();
        int indexBitsPalavra = 0;

        for (int i = 0; i < bitsRecebidos.size() - 1; i++) {
            // Procura um par de 1, o que indica que o segundo 1 é o stop bit.
            if (!stopBitEncontrado) {
                boolean bitAtual = bitsRecebidos.get(i);
                boolean bitSeguinte = bitsRecebidos.get(i + 1);

                if (bitAtual && bitSeguinte) {
                    stopBitEncontrado = true;
                }

                // Adiciona os bits da palavra em um BitSet auxiliar que é utilizado na hora de
                // somar os valores fibonacci
                bitsPalavra.set(indexBitsPalavra++, bitAtual);
            } else {
                // quando encontra o stop bit, decodifica os bits recebidos antes dele e
                // transforma em um caractere.
                int simboloEncontrado = decodificarFibonacci(bitsPalavra);
                String simbolo = Character.toString((char) simboloEncontrado);
                simbolosDecodificados.add(simbolo);

                stopBitEncontrado = false;
                bitsPalavra = new BitSet();
                indexBitsPalavra = 0;
            }

        }

        return simbolosDecodificados;
    }

    private int decodificarFibonacci(BitSet sequencia) {
        int soma = 0;

        // Adiciona o valor à soma caso o bit do índice seja 1
        for (int i = 0; i < sequencia.size(); i++) {
            if (sequencia.get(i)) {
                soma += fibonacci(i + 2);
            }
        }

        return soma;
    }

    // Encontra o maior índice de Fibonacci onde o resultado é menor ou igual ao
    // símbolo a ser codificado
    private int encontrarIndiceFibonacci(int simbolo) {
        int indice = 0;

        while (true) {
            int fibonacciAtual = fibonacci(indice);
            int fibonacciSeguinte = fibonacci(indice + 1);

            if (fibonacciAtual <= simbolo && fibonacciSeguinte > simbolo) {
                return indice;
            }

            indice++;
        }
    }

    private int fibonacci(int n) {
        return n <= 1 ? n : fibonacci(n - 1) + fibonacci(n - 2);
    }
}
