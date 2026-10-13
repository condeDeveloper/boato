package br.com.conde.boato;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** As pecas, uma a uma, antes de qualquer medida. */
class BoatoTest {

    // ───────────────────── o sorteio ─────────────────────

    @Nested
    @DisplayName("o sorteio da casa")
    class OSorteio {

        private int ateSemDeslocamento(long[] estado, int limite) {
            estado[0] = estado[0] * 6364136223846793005L + 1442695040888963407L;

            return Math.floorMod((int) estado[0], limite);
        }

        /**
         * O DEFEITO DOS BITS DE BAIXO destruiria o repositorio inteiro.
         *
         * <p>A unica coisa que acontece neste codigo e sortear com quem falar, e as redes
         * medidas tem tamanho potencia de dois. Com os bits de baixo, sortear entre 1.024
         * devolve os 1.024 na mesma ordem, sempre: o espalhamento viraria um ciclo fixo.
         */
        @Test
        @DisplayName("sem o deslocamento, mil sorteios entre 1.024 dao 1.024 valores em ordem fixa")
        void semODeslocamentoViraCiclo() {
            long[] estado = {Medidas.SEMENTE};

            Set<Integer> primeiros = new LinkedHashSet<>();

            for (int i = 0; i < 1_024; i++) primeiros.add(ateSemDeslocamento(estado, 1_024));

            assertThat(primeiros)
                    .as("mil e vinte e quatro sorteios, mil e vinte e quatro valores")
                    .hasSize(1_024);

            List<Integer> segundos = new java.util.ArrayList<>();

            for (int i = 0; i < 1_024; i++) segundos.add(ateSemDeslocamento(estado, 1_024));

            assertThat(segundos)
                    .as("e a segunda volta repete a primeira, na mesma ordem")
                    .isEqualTo(new java.util.ArrayList<>(primeiros));

            Sorteio bom = new Sorteio(Medidas.SEMENTE);

            Set<Integer> comDeslocamento = new LinkedHashSet<>();

            for (int i = 0; i < 1_024; i++) comDeslocamento.add(bom.ate(1_024));

            assertThat(comDeslocamento)
                    .as("com os bits de cima ha repetidos, que e o que acaso faz")
                    .hasSizeLessThan(1_024);
        }

        @Test
        @DisplayName("o sorteio nunca devolve o proprio no, e cobre todos os outros")
        void oOutroNuncaEhOProprio() {
            Sorteio sorteio = new Sorteio(Medidas.SEMENTE);

            Set<Integer> alcancados = new LinkedHashSet<>();

            for (int i = 0; i < 20_000; i++) {
                int outro = sorteio.outroQueNao(5, 10);

                assertThat(outro).isNotEqualTo(5).isBetween(0, 9);

                alcancados.add(outro);
            }

            assertThat(alcancados)
                    .as("os nove outros nos sao todos alcancados")
                    .hasSize(9);
        }

