package br.com.conde.boato;

/**
 * A REDE: n nos, um sabendo, e as rodadas acontecendo ate todo mundo saber.
 *
 * <p>O laco esta num lugar so de proposito. Se cada estrategia tivesse o proprio laco, uma
 * diferenca de uma linha no jeito de fechar a rodada apareceria na tabela como se fosse
 * propriedade da estrategia.
 *
 * <p>A rodada e SINCRONA: todas as conversas acontecem lendo o estado do comeco da rodada,
 * e so no fim o estado novo entra em vigor. Sem isso, um no que soubesse cedo na rodada
 * contaria para outro na mesma rodada, e o numero de rodadas passaria a depender da ordem
 * em que o laco varre os nos.
 *
 * <p>Comeca sempre pelo no zero, e isso nao perde generalidade: no modelo todo no escolhe
 * um outro com igual chance, entao nenhum no tem posicao privilegiada. Na difusao
 * combinada, que usa os numeros dos nos, comecar no zero e parte da coreografia.
 */
public final class Rede {

    private Rede() {}

    /** O que aconteceu numa execucao. */
    public record Resultado(String estrategia, int nos, long rodadas, long mensagens,
            long mensagensInuteis, int sabemNoFim, int[] sabemPorRodada) {

        public boolean todosSouberam() {
            return sabemNoFim == nos;
        }

        public int ficaramDeFora() {
            return nos - sabemNoFim;
        }

        public double mensagensPorNo() {
            return (double) mensagens / nos;
        }

        public double fracaoInutil() {
            return mensagens == 0 ? 0 : (double) mensagensInuteis / mensagens;
        }

        /** A primeira rodada em que pelo menos esta fracao da rede ja sabia. */
        public long rodadaAte(double fracao) {
            for (int rodada = 0; rodada < sabemPorRodada.length; rodada++)
                if (sabemPorRodada[rodada] >= fracao * nos) return rodada;

            return -1;
        }
    }

    /** O teto de rodadas, para uma estrategia que nunca termina nao travar a medida. */
    public static final int MAXIMO_DE_RODADAS = 1_000;

    public static Resultado rodar(Estrategia estrategia, int nos, long semente) {
        if (nos < 2) throw new IllegalArgumentException("uma rede tem dois nos ou mais");

        estrategia.comecar(nos);

        Sorteio sorteio = new Sorteio(semente);
        Contador contador = new Contador();

        boolean[] sabem = new boolean[nos];
        sabem[0] = true;

        java.util.List<Integer> porRodada = new java.util.ArrayList<>();
        porRodada.add(1);

        int quantosSabem = 1;

        while (quantosSabem < nos && contador.rodadas() < MAXIMO_DE_RODADAS) {
            boolean[] sabiam = sabem.clone();

            estrategia.rodada(sabiam, sabem, sorteio, contador);

            contador.rodada();

            int agora = 0;

            for (boolean sabe : sabem)
                if (sabe) agora++;

            quantosSabem = agora;

            porRodada.add(agora);

            /* Ninguem mais vai falar: a execucao acabou aqui, com ou sem todo mundo. */
            if (!estrategia.aindaFala(sabem)) break;
        }

        int[] linha = new int[porRodada.size()];

        for (int i = 0; i < linha.length; i++) linha[i] = porRodada.get(i);

        return new Resultado(estrategia.nome(), nos, contador.rodadas(), contador.mensagens(),
                contador.mensagensInuteis(), quantosSabem, linha);
    }

}
