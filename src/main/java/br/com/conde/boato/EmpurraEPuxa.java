package br.com.conde.boato;

/**
 * EMPURRAR E PUXAR NA MESMA CONVERSA: quem fala conta o que sabe e leva o que o outro sabe.
 *
 * <p>Ela junta as duas metades boas e e a estrategia que todo sistema de verdade usa. A
 * fase de dobrar vem da parte que empurra, e a fase de cair ao quadrado vem da parte que
 * puxa, na mesma execucao, sem ninguem ter que decidir quando trocar.
 *
 * <p>Eu esperava que ela fosse cara: todo no fala em toda rodada, como na que puxa, e cada
 * conversa carrega as duas direcoes. A tabela das mensagens diz outra coisa, porque ela
 * TERMINA MUITO ANTES: numa rede de 65.536 ela gasta 13,51 mensagens por no contra 20,43
 * da que puxa e 12,04 da que empurra, e termina em metade das rodadas desta ultima.
 *
 * <p>Ou seja, ela e quase tao barata quanto a mais barata e quase duas vezes mais rapida
 * que ela. O que sobra de desvantagem nao aparece em coluna nenhuma deste repositorio: cada
 * conversa dela carrega o dobro de BYTES, porque vai e volta com conteudo.
 *
 * <h2>Ela quebra o limite de dobrar, e esta certa</h2>
 *
 * <p>Esta classe e a razao de o limite inferior deste repositorio ter sido reescrito. Ela
 * terminou em 13 rodadas numa rede de 65.536, onde o logaritmo na base dois diz 16, e o
 * errado era o limite: um no que sabe pode ser perguntado por muitos na mesma rodada, entao
 * o numero de quem sabe pode mais que dobrar.
 */
public final class EmpurraEPuxa implements Estrategia {

    @Override
    public String nome() {
        return "empurra e puxa";
    }

    @Override
    public int limiteInferior(int nos) {
        /* Como a que puxa: quem sabe pode ser perguntado por muitos de uma vez. */
        return 1;
    }

    @Override
    public void rodada(boolean[] sabiam, boolean[] sabem, Sorteio sorteio, Contador contador) {
        for (int quem = 0; quem < sabiam.length; quem++) {
            int alvo = sorteio.outroQueNao(quem, sabiam.length);

            boolean euAprendo = !sabiam[quem] && sabiam[alvo];
            boolean eleAprende = sabiam[quem] && !sabiam[alvo] && !sabem[alvo];

            contador.mensagem(euAprendo || eleAprende);

            if (sabiam[quem] || sabiam[alvo]) {
                sabem[quem] = true;
                sabem[alvo] = true;
            }
        }
    }
}
