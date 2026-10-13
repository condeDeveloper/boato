package br.com.conde.boato;

/**
 * O SORTEIO DA CASA: um gerador linear congruente, escrito aqui de proposito.
 *
 * <p>Ele existe para que a mesma semente de o mesmo espalhamento em qualquer maquina e em
 * qualquer versao de Java. A biblioteca padrao nao promete isso entre versoes, e um
 * repositorio cujos numeros estao escritos no README nao pode depender disso.
 *
 * <p>Sao as constantes de Knuth, as mesmas do gerador de 64 bits que todo mundo usa.
 *
 * <h2>Por que o deslocamento de 32</h2>
 *
 * <p>Num gerador linear congruente os bits de baixo tem periodo curto de um jeito exato: os
 * k ultimos bits repetem com periodo 2 elevado a k.
 *
 * <p>E aqui isso destruiria o repositorio inteiro, porque a unica coisa que acontece neste
 * codigo e SORTEAR COM QUEM FALAR, e as redes medidas tem tamanho potencia de dois. Com os
 * bits de baixo, sortear entre 1.024 vizinhos devolve os 1.024 na mesma ordem, sempre: o
 * no zero falaria com o mesmo no em toda rodada, de toda execucao.
 *
 * <p>O espalhamento deixaria de ser espalhamento aleatorio e viraria um ciclo fixo. Ha um
 * teste so sobre isso, e ele conta quantos vizinhos distintos um no alcanca.
 */
public final class Sorteio {

    private long estado;

    public Sorteio(long semente) {
        this.estado = semente;
    }

    /** Um inteiro de 32 bits, tirado da metade de cima do estado. */
    public int proximo() {
        estado = estado * 6364136223846793005L + 1442695040888963407L;

        return (int) (estado >>> 32);
    }

    /** Um inteiro de 0 inclusive ate o limite exclusive. */
    public int ate(int limite) {
        if (limite < 1) throw new IllegalArgumentException("limite precisa ser positivo");

        return Math.floorMod(proximo(), limite);
    }

    /** Um no qualquer que nao seja este. */
    public int outroQueNao(int quem, int quantos) {
        if (quantos < 2) throw new IllegalArgumentException("nao ha outro no numa rede de um");

        int escolhido = ate(quantos - 1);

        return escolhido < quem ? escolhido : escolhido + 1;
    }
}
