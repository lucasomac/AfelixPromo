# AfelixPromo — dados e arquitetura

## Contexto atual

O projeto é Kotlin Multiplatform com targets Android e iOS. A UI Compose está em `shared/src/commonMain`; os pontos de entrada Android e iOS delegam para o componente compartilhado. O catálogo inicial vem de JSON em recursos Compose compartilhados; ainda não há integração Firebase.

## Fonte inicial

- O arquivo inicial é `shared/src/commonMain/composeResources/files/catalog.json`. É um array de ofertas com os campos `store`, `cupon`, `url`, `discount`, `type` e `icon`.
- O arquivo atual contém 18 ofertas. Duas URLs não têm esquema (`www.alvapersonalcare.com.br` e `www.aurabeautyclub.com.br`) e são normalizadas para `https://` durante o carregamento. O tipo `Valor` aparece em uma oferta; a unidade monetária não é informada e não deve ser inferida.
- O campo `icon` é opcional e vazio significa ausência de ícone; o primeiro ícone não vazio da loja é guardado no modelo de domínio.
- O catálogo será incluído como dado local do app, compartilhado entre Android e iOS. Atualizações serão distribuídas por uma nova versão.
- A especificação não assume campos de categoria, imagem, descrição, validade ou condições.

## Modelo conceitual

| Entidade | Campo conhecido | Observação |
|---|---|---|
| Loja | `store` | Campo que permite exibir e agrupar ofertas pelo nome da loja. |
| Loja | `icon` | URL opcional do ícone/logo; valor vazio é tratado como ausente. |
| Oferta | `cupon` | Exibir e copiar quando houver valor utilizável; o campo pode ser omitido ou nulo. |
| Oferta | `url` | Destino externo associado à oferta. Deve ser validado antes de abrir. |
| Oferta | `discount` | Preservar o valor numérico ou textual da fonte sem inferir moeda. |
| Oferta | `type` | Preservar o significado da fonte, por exemplo `Porcentagem` ou `Valor`. |

Cada objeto JSON representa uma oferta associada a uma loja; objetos com o mesmo nome `store` são agrupados. Identificadores estáveis não estão na fonte; o parser gera identificadores locais por nome da loja e posição da oferta. Definir IDs persistentes antes da migração para Firestore.

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

- **Presentation:** `App` e componentes Compose renderizam estado e encaminham eventos. `CatalogViewModel` mantém estado de tela, coordena casos de uso e emite efeitos de UI; não conhece Compose nem APIs de clipboard/URL do sistema. Ícones remotos de loja são carregados na UI multiplataforma e usam placeholder com inicial durante carregamento/erro.
- **Domain:** modelos independentes de plataforma, `CatalogRepository` (contrato), busca, carregamento e validação de URLs como casos de uso. Não depende de Compose, Firebase ou recursos Android/iOS.
- **Data:** `LocalCatalogRepository` implementa o contrato do domínio e usa `CatalogJsonParser` para converter/validar o recurso JSON.
- A criação manual do ViewModel e suas dependências ocorre no ponto de composição da aplicação; manter essa composição simples e explícita até existir necessidade de introduzir um framework de injeção de dependências.
- Efeitos pontuais (copiar cupom, abrir URL, mostrar feedback) são emitidos pelo ViewModel e executados pela UI através de uma interface de ações de plataforma.
- Falhas de parsing, validação ou carregamento permanecem distintas de um catálogo vazio; a ViewModel expõe estado de erro e a UI oferece retry.
- O JSON reside diretamente em `commonMain/composeResources/files/` e é carregado como recurso multiplataforma pelo repositório local.

Implementação atual em `shared/src/commonMain/kotlin/br/com/lucolimac/afelixpromo/`:

- `presentation/catalog/`: `CatalogScreen`, `CatalogUiState` e `CatalogViewModel`.
- `domain/model/`, `domain/repository/` e `domain/usecase/`: regras e contratos independentes de framework.
- `data/catalog/` e `data/repository/`: parser JSON e implementação local do repositório.
- `App.kt`: ponto de composição que conecta o repositório ao ViewModel; os detalhes de plataforma continuam atrás de `PlatformActions`.

## Evolução futura para Firestore

- Firestore será uma fonte central remota em uma etapa posterior, depois de confirmar requisitos de atualização, disponibilidade offline e administração do catálogo.
- A aplicação cliente continua somente leitura no escopo atual. Não colocar credenciais administrativas no app e não permitir escrita pública por regras do Firestore.
- Definir coleções/documentos, IDs, índices, regras de acesso, comportamento offline, cache e estratégia de atualização junto com a futura ferramenta administrativa.
- Implementar o adaptador Firestore na camada `data`, mantendo modelos e contrato do domínio sem dependência do SDK e preservando a apresentação/ViewModel.
- Validar o catálogo e as regras de acesso antes de publicar dados reais.

## Questões pendentes antes da implementação de dados

- Confirmar se podem existir ofertas sem cupom no formato publicado.
- Definir normalização, deduplicação e identificadores estáveis.
- Confirmar se os nomes e tipos dos campos JSON permanecerão estáveis.
- Definir a estratégia de sincronização e administração que justifique a migração a Firestore.
