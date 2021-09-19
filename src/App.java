import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.BitSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class App {
    public static void main(String[] args) {
        try {
            boolean executar = true;

            while (executar) {
                // 0 = codificar e 1 = decodificar
                Object[] opcoes = { "Codificar arquivo", "Decodificar arquivo" };
                int opcao = JOptionPane.showOptionDialog(null, "Escolha o que executar: ",
                        "Trabalho GA - Encoder/Decoder", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE,
                        null, opcoes, opcoes[0]);

                if (opcao != -1) {
                    JFileChooser chooser = new JFileChooser();

                    File path = new File("./arquivos");
                    chooser.setCurrentDirectory(path);
                    chooser.setMultiSelectionEnabled(false);

                    FileNameExtensionFilter filtro;
                    if (opcao == 0) {
                        filtro = new FileNameExtensionFilter("*.txt", "txt");
                    } else {
                        filtro = new FileNameExtensionFilter("*.cod", "cod");
                    }

                    chooser.setFileFilter(filtro);
                    chooser.addChoosableFileFilter(filtro);

                    int retornoChooser = chooser.showOpenDialog(null);

                    // sair se o arquivo for inválido
                    if (retornoChooser != JFileChooser.APPROVE_OPTION) {
                        JOptionPane.showMessageDialog(null, "Saindo...", "Arquivo inválido!",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    File arquivo = chooser.getSelectedFile();
                    Path caminho = arquivo.toPath();
                    String nomeArquivo = caminho.toString().replaceFirst("[.][^.]+$", "");

                    byte[] data = read(caminho);

                    int algoritmo = 0;
                    int divisor = 0;

                    if (opcao == 0) {
                        Object[] algoritmos = { "Golumb", "Elias-Gamma", "Fibonacci", "Unaria", "Delta" };
                        algoritmo = JOptionPane.showOptionDialog(null, "Escolha o algoritmo: ",
                                "Trabalho GA - Encoder/Decoder", JOptionPane.DEFAULT_OPTION,
                                JOptionPane.INFORMATION_MESSAGE, null, algoritmos, algoritmos[0]);

                        if (algoritmo == 0) {
                            String divisorString = JOptionPane.showInputDialog(null, "Informe o divisor: ",
                                    "Trabalho GA - Encoder/Decoder", JOptionPane.QUESTION_MESSAGE);

                            divisor = Integer.parseInt(divisorString == "" ? "0" : divisorString);

                            // sair se for menor que zero ou não for par
                            if (divisor < 0 || divisor % 2 != 0) {
                                JOptionPane.showMessageDialog(null, "Saindo...", "Divisor inválido!",
                                        JOptionPane.ERROR_MESSAGE);
                                return;
                            }
                        }

                        IEncoder encoder = getEncoder(algoritmo, divisor);

                        byte algoritmoByte = (byte) algoritmo;
                        byte divisorByte = (byte) divisor;

                        BitSet encodedBits = encoder.encode(data);
                        writeBits(nomeArquivo + ".cod", encodedBits, algoritmoByte, divisorByte);
                        JOptionPane.showMessageDialog(null, "Arquivo codificado!", "Sucesso!",
                                JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        algoritmo = (int) data[0];
                        divisor = (int) data[1];

                        byte[] codewords = new byte[data.length - 2];
                        System.arraycopy(data, 2, codewords, 0, data.length - 2);

                        IEncoder encoder = getEncoder(algoritmo, divisor);
                        ArrayList<String> simbolosDecodificados = encoder.decode(codewords);
                        writeTxt(nomeArquivo + ".dec", simbolosDecodificados);
                        JOptionPane.showMessageDialog(null, "Arquivo decodificado!", "Sucesso!",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    executar = false;
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ah não, uma exceção aconteceu! Saindo...", "Erro fatal!",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public static IEncoder getEncoder(int algoritmo, int divisor) {
        IEncoder encoder = null;

        switch (algoritmo) {
            case 0:
                encoder = new Golomb(divisor);
                break;
            case 1:
                encoder = new EliasGamma();
                break;
            default:
                break;
        }

        return encoder;
    }

    public static void writeBits(String filename, BitSet bits, byte algoritmo, byte divisor) throws IOException {
        File outFile = new File(filename);
        FileOutputStream fos = new FileOutputStream(outFile);

        // criar array de bytes e adicionar algoritmo e divisor no inicio
        byte[] codewords = bits.toByteArray();
        byte[] bytes = new byte[codewords.length + 2];
        bytes[0] = algoritmo;
        bytes[1] = divisor;
        System.arraycopy(codewords, 0, bytes, 2, codewords.length);

        // BitSet teste = BitSet.valueOf(bytes);
        // for (int i = 0; i < teste.length(); i++) {
        // System.out.print(teste.get(i) == true ? "1" : "0");
        // }
        // System.out.println();

        fos.write(bytes);
        fos.close();
    }

    public static byte[] read(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    public static void writeTxt(String filename, ArrayList<String> simbolos) throws IOException {
        FileWriter writer = new FileWriter(filename);

        for (int i = 0; i < simbolos.size(); i++) {
            writer.write(simbolos.get(i));
        }

        writer.close();
    }
}
