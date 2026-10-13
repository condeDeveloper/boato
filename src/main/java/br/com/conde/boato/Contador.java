package br.com.conde.boato;

/**
 * O CONTADOR: rodadas e mensagens. Nunca segundos.
 *
 * <p>A RODADA e a medida certa porque e a unidade de tempo do modelo: numa rede de verdade
 * uma rodada e uma ida e volta, e o que custa e a espera, nao a conta. Duas estrategias que
 * terminam na mesma rodada terminam ao mesmo tempo, por mais diferente que seja o codigo
 * delas.
 *
 * <p>E ela tem um piso conhecido, que e o que torna a coluna legivel sem comparar com nada:
 * nenhuma estrategia em que cada no fala com um no por rodada pode terminar antes de
 * logaritmo de n na base dois rodadas, porque o numero de quem sabe no maximo dobra.
 *
 * <h2>A segunda conta</h2>
 *
 * <p>MENSAGEM e a outra, e as duas andam ao contrario. Terminar em menos rodadas quase
 * sempre custa falar mais, e uma estrategia que fala o dobro por rodada precisa terminar em
 * menos da metade das rodadas para sair ganhando. A tabela das mensagens e essa conta.
 *
 * <p>A mensagem INUTIL tem coluna propria: e a conversa em que ninguem aprende nada. E ela
 * que explica por que empurrar e lento no fim, e e a unica coisa que uma regra de parada
 * pode economizar.
 *
 * <p>Segundos nao serviriam: nada aqui e uma rede, e um cronometro mediria o laco do Java.
 */
public final class Contador {

    private long rodadas;
    private long mensagens;
    private long mensagensUteis;

    public void rodada() {
        rodadas++;
    }

    /** Uma conversa aconteceu. Util quer dizer que alguem passou a saber por causa dela. */
    public void mensagem(boolean util) {
        mensagens++;

        if (util) mensagensUteis++;
    }

    public long rodadas() {
        return rodadas;
    }

    public long mensagens() {
        return mensagens;
    }

    public long mensagensUteis() {
        return mensagensUteis;
    }

    public long mensagensInuteis() {
        return mensagens - mensagensUteis;
    }

    public double fracaoInutil() {
        return mensagens == 0 ? 0 : (double) mensagensInuteis() / mensagens;
    }
}
