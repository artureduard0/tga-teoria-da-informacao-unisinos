# Trabalho GA

Trabalho do GA de Teoria da Computação.

Para rodar o projeto, basta executar o arquivo App.java. Ao executar, uma janela será aberta perguntando qual será o tipo de função a ser empregada: encode ou decode. Ao selecioner encode, deve-se selecionar o arquivo (o filtro por padrão é para .txt, mas é possível mudar) e o algoritmo. Se selecionado o Golomb, deverá ser escolhido o divisor. Ao selecioner decode, será necessário selecionar o arquivo .cod. 

O fluxo da aplicação para codificação é o seguinte: 
1. Selecionar "Codificar arquivo"
2. Selecionar o arquivo 
3. Selecionar o algorimo 
4. Se Golomb, selecionar o divisor 
5. Encode dos bits e adição do cabeçalho (byte do algoritmo e byte do divisor)
6. Gravado o .cod
7. Calculado o CRC 8 do cabeçalho e Hamming dos bits do conteúdo 
8. Gravado o .ecc.

O fluxo da aplicação para decodificação é o seguinte:
1. Selecionar "Decodificar arquivo"
2. Selecionar o arquivo .cod
3. Verificação do .ecc
    1. Se houver divergência nos bits calculados pelo CRC 8, sai da aplicação;
    2. Se houver divergência de um bit de pariedade no Hamming, é feita a correção.
5. Conteúdo é decodificado para o .dec.

## Algoritmos de Compressão

### Golomb

O algoritmo Golomb se baseia em um divisor para determinar seu encode/decode.

O codificador Golomb presente nesse projeto segue os seguintes passos, após ler o arquivo e o divisor (potência de 2), a cada byte:
1. O símbolo é transformado em um inteiro (unsigned int);
2. Através desse inteiro, é criada o codeword concatenando:
    1. Prefixo: repete 0 conforme o número do resultado da divisão do símbolo atual pelo divisor;
    2. Stop bit: sempre será 1;
    3. Sufixo: será o resto da divisão convertido em binário de tamanho igual ao log base 2 do divisor.
3. Esse codeword é inserido no final do vetor de bits e gravado no arquivo .cod.

O decodificador Golomb lê o arquivo e faz o caminho inverso, lendo todos os bits a partir do cabeçalho:
1. Encontra o tamanho do prefixo iterando os bits até o primeiro 1;
2. Encontra o stopbit e procura o sufixo através do tamanho do mesmo. O tamanho será igual ao resto da divisão convertido em binário de tamanho igual ao log base 2 do divisor;
3. Através do tamanho do prefixo encontrado anteriormente, obtém o tamanho do prefixo e soma ao sufixo que de binário é convertido para decimal;
4. Com o inteiro novamente, tranforma-se no símbolo e adiciona ao vetor de palavras decodificadadas;
5. Ao final, grava no arquivo .dec.

### Elias-Gamma

Diferente de Golomb, o algoritmo Elias-Gamma não utiliza um divisor para determinar sua codificação e decodificação. Ele se baseia em potências de base 2 para encontrar um expoente N, que será o comprimento de seu prefixo, constituído por bits com valor 0, e também do seu sufixo, constituido do resto da divisão do valor a ser codificado por 2<sup>N</sup>. Entre prefixo e sufixo, fica o stop bit. Os processos de codificação e decodificação ocorrem conforme descrito abaixo:

###### Codificador
1. Cada byte é lido como um inteiro (denominado X);
2. Partindo de 2<sup>0</sup>, incrementando o expoente em 1, procura o expoente N cujo resultado da potência 2<sup>N</sup> seja o mais próximo ou exatamente o valor de X;
3. Tendo o valor de N, adiciona N bits 0 de forma unária como prefixo do símbolo codificado;
4. O valor do sufixo é o resto da divisão de X por 2<sup>N</sup>, em binário;
5. O stop bit é adicionado após o prefixo.
6. É feito um left shift com o expoente N sobre o stop bit, para que sejam adicionados N zeros no sufixo, tornando-o do mesmo tamanho que o prefixo. 
7. É feito um or exclusivo para que o resto da divisão, em binário, substitua os 0s necessários no sufixo.
8. Adiciona o resultado à lista de símbolos codificados. Depois de codificar todos os símbolos recebidos, grava em um arquivo .cod.

###### Decodificador:
1. Percorre os bits até encontrar o stop bit. Adiciona-os à uma string para fazer a contagem de caracteres.
2. Após encontrar o stopbit, passa a adicionar os bits em uma outra string, a de sufixo.
3. Quando o comprimento do sufixo atingir o valor do comprimento do prefixo, calcula a potência 2<sup>N</sup>, sendo N o comprimento do prefixo.
4. Transforma o sufixo de bits para inteiro, configurando o resto da divisão do valor decodificado pelo resultado do passo anterior. 
5. Soma esses dois valores e tem o valor original, adicionando-o à lista de caracteres decodificados.
6. Após decodificar todos os caracteres, salva o resultado em um arquivo .dec.

### Fibonacci

O algoritmo de fibonacci utiliza o resultado de fibonacci mais próximo do valor a ser codificado para efetuar a codificação e decodificação. Sem considerar fibonacci(0) e fibonacci(1), pois têm resultados descartáveis para o algoritmo, parte de fibonacci(2) até fibonacci(N), sendo N o valor que faça o resultado do cálculo de fibonacci ser o mais próximo ou igual ao valor a ser codificado. Esse algoritmo tem o stop bit no final da palavra, formando uma dupla de bits 1, pois o valor de fibonacci(N) sempre vai ser considerado válido na codificação. O símbolo codificado é gerado fazendo a sequência fibonacci de forma decrescente, partindo de fibonacci(N) até fibonacci(2). Para cada valor dessa sequência, o resultado do fibonacci é adicionado à uma variável de soma. Caso a soma seja menor ou igual ao valor a ser codificado, o índice atual é adicionado à lista de índices válidos. Caso fosse ultrapassar, o índice nao é ultrapassado à lista. Após essas iterações, são gravados bits com valor de 0 para os índices inválidos e com valor 1 para os válidos. Abaixo os passos de codificação e decodificação:

###### Codificador:
1. Cada byte é lido como um inteiro (denominado X);
2. Encontra o índice máximo, denominado N, da sequência Fibonacci com resultado menor ou igual a X;
3. Parte de N, de forma decrescente, até 2, efetuando a soma descrita acima e guardando os índices válidos em uma lista.
4. Partindo de 2 até o N, grava 0 ou 1 no símbolo codificado de acordo com o índice ser válido ou não para a soma do algoritmo.
5. Adiciona o stop bit ao final do processo.
6. Os passos se repetem para cada byte recebido, e o resultado é gravado em um arquivo .cod.

###### Decodificador:
1. Adiciona os bits em uma estrutura auxiliar até que encontre uma sequência de bits com valor 1. 
2. Ao encontrar o stop bit, itera sobre os bits do símbolo codificado e calcula o valor de fibonacci(i + 2) (aumentando em 2 o índice por não serem considerados 0 e 1) para os índices onde tem bits com valor 1.
3. Decodifica essa soma e adiciona à uma lista de caracteres decodificados. Repete até o final dos dados.
4. Grava a lista em um arquivo .dec.


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
