package br.edu.unipe.meubolso.ui.movimentacoes

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.IntentCompat

class FormularioMovimentacao : ActivityResultContract<RascunhoMovimentacao?, RascunhoMovimentacao?>() {
    override fun createIntent(context: Context, input: RascunhoMovimentacao?): Intent =
        Intent(context, MovimentacaoActivity::class.java).putExtra(CHAVE, input)

    override fun parseResult(resultCode: Int, intent: Intent?): RascunhoMovimentacao? =
        if (resultCode == Activity.RESULT_OK && intent != null) ler(intent) else null

    companion object {
        private const val CHAVE = "br.edu.unipe.meubolso.MOVIMENTACAO"
        fun ler(intent: Intent): RascunhoMovimentacao? =
            IntentCompat.getSerializableExtra(intent, CHAVE, RascunhoMovimentacao::class.java)
        fun resultado(dados: RascunhoMovimentacao): Intent = Intent().putExtra(CHAVE, dados)
    }
}
