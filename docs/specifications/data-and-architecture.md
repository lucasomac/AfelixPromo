# AfelixPromo — dados e arquitetura

## Contexto atual

O projeto é Kotlin Multiplatform com targets Android e iOS. A UI Compose está em `shared/src/commonMain`; os pontos de entrada Android e iOS delegam para o componente compartilhado. O catálogo de produção é lido da coleção `stores` do Cloud Firestore por SDKs nativos, com mapeamento e validação comuns às duas plataformas. O JSON permanece como fonte de importação e fixture local, não como fallback do app.

## Fonte inicial

- A fonte de importação é `shared/src/commonMain/composeResources/files/catalog.json`. É um array de ofertas com os campos `store`, `cupon`, `url`, `discount`, `type`, `icon` e `description`.
- O arquivo atual contém 19 ofertas. Duas URLs não têm esquema (`www.alvapersonalcare.com.br` e `www.aurabeautyclub.com.br`) e são normalizadas para `https://` durante a importação. A apresentação de descontos do tipo `Valor` usa BRL com formatação pt-BR; valores sem representação numérica são exibidos sem símbolo monetário.
- O campo `icon` é opcional e vazio significa ausência de ícone; o primeiro ícone não vazio da loja é guardado no modelo de domínio.
- O campo `description` é opcional por oferta; quando há conteúdo, o app o apresenta no cartão. Ausências e textos em branco não produzem uma linha no cartão.
- O arquivo continua versionado para permitir importações repetíveis. O app não o empacota como alternativa quando o Firestore estiver indisponível.
- A especificação não assume campos de categoria, imagem, descrição, validade ou condições.

## Modelo conceitual

| Entidade | Campo conhecido | Observação                                                                        |
|----------|-----------------|-----------------------------------------------------------------------------------|
| Loja     | `store`         | Campo que permite exibir e agrupar ofertas pelo nome da loja.                     |
| Loja     | `icon`          | URL opcional do ícone/logo; valor vazio é tratado como ausente.                   |
| Oferta   | `cupon`         | Exibir e copiar quando houver valor utilizável; o campo pode ser omitido ou nulo. |
| Oferta   | `url`           | Destino externo associado à oferta. Deve ser validado antes de abrir.             |
| Oferta   | `discount`      | Preservar o valor da fonte; formatar como BRL somente na apresentação de `Valor`. |
| Oferta   | `type`          | Preservar o significado da fonte, por exemplo `Porcentagem` ou `Valor`.           |
| Oferta   | `description`   | Texto opcional com detalhes/condições da oferta; ausência é válida.               |

Cada objeto JSON representa uma oferta associada a uma loja; objetos com o mesmo nome `store` são agrupados. O importador gera IDs determinísticos a partir do nome normalizado da loja e da posição da oferta na fonte. No Firestore, cada documento `stores/{storeId}` contém os campos `name`, `iconUrl` (opcional) e uma lista `offers`; cada oferta contém `id`, `couponCode` (opcional), `affiliateUrl`, `discountValue`, `discountType`, `description` (opcional) e `sortOrder`.

O app ordena as lojas alfabeticamente pelo nome, sem diferenciar maiúsculas/minúsculas e ignorando diacríticos comuns do alfabeto latino; em nomes equivalentes, usa o nome original e o ID Firestore como desempate determinístico. Não é necessário armazenar ordem de exibição nas lojas.

## Validação e integridade do catálogo

Ao atualizar o catálogo:

1. Conferir que o arquivo contém um array JSON de ofertas com os campos previstos.
2. Verificar nomes vazios, ofertas duplicadas, cupons nulos/vazios e URLs inválidas.
3. Confirmar tipo e representação do desconto sem inferir moeda apenas pelo valor.
4. Validar URLs de ícone não vazias e deixar ausências como string vazia ou `null`.
5. Resolver registros inválidos sem descartar ofertas silenciosamente; o parser deve falhar explicitamente.
6. Conferir contagem de ofertas e lojas agrupadas antes da publicação.

O parser compartilhado rejeita JSON malformado, campos obrigatórios ausentes, nomes de loja ou tipos vazios, descontos booleanos e URLs inválidas. Cupons e ícones ausentes são representados como ausentes. A normalização HTTPS aplica-se a links sem esquema; esquemas diferentes de HTTP(S) são rejeitados.

## Clean Architecture e MVVM

Organizar o código compartilhado em camadas com dependências apontando para o domínio:

```text
presentation (MVVM: Compose UI + ViewModel + UiState/UiEffect)
        ↓ usa
domain (modelos + contratos de repositório + casos de uso)
        ↑ implementa
data (repositórios concretos + parser + fontes de dados)
```

