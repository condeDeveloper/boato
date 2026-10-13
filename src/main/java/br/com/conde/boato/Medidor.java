package br.com.conde.boato;

import java.util.Locale;

/**
 * O MEDIDOR: imprime as tabelas que o README mostra e que os testes conferem.
 *
 * <p>Toda tabela daqui tem um teste do lado que afirma os mesmos numeros. Texto nao quebra
 * quando deixa de ser verdade; teste quebra.
 */
public final class Medidor {

    private Medidor() {
    }

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";

        if (comando.equals("correcao") || comando.equals("tudo")) correcao();
        if (comando.equals("rodadas") || comando.equals("tudo")) rodadas();
        if (comando.equals("cauda") || comando.equals("tudo")) cauda();
        if (comando.equals("mensagens") || comando.equals("tudo")) mensagens();
        if (comando.equals("parada") || comando.equals("tudo")) parada();
    }

    private static void correcao() {
        System.out.println();
        System.out.println("TODO MUNDO FICA SABENDO, E NINGUEM BATE O LIMITE INFERIOR?");
        System.out.println(Medidas.EXECUCOES + " execucoes por linha.");
        System.out.println();

        var linhas = Medidas.aCorrecao();
        long invalidas = linhas.stream().filter(l -> !l.valida()).count();

        System.out.printf("  %-22s %10s %18s %16s %16s %10s %s%n",
                "estrategia", "nos", "limite inferior", "menor rodadas", "ficaram de fora",
                "confere", "problema");

        for (var l : linhas)
            System.out.printf("  %-22s %10d %18d %16d %16d %10s %s%n",
                    l.estrategia(), l.nos(), l.limiteInferior(), l.menorRodadas(),
                    l.ficaramDeFora(), l.valida() ? "sim" : "NAO", l.problema());

        System.out.println();
        System.out.printf("  %d combinacoes, %d invalidas.%n", linhas.size(), invalidas);
        System.out.println();
    }

    private static void rodadas() {
        System.out.println();
        System.out.println("RODADAS ATE TODO MUNDO SABER.");
        System.out.println();

        System.out.printf("  %-22s %10s %18s %12s %12s %16s%n",
                "estrategia", "nos", "limite inferior", "media", "pior", "vezes o limite");

        for (var l : Medidas.asRodadas())
            System.out.printf(Locale.ROOT, "  %-22s %10d %18d %12.2f %12.0f %16.3f%n",
                    l.estrategia(), l.nos(), l.limiteInferior(), l.media(), l.pior(),
                    l.vezesOLimite());

        System.out.println();
    }

    private static void cauda() {
        System.out.println();
        System.out.println("ONDE CADA ESTRATEGIA GASTA O TEMPO.");
        System.out.println("A ultima coluna e o custo do ultimo por cento, em fracao do resto.");
        System.out.println();

        System.out.printf("  %-22s %10s %10s %10s %10s %10s %18s%n",
                "estrategia", "nos", "ate 50%", "ate 90%", "ate 99%", "ate 100%",
                "o ultimo por cento");

        for (var l : Medidas.aCauda())
            System.out.printf(Locale.ROOT, "  %-22s %10d %10.2f %10.2f %10.2f %10.2f %18.3f%n",
                    l.estrategia(), l.nos(), l.ate50(), l.ate90(), l.ate99(), l.ate100(),
                    l.doFimParaOComeco());

        System.out.println();
    }

    private static void mensagens() {
        System.out.println();
        System.out.println("QUANTAS MENSAGENS CUSTA.");
        System.out.println();

        System.out.printf("  %-22s %10s %16s %14s %16s %12s%n",
                "estrategia", "nos", "mensagens", "por no", "fracao inutil", "rodadas");

        for (var l : Medidas.asMensagens())
            System.out.printf(Locale.ROOT, "  %-22s %10d %16.0f %14.2f %16.4f %12.2f%n",
                    l.estrategia(), l.nos(), l.mensagens(), l.porNo(), l.fracaoInutil(),
                    l.rodadas());

        System.out.println();
    }

    private static void parada() {
        System.out.println();
        System.out.println("A REGRA DE PARADA: o no cala depois de k conversas seguidas inuteis.");
        System.out.println("Rede de " + Medidas.TAMANHO + " nos, " + Medidas.EXECUCOES
                + " execucoes por linha.");
        System.out.println();

        System.out.printf("  %8s %12s %16s %16s %22s %18s%n",
                "k", "rodadas", "mensagens/no", "fracao de fora", "execucoes com alguem de fora",
                "economia");

        for (var l : Medidas.aParada())
            System.out.printf(Locale.ROOT, "  %8d %12.2f %16.2f %16.6f %22.4f %18.4f%n",
                    l.limite(), l.rodadas(), l.mensagensPorNo(), l.fracaoDeFora(),
                    l.execucoesComAlguemDeFora(), l.economiaDeMensagens());

        System.out.println();
    }
}
