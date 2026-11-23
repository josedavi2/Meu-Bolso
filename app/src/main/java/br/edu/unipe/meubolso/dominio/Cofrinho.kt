package br.edu.unipe.meubolso.dominio

import java.math.BigInteger

enum class Cofrinho(val mensagem: String) {
    SEM_RECEITA("Sem receitas registradas"),
    VAZIO("Seu saldo acabou ou ficou negativo."),
    BAIXO("Atenção ao saldo disponível."),
    MEDIO("Acompanhe seus próximos gastos."),
    CHEIO("Boa reserva neste mês.");

    companion object {
        fun avaliar(receita: BigInteger, saldo: BigInteger): Cofrinho {
            val pontos = saldo * 100.toBigInteger()
            return when {
                receita.signum() == 0 -> SEM_RECEITA
                pontos > receita * 50.toBigInteger() -> CHEIO
                pontos > receita * 20.toBigInteger() -> MEDIO
                pontos.signum() > 0 -> BAIXO
                else -> VAZIO
            }
        }
    }
}
