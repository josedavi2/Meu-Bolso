# Meu Bolso

Projeto inicial de Vitória Luclécia e José Davi para a disciplina de Desenvolvimento Mobile do UNIPÊ.

## Rodar no Android Studio

Extraia o ZIP e abra a pasta **MeuBolso**, onde está o arquivo `settings.gradle.kts`. Use o JDK 17, o Android SDK Platform 35 e o Build-Tools 34.0.0. Após o Gradle Sync, execute o módulo **app** em um emulador ou aparelho com Android 8.0 (API 26) ou superior.

A primeira tela é um menu temporário de desenvolvimento. Toque no nome de uma tela para abrir o layout vazio e use **Voltar** para retornar. Cada integrante pode abrir sua tela mesmo enquanto o outro desenvolve o login ou o banco. Ao integrar a Splash, a dupla pode torná-la a entrada do aplicativo no manifesto.

## Divisão do plano de projeto

| Responsável | Tarefas | Onde programar |
| --- | --- | --- |
| Vitória Luclécia (líder) | Organização e entrega do documento, Splash, login, cadastro, dashboard, saldo, cofrinho, telas de contas fixas e regras de status | Pacotes `ui/acesso`, `ui/painel` e `ui/contas` e seus layouts XML |
| José Davi | Cadastro e edição de movimentações, categorias, histórico, filtros, relatórios e gráficos | Pacotes `ui/movimentacoes`, `ui/historico` e `ui/relatorios` e seus layouts XML |
| Ambos | Modelo do banco na primeira semana (entidades e DAOs), confirmação de pagamento, notificações, testes e apresentação | `dados/entidades`, `dados/dao`, `notificacoes` e `app/src/test` |

O código Kotlin está em `app/src/main/java/br/edu/unipe/meubolso/` e os layouts estão em `app/src/main/res/layout/`. Cada Activity indica seu XML. Room, RecyclerView, Material Components, MPAndroidChart, WorkManager e JUnit já estão declarados no Gradle.

## Primeiro trabalho em dupla

1. Publiquem a base uma vez na branch `main` de um repositório compartilhado. A outra pessoa entra como colaboradora e clona o mesmo repositório.
2. Desenhem as entidades **Usuario**, **Categoria**, **Movimentacao**, **ContaFixa** e **ParcelaConta** conforme a seção 6 do PDF. Combinem os campos e as assinaturas dos DAOs que cada tela vai usar.
3. Cada pessoa cria sua branch de funcionalidade a partir da `main` atualizada. Por exemplo, Vitória pode começar em `feature/login` e José Davi em `feature/movimentacoes`. Cada um integra sua parte por pull request.
