# boato

Espalhamento de boato do zero em Java 21: **empurrar e puxar chegam juntos à metade da rede, e a
diferença inteira está no último um por cento.**

O juiz é a **difusão combinada**: o nó `i` conta para o nó `i + 2^r` na rodada `r`. Ela é o juiz
certo e não só o conveniente porque ela atinge o limite inferior e prova que o limite não é
frouxo: dobra exatamente a cada rodada, sem desperdiçar uma conversa, e termina no teto do
logaritmo na base dois.

E ela não é uma estratégia de verdade, o que é justamente o ponto. O nó precisa saber o próprio
número, o número da rodada e o tamanho da rede, e precisa que ninguém entre, saia ou caia. É uma
coreografia, e a distância até ela é o preço de não ter coordenação nenhuma.

O juiz exige **validade, não velocidade**: que todo mundo fique sabendo e que ninguém termine
antes do limite que vale para a sua classe. A estratégia que gasta o dobro de mensagens passa; a
que deixa 17% da rede sem saber aparece numa tabela em vez de ser reprovada.

A unidade é a **rodada** e a **mensagem**, nunca o segundo. Numa rede de verdade uma rodada é uma
ida e volta, e o que custa é a espera.

## O modelo, numa frase

A cada rodada, cada nó escolhe **um outro nó qualquer da rede, com igual chance**, e fala com ele.
Não há topologia, não há vizinhos fixos, não há quem conhece quem.

O que muda entre as estratégias é quem fala e o que se passa na conversa: **empurrar** (só quem
sabe fala, e conta), **puxar** (todo mundo fala, e pergunta) e **empurrar e puxar** (as duas
coisas na mesma conversa).

## Sem coordenação nenhuma, e mais rápido que a coreografia

```
$ mvn exec:java -Dexec.args="rodadas"

  estrategia                    nos    limite inferior        media         pior   vezes o limite
  difusao combinada           65536                 16        16.00           16            1.000
  empurra                     65536                 16        28.15           31            1.759
  puxa                        65536                 16        20.43           27            1.277
  empurra e puxa              65536                 16        13.51           15            0.844
```

A difusão combinada é a coreografia perfeita: 16 rodadas, sem variação nenhuma, sem uma mensagem
desperdiçada. Empurrar e puxar não coordena nada, joga fora 92% das mensagens, e termina em
**13,51**.

Isso não é erro de medida, e descobri que não era quando o meu próprio juiz reprovou a melhor das
três. A seção seguinte é sobre isso.

## Como rodar

```
mvn test
mvn exec:java -Dexec.args="tudo"
```

Os comandos do medidor são `correcao`, `rodadas`, `cauda`, `mensagens` e `parada`.

## O que as medidas me corrigiram

**Eu escrevi um limite inferior e apliquei a todas. Ele só vale para quem empurra, e quem puxa o
quebra.**

O argumento é de uma linha e parece universal: cada nó manda uma mensagem por rodada, cada
mensagem ensina no máximo um nó, então o número de quem sabe no máximo **dobra**. Com 65.536 nós,
nenhuma estratégia termina antes de 16 rodadas.

A primeira execução da tabela da correção disse isto:

```
  empurra e puxa              65536                 16               13        NAO terminou em 13, abaixo do limite de 16
```

O errado era o limite. O argumento supõe que **quem fala é quem ensina**, e isso vale para
empurrar. Quando alguém **pergunta**, um nó que sabe pode ser perguntado por muitos na mesma
rodada e todos aprendem de uma vez: o conjunto de quem sabe pode mais que dobrar, e em teoria a
rede inteira pode aprender numa rodada só.

Então o limite de dobrar é um teorema **sobre um modelo**, não sobre espalhamento. Hoje cada
estratégia declara o limite que vale para ela, e há um teste que afirma que empurrar nunca o
quebra e outro que afirma que empurrar e puxar quebra.

E uma consequência que eu não teria escrito antes de medir: **a melhor estratégia sem coordenação
nenhuma é mais rápida que a coreografia perfeita.** Não porque ela seja esperta, mas porque a
coreografia tem uma mão amarrada: na difusão, um nó informa um nó por rodada, e não há como
alguém ser perguntado.

### Puxar não é lento no começo. É idêntico no começo.

```
$ mvn exec:java -Dexec.args="cauda"

  estrategia                    nos    ate 50%    ate 90%    ate 99%   ate 100% o ultimo por cento
  empurra                     65536      16.00      19.00      21.00      28.15              0.340
  puxa                        65536      16.37      18.10      19.10      20.43              0.070
  empurra e puxa              65536      10.37      11.58      12.37      13.51              0.092
```

Eu tinha escrito que puxar quase não anda enquanto poucos sabem: com um nó sabendo numa rede de
65.536, cada pergunta tem uma chance em 65.536 de cair nele.

A conta está certa e a conclusão não. São **65.535 perguntas por rodada**, e com `i` nós sabendo,
os que não sabem acertam uns `i` deles: dobra, exatamente como empurrar. Os dois chegam à metade
da rede na mesma rodada, **16,00 contra 16,37**.

