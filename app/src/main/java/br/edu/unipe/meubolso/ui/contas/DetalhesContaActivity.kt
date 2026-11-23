package br.edu.unipe.meubolso.ui.contas

import br.edu.unipe.meubolso.notificacoes.Lembretes
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import br.edu.unipe.meubolso.dados.*
import br.edu.unipe.meubolso.databinding.TelaDetalhesContaBinding
import br.edu.unipe.meubolso.ui.movimentacoes.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.UUID

class DetalhesContaActivity : AppCompatActivity() {
    private lateinit var tela: TelaDetalhesContaBinding
    private val app get() = MeuBolso.app
    private val mes by lazy { YearMonth.parse(intent.getStringExtra("mes") ?: YearMonth.now().toString()) }
    private var conta: ContaFixa? = null
    private val data = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val perfilAviso = intent.getStringExtra("perfil_aviso")
        if (app.perfil.isBlank() || (perfilAviso != null && perfilAviso != app.perfil)) {
            finish()
            return
        }
        tela = TelaDetalhesContaBinding.inflate(layoutInflater)
        setContentView(tela.root)
        conta = app.dao.contas(app.perfil).find { it.id == intent.getStringExtra("id") }
        if (intent.hasExtra("id") && intent.getStringExtra("id") != null && conta == null) {
            finish()
            return
        }
        tela.categoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, Categoria.values().map { it.rotulo })
        conta?.let {
            tela.descricao.setText(it.descricao)
            tela.valor.setText(ValorEmReais.paraCampo(it.valor))
            tela.dia.setText(it.dia.toString())
            tela.categoria.setSelection(Categoria.valueOf(it.categoria).ordinal)
        }
        tela.salvar.setOnClickListener { salvar() }
        tela.pendente.setOnClickListener { finish() }
        tela.pagar.setOnClickListener {
            val hoje = LocalDate.now()
            DatePickerDialog(this, { _, ano, m, dia ->
                conta?.let {
                    app.dao.pagar(it, mes.toString(), LocalDate.of(ano, m+1, dia))
                    Lembretes.retirar(this, it.id, mes.toString())
                }
                mostrar()
            }, hoje.year, hoje.monthValue-1, hoje.dayOfMonth).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
        tela.desfazer.setOnClickListener {
            conta?.let { c -> app.dao.parcelas(c.id).find { it.mes == mes.toString() }?.movimento?.let { app.dao.excluir(it, app.perfil) } }
            mostrar()
        }
        mostrar()
    }
    private fun salvar() {
        val nome = tela.descricao.text.toString().trim()
        val valor = ValorEmReais.lerCentavos(tela.valor.text.toString())
        val dia = tela.dia.text.toString().toIntOrNull()
        if (nome.isEmpty()) { tela.descricao.error = "Informe a descrição"; return }
        if (valor == null) { tela.valor.error = "Informe um valor positivo"; return }
        if (dia == null || dia !in 1..31) { tela.dia.error = "Use um dia de 1 a 31"; return }
        val anterior = conta
        val nova = ContaFixa(anterior?.id ?: UUID.randomUUID().toString(), app.perfil, nome, valor,
            Categoria.values()[tela.categoria.selectedItemPosition].name, dia, anterior?.inicio ?: mes.toString())
        app.dao.salvarConta(nova, mes, anterior == null)
        if (anterior != null && anterior.dia != dia) Lembretes.retirar(this, nova.id, mes.toString())
        Lembretes.conferirAgora(this)
        finish()
    }
    private fun mostrar() {
        val historico = conta?.let { app.dao.parcelas(it.id) }.orEmpty()
        val atual = historico.find { it.mes == mes.toString() }
        tela.pagar.isVisible = atual != null && atual.movimento == null
        tela.pendente.isVisible = tela.pagar.isVisible
        tela.desfazer.isVisible = atual?.movimento != null
        tela.situacao.text = atual?.let { it.status(LocalDate.now())+" · "+LocalDate.parse(it.vencimento).format(data)+" · "+ValorEmReais.paraExibir(it.valor) }.orEmpty()
        val movimentos = app.dao.movimentos(app.perfil).associateBy { it.id }
        tela.parcelas.text = historico.joinToString("\n") { p ->
            val pagamento = movimentos[p.movimento]?.let { " · pago em "+LocalDate.parse(it.data).format(data) }.orEmpty()
            LocalDate.parse(p.vencimento).format(data)+" · "+p.status(LocalDate.now())+pagamento
        }
    }
}