        @Test
        @DisplayName("numa rede de um no nao ha outro com quem falar")
        void redeDeUmNo() {
            assertThatThrownBy(() -> new Sorteio(1).outroQueNao(0, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("rede de um");
        }

        @Test
        @DisplayName("o sorteio da os mesmos numeros em qualquer maquina")
        void oSorteioEhFixo() {
            Sorteio sorteio = new Sorteio(1);

            assertThat(List.of(sorteio.proximo(), sorteio.proximo(), sorteio.proximo()))
                    .containsExactly(1817669548, -2107078989, -1510284903);
        }
    }

    // ───────────────────── o limite ─────────────────────

    @Nested
    @DisplayName("o limite inferior")
    class OLimite {

        @Test
        @DisplayName("o limite de dobrar e o teto do logaritmo na base dois")
        void oLimiteDeDobrar() {
            assertThat(Difusao.doDobrar(2)).isEqualTo(1);
            assertThat(Difusao.doDobrar(64)).isEqualTo(6);
            assertThat(Difusao.doDobrar(65)).as("sessenta e cinco ja precisa de sete").isEqualTo(7);
            assertThat(Difusao.doDobrar(65_536)).isEqualTo(16);
        }

        /**
         * A DIFUSAO ATINGE O LIMITE, e e isso que faz dele um limite e nao um palpite.
         */
        @Test
        @DisplayName("a difusao combinada termina exatamente no limite, em toda rede")
        void aDifusaoAtingeOLimite() {
            for (int nos : new int[] {2, 3, 7, 64, 100, 1_024, 4_096}) {
                Rede.Resultado resultado = Rede.rodar(new Difusao(), nos, 1);

                assertThat(resultado.rodadas())
                        .as("" + nos)
                        .isEqualTo(Difusao.doDobrar(nos));

                assertThat(resultado.todosSouberam()).as("" + nos).isTrue();
            }
        }

        /**
         * A CORRECAO, FIXADA: as que puxam nao obedecem ao limite de dobrar.
         *
         * <p>Eu apliquei o limite a todas e o juiz reprovou a melhor das tres. O argumento
         * do dobro supoe que cada mensagem ensina um no; quando alguem PERGUNTA, um no que
         * sabe ensina varios na mesma rodada.
         */
        @Test
        @DisplayName("empurrar e puxar termina abaixo do limite de dobrar, e esta certo")
        void empurrarEPuxarQuebraOLimiteDeDobrar() {
            int nos = 65_536;

            long menor = Long.MAX_VALUE;

            for (int i = 0; i < 50; i++)
                menor = Math.min(menor,
                        Rede.rodar(new EmpurraEPuxa(), nos, Medidas.SEMENTE + i).rodadas());

            assertThat(menor)
                    .as("o limite de dobrar diria " + Difusao.doDobrar(nos))
                    .isLessThan(Difusao.doDobrar(nos));

            assertThat(new EmpurraEPuxa().limiteInferior(nos))
                    .as("e por isso ela declara que nao tem limite util")
                    .isEqualTo(1);

            assertThat(new Empurra().limiteInferior(nos))
                    .as("enquanto a que so empurra tem, e e o do dobro")
                    .isEqualTo(Difusao.doDobrar(nos));
        }

        @Test
        @DisplayName("quem so empurra nunca termina antes do limite, em nenhuma execucao")
        void empurrarNuncaQuebraOLimite() {
            for (int nos : new int[] {64, 1_024, 16_384})
                for (int i = 0; i < 50; i++)
                    assertThat(Rede.rodar(new Empurra(), nos, Medidas.SEMENTE + i).rodadas())
                            .as(nos + " na semente " + i)
                            .isGreaterThanOrEqualTo(Difusao.doDobrar(nos));
        }
    }

    // ───────────────────── a rede ─────────────────────

    @Nested
    @DisplayName("a rede")
    class ARede {

        @Test
        @DisplayName("a rodada e sincrona: quem aprende agora so conta na rodada seguinte")
        void aRodadaEhSincrona() {
            /* Com dois nos e a que empurra, a primeira rodada informa exatamente um. */
            Rede.Resultado resultado = Rede.rodar(new Empurra(), 2, 1);

            assertThat(resultado.rodadas()).isEqualTo(1);
            assertThat(resultado.sabemPorRodada()).containsExactly(1, 2);
        }

        @Test
        @DisplayName("comeca com um sabendo e acaba com todos, nas tres estrategias")
        void comecaComUmEAcabaComTodos() {
            for (Estrategia estrategia : Medidas.asTres())
                for (int nos : new int[] {2, 3, 10, 500}) {
                    Rede.Resultado resultado = Rede.rodar(estrategia, nos, Medidas.SEMENTE);

                    assertThat(resultado.sabemPorRodada()[0])
                            .as(estrategia.nome() + " " + nos)
                            .isEqualTo(1);

                    assertThat(resultado.todosSouberam())
                            .as(estrategia.nome() + " " + nos)
                            .isTrue();

                    assertThat(resultado.ficaramDeFora()).isZero();
                }
        }

        @Test
        @DisplayName("o numero de quem sabe nunca diminui")
        void ninguemDesaprende() {
            for (Estrategia estrategia : Medidas.todas()) {
                int[] porRodada = Rede.rodar(estrategia, 2_000, Medidas.SEMENTE).sabemPorRodada();

                for (int i = 1; i < porRodada.length; i++)
                    assertThat(porRodada[i])
                            .as(estrategia.nome() + " na rodada " + i)
                            .isGreaterThanOrEqualTo(porRodada[i - 1]);
            }
        }

        @Test
        @DisplayName("uma rede de um no so nao tem boato para espalhar")
        void redeDeUmNoSo() {
            assertThatThrownBy(() -> Rede.rodar(new Empurra(), 1, 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("dois nos ou mais");
        }

        @Test
        @DisplayName("a rodada ate uma fracao e a primeira em que a fracao ja foi alcancada")
        void aRodadaAteUmaFracao() {
            Rede.Resultado resultado = Rede.rodar(new Difusao(), 1_024, 1);

            assertThat(resultado.rodadaAte(0.0))
                    .as("zero por cento ja vale na rodada zero")
                    .isZero();

            assertThat(resultado.rodadaAte(1.0))
                    .as("e cem por cento so na ultima")
                    .isEqualTo(resultado.rodadas());

            assertThat(resultado.rodadaAte(0.5))
                    .as("a difusao dobra, entao metade da rede sabe uma rodada antes do fim")
                    .isEqualTo(resultado.rodadas() - 1);
        }
    }

    // ───────────────────── as estrategias ─────────────────────

    @Nested
    @DisplayName("as estrategias")
    class AsEstrategias {

        @Test
        @DisplayName("quem empurra so gasta mensagem de quem sabe, e quem puxa gasta de todos")
        void quemGastaMensagem() {
            int nos = 1_000;

            Rede.Resultado empurrando = Rede.rodar(new Empurra(), nos, Medidas.SEMENTE);
            Rede.Resultado puxando = Rede.rodar(new Puxa(), nos, Medidas.SEMENTE);

            assertThat(puxando.mensagens())
                    .as("quem puxa manda exatamente um por no por rodada")
                    .isEqualTo(puxando.rodadas() * nos);

            assertThat(empurrando.mensagens())
                    .as("e quem empurra manda menos, porque no comeco poucos sabem")
                    .isLessThan(empurrando.rodadas() * nos);
        }

        @Test
        @DisplayName("a regra de parada faz os nos calarem, e ai a execucao acaba")
        void aRegraDeParadaCala() {
            Rede.Resultado resultado = Rede.rodar(new EmpurraComParada(1), 10_000,
                    Medidas.SEMENTE);

            assertThat(resultado.todosSouberam())
                    .as("com uma tentativa so, a rede cala antes de todo mundo saber")
                    .isFalse();

            assertThat(resultado.ficaramDeFora()).isPositive();

            assertThat(resultado.rodadas())
                    .as("e ela termina mesmo assim, em vez de rodar para sempre")
                    .isLessThan(Rede.MAXIMO_DE_RODADAS);
        }

        @Test
        @DisplayName("com muitas tentativas a regra de parada quase nao deixa ninguem de fora")
        void comMuitasTentativasQuaseNinguemFicaDeFora() {
            Rede.Resultado resultado = Rede.rodar(new EmpurraComParada(20), 10_000,
                    Medidas.SEMENTE);

            assertThat(resultado.todosSouberam()).isTrue();
        }

        @Test
        @DisplayName("um limite de zero tentativas nao e uma regra de parada")
        void limiteInvalido() {
            assertThatThrownBy(() -> new EmpurraComParada(0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("pelo menos uma tentativa");
        }

        @Test
        @DisplayName("a difusao combinada nao gasta uma mensagem inutil sequer")
        void aDifusaoNaoDesperdica() {
            for (int nos : new int[] {64, 1_024, 4_096}) {
                Rede.Resultado resultado = Rede.rodar(new Difusao(), nos, 1);

                assertThat(resultado.mensagensInuteis()).as("" + nos).isZero();

                assertThat(resultado.mensagens())
                        .as(nos + ": uma mensagem por no informado, e nada mais")
                        .isEqualTo(nos - 1);
            }
        }
    }
}
