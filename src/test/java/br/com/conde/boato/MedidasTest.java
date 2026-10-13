package br.com.conde.boato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os numeros do README, afirmados.
 *
 * <p>Um README e texto: ele nao quebra quando deixa de ser verdade. Cada afirmacao das
 * tabelas esta repetida aqui para quebrar.
 */
class MedidasTest {

    private static final List<Medidas.LinhaRodadas> RODADAS = Medidas.asRodadas();
    private static final List<Medidas.LinhaCauda> CAUDA = Medidas.aCauda();
    private static final List<Medidas.LinhaMensagens> MENSAGENS = Medidas.asMensagens();
    private static final List<Medidas.LinhaParada> PARADA = Medidas.aParada();

    // ───────────────────── 1. a correcao ─────────────────────

    @Test
    @DisplayName("nas 16 combinacoes todo mundo fica sabendo e ninguem bate o limite que vale")
    void todasSaoValidas() {
        List<Medidas.LinhaCorrecao> linhas = Medidas.aCorrecao();

        assertThat(linhas).hasSize(16);

        assertThat(linhas).allSatisfy(l -> {
            assertThat(l.valida()).as(l.estrategia() + " " + l.nos() + ": " + l.problema())
                    .isTrue();

            assertThat(l.ficaramDeFora()).as(l.estrategia() + " " + l.nos()).isZero();
        });
    }

    // ───────────────────── 2. as rodadas ─────────────────────

    /**
     * A MEDIDA PRINCIPAL: empurrar e puxar termina ABAIXO da difusao coordenada.
     *
     * <p>A difusao combinada e a coreografia perfeita: cada no sabe o proprio numero, o
     * numero da rodada e o tamanho da rede, e nenhuma mensagem e desperdicada. Ela termina
     * em 16 rodadas numa rede de 65.536.
     *
     * <p>Empurrar e puxar nao coordena nada, desperdicia 92% das mensagens, e termina em
     * 13,51. Isso nao e um erro de medida: puxar deixa um no que sabe ensinar varios na
     * mesma rodada, e a coreografia nao pode fazer isso.
     */
    @Test
    @DisplayName("empurrar e puxar termina em 13,51 rodadas onde a coreografia perfeita leva 16")
    void empurrarEPuxarBateACoreografia() {
        assertThat(rodadas("difusao combinada", 65_536).media()).isEqualTo(16.0);
        assertThat(rodadas("empurra e puxa", 65_536).media()).isCloseTo(13.51, within(0.01));

        assertThat(rodadas("empurra e puxa", 65_536).vezesOLimite())
                .as("84% do que a coordenacao perfeita leva")
                .isCloseTo(0.844, within(0.001));

        for (int nos : Medidas.TAMANHOS)
            assertThat(rodadas("empurra e puxa", nos).media())
                    .as("" + nos)
                    .isLessThanOrEqualTo(rodadas("difusao combinada", nos).media());
    }

    @Test
    @DisplayName("a difusao combinada termina exatamente no limite em toda rede, sem variacao")
    void aDifusaoEhExata() {
        for (int nos : Medidas.TAMANHOS) {
            assertThat(rodadas("difusao combinada", nos).media())
                    .as("" + nos)
                    .isEqualTo(rodadas("difusao combinada", nos).limiteInferior());

            assertThat(rodadas("difusao combinada", nos).pior())
                    .as(nos + ": nao ha execucao pior, porque nao ha sorteio nela")
                    .isEqualTo(rodadas("difusao combinada", nos).media());
        }
    }

    @Test
    @DisplayName("empurrar leva 1,76 vezes a coreografia e puxar leva 1,28")
    void quantoCadaUmaLevaAMais() {
        assertThat(rodadas("empurra", 65_536).media()).isCloseTo(28.15, within(0.01));
        assertThat(rodadas("puxa", 65_536).media()).isCloseTo(20.43, within(0.01));

        assertThat(rodadas("empurra", 65_536).vezesOLimite()).isCloseTo(1.759, within(0.001));
        assertThat(rodadas("puxa", 65_536).vezesOLimite()).isCloseTo(1.277, within(0.001));
    }

