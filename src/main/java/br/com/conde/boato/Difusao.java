package br.com.conde.boato;

/**
 * A DIFUSAO COMBINADA: o no i conta para o no i mais dois elevado a rodada menos um.
 *
 * <p>Ela e o JUIZ deste repositorio, e e o juiz certo e nao so o conveniente porque ela
 * atinge o limite inferior e prova que o limite e atingivel.
 *
 * <p>O limite e de uma linha: se cada no fala com um no por rodada, o numero de quem sabe
 * no maximo DOBRA por rodada. Comecando em um, depois de r rodadas no maximo dois elevado
 * a r sabem, entao nenhuma estrategia termina antes de logaritmo de n na base dois.
 *
 * <p>Essa conta sozinha seria so um limite, e limite pode ser frouxo. Esta classe mostra
 * que ele nao e: ela dobra EXATAMENTE a cada rodada, sem desperdicar uma conversa, e
 * termina no teto do logaritmo. Os dois lados se encontram.
 *
 * <h2>Por que ela nao e uma estrategia de verdade</h2>
 *
 * <p>Porque o no i precisa saber o numero dele, o numero da rodada e o tamanho da rede, e
 * precisa que ninguem entre, saia ou caia. Ela e uma coreografia, nao um espalhamento.
 *
 * <p>Um boato de verdade acontece onde nada disso vale: nos entram e saem, ninguem conhece
 * a lista toda, e nao ha rodada global. E e exatamente por isso que ela serve de regua: ela
 * e o que seria possivel se tudo fosse perfeito, e a distancia ate ela e o preco de nada
 * ser.
 */
public final class Difusao implements Estrategia {

    private int rodada;

    @Override
    public String nome() {
        return "difusao combinada";
    }

    @Override
    public int limiteInferior(int nos) {
        return doDobrar(nos);
    }

    @Override
    public void comecar(int nos) {
        rodada = 0;
    }

    @Override
    public void rodada(boolean[] sabiam, boolean[] sabem, Sorteio sorteio, Contador contador) {
        int salto = 1 << rodada;

        rodada++;

        for (int quem = 0; quem < sabiam.length; quem++) {
            if (!sabiam[quem]) continue;

            int alvo = quem + salto;

            /* O salto pode passar do fim da rede: ali nao ha com quem falar, e nao ha
             * conversa nenhuma para contar. */
            if (alvo >= sabiam.length) continue;

            contador.mensagem(!sabiam[alvo] && !sabem[alvo]);

            sabem[alvo] = true;
        }
    }

    /**
     * O menor numero de rodadas em que uma estrategia que SO EMPURRA pode terminar.
     *
     * <p>Vale para esta difusao e para a que empurra, onde cada no manda uma mensagem por
     * rodada e cada mensagem ensina no maximo um no. Nao vale para as que puxam, e nao
     * valer foi o conserto principal deste repositorio.
     */
    public static int doDobrar(int nos) {
        int rodadas = 0;
        long alcance = 1;

        while (alcance < nos) {
            alcance *= 2;
            rodadas++;
        }

        return rodadas;
    }
}
