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
import br.edu.unipe.meubolso.ui.movimentacoes.MovimentacaoActivity
import br.edu.unipe.meubolso.ui.historico.HistoricoActivity
import br.edu.unipe.meubolso.ui.relatorios.RelatoriosActivity

// Entrada temporária para testar cada tela durante o desenvolvimento em paralelo.
class InicioActivity : AppCompatActivity(R.layout.inicio) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vincular(R.id.abrir_splash, SplashActivity::class.java)
        vincular(R.id.abrir_login, LoginActivity::class.java)
        vincular(R.id.abrir_cadastro, CadastroActivity::class.java)
        vincular(R.id.abrir_painel, PainelActivity::class.java)
        vincular(R.id.abrir_contas_fixas, ContasFixasActivity::class.java)
        vincular(R.id.abrir_detalhes_conta, DetalhesContaActivity::class.java)
        vincular(R.id.abrir_movimentacao, MovimentacaoActivity::class.java)
        vincular(R.id.abrir_historico, HistoricoActivity::class.java)
        vincular(R.id.abrir_relatorios, RelatoriosActivity::class.java)
    }

    private fun vincular(botao: Int, destino: Class<out AppCompatActivity>) {
        findViewById<View>(botao).setOnClickListener { startActivity(Intent(this, destino)) }
    }
}