    private static Medidas.LinhaRodadas rodadas(String estrategia, int nos) {
        return RODADAS.stream()
                .filter(l -> l.estrategia().equals(estrategia) && l.nos() == nos)
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 3. a cauda ─────────────────────

    /**
     * A CORRECAO: puxar NAO e lento no comeco. Ele e identico no comeco.
     *
     * <p>Eu tinha escrito que puxar quase nao anda enquanto poucos sabem, porque as
     * perguntas quase nunca acertam quem sabe. A conta esta certa e a conclusao nao: com i
     * sabendo, os que nao sabem acertam i deles por rodada, que e exatamente o mesmo que
     * empurrar consegue. Os dois dobram.
     *
     * <p>A diferenca inteira esta no fim.
     */
    @Test
    @DisplayName("empurrar e puxar chegam a metade da rede na mesma rodada, 16,00 contra 16,37")
    void puxarNaoEhLentoNoComeco() {
        assertThat(cauda("empurra", 65_536).ate50()).isCloseTo(16.00, within(0.01));
        assertThat(cauda("puxa", 65_536).ate50()).isCloseTo(16.37, within(0.01));

        assertThat(cauda("puxa", 65_536).ate50())
                .as("quatro por cento de diferenca, e nao a lentidao que eu esperava")
                .isLessThan(cauda("empurra", 65_536).ate50() * 1.05);
    }

    /**
     * E A DIFERENCA INTEIRA ESTA NO ULTIMO POR CENTO.
     *
     * <p>Empurrar gasta mais 34% do tempo que levou ate os 99% so para fechar o ultimo por
     * cento. Puxar gasta mais 7%.
     *
     * <p>O motivo e exato: com u sem saber e quase todos sabendo, empurrar erra cada um
     * deles com chance um sobre e, entao u cai por um fator e por rodada. Puxar faz cada um
     * dos u acertar com chance quase um, entao u cai AO QUADRADO.
     */
    @Test
    @DisplayName("o ultimo por cento custa 34% a mais para empurrar e 7% para puxar")
    void aDiferencaEstaNoUltimoPorCento() {
        assertThat(cauda("empurra", 65_536).doFimParaOComeco()).isCloseTo(0.340, within(0.001));
        assertThat(cauda("puxa", 65_536).doFimParaOComeco()).isCloseTo(0.070, within(0.001));
        assertThat(cauda("empurra e puxa", 65_536).doFimParaOComeco())
                .isCloseTo(0.092, within(0.001));

        assertThat(cauda("empurra", 65_536).doFimParaOComeco())
                .as("quase cinco vezes mais cara que puxar, no mesmo ultimo por cento")
                .isGreaterThan(cauda("puxa", 65_536).doFimParaOComeco() * 4);

        assertThat(cauda("empurra", 65_536).ate99()).isCloseTo(21.00, within(0.01));
        assertThat(cauda("empurra", 65_536).ate100()).isCloseTo(28.15, within(0.01));
    }

    @Test
    @DisplayName("a cauda de empurrar piora com a rede e a de puxar nao")
    void aCaudaDeEmpurrarPioraComARede() {
        assertThat(cauda("empurra", 1_024).doFimParaOComeco()).isCloseTo(0.193, within(0.001));
        assertThat(cauda("empurra", 16_384).doFimParaOComeco()).isCloseTo(0.315, within(0.001));
        assertThat(cauda("empurra", 65_536).doFimParaOComeco()).isCloseTo(0.340, within(0.001));

        for (int nos : new int[] {1_024, 16_384, 65_536})
            assertThat(cauda("puxa", nos).doFimParaOComeco())
                    .as("" + nos)
                    .isLessThan(0.08);
    }

    private static Medidas.LinhaCauda cauda(String estrategia, int nos) {
        return CAUDA.stream()
                .filter(l -> l.estrategia().equals(estrategia) && l.nos() == nos)
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 4. as mensagens ─────────────────────

    /**
     * A SEGUNDA CORRECAO: empurrar e puxar nao e cara.
     *
     * <p>Eu esperava que ela custasse o dobro, porque todo no fala em toda rodada e a
     * conversa vai nos dois sentidos. Ela gasta 13,51 mensagens por no contra 12,04 de
     * empurrar e 20,43 de puxar: doze por cento acima da mais barata, e termina em metade
     * das rodadas dela.
     */
    @Test
    @DisplayName("empurrar e puxar gasta 13,51 mensagens por no contra 12,04 de empurrar")
    void empurrarEPuxarNaoEhCara() {
        assertThat(mensagens("empurra", 65_536).porNo()).isCloseTo(12.04, within(0.01));
        assertThat(mensagens("puxa", 65_536).porNo()).isCloseTo(20.43, within(0.01));
        assertThat(mensagens("empurra e puxa", 65_536).porNo()).isCloseTo(13.51, within(0.01));

        assertThat(mensagens("empurra e puxa", 65_536).porNo())
                .as("doze por cento acima da mais barata")
                .isLessThan(mensagens("empurra", 65_536).porNo() * 1.15);

        assertThat(mensagens("empurra e puxa", 65_536).rodadas())
                .as("e termina em metade das rodadas dela")
                .isLessThan(mensagens("empurra", 65_536).rodadas() * 0.52);
    }

    @Test
    @DisplayName("mais de nove em dez mensagens nao ensinam nada a ninguem")
    void quaseTodaMensagemEhInutil() {
        for (String estrategia : List.of("empurra", "puxa", "empurra e puxa"))
            assertThat(mensagens(estrategia, 65_536).fracaoInutil())
                    .as(estrategia)
                    .isGreaterThan(0.91);

        assertThat(mensagens("puxa", 65_536).fracaoInutil())
                .as("puxar e a mais desperdiçadora, porque ate quem ja sabe pergunta")
                .isCloseTo(0.9508, within(0.0001));
    }

    private static Medidas.LinhaMensagens mensagens(String estrategia, int nos) {
        return MENSAGENS.stream()
                .filter(l -> l.estrategia().equals(estrategia) && l.nos() == nos)
                .findFirst()
                .orElseThrow();
    }

    // ───────────────────── 5. a parada ─────────────────────

    /**
     * A REGRA DE PARADA ECONOMIZA MUITO E DEIXA GENTE DE FORA, nessa ordem.
     *
     * <p>Com uma tentativa so, ela corta 84% das mensagens e deixa 17,5% da rede sem saber.
     * Nao e atraso: e perda, e ninguem fica sabendo que ficou.
     */
    @Test
    @DisplayName("parar na primeira conversa inutil economiza 84% e deixa 17,5% da rede de fora")
    void aParadaEconomizaECorta() {
        assertThat(parada(1).economiaDeMensagens()).isCloseTo(0.8398, within(0.0001));
        assertThat(parada(1).fracaoDeFora()).isCloseTo(0.175, within(0.001));

        assertThat(parada(1).mensagensPorNo()).isCloseTo(1.74, within(0.01));
    }

    @Test
    @DisplayName("a economia cai e o estrago some juntos, e nenhum k zera os dois")
    void aTrocaEhMonotona() {
        double economiaAnterior = 1;
        double deForaAnterior = 1;

        for (int limite : Medidas.LIMITES) {
            assertThat(parada(limite).economiaDeMensagens())
                    .as("k " + limite)
                    .isLessThan(economiaAnterior);

            assertThat(parada(limite).fracaoDeFora())
                    .as("k " + limite)
                    .isLessThan(deForaAnterior);

            economiaAnterior = parada(limite).economiaDeMensagens();
            deForaAnterior = parada(limite).fracaoDeFora();
        }

        assertThat(parada(6).fracaoDeFora()).isCloseTo(0.000477, within(0.00001));
        assertThat(parada(6).economiaDeMensagens()).isCloseTo(0.2924, within(0.0001));
    }

    /**
     * E A REGRA NUNCA DA GARANTIA, por mais alto que o k seja.
     *
     * <p>Com dez tentativas ela economiza 3% e ainda assim, em 17,5% das execucoes, alguem
     * fica de fora. Uma regra local nao tem como saber que acabou.
     */
    @Test
    @DisplayName("mesmo com dez tentativas, em 17,5% das execucoes alguem fica de fora")
    void aRegraNuncaDaGarantia() {
        assertThat(parada(10).execucoesComAlguemDeFora()).isCloseTo(0.175, within(0.001));
        assertThat(parada(10).economiaDeMensagens()).isCloseTo(0.0329, within(0.0001));

        for (int limite : Medidas.LIMITES)
            assertThat(parada(limite).execucoesComAlguemDeFora())
                    .as("k " + limite)
                    .isPositive();
    }

    private static Medidas.LinhaParada parada(int limite) {
        return PARADA.stream()
                .filter(l -> l.limite() == limite)
                .findFirst()
                .orElseThrow();
    }
}
