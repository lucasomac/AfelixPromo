# AfelixPromo

Aplicativo de ofertas e cupons de lojas parceiras da Bruna Felix. A primeira versão oficial, **1.0.0**, está disponível para Android e iOS. O catálogo é carregado do Cloud Firestore.

## Funcionalidades

- Pesquise lojas e consulte seus descontos e cupons.
- Copie um cupom com uma ação e confirmação próprias.
- Abra a loja pelo link associado à oferta, com indicação de que links podem ser afiliados.
- Use o layout adaptável: em janelas largas, catálogo e detalhes ficam lado a lado; em janelas compactas, a navegação permanece sequencial.
- Consulte o catálogo do Firestore; após uma sincronização bem-sucedida, o cache persistente do Firebase permite o acesso offline.

## Plataformas e tecnologias

- Android e iOS com interface compartilhada em Compose Multiplatform.
- Kotlin Multiplatform, MVVM e Clean Architecture nas camadas compartilhadas.
- O app usa os SDKs Firebase nativos em cada plataforma e mapeia os documentos do Firestore em modelos compartilhados.
- `shared/src/commonMain/composeResources/files/catalog.json` é a fonte versionada para importar/atualizar o catálogo; não é fallback do app.

## Executar e testar

Configure o Firebase antes de executar o app:

- Android: coloque `google-services.json` em `androidApp/`.
- iOS: coloque `GoogleService-Info.plist` no target `iosApp` e no bundle do app.
- iOS: a dependência Firebase e o framework Kotlin são integrados ao Xcode por Swift Package Manager e pela integração direta do Kotlin Multiplatform. Para configurar novamente o vínculo inicial:

  ```shell
  XCODEPROJ_PATH="$PWD/iosApp/iosApp.xcodeproj" \
    GRADLE_PROJECT_PATH=":shared" \
    ./gradlew :shared:integrateEmbedAndSign :shared:integrateLinkagePackage
  ```

  Abra `iosApp/iosApp.xcodeproj` no Xcode. O Kotlin Gradle Plugin está em `2.5.0-Beta1` para suportar a integração SwiftPM usada neste projeto.

Use o Android Studio para executar a configuração Android ou os comandos abaixo:

```shell
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

Para importar o catálogo, consulte [Dados e arquitetura](./docs/specifications/data-and-architecture.md). O importador usa credenciais ADC e simula as gravações por padrão; nenhuma credencial administrativa deve ser incluída no repositório.

## Documentação

- [Histórico de alterações](./CHANGELOG.md)
- [Notas da versão 1.0.0](./docs/releases/1.0.0.md)
- [Política de Privacidade](docs/site/privacy.md)
- [Requisitos do produto](./docs/specifications/product-requirements.md)
- [Fluxos e telas](./docs/specifications/ux-flows-and-screens.md)
- [Dados e arquitetura](./docs/specifications/data-and-architecture.md)
- [Regras do Firestore](./firestore.rules)
- [Convenções de desenvolvimento](./docs/development/code-conventions.md)
