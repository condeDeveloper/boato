package br.com.conde.boato;

/**
 * EMPURRAR: so quem sabe fala, e conta para quem escolheu.
 *
 * <p>E a estrategia que qualquer um inventa, e no comeco ela e imbativel: o numero de quem
 * sabe DOBRA por rodada enquanto quase todo mundo ainda nao sabe, porque quase toda escolha
 * cai em alguem novo. Dobrar por rodada e o melhor que existe, e o limite inferior do
 * repositorio e exatamente isso.
 *
 * <p>No fim ela desmonta. Quando noventa e nove por cento ja sabem, noventa e nove por
 * cento das conversas sao com gente que ja sabia. Achar o ultimo que falta vira o problema
 * do colecionador de figurinhas: cada no que sabe tem uma chance de um em n de acertar
 * aquele, e sao precisas umas n log n tentativas.
 *
 * <p>A tabela da cauda mede isso: ela chega a noventa por cento em poucas rodadas e gasta
 * quase o mesmo tanto de novo para fechar os dez por cento que faltam.
 */
public final class Empurra implements Estrategia {

    @Override
    public String nome() {
        return "empurra";
    }

    @Override
    public void rodada(boolean[] sabiam, boolean[] sabem, Sorteio sorteio, Contador contador) {
        for (int quem = 0; quem < sabiam.length; quem++) {
            if (!sabiam[quem]) continue;

            int alvo = sorteio.outroQueNao(quem, sabiam.length);

            /* Util quer dizer que o alvo nao sabia no comeco da rodada. Dois nos que
             * contam para o mesmo alvo na mesma rodada: so o primeiro conta como util. */
            boolean util = !sabiam[alvo] && !sabem[alvo];

            contador.mensagem(util);

            sabem[alvo] = true;
        }
    }
}
