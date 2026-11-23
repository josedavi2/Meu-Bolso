package br.edu.unipe.meubolso.ui.contas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import br.edu.unipe.meubolso.databinding.TelaContasFixasBinding
import br.edu.unipe.meubolso.dados.MeuBolso
import br.edu.unipe.meubolso.ui.movimentacoes.ValorEmReais
import java.time.*
import java.time.format.DateTimeFormatter

class ContasFixasActivity : AppCompatActivity() {
    private lateinit var tela: TelaContasFixasBinding
    private lateinit var mes: YearMonth
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mes = YearMonth.parse(state?.getString("mes") ?: intent.getStringExtra("mes") ?: YearMonth.now().toString())
        tela = TelaContasFixasBinding.inflate(layoutInflater)
        setContentView(tela.root)
        tela.anterior.setOnClickListener { mes = mes.minusMonths(1); mostrar() }
        tela.seguinte.setOnClickListener { mes = mes.plusMonths(1); mostrar() }
        tela.nova.setOnClickListener { abrir(null) }
    }
    private fun abrir(id: String?) {
        startActivity(Intent(this, DetalhesContaActivity::class.java).putExtra("id", id).putExtra("mes", mes.toString()))
    }
    override fun onSaveInstanceState(state: Bundle) { super.onSaveInstanceState(state); state.putString("mes", mes.toString()) }
    override fun onResume() { super.onResume(); mostrar() }
    private fun mostrar() {
        val app = MeuBolso.app
        app.dao.preparar(app.perfil, mes)
        tela.periodo.text = mes.format(DateTimeFormatter.ofPattern("MM/uuuu"))
        val linhas = mutableListOf<Pair<String?, String>>()
        for (conta in app.dao.contas(app.perfil)) {
            val parcela = app.dao.parcelas(conta.id).find { it.mes == mes.toString() } ?: continue
            val status = parcela.status(LocalDate.now())
            val icone = when (status) { "Pago" -> "✓"; "Vencido" -> "!"; else -> "◷" }
            val rotulo = conta.descricao+" · "+ValorEmReais.paraExibir(parcela.valor)+"\n"+icone+" "+status+" · "+LocalDate.parse(parcela.vencimento).format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
            linhas.add(conta.id to rotulo)
        }
        if (linhas.isEmpty()) linhas.add(null to "Nenhuma conta fixa neste mês.")
        tela.lista.layoutManager = LinearLayoutManager(this)
        tela.lista.adapter = object : RecyclerView.Adapter<Linha>() {
            override fun getItemCount() = linhas.size
            override fun onCreateViewHolder(parent: ViewGroup, tipo: Int) = Linha(Button(parent.context))
            override fun onBindViewHolder(holder: Linha, posicao: Int) {
                val (id, texto) = linhas[posicao]
                holder.botao.isAllCaps = false
                holder.botao.text = texto
                holder.botao.isEnabled = id != null
                holder.botao.setOnClickListener { abrir(id) }
            }
        }
    }
    private class Linha(val botao: Button) : RecyclerView.ViewHolder(botao)
}
