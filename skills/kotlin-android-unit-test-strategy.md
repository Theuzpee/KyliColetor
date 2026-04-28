---
name: kotlin-android-unit-test-strategy
description: Estratégia abrangente de testes unitários para projetos Android Kotlin usando Jetpack Compose, MVVM e Clean Architecture.
---

# Estratégia de Teste Unitário Kotlin Android

## 🎯 Objetivo
Garantir que testes unitários abrangentes, confiáveis e sustentáveis sejam escritos para todas as camadas (Domínio, Dados, Apresentação) de um projeto Android Kotlin desde o início.

## 📋 Gatilho / Ativação
**Anunciar no início:** "Estou utilizando a skill de Estratégia de Teste Unitário Kotlin Android para implementar testes unitários abrangentes para seus componentes."

## 🏗️ Arquitetura & Stack
- **Frameworks de Teste:** JUnit 5, MockK (ou Fakes customizados), Testes de Coroutines Kotlinx
- **Teste de UI:** Compose Test Rule
- **Camadas Alvo:** Domínio (Casos de Uso), Dados (Repositórios/DAOs), Apresentação (ViewModels/Compose), Hardware (Broadcast Receivers)

## 📜 Diretrizes Principais (CRÍTICO)

### 1. Princípios Gerais de Teste
- **Teste Cedo:** Escreva testes antes ou imediatamente após a implementação do código.
- **Teste as Fronteiras:** Foque em interfaces, APIs públicas e implementações de contratos.
- **Fakes em vez de Mocks:** Prefira implementações Fake manuais de interfaces em vez de frameworks de mocking baseados em reflexão sempre que possível.
- **Coroutines:** Sempre use `runTest` e `UnconfinedCoroutineDispatcher` (ou `TestDispatcher` padrão) para testar coroutines.

### 2. Teste da Camada de Domínio (Casos de Uso & Entidades)
- **Cobertura:** Teste todas as combinações de entrada, regras de negócio, tratamento de erros e casos de borda.
- **Dependências:** Injete repositórios Fake. SEM dependências Android.
- **Verificação:** Garanta que o Caso de Uso retorne os modelos de domínio corretos ou erros específicos do domínio.

### 3. Teste da Camada de Dados (Repositórios & DAOs)
- **Repositórios:** Teste a lógica de cache, mapeamento de dados, estratégias de fallback e tratamento de erros. Use Mocks/Fakes para fontes de dados remotas.
- **Room DAOs:** Use um banco de dados em memória (`Room.inMemoryDatabaseBuilder`). Teste todas as consultas, restrições e casos de borda.
- **Transações:** Verifique se as transações do banco de dados ocorrem de forma atômica (sucesso ou falha total).

### 4. Teste da Camada de Apresentação (ViewModels & Compose)
- **ViewModels:** Teste as transições de estado (`StateFlow`), o tratamento de eventos e a interação com Casos de Uso.
- **Escopo:** Valide os escopos das coroutines e cancelamentos dentro da ViewModel.
- **Compose UI:** Use `composeTestRule`. Teste as interações do usuário, garanta que os dados corretos sejam exibidos e verifique os estados de erro/carregamento.

### 5. Teste de Integração de Hardware (Broadcast Receivers)
- **Mock de Intent:** Simule a criação e entrega de `Intent`.
- **Parsing de Dados:** Teste a lógica de análise de dados do scanner DataWedge (formatos válidos e inválidos).
- **Ciclo de Vida:** Verifique a lógica adequada de registro e remoção (mesmo que testada em nível de componente).

## 🛠️ Detalhes de Implementação
- **Testar Coroutines:** 
  ```kotlin
  @Test
  fun example() = runTest(UnconfinedCoroutineDispatcher()) { ... }
  ```
- **Testar Compose:**
  ```kotlin
  @get:Rule val composeTestRule = ComposeTestRule()
  // ...
  composeTestRule.onNodeWithText("Carregando...").assertIsDisplayed()
  ```

## 🚫 Anti-Patterns (NUNCA FAÇA ISSO)
- Não escreva testes que acessem APIs de produção reais ou bancos de dados persistentes.
- Não pule o teste de casos de borda ou estados de erro.
- Não use `Thread.sleep` em testes; use as funções adequadas de avanço de tempo de teste de coroutines.
- Não teste detalhes de implementação (métodos privados); teste a API pública e o estado.

## ✅ Checklist de Verificação
- [ ] Caminhos de sucesso e falha estão testados?
- [ ] As Coroutines são testadas usando `runTest`?
- [ ] Os testes de banco de dados Room usam um banco em memória?
- [ ] Os testes de Compose verificam os estados da UI (carregando, sucesso, erro)?
- [ ] As lógicas de parsing de hardware são testadas de forma robusta com entradas inválidas?