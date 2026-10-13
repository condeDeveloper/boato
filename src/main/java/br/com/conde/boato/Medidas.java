package br.com.conde.boato;

import java.util.ArrayList;
import java.util.List;

/**
 * AS CINCO TABELAS que o medidor imprime e que os testes conferem numero por numero.
 *
 * <p>Tudo aqui e contado, nunca cronometrado: rodadas, mensagens e quantos ficaram de
 * fora. Os mesmos numeros saem em qualquer sistema.
 *
 * <p>Toda linha e a media de muitas execucoes com sementes diferentes, porque uma execucao
 * sozinha de um processo aleatorio nao e uma medida, e uma anedota. A coluna do pior caso
 * esta do lado para a media nao esconder a cauda.
 */
public final class Medidas {

    public static final long SEMENTE = 20_261_013L;

    /** Quantas execucoes por linha de tabela. */
    public static final int EXECUCOES = 200;

    /** Os tamanhos de rede medidos. */
    public static final int[] TAMANHOS = {64, 1_024, 16_384, 65_536};

    /** O tamanho da rede nas tabelas que fixam um so. */
    public static final int TAMANHO = 16_384;

    private Medidas() {}

    /** As tres estrategias de boato, sem a difusao e sem a regra de parada. */
    public static List<Estrategia> asTres() {
        return List.of(new Empurra(), new Puxa(), new EmpurraEPuxa());
    }

    public static List<Estrategia> todas() {
        return List.of(new Difusao(), new Empurra(), new Puxa(), new EmpurraEPuxa());
    }

    private static final java.util.Map<String, List<Rede.Resultado>> RODADAS =
            new java.util.HashMap<>();

    /** As execucoes de uma estrategia num tamanho, calculadas uma vez so. */
    public static synchronized List<Rede.Resultado> execucoes(Estrategia estrategia, int nos) {
        return RODADAS.computeIfAbsent(estrategia.nome() + " " + nos, chave -> {
            List<Rede.Resultado> saida = new ArrayList<>();

            for (int i = 0; i < EXECUCOES; i++)
                saida.add(Rede.rodar(estrategia, nos, SEMENTE + i * 7919L));

            return saida;
        });
    }

    private static double media(List<Rede.Resultado> quais,
            java.util.function.ToDoubleFunction<Rede.Resultado> coluna) {

        double soma = 0;

        for (Rede.Resultado resultado : quais) soma += coluna.applyAsDouble(resultado);

        return soma / quais.size();
    }

    private static double maior(List<Rede.Resultado> quais,
            java.util.function.ToDoubleFunction<Rede.Resultado> coluna) {

        double maior = Double.NEGATIVE_INFINITY;

        for (Rede.Resultado resultado : quais)
            maior = Math.max(maior, coluna.applyAsDouble(resultado));

        return maior;
    }

    // ───────────────────── 1. a correcao ─────────────────────

    public record LinhaCorrecao(String estrategia, int nos, int limiteInferior, long menorRodadas,
            long ficaramDeFora, boolean valida, String problema) {}

    /** Todo mundo fica sabendo, e nenhuma estrategia termina antes do limite. */
    public static List<LinhaCorrecao> aCorrecao() {
        List<LinhaCorrecao> saida = new ArrayList<>();

        for (int nos : TAMANHOS)
            for (Estrategia estrategia : todas()) {
                List<Rede.Resultado> quais = execucoes(estrategia, nos);

                int limite = estrategia.limiteInferior(nos);

                long menor = Long.MAX_VALUE;
                long deFora = 0;
                String problema = "";

                for (Rede.Resultado resultado : quais) {
                    menor = Math.min(menor, resultado.rodadas());

                    deFora += resultado.ficaramDeFora();

                    if (!resultado.todosSouberam() && problema.isEmpty())
                        problema = resultado.ficaramDeFora() + " ficaram de fora";

                    if (resultado.rodadas() < limite && problema.isEmpty())
                        problema = "terminou em " + resultado.rodadas()
                                + ", abaixo do limite de " + limite;
                }

                saida.add(new LinhaCorrecao(estrategia.nome(), nos, limite, menor, deFora,
                        problema.isEmpty(), problema));
            }

        return saida;
    }

    // ───────────────────── 2. as rodadas ─────────────────────

    public record LinhaRodadas(String estrategia, int nos, int limiteInferior, double media,
            double pior, double vezesOLimite) {}

