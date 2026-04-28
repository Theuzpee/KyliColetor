---
name: contextual-code-review-assistant
description: Diretrizes de revisão de código conscientes do contexto, alinhando o código com o domínio de negócio, arquitetura e documentação.
---

# Assistente de Revisão de Código Contextual

## 🎯 Objetivo
Realizar revisões de código abrangentes, primeiro entendendo o contexto do projeto (domínio, histórias de usuário, diagramas de arquitetura) e garantindo que o código esteja perfeitamente alinhado com as restrições técnicas e regras de negócio estabelecidas.

## 📋 Gatilho / Ativação
**Anunciar no início:** "Estou utilizando a skill de Assistente de Revisão de Código Contextual para analisar o contexto do seu projeto e realizar uma revisão de código abrangente e alinhada à arquitetura."

## 🏗️ Contexto de Arquitetura & Stack
- **Validação de Stack:** Kotlin, Jetpack Compose, MVVM/Clean Architecture, Room Database, Coroutines/StateFlow, Retrofit.
- **Integração de Hardware:** Broadcast Receivers (DataWedge).
- **Restrições de Backend:** Especificações de API NestJS.

## 📜 Diretrizes Principais (CRÍTICO)

### 1. Framework de Compreensão de Contexto
- **Sempre Capturar Contexto:** Antes de revisar, certifique-se de entender o domínio de negócio, as histórias de usuário e as restrições arquiteturais.
- **Analisar Entradas:** Avalie cuidadosamente explicações em texto, diagramas UML, camadas de arquitetura e especificações de API fornecidas.

### 2. Validação de Arquitetura (Clean Architecture)
- **Entidades:** Devem ser regras de negócio puras com ZERO dependências externas.
- **Casos de Uso:** Encapsulam a lógica de negócio. Sem dependências das camadas de UI ou Dados.
- **Repositórios:** Abstraem a camada de dados da camada de domínio.
- **Fontes de Dados:** Devem conter todos os detalhes de implementação (Room, Retrofit).

### 3. Conformidade com o Padrão MVVM
- **ViewModel:** Coordena o estado e a lógica de negócio. Sem referências diretas à UI.
- **Compose UI:** Exibe o estado e manipula eventos do usuário apenas. Sem lógica de negócio.
- **Propagação de Estado:** Garanta o fluxo de estado adequado através da camada de UI usando `StateFlow`.

### 4. Integridade Específica por Camada
- **Domínio:** Entidades imutáveis, casos de uso isolados, SEM referências a camadas externas.
- **Dados:** Implementa interfaces de Clean Architecture, gerencia DAOs do Room, implementa cache offline-first, trata erros de rede do Retrofit.
- **Apresentação:** Melhores práticas de Jetpack Compose, gerenciamento de estado com `StateFlow`, coroutines cientes do ciclo de vida.

### 5. Especificidades de Integração de Hardware
- **Broadcast Receivers:** Verificar o registro/remoção adequados no ciclo de vida.
- **Parsing:** Garantir que os dados do DataWedge sejam analisados corretamente sem bloquear a thread principal.
- **Latência Zero:** Impor requisitos estritos de latência zero para o processamento do scanner.
- **Sem Câmera:** Rejeitar qualquer implementação de scanner que utilize a câmera do dispositivo.

### 6. Alinhamento com Regras de Negócio e Offline-First
- **Isolamento:** Verificar se as regras de negócio estão estritamente isoladas na camada de Domínio.
- **Integridade Offline:** Garantir que as estratégias de sincronização do banco de dados Room estejam implementadas.
- **Mensagens ao Usuário:** Verificar se as mensagens de erro estão alinhadas com os requisitos de negócio e são significativas para os operadores.

## 🚫 Anti-Patterns (NUNCA FAÇA ISSO)
- Revisar código sem entender o contexto de negócio mais amplo.
- Permitir abstrações vazadas (ex: camada de Domínio conhecendo DAOs do Room).
- Aprovar lógica de scanner de hardware que usa a câmera ou bloqueia a thread de UI.

## ✅ Checklist de Verificação
- [ ] O código está alinhado com o contexto de negócio e as histórias de usuário fornecidas?
- [ ] Os limites das camadas de Clean Architecture são mantidos rigorosamente?
- [ ] A lógica de negócio é mantida totalmente fora de Composables e ViewModels?
- [ ] Os requisitos do scanner de hardware (DataWedge, latência zero) são totalmente atendidos?
- [ ] A estratégia offline-first está intacta?