package br.edu.unipe.meubolso.ui.movimentacoes

import java.io.Serializable
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

enum class TipoMovimentacao(val rotulo: String) { RECEITA("Receita"), DESPESA("Despesa") }

enum class Categoria(val rotulo: String) {
    ALIMENTACAO("Alimentação"), TRANSPORTE("Transporte"), LAZER("Lazer"),
    ESTUDOS("Estudos"), OUTROS("Outros")
}

// Dados trocados entre o formulário e a tela que o abriu.
data class RascunhoMovimentacao(
    val descricao: String,
    val centavos: Long,
    val tipo: TipoMovimentacao,
    val categoria: Categoria,
    val data: LocalDate
) : Serializable

object ValorEmReais {
    private val formato = Regex("""(?:[0-9]+|[0-9]{1,3}(?:\.[0-9]{3})+)(?:,[0-9]{1,2})?""")

    fun lerCentavos(texto: String): Long? {
        val entrada = texto.trim()
        if (!formato.matches(entrada)) return null
        val decimal = BigDecimal(entrada.replace(".", "").replace(',', '.'))
        return try {
            decimal.movePointRight(2).longValueExact().takeIf { it > 0 }
        } catch (_: ArithmeticException) {
            null
        }
    }

    fun paraCampo(centavos: Long): String =
        BigDecimal.valueOf(centavos, 2).toPlainString().replace('.', ',')

    fun paraExibir(centavos: Long): String =
        NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
            .format(BigDecimal.valueOf(centavos, 2))
}