    /** Quantas rodadas ate todo mundo saber, por tamanho de rede. */
    public static List<LinhaRodadas> asRodadas() {
        List<LinhaRodadas> saida = new ArrayList<>();

        for (int nos : TAMANHOS)
            for (Estrategia estrategia : todas()) {
                List<Rede.Resultado> quais = execucoes(estrategia, nos);

                /* Na tabela das rodadas a coluna de comparacao e sempre a difusao, que e
                 * o que seria possivel com coordenacao perfeita. O limite que cada
                 * estrategia tem como teorema esta na tabela da correcao. */
                int limite = Difusao.doDobrar(nos);

                double media = media(quais, Rede.Resultado::rodadas);

                saida.add(new LinhaRodadas(estrategia.nome(), nos, limite, media,
                        maior(quais, Rede.Resultado::rodadas), media / limite));
            }

        return saida;
    }

    // ───────────────────── 3. a cauda ─────────────────────

    public record LinhaCauda(String estrategia, int nos, double ate50, double ate90,
            double ate99, double ate100, double doFimParaOComeco) {}

    /**
     * ONDE CADA ESTRATEGIA GASTA O TEMPO.
     *
     * <p>A coluna que importa e a ultima: quantas rodadas levou para ir de noventa e nove
     * por cento a cem, dividido por quantas levou para chegar aos noventa e nove. Ela diz
     * se o ultimo por cento e barato ou e metade do trabalho.
     */
    public static List<LinhaCauda> aCauda() {
        List<LinhaCauda> saida = new ArrayList<>();

        for (int nos : new int[] {1_024, 16_384, 65_536})
            for (Estrategia estrategia : asTres()) {
                List<Rede.Resultado> quais = execucoes(estrategia, nos);

                double ate50 = media(quais, r -> r.rodadaAte(0.50));
                double ate90 = media(quais, r -> r.rodadaAte(0.90));
                double ate99 = media(quais, r -> r.rodadaAte(0.99));
                double ate100 = media(quais, Rede.Resultado::rodadas);

                saida.add(new LinhaCauda(estrategia.nome(), nos, ate50, ate90, ate99, ate100,
                        ate99 == 0 ? 0 : (ate100 - ate99) / ate99));
            }

        return saida;
    }

    // ───────────────────── 4. as mensagens ─────────────────────

    public record LinhaMensagens(String estrategia, int nos, double mensagens,
            double porNo, double fracaoInutil, double rodadas) {}

    /** Quantas mensagens cada estrategia gasta, e quantas delas nao ensinam nada. */
    public static List<LinhaMensagens> asMensagens() {
        List<LinhaMensagens> saida = new ArrayList<>();

        for (int nos : TAMANHOS)
            for (Estrategia estrategia : asTres()) {
                List<Rede.Resultado> quais = execucoes(estrategia, nos);

                saida.add(new LinhaMensagens(estrategia.nome(), nos,
                        media(quais, Rede.Resultado::mensagens),
                        media(quais, Rede.Resultado::mensagensPorNo),
                        media(quais, Rede.Resultado::fracaoInutil),
                        media(quais, Rede.Resultado::rodadas)));
            }

        return saida;
    }

    // ───────────────────── 5. a parada ─────────────────────

    /** Os limites de tentativas inuteis testados. */
    public static final int[] LIMITES = {1, 2, 3, 4, 6, 10};

    public record LinhaParada(int limite, int nos, double rodadas, double mensagensPorNo,
            double fracaoDeFora, double execucoesComAlguemDeFora, double economiaDeMensagens) {}

    /** O que a regra de parada economiza, e quem ela deixa de fora. */
    public static List<LinhaParada> aParada() {
        List<LinhaParada> saida = new ArrayList<>();

        int nos = TAMANHO;

        double semParada = media(execucoes(new Empurra(), nos), Rede.Resultado::mensagensPorNo);

        for (int limite : LIMITES) {
            List<Rede.Resultado> quais = execucoes(new EmpurraComParada(limite), nos);

            double deFora = media(quais, r -> (double) r.ficaramDeFora() / r.nos());

            long comAlguemDeFora = quais.stream().filter(r -> !r.todosSouberam()).count();

            double porNo = media(quais, Rede.Resultado::mensagensPorNo);

            saida.add(new LinhaParada(limite, nos, media(quais, Rede.Resultado::rodadas),
                    porNo, deFora, (double) comAlguemDeFora / quais.size(),
                    1 - porNo / semParada));
        }

        return saida;
    }
}
