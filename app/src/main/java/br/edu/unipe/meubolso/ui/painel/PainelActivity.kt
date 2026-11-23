package br.edu.unipe.meubolso.ui.painel

import android.Manifest
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.view.isVisible
import androidx.activity.result.contract.ActivityResultContracts
import br.edu.unipe.meubolso.notificacoes.Lembretes
import androidx.appcompat.app.AppCompatDelegate
import android.content.Intent
import br.edu.unipe.meubolso.ui.comum.configurarAbas
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.databinding.TelaPainelBinding
import br.edu.unipe.meubolso.dados.MeuBolso
import br.edu.unipe.meubolso.dominio.Cofrinho
import br.edu.unipe.meubolso.ui.acesso.LoginActivity
import br.edu.unipe.meubolso.ui.contas.ContasFixasActivity
import br.edu.unipe.meubolso.ui.historico.*
import br.edu.unipe.meubolso.ui.movimentacoes.FormularioMovimentacao
import br.edu.unipe.meubolso.ui.relatorios.*
import java.math.BigInteger
import java.math.RoundingMode
import java.text.NumberFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

class PainelActivity : AppCompatActivity() {
    private lateinit var tela: TelaPainelBinding
    private var mes = YearMonth.now()
    private var anual = false
    private val brasil = Locale.forLanguageTag("pt-BR")
    private val app get() = MeuBolso.app
    private val formulario = registerForActivityResult(FormularioMovimentacao()) {
        if (it != null) HistoricoSessao.atual.adicionar(it)
        atualizar()
    }
    private val permissao = registerForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        if (concedida) Lembretes.conferirAgora(this)
        atualizarBotaoLembretes()
    }
    private fun atualizarBotaoLembretes() {
        tela.lembretes.isVisible = !getSystemService(NotificationManager::class.java).areNotificationsEnabled() || Lembretes.canalBloqueado(this)
    }

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        state?.getString("mes")?.let { mes = YearMonth.parse(it) }
        anual = state?.getBoolean("anual") == true
        tela = TelaPainelBinding.inflate(layoutInflater)
        setContentView(tela.root)
        configurarAbas(br.edu.unipe.meubolso.R.id.aba_inicio)
        tela.tema.setOnClickListener {
            val preferencias = getSharedPreferences("aparencia", MODE_PRIVATE)
            val modos = listOf(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, AppCompatDelegate.MODE_NIGHT_NO, AppCompatDelegate.MODE_NIGHT_YES)
            val atual = preferencias.getInt("modo", modos.first())
            androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Aparência")
                .setSingleChoiceItems(arrayOf("Seguir o sistema", "Claro", "Escuro"), modos.indexOf(atual)) { janela, i ->
                    janela.dismiss()
                    preferencias.edit().putInt("modo", modos[i]).apply()
                    AppCompatDelegate.setDefaultNightMode(modos[i])
                }.show()
        }
        Lembretes.agendar(this)
        tela.lembretes.setOnClickListener {
            val prefs = getSharedPreferences("avisos", MODE_PRIVATE)
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED &&
                (!prefs.getBoolean("pedido", false) || shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS))) {
                prefs.edit().putBoolean("pedido", true).apply()
                permissao.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                startActivity(Lembretes.ajustes(this))
            }
        }
        tela.anterior.setOnClickListener { mover(-1) }
        tela.seguinte.setOnClickListener { mover(1) }
        tela.alternar.setOnClickListener { anual = !anual; atualizar() }
        tela.nova.setOnClickListener { formulario.launch(null) }
        tela.historico.setOnClickListener { startActivity(Intent(this, HistoricoActivity::class.java)) }
        tela.relatorios.setOnClickListener { startActivity(Intent(this, RelatoriosActivity::class.java)) }
        tela.contas.setOnClickListener { startActivity(Intent(this, ContasFixasActivity::class.java).putExtra("mes", mes.toString())) }
        tela.sair.setOnClickListener {
            app.perfil = ""
            getSystemService(NotificationManager::class.java).cancelAll()
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
    override fun onResume() {
        super.onResume()
        atualizarBotaoLembretes()
        atualizar()
        Lembretes.conferirAgora(this)
    }
    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        state.putString("mes", mes.toString())
        state.putBoolean("anual", anual)
    }
    private fun mover(delta: Long) {
        mes = if (anual) mes.plusYears(delta) else mes.plusMonths(delta)
        atualizar()
    }
    private fun moeda(valor: BigInteger) = NumberFormat.getCurrencyInstance(brasil).format(valor.toBigDecimal(2))
    private fun atualizar() {
        val dao = app.dao
        dao.preparar(app.perfil, YearMonth.now())
        dao.preparar(app.perfil, mes)
        val movimentos = HistoricoSessao.atual.itens.map { it.dados }
        val totais = ResumoFinanceiro.calcular(movimentos, mes.year, mes.monthValue.takeUnless { anual }).totais
        val pendencias = dao.contas(app.perfil).flatMap { conta ->
            dao.parcelas(conta.id).filter { it.movimento == null && (if (anual) YearMonth.parse(it.mes).year == mes.year else it.mes == mes.toString()) }
                .map { conta.descricao to it }
        }.sortedBy { it.second.vencimento }
        val previsto = totais.saldo - pendencias.sumOf { it.second.valor.toBigInteger() }
        tela.periodo.text = if (anual) mes.year.toString() else mes.format(DateTimeFormatter.ofPattern("MMMM 'de' uuuu", brasil))
        tela.totais.text = moeda(totais.saldo)
        tela.previsto.text = "Previsto   " + moeda(previsto)
        tela.receitas.text = moeda(totais.receitas)
        tela.despesas.text = moeda(totais.despesas)
        tela.alternar.text = if (anual) "Ver resumo mensal" else "Ver resumo anual"
        val mensal = ResumoFinanceiro.calcular(movimentos, mes.year, mes.monthValue).totais
        val faixa = Cofrinho.avaliar(mensal.receitas, mensal.saldo)
        val percentual = if (mensal.receitas.signum() > 0) (mensal.saldo * 100.toBigInteger()).toBigDecimal().divide(mensal.receitas.toBigDecimal(), 1, RoundingMode.HALF_UP).toPlainString().replace('.', ',')+"% restantes · " else ""
        tela.cofrinho.text = "Cofrinho de "+mes.format(DateTimeFormatter.ofPattern("MM/uuuu"))+"\n"+percentual+faixa.mensagem
        val imagem = when (faixa) {
            Cofrinho.CHEIO -> R.drawable.cofrinho_cheio
            Cofrinho.MEDIO -> R.drawable.cofrinho_medio
            Cofrinho.BAIXO -> R.drawable.cofrinho_baixo
            else -> R.drawable.cofrinho_vazio
        }
        tela.cofrinho.setCompoundDrawablesWithIntrinsicBounds(imagem,0,0,0)
        tela.vencimentos.text = pendencias.joinToString("\n") { (nome, p) ->
            nome+" · "+LocalDate.parse(p.vencimento).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))+" · "+p.status(LocalDate.now())
        }.ifEmpty { "Nenhuma conta em aberto neste período." }
    }
}
