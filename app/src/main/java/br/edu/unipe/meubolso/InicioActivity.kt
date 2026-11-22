package br.edu.unipe.meubolso

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import br.edu.unipe.meubolso.ui.acesso.SplashActivity
import br.edu.unipe.meubolso.ui.acesso.LoginActivity
import br.edu.unipe.meubolso.ui.acesso.CadastroActivity
import br.edu.unipe.meubolso.ui.painel.PainelActivity
import br.edu.unipe.meubolso.ui.contas.ContasFixasActivity
import br.edu.unipe.meubolso.ui.contas.DetalhesContaActivity
import android.widget.TextView
import androidx.core.os.BundleCompat
import br.edu.unipe.meubolso.ui.movimentacoes.FormularioMovimentacao
import br.edu.unipe.meubolso.ui.movimentacoes.RascunhoMovimentacao
import br.edu.unipe.meubolso.ui.movimentacoes.ValorEmReais
import java.time.format.DateTimeFormatter
import br.edu.unipe.meubolso.ui.historico.HistoricoActivity
import br.edu.unipe.meubolso.ui.relatorios.RelatoriosActivity

// Entrada temporária para testar cada tela durante o desenvolvimento em paralelo.
class InicioActivity : AppCompatActivity(R.layout.inicio) {
    private var ultimo: RascunhoMovimentacao? = null
    private val formulario = registerForActivityResult(FormularioMovimentacao()) { resultado ->
        if (resultado != null) exibirRascunho(resultado)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vincular(R.id.abrir_splash, SplashActivity::class.java)
        vincular(R.id.abrir_login, LoginActivity::class.java)
        vincular(R.id.abrir_cadastro, CadastroActivity::class.java)
        vincular(R.id.abrir_painel, PainelActivity::class.java)
        vincular(R.id.abrir_contas_fixas, ContasFixasActivity::class.java)
        vincular(R.id.abrir_detalhes_conta, DetalhesContaActivity::class.java)
        findViewById<View>(R.id.abrir_movimentacao).setOnClickListener { formulario.launch(null) }
        findViewById<View>(R.id.editar_rascunho).setOnClickListener { formulario.launch(ultimo) }
        savedInstanceState?.let {
            BundleCompat.getSerializable(it, "ultimo_rascunho", RascunhoMovimentacao::class.java)
        }?.let(::exibirRascunho)
        vincular(R.id.abrir_historico, HistoricoActivity::class.java)
        vincular(R.id.abrir_relatorios, RelatoriosActivity::class.java)
    }

    private fun exibirRascunho(dados: RascunhoMovimentacao) {
        ultimo = dados
        findViewById<TextView>(R.id.ultimo_rascunho).apply {
            text = getString(R.string.resumo_rascunho, dados.descricao, ValorEmReais.paraExibir(dados.centavos),
                dados.tipo.rotulo, dados.categoria.rotulo, dados.data.format(DateTimeFormatter.ofPattern("dd/MM/uuuu")))
            visibility = View.VISIBLE
        }
        findViewById<View>(R.id.editar_rascunho).visibility = View.VISIBLE
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("ultimo_rascunho", ultimo)
    }

    private fun vincular(botao: Int, destino: Class<out AppCompatActivity>) {
        findViewById<View>(botao).setOnClickListener { startActivity(Intent(this, destino)) }
    }
}
