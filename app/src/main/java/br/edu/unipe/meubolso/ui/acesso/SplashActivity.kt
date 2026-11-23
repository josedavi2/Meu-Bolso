package br.edu.unipe.meubolso.ui.acesso
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.dados.MeuBolso
import br.edu.unipe.meubolso.ui.painel.PainelActivity
import kotlinx.coroutines.*

class SplashActivity : AppCompatActivity(R.layout.tela_splash) {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        findViewById<android.widget.TextView>(R.id.logo).setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.cofrinho_cheio,0,0)
        lifecycleScope.launch {
            delay(600)
            val destino = if (MeuBolso.app.perfil.isEmpty()) LoginActivity::class.java else PainelActivity::class.java
            startActivity(Intent(this@SplashActivity, destino))
            finish()
        }
    }
}
