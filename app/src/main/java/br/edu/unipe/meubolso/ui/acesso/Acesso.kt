package br.edu.unipe.meubolso.ui.acesso

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.edu.unipe.meubolso.R
import br.edu.unipe.meubolso.dados.*
import br.edu.unipe.meubolso.ui.painel.PainelActivity
import kotlinx.coroutines.*
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.UUID
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

abstract class Acesso(private val cadastro: Boolean) : AppCompatActivity(if (cadastro) R.layout.tela_cadastro else R.layout.tela_login) {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val email = findViewById<EditText>(R.id.email)
        val senha = findViewById<EditText>(R.id.senha)
        val botao = findViewById<TextView>(if (cadastro) R.id.salvar else R.id.entrar)
        if (!cadastro) findViewById<TextView>(R.id.cadastro).setOnClickListener { startActivity(Intent(this, CadastroActivity::class.java)) }
        botao.setOnClickListener {
            val endereco = email.text.toString().trim().lowercase(Locale.ROOT)
            val segredo = senha.text.toString()
            val nome = findViewById<EditText>(R.id.nome)
            val nomeCompleto = nome?.text?.toString()?.trim().orEmpty()
            if (cadastro && nomeCompleto.isBlank()) { nome?.error = "Informe seu nome"; return@setOnClickListener }
            if (!Patterns.EMAIL_ADDRESS.matcher(endereco).matches()) { email.error = "Informe um e-mail válido"; return@setOnClickListener }
            if (segredo.isBlank()) { senha.error = "Informe a senha"; return@setOnClickListener }
            botao.isEnabled = false
            lifecycleScope.launch {
                val app = MeuBolso.app
                val perfil = withContext(Dispatchers.IO) {
                    if (cadastro) {
                        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
                        val usuario = Usuario(UUID.randomUUID().toString(), nomeCompleto, endereco, salt, hash(segredo, salt))
                        usuario.takeIf { app.dao.cadastrar(it) != -1L }
                    } else app.dao.usuario(endereco)?.takeIf { MessageDigest.isEqual(it.hash, hash(segredo, it.salt)) }
                }
                botao.isEnabled = true
                if (perfil == null) {
                    if (cadastro) email.error = "E-mail já cadastrado" else senha.error = "E-mail ou senha incorretos"
                } else {
                    app.perfil = perfil.id
                    startActivity(Intent(this@Acesso, PainelActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                }
            }
        }
    }
    private fun hash(senha: String, salt: ByteArray): ByteArray {
        val chave = PBEKeySpec(senha.toCharArray(), salt, 210000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(chave).encoded } finally { chave.clearPassword() }
    }
}
