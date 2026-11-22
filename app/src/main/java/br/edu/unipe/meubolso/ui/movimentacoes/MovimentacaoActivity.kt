package br.edu.unipe.meubolso.ui.movimentacoes

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.databinding.TelaMovimentacaoBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MovimentacaoActivity : AppCompatActivity() {
    private lateinit var tela: TelaMovimentacaoBinding
    private var data = LocalDate.now()
    private val categorias = Categoria.values()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tela = TelaMovimentacaoBinding.inflate(layoutInflater)
        setContentView(tela.root)
        val existente = FormularioMovimentacao.ler(intent)
        tela.titulo.setText(if (existente == null) R.string.nova_movimentacao else R.string.editar_movimentacao)

        tela.categoria.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            categorias.map { it.rotulo })
        if (savedInstanceState == null && existente != null) {
            tela.descricao.setText(existente.descricao)
            tela.valor.setText(ValorEmReais.paraCampo(existente.centavos))
            tela.tipo.check(if (existente.tipo == TipoMovimentacao.RECEITA) R.id.receita else R.id.despesa)
            tela.categoria.setSelection(categorias.indexOf(existente.categoria))
        }
        val dataSalva = savedInstanceState?.getString("data_formulario")
        mostrarData(dataSalva?.let(LocalDate::parse) ?: existente?.data ?: LocalDate.now())
        tela.data.setOnClickListener {
            DatePickerDialog(this, { _, ano, mes, dia -> mostrarData(LocalDate.of(ano, mes + 1, dia)) },
                data.year, data.monthValue - 1, data.dayOfMonth).show()
        }
        tela.confirmar.setOnClickListener { confirmar() }
        tela.cancelar.setOnClickListener { finish() }
    }

    private fun confirmar() {
        val descricao = tela.descricao.text.toString().trim()
        val centavos = ValorEmReais.lerCentavos(tela.valor.text.toString())
        tela.campoDescricao.error = if (descricao.isEmpty()) getString(R.string.erro_descricao) else null
        tela.campoValor.error = if (centavos == null) getString(R.string.erro_valor) else null
        if (descricao.isEmpty()) {
            tela.descricao.requestFocus()
            return
        }
        if (centavos == null) {
            tela.valor.requestFocus()
            return
        }
        val tipo = if (tela.receita.isChecked) TipoMovimentacao.RECEITA else TipoMovimentacao.DESPESA
        val dados = RascunhoMovimentacao(descricao, centavos, tipo, categorias[tela.categoria.selectedItemPosition], data)
        setResult(RESULT_OK, FormularioMovimentacao.resultado(dados))
        finish()
    }

    private fun mostrarData(nova: LocalDate) {
        data = nova
        tela.data.text = nova.format(DateTimeFormatter.ofPattern("dd/MM/uuuu"))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("data_formulario", data.toString())
    }
}
