# AfelixPromo — dados e arquitetura

## Contexto atual

O projeto é um esqueleto Kotlin Multiplatform com targets Android e iOS. A UI Compose está em `shared/src/commonMain`; os pontos de entrada Android e iOS delegam para o componente compartilhado. Ainda não há CSV no repositório nem integração Firebase.

## Fonte inicial

- O CSV informado contém estes conceitos: nome da loja, cupom, link, valor do desconto e tipo do desconto.
- O CSV real ainda não está disponível no repositório. Cabeçalhos literais, codificação, delimitador, regras de aspas, separadores decimais, valores opcionais e amostras devem ser inspecionados antes de implementar a leitura.
- O catálogo será incluído como dado local do app, compartilhado entre Android e iOS. Atualizações serão distribuídas por uma nova versão.
- A especificação não assume campos de categoria, imagem, descrição, validade ou condições.

## Modelo conceitual

| Entidade | Campo conhecido | Observação |
|---|---|---|
| Loja | Nome | Campo que permite exibir e localizar a loja; a regra para normalizar e agrupar nomes depende do CSV real. |
| Oferta | Cupom | Exibir e copiar quando houver valor utilizável; confirmar se o campo pode ser vazio. |
| Oferta | Link de afiliado | Destino externo associado à oferta. Deve ser validado antes de abrir. |
| Oferta | Valor do desconto | Preservar a informação da fonte. Tipo numérico e regras de formatação dependem do formato real. |
| Oferta | Tipo do desconto | Preservar o significado da fonte; domínio de valores ainda não conhecido. |

Uma linha do CSV representa inicialmente uma associação de loja com uma oferta. Se o arquivo demonstrar outra cardinalidade, ajustar o modelo antes de importar. Identificadores estáveis não estão entre os campos informados; a estratégia para gerar IDs locais e migrar para IDs Firestore será definida após examinar duplicatas e qualidade dos dados.

## Validação e integridade do catálogo

Antes de empacotar o catálogo:

1. Confirmar os cabeçalhos e mapear cada coluna para o modelo conceitual.
2. Confirmar encoding, delimitador, aspas/escapes e regras para linhas vazias.
3. Verificar nomes vazios, ofertas duplicadas, cupons vazios e URLs inválidas.
4. Inspecionar tipos e representações do desconto sem inferir que o valor é percentual ou monetário apenas pelo formato.
5. Resolver o comportamento para linhas inválidas: não descartar registros silenciosamente; falhar a validação de preparação do catálogo ou apresentar erro explícito.
6. Conferir a contagem de linhas importadas e lojas agrupadas contra a fonte antes da publicação.

Os pontos que dependem do CSV permanecem decisões pendentes; esta especificação não inventa valores de exemplo como se fossem dados reais.

## Organização de responsabilidades

Manter a UI independente da forma física do catálogo:

```text
Compose UI (commonMain)
        ↓
Estado/ações de apresentação compartilhados
        ↓
Repositório de lojas e ofertas (contrato compartilhado)
        ↓
Fonte local empacotada (MVP)
        ↓ futuro
Adaptador Firebase Firestore
```

- Modelos compartilhados representam loja e oferta, sem depender de classes do Firebase.
- A tela de catálogo solicita lojas/ofertas por uma API de repositório compartilhada; busca e agrupamento seguem regras explícitas e testáveis.
- A fonte local e o futuro adaptador remoto devem implementar o mesmo contrato de leitura. Não adicionar Firebase SDK nem acoplar modelos de domínio ao Firestore nesta etapa.
- Falhas de parsing, validação ou carregamento devem permanecer distinguíveis de um catálogo vazio; a UI apresenta estado de erro explícito.
- O formato de empacotamento (CSV lido em runtime ou dado transformado durante a preparação) será escolhido após inspecionar o arquivo e validar as opções de recursos multiplataforma já disponíveis.

## Evolução futura para Firestore

- Firestore será uma fonte central remota em uma etapa posterior, depois de confirmar requisitos de atualização, disponibilidade offline e administração do catálogo.
- A aplicação cliente continua somente leitura no escopo atual. Não colocar credenciais administrativas no app e não permitir escrita pública por regras do Firestore.
- Definir coleções/documentos, IDs, índices, regras de acesso, comportamento offline, cache e estratégia de atualização junto com a futura ferramenta administrativa.
- Preservar o contrato do repositório compartilhado para trocar ou combinar fontes sem reescrever a UI.
- Validar o catálogo e as regras de acesso antes de publicar dados reais.

## Questões pendentes antes da implementação de dados

- Obter o CSV e definir uma especificação concreta de importação com exemplos reais e casos-limite.
- Confirmar se podem existir várias ofertas por loja e ofertas sem cupom.
- Definir normalização, deduplicação e identificadores estáveis.
- Decidir formato de empacotamento a partir das características do CSV e dos recursos KMP disponíveis.
- Definir a estratégia de sincronização e administração que justifique a migração a Firestore.
