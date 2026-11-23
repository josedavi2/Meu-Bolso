# Meu Bolso · demonstração em dupla

**Vitória abre o app.** “Criamos o Meu Bolso para quem quer acompanhar entradas, gastos e contas recorrentes pelo celular. Cada perfil mantém seus lançamentos no aparelho.” Crie um perfil de demonstração, entre no painel e mantenha o mês atual selecionado.

**José Davi registra duas movimentações.** Lance uma receita “Bolsa” de R$ 1.000,00 e uma despesa “Mercado” de R$ 750,00 na categoria Alimentação. Mostre no painel o saldo de R$ 250,00 e os 25% restantes. Abra o histórico, filtre por despesa e abra o relatório do mês para mostrar a categoria com maior gasto. Em “Ano inteiro”, aponte o mês dos lançamentos no gráfico de barras.

**Vitória cadastra a conta fixa “Internet”.** Use R$ 90,00, categoria Outros e o dia de hoje como vencimento. Na lista, mostre a parcela pendente. No painel, compare o saldo atual de R$ 250,00 com o previsto de R$ 160,00. Em Android 13 ou superior, toque em “Ativar lembretes de vencimento” e conceda a permissão para demonstrar o aviso. A execução do trabalho em segundo plano depende do Android. A demonstração também pode seguir pela lista de contas.

**A dupla confirma o pagamento.** José abre a parcela e toca em “Ainda não paguei”. Vitória confere que o saldo continua em R$ 250,00. Abra a mesma parcela, toque em “Já paguei” e escolha a data de hoje. O saldo passa a R$ 160,00 e o histórico ganha uma despesa de R$ 90,00. Volte aos detalhes e demonstre “Desfazer pagamento”. O saldo volta a R$ 250,00.

**José explica a integração.** “A parcela mensal guarda o identificador da despesa. O Room registra as duas mudanças em uma transação e o relatório soma os lançamentos pela data em que o pagamento aconteceu.” Feche o aplicativo e abra novamente para mostrar os registros persistidos. Entre em outro perfil e confira o histórico vazio desse perfil.

**Vitória encerra com os testes.** Mostre os resultados de `testDebugUnitTest`. Os casos de integração cobrem fevereiro, repetição do pagamento, separação por perfil, seleção dos lembretes e atualização do banco antigo com preservação de dados.

## Reproduzir no Android Studio

Na raiz do projeto, execute `./gradlew assembleDebug testDebugUnitTest` no Git Bash ou `.\gradlew.bat assembleDebug testDebugUnitTest` no PowerShell. O relatório de testes fica em `app/build/reports/tests/testDebugUnitTest/index.html`.