A diferença inteira está no fim, e ela é enorme. A última coluna é quantas rodadas a mais custou
fechar o último um por cento, em fração do que custou chegar aos 99%:

- **Empurrar paga 34%.** Com `u` nós sem saber e quase todos sabendo, cada um deles escapa de
  todas as 65 mil mensagens com chance `(1 - 1/n)^n`, que é um sobre `e`. O número de ignorantes
  cai por um fator `e` por rodada: são `ln n` rodadas de rabo.
- **Puxar paga 7%.** Cada um dos `u` que não sabem pergunta e acerta alguém que sabe com chance
  quase 1. O número de ignorantes não cai por um fator, cai **ao quadrado**: de 1% para 0,01% numa
  rodada.

E o rabo de empurrar **piora com a rede**: 19% com mil nós, 32% com dezesseis mil, 34% com
sessenta e cinco mil. O de puxar fica em 7% nos três.

### E empurrar e puxar não é cara

```
$ mvn exec:java -Dexec.args="mensagens"

  estrategia                    nos        mensagens         por no    fracao inutil      rodadas
  empurra                     65536           789339          12.04           0.9163        28.15
  puxa                        65536          1338573          20.43           0.9508        20.43
  empurra e puxa              65536           885064          13.51           0.9171        13.51
```

Eu esperava que ela custasse o dobro: todo nó fala em toda rodada, como na que puxa, e a conversa
vai nos dois sentidos.

Ela gasta **13,51 mensagens por nó** contra 12,04 de empurrar, doze por cento acima da mais
barata, e termina em **metade das rodadas** dela. O motivo é simples depois de visto: quem puxa
manda exatamente uma mensagem por nó por rodada, então o custo total é o número de rodadas, e ela
tem poucas.

O que sobra de desvantagem não aparece em coluna nenhuma daqui: cada conversa dela carrega o dobro
de **bytes**, porque vai e volta com conteúdo. Se o que aperta for a banda e não a espera, a conta
muda.

E as três linhas dizem a mesma coisa sobre a natureza do espalhamento aleatório: **mais de nove em
cada dez mensagens não ensinam nada a ninguém.** Puxar é a mais desperdiçadora, 95%, porque até
quem já sabe continua perguntando.

## A regra de parada, e quem fica de fora

```
$ mvn exec:java -Dexec.args="parada"

         k      rodadas     mensagens/no   fracao de fora execucoes com alguem de fora           economia
         1        24.53             1.74         0.174997                 1.0000             0.8398
         2        25.40             3.30         0.036846                 1.0000             0.6968
         3        26.79             4.54         0.010746                 1.0000             0.5834
         4        28.15             5.64         0.003587                 1.0000             0.4821
         6        30.54             7.70         0.000477                 1.0000             0.2924
        10        26.46            10.53         0.000011                 0.1750             0.0329
```

O problema da cauda de empurrar não é demorar: é que no fim quase toda conversa é com quem já
sabe, e a rede continua gastando mensagem para sempre se ninguém a mandar parar. A regra é local e
não precisa de ninguém: cada nó conta quantas vezes seguidas falou com alguém que já sabia, e ao
chegar em `k` ele cala.

Com `k = 1` ela corta **84% das mensagens** e deixa **17,5% da rede sem saber**. Não é atraso, é
perda, e ninguém fica sabendo que ficou de fora.

Os dois números andam juntos e nenhum `k` zera os dois. Com `k = 6` a economia cai para 29% e a
perda para 0,05%. E a última linha é a que importa mais: com `k = 10`, economizando só 3%, **em
17,5% das execuções ainda sobra alguém.**

É a natureza de um protocolo que só tem regra local: nenhum nó sabe que acabou, então nenhum nó
pode garantir que acabou. É por isso que sistemas de verdade usam isto para espalhar depressa e
têm outro mecanismo, mais lento e mais caro, para fechar a conta no fim.

## O que não tem aqui

**Topologia.** Aqui todo nó pode falar com todo nó, com igual chance. Numa rede de verdade cada nó
tem uma lista parcial, e a qualidade dessa lista é o que decide se os números daqui valem. Uma
lista mal escolhida cria ilhas, e nenhuma estratégia desta tabela sai de uma ilha.

**Falhas.** Ninguém cai, ninguém entra, nenhuma mensagem se perde. Perda de mensagem é quase
inofensiva aqui, porque a redundância é tão grande que a próxima rodada conserta; queda de nó no
meio é outra história e não está medida.

**Mensagem grande.** Toda conversa carrega o boato inteiro. Quando o boato é grande, a conversa
passa a ser "você tem novidade?" seguida da transferência, e a conta de bytes deixa de ser a conta
de mensagens.

**Convergência de estado.** Aqui o boato é um bit: ou se sabe ou não se sabe. Espalhar um estado
que muda, em que dois nós podem ter versões diferentes, é outro assunto, e está em
[`crdt`](https://github.com/condeDeveloper/crdt).

## Licença

MIT.
