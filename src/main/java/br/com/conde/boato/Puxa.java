package br.com.conde.boato;

/**
 * PUXAR: todo mundo fala, e pergunta ao escolhido se ha novidade.
 *
 * <p>Ela e o espelho exato da que empurra, e eu esperava que isso a fizesse ser ruim no
 * comeco e boa no fim. So a segunda metade se confirmou.
 *
 * <p>No comeco ela anda IGUAL. Com i nos sabendo, cada um dos que nao sabem tem chance i
 * sobre n de acertar um deles, entao aprendem uns i por rodada: dobra, exatamente como
 * quem empurra. A tabela da cauda mostra os dois chegando a metade da rede na mesma
 * rodada, 16,37 contra 16,00 numa rede de 65.536.
 *
 * <p>No fim ela e devastadora. Com metade sabendo, metade dos que nao sabem acerta alguem
 * que sabe: quem falta cai pela METADE por rodada. Com noventa por cento sabendo, noventa
 * por cento dos que faltam aprendem de uma vez. O numero de ignorantes nao cai pela metade,
 * cai ao QUADRADO: de um por cento para um decimo de por cento numa rodada so.
 *
 * <p>Repare no preco que nao aparece na coluna de rodadas: aqui TODO no fala em toda
 * rodada, inclusive os que ja sabem e os que vao continuar sem saber. Quem empurra so gasta
 * mensagem de quem sabe, e por isso gasta menos mensagem mesmo levando mais rodadas.
 *
 * <p>E ela nao obedece ao limite de dobrar. Um no que sabe pode ser perguntado por muitos
 * na mesma rodada, e todos aprendem juntos: o argumento que limita quem empurra nao se
 * aplica aqui.
 */
public final class Puxa implements Estrategia {

    @Override
    public String nome() {
        return "puxa";
    }

    @Override
    public int limiteInferior(int nos) {
        /* Em teoria a rede inteira pode aprender numa rodada so: nao ha limite util. */
        return 1;
    }

    @Override
    public void rodada(boolean[] sabiam, boolean[] sabem, Sorteio sorteio, Contador contador) {
        for (int quem = 0; quem < sabiam.length; quem++) {
            int alvo = sorteio.outroQueNao(quem, sabiam.length);

            boolean aprendeu = !sabiam[quem] && sabiam[alvo];

            contador.mensagem(aprendeu);

            if (aprendeu) sabem[quem] = true;
        }
    }
}
