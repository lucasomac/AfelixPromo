# AfelixPromo

Aplicativo de ofertas e cupons de lojas parceiras da Bruna Felix. A primeira versão oficial, **1.0.0**, está disponível para Android e iOS e inclui um catálogo inicial de 18 lojas e 18 ofertas.

## Funcionalidades

- Pesquise lojas e consulte seus descontos e cupons.
- Copie um cupom com uma ação e confirmação próprias.
- Abra a loja pelo link associado à oferta, com indicação de que links podem ser afiliados.
- Use o layout adaptável: em janelas largas, catálogo e detalhes ficam lado a lado; em janelas compactas, a navegação permanece sequencial.
- Consulte o catálogo empacotado no app sem conexão; imagens remotas e links externos requerem acesso à internet.

## Plataformas e tecnologias

- Android e iOS com interface compartilhada em Compose Multiplatform.
- Kotlin Multiplatform, MVVM e Clean Architecture nas camadas compartilhadas.
- O catálogo inicial é carregado de `shared/src/commonMain/composeResources/files/catalog.json`.

## Executar e testar

Use o Android Studio para executar a configuração Android ou os comandos abaixo:

```shell
./gradlew :androidApp:assembleDebug
./gradlew :shared:testAndroidHostTest
./gradlew :shared:iosSimulatorArm64Test
```

Para executar no iOS, abra `iosApp/iosApp.xcodeproj` no Xcode e escolha um simulador ou dispositivo.

## Documentação

- [Histórico de alterações](./CHANGELOG.md)
- [Notas da versão 1.0.0](./docs/releases/1.0.0.md)
- [Política de Privacidade](./docs/site/privacity.md)
- [Requisitos do produto](./docs/specifications/product-requirements.md)
- [Fluxos e telas](./docs/specifications/ux-flows-and-screens.md)
- [Dados e arquitetura](./docs/specifications/data-and-architecture.md)
- [Convenções de desenvolvimento](./docs/development/code-conventions.md)
