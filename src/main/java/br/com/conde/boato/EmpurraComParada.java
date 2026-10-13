package br.com.conde.boato;

/**
 * EMPURRAR ATE CANSAR: o no para de contar depois de k conversas seguidas inuteis.
 *
 * <p>Esta e a resposta de engenharia para a cauda da que empurra. O problema de la nao e
 * demorar: e que no fim quase toda conversa e com quem ja sabe, e a rede continua gastando
 * mensagem para sempre se ninguem a mandar parar.
 *
 * <p>A regra e local e nao precisa de ninguem: cada no conta quantas vezes seguidas falou
 * com alguem que ja sabia, e ao chegar em k ele cala. Nao ha coordenacao, nao ha contagem
 * global, nao ha quem decida que acabou.
 *
 * <p>E ela custa alguma coisa, e o que ela custa e o assunto da tabela da parada: com k
 * pequeno a rede cala cedo e ALGUEM PODE FICAR SEM SABER, para sempre. Nao e atraso, e
 * perda. Com k grande ela economiza pouco.
 *
 * <p>Repare que o no nao sabe se ficou alguem de fora. Ninguem sabe. Essa e a natureza de
 * um protocolo que so tem regra local, e e por isso que sistemas de verdade usam isto para
 * espalhar depressa e tem outro mecanismo, mais lento e mais caro, para garantir que no
 * fim todos receberam.
 */
public final class EmpurraComParada implements Estrategia {

    private final int limiteDeInuteis;

    private int[] inuteisSeguidos;
    private boolean[] calou;

    public EmpurraComParada(int limiteDeInuteis) {
        if (limiteDeInuteis < 1) throw new IllegalArgumentException("pelo menos uma tentativa");

        this.limiteDeInuteis = limiteDeInuteis;
    }

    public int limiteDeInuteis() {
        return limiteDeInuteis;
    }

    @Override
    public String nome() {
        return "empurra ate " + limiteDeInuteis;
    }

    @Override
    public void comecar(int nos) {
        inuteisSeguidos = new int[nos];
        calou = new boolean[nos];
    }

    @Override
    public boolean aindaFala(boolean[] sabem) {
        for (int quem = 0; quem < sabem.length; quem++)
            if (sabem[quem] && !calou[quem]) return true;

        return false;
    }

    @Override
    public void rodada(boolean[] sabiam, boolean[] sabem, Sorteio sorteio, Contador contador) {
        for (int quem = 0; quem < sabiam.length; quem++) {
            if (!sabiam[quem] || calou[quem]) continue;

            int alvo = sorteio.outroQueNao(quem, sabiam.length);

            boolean util = !sabiam[alvo] && !sabem[alvo];

            contador.mensagem(util);

            sabem[alvo] = true;

            /* O no so sabe se o OUTRO ja sabia, e e com isso que ele decide. */
            if (sabiam[alvo]) {
                inuteisSeguidos[quem]++;

                if (inuteisSeguidos[quem] >= limiteDeInuteis) calou[quem] = true;
            } else {
                inuteisSeguidos[quem] = 0;
            }
        }
    }
}