- **Presentation:** `App` e componentes Compose renderizam estado e encaminham eventos. `CatalogViewModel` mantém estado de tela, coordena casos de uso e emite efeitos de UI; não conhece Compose nem APIs de clipboard/URL do sistema. A lista permite atualização por gesto de deslizar para baixo; o ViewModel bloqueia cargas concorrentes, preserva o estado carregado enquanto atualiza e substitui a seleção pelo registro atualizado quando a loja continua disponível. Ícones remotos de loja são carregados na UI multiplataforma e usam placeholder com inicial durante carregamento/erro.
- **Domain:** modelos independentes de plataforma, `CatalogRepository` (contrato), busca, carregamento e validação de URLs como casos de uso. Não depende de Compose, Firebase ou recursos Android/iOS.
- **Data:** `FirestoreCatalogRepository` implementa o contrato do domínio. Serviços Android e iOS consultam a coleção por seus SDKs Firebase nativos; o mapper comum valida o esquema e converte documentos para modelos de domínio. `CatalogJsonParser` valida a fonte JSON usada pelo importador/testes.
- A criação manual do ViewModel e suas dependências ocorre no ponto de composição da aplicação; manter essa composição simples e explícita até existir necessidade de introduzir um framework de injeção de dependências.
- Efeitos pontuais (copiar cupom, abrir URL, mostrar feedback) são emitidos pelo ViewModel e executados pela UI através de uma interface de ações de plataforma.
- Falhas de parsing, validação ou carregamento permanecem distintas de um catálogo vazio; a ViewModel expõe estado de erro e a UI oferece retry.
- O JSON permanece em `commonMain/composeResources/files/` como fonte de importação e fixture dos testes; não é carregado pelo app em produção.

Implementação atual em `shared/src/commonMain/kotlin/br/com/lucolimac/afelixpromo/`:

- `presentation/catalog/`: `CatalogScreen`, `CatalogUiState` e `CatalogViewModel`.
- `domain/model/`, `domain/repository/` e `domain/usecase/`: regras e contratos independentes de framework.
- `data/catalog/`, `data/repository/` e `data/service/`: parser e validador JSON, mapper Firestore, repositório comum e serviços Firebase nativos por plataforma.
- `App.kt`: ponto de composição que conecta o repositório remoto ao ViewModel; os detalhes de plataforma continuam atrás de `PlatformActions`.

## Firestore

- O cliente lê a coleção `stores` em uma consulta inicial e repete a leitura quando a pessoa atualiza a lista com o gesto de deslizar para baixo. O estado de atualização é separado do carregamento inicial e impede requisições simultâneas. As lojas são ordenadas alfabeticamente pelo nome no mapper comum; ofertas são ordenadas pelo `sortOrder` numérico dentro de cada loja (menor primeiro, com ID como desempate). Esse campo permite definir uma sequência editorial de ofertas sem afetar a ordem alfabética das lojas.
- O importador grava `sortOrder` somente nas ofertas, seguindo a ordem em que aparecem no JSON. Documentos antigos de loja podem continuar contendo `sortOrder`; o app não o exige nem o usa, e novas importações deixam de gravá-lo.
- Android usa o SDK Firebase nativo via Gradle; iOS importa FirebaseCore e FirebaseFirestore do Swift Package Manager usando a integração SwiftPM do Kotlin 2.5.0-Beta1 e a integração direta com Xcode. O cache persistente padrão dos SDKs permite leituras offline após a primeira sincronização; sem cache disponível, a falha é exibida e a UI permite tentar novamente.
- O app é somente leitura. As regras em `firestore.rules` permitem leitura pública dos documentos de catálogo e negam qualquer escrita por clientes.
- O importador em `tools/firestore/import_catalog.py` usa Application Default Credentials. A execução é uma simulação por padrão; `--apply` confirma a gravação. Não armazenar credenciais administrativas, arquivos de serviço ou configuração Firebase local no repositório.
- Android requer `androidApp/google-services.json`; iOS requer `GoogleService-Info.plist` incluído no bundle do app. Ambos são arquivos locais ignorados pelo Git. O pacote de vínculo Kotlin/SwiftPM fica em `iosApp/KotlinMultiplatformLinkedPackage/`; abra `iosApp/iosApp.xcodeproj` no Xcode. O projeto não usa CocoaPods.
- Regras e configuração são publicadas com `firebase deploy --only firestore:rules --project <project-id>`. O ID do projeto e as configurações de cliente reais devem ser fornecidos pelo responsável pelo Firebase antes de validar o backend de produção.

## Operação e validação do catálogo

- Instale as dependências do importador com `python3 -m pip install -r tools/firestore/requirements.txt`.
- Autentique a CLI com `gcloud auth application-default login` ou defina `GOOGLE_APPLICATION_CREDENTIALS` apontando para um arquivo local não versionado.
- Revise a simulação antes de publicar: `python3 tools/firestore/import_catalog.py --project <project-id>`.
- Grave os documentos após a revisão: `python3 tools/firestore/import_catalog.py --project <project-id> --apply`.
- As gravações substituem documentos com IDs correspondentes e não removem documentos que tenham sido excluídos da fonte; revise manualmente registros obsoletos antes de qualquer remoção.
- Sem arquivos de configuração Firebase, o app não consegue conectar ao backend e apresenta o estado de erro com opção de retry. Não existe fallback silencioso para o JSON empacotado.
