package br.com.conde.boato;

/**
 * UMA ESTRATEGIA DE ESPALHAMENTO: o que cada no faz em cada rodada.
 *
 * <p>O modelo e o de sempre e e so isto: a cada rodada, CADA no escolhe um outro no
 * qualquer da rede, com igual chance, e fala com ele. Nao ha topologia, nao ha vizinhos
 * fixos, nao ha quem conhece quem. Isso e uma idealizacao e e a certa para a pergunta:
 * numa rede de verdade os nos tem uma lista parcial de outros nos, e o que se quer saber e
 * quantas rodadas o boato leva quando essa lista e boa.
 *
 * <p>O que muda entre as estrategias e QUEM FALA e O QUE SE PASSA na conversa:
 *
 * <ul>
 *   <li>EMPURRAR: so quem sabe fala, e conta para quem ele escolheu.
 *   <li>PUXAR: todo mundo fala, e pergunta ao escolhido se ha novidade.
 *   <li>EMPURRAR E PUXAR: as duas coisas na mesma conversa.
 * </ul>
 *
 * <p>Essa diferenca parece pequena e decide tudo. Quem empurra depende de quem SABE achar
 * quem nao sabe, e no fim quase todo mundo sabe, entao quase toda conversa e inutil. Quem
 * puxa depende de quem NAO sabe achar quem sabe, e no fim quase toda conversa acerta.
 *
 * <p>Uma rodada e sincrona: todos falam, e so depois o estado muda. Sem isso um no que
 * soubesse cedo na rodada poderia contar para outro na mesma rodada, e o numero de rodadas
 * passaria a depender da ordem em que o laco varre os nos, que nao e propriedade de
 * estrategia nenhuma.
 */
public interface Estrategia {

    String nome();

    /** Zera o estado interno, se a estrategia tiver algum, para uma rede deste tamanho. */
    default void comecar(int nos) {
    }

    /**
     * O menor numero de rodadas em que ESTA estrategia pode terminar, como teorema.
     *
     * <p>O limite nao e o mesmo para todas, e descobrir isso foi o conserto principal deste
     * repositorio. Para quem so EMPURRA ele e o logaritmo na base dois: cada no que sabe
     * manda uma mensagem por rodada e cada mensagem ensina no maximo um no, entao o numero
     * de quem sabe no maximo DOBRA.
     *
     * <p>Para quem PUXA esse argumento nao existe. Um no que sabe pode ser perguntado por
     * muitos na mesma rodada, e todos aprendem de uma vez: em teoria a rede inteira pode
     * ficar sabendo numa rodada so. Nao ha limite util, e devolver um e dizer isso.
     *
     * <p>O limite de dobrar e um teorema sobre um modelo, nao sobre espalhamento. Aplicar o
     * modelo errado foi o que me fez escrever um juiz que reprovava a melhor das tres.
     */
    default int limiteInferior(int nos) {
        return Difusao.doDobrar(nos);
    }

    /**
     * Ainda vai sair alguma mensagem, ou a execucao travou de vez?
     *
     * <p>So faz sentido para a estrategia com regra de parada, que pode calar a rede toda
     * com gente de fora. Para as outras, uma rodada sem novidade e azar e a seguinte pode
     * andar, entao a resposta e sempre sim.
     */
    default boolean aindaFala(boolean[] sabem) {
        return true;
    }

    /**
     * Uma rodada: quem sabia antes esta em sabiam, e quem passa a saber entra em sabem.
     *
     * <p>Os dois vetores sao diferentes de proposito: ler o antigo e escrever no novo e o
     * que torna a rodada sincrona.
     */
    void rodada(boolean[] sabiam, boolean[] sabem, Sorteio sorteio, Contador contador);
}
