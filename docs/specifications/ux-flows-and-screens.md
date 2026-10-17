# AfelixPromo — especificação de UX/UI

## Princípios

- **Encontrar antes de promover:** a busca por loja é a entrada principal; não exigir navegação por categorias ainda inexistentes.
- **Ação explícita:** copiar o cupom e abrir a loja são controles separados, com rótulos claros.
- **Transparência:** informar que os links podem gerar comissão para Bruna Felix sem esconder essa informação em uma tela distante.
- **Fidelidade aos dados:** mostrar apenas desconto, cupom e destino recebidos da fonte; não sugerir validade ou condições que não foram fornecidas.
- **Identidade visual:** usar a paleta de azul-marinho, roxo, dourado e magenta extraída da logo da AfelixPromo, aplicada por um tema Material 3 compartilhado. A tipografia usa sans-serif multiplataforma até que uma fonte de marca seja fornecida.

## Arquitetura de informação

```text
Catálogo de lojas
└── Detalhe da loja
    └── Uma ou mais ofertas
        ├── Desconto
        ├── Cupom (se disponível)
        ├── Copiar cupom (se disponível)
        └── Acessar loja (link de afiliado)
```

Não há abas, favoritos, filtros por categoria nem área de conta no MVP.

## Fluxo principal

1. A pessoa abre o app e vê o catálogo de lojas e a busca.
2. Digita o nome de uma loja ou percorre a lista.
3. Seleciona uma loja para ver as ofertas associadas.
4. Opcionalmente copia um cupom; o app confirma a cópia e mantém a pessoa na tela.
5. Opcionalmente toca em **Acessar loja**; o sistema abre o destino externo e o app não deve alegar que a compra foi concluída.

## Tela: catálogo de lojas

- Título de tela com o nome do app, sem exigir um logotipo final.
- Campo de busca com rótulo acessível, dica para buscar lojas e ação de limpar quando houver texto.
- Lista de lojas derivada dos dados disponíveis. Cada item precisa ter uma área de toque clara e abrir o detalhe.
- Exibir o ícone da loja quando o JSON fornecer uma URL; enquanto carrega ou em caso de falha, mostrar um placeholder arredondado com a inicial do nome.
- Pode ser exibida a quantidade de ofertas por loja, pois ela é derivável do catálogo; não apresentar contagens se os dados ainda não tiverem sido validados.
- Informação curta sobre links de afiliado, visível nesta tela ou em um elemento persistente de informação acessível.

**Estados**

- **Catálogo carregado:** lista de lojas.
- **Busca sem resultados:** mensagem específica e ação para limpar a busca.
- **Catálogo vazio válido:** mensagem informativa distinta de erro.
- **Falha ao carregar/validar:** mensagem de erro explícita; não mascarar a falha como catálogo vazio.

## Tela: detalhe da loja

- Nome da loja como título.
- Lista de ofertas da loja, cada uma em um cartão ou seção visualmente separada.
- Em cada oferta, mostrar o desconto como veio definido pelos dados e, se houver, o cupom.
- Ação **Copiar cupom** apenas quando houver um valor de cupom utilizável.
- Ação **Acessar loja** associada ao link específico da oferta.
- Aviso de transparência sobre links de afiliado, próximo às ações de saída.
- Navegação de retorno para a lista de lojas conforme os padrões de cada plataforma.
- No Android, apresentar uma seta de retorno no padrão da plataforma e fazer o botão/gesto de voltar do sistema retornar ao catálogo; no catálogo, manter o comportamento padrão de saída do Android.
- No iOS, preservar o controle textual de retorno existente.

**Estados e feedback**

- Confirmação de cópia via mensagem acessível (por exemplo, snackbar) e anúncio para leitor de tela.
- Se a abertura do destino falhar, informar a falha sem apagar o cupom ou bloquear o retorno.
- Se uma loja não tiver ofertas válidas, apresentar um estado vazio próprio; não criar dados de exemplo em produção.

## Busca, ordenação e agrupamento

- Busca inicial por nome de loja; não pesquisar cupons, pois o objetivo selecionado é encontrar a loja primeiro.
- A busca deve ignorar diferenças entre maiúsculas/minúsculas. Normalização de acentos e regras de correspondência devem ser testadas com nomes reais em português.
- A ordenação das lojas ainda precisa ser confirmada ao conhecer o catálogo. A ordem alfabética é uma opção inicial previsível, não uma regra de produto aprovada.
- A tela agrupa ofertas pelo campo `store` do JSON; a regra para grafias variantes depende da fonte.

## Interação com recursos do sistema

- **Clipboard:** copiar exatamente o texto do cupom; oferecer confirmação perceptível sem interromper o fluxo.
- **Link:** validar o esquema e o destino antes de solicitar abertura ao sistema; preferir os handlers do sistema para URLs web/links universais.
- **Erro ao abrir:** não alegar sucesso se não houver handler ou se a solicitação falhar.
- **Rede:** navegar no catálogo não depende de conectividade, pois os dados iniciais são locais.

## Diretrizes visuais e acessibilidade

- Usar componentes Material 3 compartilhados, espaçamento consistente e o tema da marca definido em `presentation/theme/`; oferecer esquemas claro e escuro com contraste adequado.
- Não depender apenas de cor, ícones ou movimento para transmitir estado ou ação.
- Usar semântica e rótulos apropriados para busca, cópia, abertura de link e feedback.
- Preservar contraste de texto e estados de foco/seleção, suportar ampliação de fonte e não truncar cupom ou informação essencial.
- As áreas interativas devem respeitar os tamanhos mínimos recomendados pelas plataformas; validar em Android e iOS.
- Validar layout em telas compactas e largas, orientação e escalas de texto antes de implementar a UI final.

## Decisões em aberto

- Confirmar se existe uma fonte de marca a incorporar futuramente sem alterar os fluxos.
- Confirmar ordenação inicial e se a contagem de ofertas será mostrada.
- Confirmar se o JSON terá lojas sem cupom, ofertas duplicadas ou mais de uma oferta por loja.
- Definir a redação final do aviso de afiliados com a responsável pela marca.
