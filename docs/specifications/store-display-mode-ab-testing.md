# Especificação: Modo de Exibição de Lojas e Teste A/B via Firebase Remote Config

Este documento descreve a arquitetura, a implementação técnica e o guia operacional para o teste A/B do layout de exibição de lojas (Lista vs. Grid) no aplicativo **AfelixPromo**.

---

## 1. Visão Geral do Recurso

O AfelixPromo suporta dois modos visuais para a listagem de lojas parceiras:
1. **Lista (`StoreDisplayMode.LIST`):** Layout vertical tradicional com uma única coluna (`LazyColumn`), exibindo o ícone da loja, o nome e a contagem de ofertas em formato de linha.
2. **Grade (`StoreDisplayMode.GRID`):** Layout em grade de cartões com duas colunas (`LazyVerticalGrid`), otimizado para navegação visual rápida e engajamento.

A alternância entre os modos é dinâmica e controlada por **Firebase Remote Config**, permitindo a execução de testes A/B em tempo real sem novas versões na loja de aplicativos.

---

## 2. Arquitetura e Implementação Técnica

### 2.1. Modelo de Domínio/UI (`StoreDisplayMode`)
O modo de exibição é fortemente tipado por meio do enum `StoreDisplayMode`:
- Localização: `shared/src/commonMain/kotlin/br/com/lucolimac/afelixpromo/presentation/catalog/StoreDisplayMode.kt`
- Valores suportados:
  - `LIST ("list")`
  - `GRID ("grid")`

### 2.2. Integração com Firebase Remote Config
- **Valores Padrão (`RemoteConfigDefaults`):**
  - Localização: `shared/src/commonMain/kotlin/br/com/lucolimac/afelixpromo/data/service/RemoteConfigDefaults.kt`
  - Garante o fallback local (`"list"`) em modo offline ou na primeira inicialização.
- **Serviço Multiplatform (`RemoteConfigService`):**
  - **Android:** Implementado em `RemoteConfigService.android.kt` utilizando o SDK nativo do Firebase Remote Config (`fetchAndActivate`).
  - **iOS:** Implementado em `RemoteConfigService.ios.kt` utilizando o SDK nativo via SwiftPM / Objective-C bindings (`FIRRemoteConfig`).
- **ViewModel (`CatalogViewModel`):**
  - Realiza a chamada assíncrona do Remote Config na inicialização e atualiza o estado reativo (`CatalogUiState.displayMode`).

---

## 3. Guia de Configuração do Teste A/B no Console do Firebase

Para conduzir o experimento de engajamento no Console do Firebase, siga o procedimento abaixo:

1. **Acesso ao Console:**
   - Abra o [Console do Firebase](https://console.firebase.google.com/) e selecione o projeto **AfelixPromo**.
2. **Configuração do Remote Config:**
   - Navegue até **Engajamento** > **Remote Config**.
   - Certifique-se de que o parâmetro base está criado:
     - **Chave do parâmetro:** `store_display_mode`
     - **Tipo de valor:** Cadeia de caracteres (`String`)
     - **Valor padrão:** `list`
   - Publique as alterações.
3. **Criação do Experimento (A/B Testing):**
   - No menu lateral, acesse **Engajamento** > **A/B Testing**.
   - Clique em **Criar experimento** e selecione **Remote Config**.
   - **Detalhes do Experimento:**
     - **Nome:** Teste de Layout da Lista de Lojas (Lista vs. Grid)
     - **Aplicativo:** Selecione o aplicativo correspondente (Android / iOS).
     - **Porcentagem de público-alvo:** Ex: 50% dos usuários.
   - **Variantes:**
     - **Controle:** `store_display_mode` = `list`
     - **Variante A:** `store_display_mode` = `grid`
   - **Métrica principal:** Cliques em ofertas/cupons ou conversões de links de afiliados.
4. **Início do Experimento:**
   - Reveja as configurações e clique em **Iniciar experimento**. O SDK do aplicativo receberá as variantes dinamicamente e adaptará a interface conforme a segmentação do usuário.
