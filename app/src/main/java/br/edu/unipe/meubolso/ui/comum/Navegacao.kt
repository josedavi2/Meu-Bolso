package br.edu.unipe.meubolso.ui.comum

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.ui.historico.HistoricoActivity
import br.edu.unipe.meubolso.ui.painel.PainelActivity
import br.edu.unipe.meubolso.ui.relatorios.RelatoriosActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

fun AppCompatActivity.configurarAbas(atual: Int) {
    val abas = findViewById<BottomNavigationView>(R.id.abas)
    abas.selectedItemId = atual
    abas.setOnItemSelectedListener { item ->
        if (item.itemId != atual) {
            val destino = when (item.itemId) {
                R.id.aba_inicio -> PainelActivity::class.java
                R.id.aba_historico -> HistoricoActivity::class.java
                else -> RelatoriosActivity::class.java
            }
            val intent = Intent(this, destino)
            if (item.itemId == R.id.aba_inicio) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            startActivity(intent)
            if (atual != R.id.aba_inicio) finish()
        }
        item.itemId == atual
    }
}
