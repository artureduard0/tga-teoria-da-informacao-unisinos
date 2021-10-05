# Trabalho GA

Trabalho do GA de Teoria da Computação.

## Algoritmos de Compressão

### Golomb

O algoritmo Golomb se baseia em um divisor para determinar seu encode/decode.

O codificador Golomb presente nesse projeto segue os seguintes passos, após ler o arquivo e o divisor (potência de 2), a cada byte:
1. O símbolo é transformado em um inteiro (unsigned int);
2. Através desse inteiro, é criada o codeword concatenando:
    2.1. Prefixo: repete 0 conforme o número do resultado da divisão do símbolo atual pelo divisor;
    2.2. Stop bit: sempre será 1;
    2.3. Sufixo: será o resto da divisão convertido em binário de tamanho igual ao log base 2 do divisor.
3. Esse codeword é inserido no final do vetor de bits e gravado no arquivo .cod.

O decodificador Golomb lê o arquivo e faz o caminho inverso, lendo todos os bits a partir do cabeçalho:
1. Encontra o tamanho do prefixo iterando os bits até o primeiro 1;
2. Encontra o stopbit e procura o sufixo através do tamanho do mesmo. O tamanho será igual ao resto da divisão convertido em binário de tamanho igual ao log base 2 do divisor;
3. Através do tamanho do prefixo encontrado anteriormente, obtém o tamanho do prefixo e soma ao sufixo que de binário é convertido para decimal;
4. Com o inteiro novamente, tranforma-se no símbolo e adiciona ao vetor de palavras decodificadadas;
5. Ao final, grava no arquivo .dec.

### Unária

Este algortimo representa um valor como o tamanho de uma sequência de 0's e representa o fim/início de uma sequência com o stop-bit 1.  
Ex.:  Representação dos valores 04, 09 e 01: 0000100000000010.

### Delta

O algoritmo delta utiliza-se de um valor de variação (delta) para representar a diferença de um símbolo para outro, esta variação pode ser negativa e positiva, para isto é concatenado ao valor do delta um bit que informa se o sinal é positivo ou negativo.  
Passo a passo do codificador Delta: 
1. Maior delta - O algoritmo percorre o arquivo à ser codificado e descobre o maior valor de variação dentre os símbolos do arquivo, à partir deste valor, descobre o número necessário para representar este maior valor em bits. Ex.: Para representar o delta '5' são necessários 3 bits. Este valor é salvo em um cabeçalho do algoritmo, no primeiro byte.
2. Primeiro símbolo - O primeiro símbolo é representado inteiramente no segundo byte.
3. Stop-bit - 
    1. Símbolo repetido - Quando o próximo símbolo é repetido, apenas adiciona um 0 à sequência.
    2. Símbolo diferente - Adiciona o valor 1 para indicar mudança.
4. Delta - Caso o símbolo seja diferente adiciona à sequência, no tamanho verificado no valor 1 à variação entre o símbolo atual e o próximo, precedida de um bit que indica se o sinal é negativo ou não. Retorna ao passo 3 ou adiciona um bit positivo caso seja o fim do arquivo.

Passo a passo do decodificador Delta:  
1. Verifica o primeiro byte que contém o tamanho fixo do delta utilizado na compressão do arquivo.
2. Verifica e adiciona o primeiro símbolo no output.
3. Verifica o stop-bit.
   1. 0 - Adiciona o último símbolo no output.
   2. 1 - Verifica o sinal do delta que será recebido.
4. Verifica o delta recebido e seu respectivo sinal e adiciona o próximo símbolo no output. Retorna ao passo 3 ou finaliza caso encontre o último bit positivo do arquivo.

## Tratamento de erros

### CRC 8

Os primeiros dois bytes do arquivo codificado representam o cabeçalho do arquivo. O primeiro byte é o algoritmo escolhido e o segundo é o divisor do Golomb. Através dessas informações, cria-se um novo byte que será resultado do algoritmo CRC 8. 

Após codificar o arquivo e gravar o .cod, é feito o cálculo do CRC 8 através da adição dos bits dos primeiros dois bytes mais um byte de 0 dividos pelo polinômio gerador de 8. A cada resto da divisão é feito o XOR com o polinômio e o resto final é gravado no 3 byte do arquivo .ecc.

Ao decodificar um arquivo, será procurado o .ecc para validar o tratamento de erros. O CRC 8 será calculado novamente através dos primeiros dois bytes e será feita a validação do 3 byte para ver se é igual ao disponível no arquivo. Se não for, encerra a execução do programa, pois houve um problema no cabeçalho.

### Hamming
À partir do 3 byte do arquivo compactado (após a aplicação do CRC no cabeçalho) é adicionada a redundância com palavras Hamming para tratamento e recuperação de erros,
à cada 4 bits de conteúdo são adicionados 3 bits de redundância para o tratamento de erros, seguindo o algoritmo Hamming. Esta etapa é sempre realizada após a codificação com
algum dos algoritmos disponíveis, na etapa de decodificação, o algoritmo verifica os erros, corrige caso necessário, informa no console a posição onde o erro foi detectado e 
então remove os 3 bits de cada palavra para enviar ao decoder específico de cada algoritmo de compressão.