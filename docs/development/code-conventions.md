# Convenções de código

## KDoc obrigatório

Todo código novo gerado ou introduzido no projeto deve ser acompanhado de KDoc. Documente classes, interfaces, funções, propriedades expostas e componentes de UI; inclua `@param`, `@property`, `@return` e `@throws` quando ajudarem a explicar o contrato.

- Escreva a documentação junto com a declaração no mesmo change.
- Descreva propósito, comportamento e restrições observáveis, não apenas repita o nome do símbolo.
- Documente também declarações privadas quando a intenção ou as regras não forem óbvias.
- Em implementações `actual` de uma API `expect`, mantenha KDoc que esclareça o comportamento específico da plataforma.
- Ao alterar uma declaração documentada, atualize seu KDoc para refletir o comportamento novo.
- Comentários de linha podem complementar o KDoc para explicar decisões locais, mas não substituem a documentação da declaração.

## Organização de composables

- Mantenha um composable principal por arquivo e use o nome do arquivo correspondente ao composable, para que a responsabilidade fique clara ao navegar pelo projeto.
- Helpers pequenos e privados, usados apenas pelo composable principal, podem permanecer no mesmo arquivo.
- Extraia componentes compartilhados ou com responsabilidade visual independente para arquivos próprios.
- Evite sobrecarregar o nome de um composable para telas diferentes; use nomes que identifiquem a tela ou finalidade, como `StoreListScreen` e `StoreOffersScreen`.

## Strings de interface

- Mantenha textos exibidos ao usuário em `shared/src/commonMain/composeResources/values/strings.xml` e leia-os com os recursos Compose para manter Android e iOS consistentes.
- Textos restritos à implementação Android devem ficar em `shared/src/androidMain/res/values/strings.xml`; use recursos equivalentes da plataforma para textos exclusivos de iOS.
- Não coloque textos visíveis, mensagens de feedback ou descrições acessíveis diretamente em composables ou no `ViewModel`. Emita efeitos tipados e resolva a string na camada de UI.
- Mensagens técnicas internas de parsing e validação podem permanecer na camada de dados, mas não devem ser mostradas diretamente como texto da interface.
