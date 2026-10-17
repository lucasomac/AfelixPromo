# AfelixPromo — requisitos de produto

**Status:** especificação inicial do MVP  
**Plataformas:** Android e iOS, com UI compartilhada em Compose Multiplatform
**Arquitetura:** MVVM sobre Clean Architecture nas camadas compartilhadas

## Visão

O AfelixPromo centraliza as lojas parceiras de Bruna Ariane Felix da Silva (Bruna Felix) e as ofertas de afiliados associadas a cada loja. A pessoa usuária encontra uma loja, consulta o desconto e o cupom disponíveis e pode copiar o cupom ou seguir para a loja pelo link de afiliado.

## Público e necessidade

- **Público principal:** pessoas que acompanham Bruna Felix e procuram seus cupons e links de afiliados.
- **Necessidade:** localizar rapidamente uma loja e entender como usar a oferta sem procurar informações espalhadas por diferentes canais.

## Escopo do MVP

- Catálogo público, sem autenticação ou criação de perfil.
- Lista pesquisável de lojas; a seleção de uma loja apresenta as ofertas cadastradas para ela.
- Cada oferta apresenta o desconto e, quando houver cupom, permite copiá-lo.
- Ação separada para abrir o link de afiliado associado à oferta.
- Feedback visível após copiar o cupom e indicação clara de que os links podem ser afiliados.
- Catálogo fornecido como dado local empacotado com o aplicativo. Alterações no catálogo são distribuídas em uma nova versão do app.
- Aplicativo disponível para Android e iOS, com UI compartilhada.
- Layout adaptável a janelas compactas e largas, incluindo tablets e dobráveis; em telas largas, catálogo e detalhe podem permanecer visíveis em painéis separados.
- Separação entre apresentação (Compose e ViewModel), domínio (modelos, contratos e casos de uso) e dados (implementações e parsing).
- Tema compartilhado baseado na paleta da logo da AfelixPromo, com tipografia sans-serif multiplataforma até que uma fonte de marca seja fornecida.

## Fora do escopo do MVP

- Login, perfil, favoritos ou personalização por usuário.
- Cadastro e edição de lojas/ofertas no app, painel administrativo ou ferramenta de importação operada por clientes.
- Firebase/Firestore em produção; a integração é uma evolução futura.
- Categorias, filtros, imagens/logos, validade de cupons, notificações, avaliações ou histórico, pois esses campos e comportamentos não foram confirmados.

## Histórias de usuário e critérios de aceitação

### Descobrir uma loja

**Como cliente, quero procurar uma loja pelo nome para encontrar as ofertas associadas a ela.**

- O catálogo apresenta as lojas conhecidas pelos dados carregados.
- A busca filtra lojas pelo nome e atualiza os resultados conforme o texto é digitado.
- Uma busca sem correspondências informa que nenhum resultado foi encontrado e permite limpar a busca.
- Selecionar uma loja abre sua tela de detalhe.

### Consultar uma oferta

**Como cliente, quero ver o desconto e o cupom de uma loja para decidir se quero usar a oferta.**

- O detalhe da loja exibe cada oferta vinculada a ela e os valores/tipos de desconto fornecidos na fonte.
- O cupom e o desconto são apresentados sem alterar seu significado ou inventar condições não presentes nos dados.
- Ofertas sem cupom, se existirem no JSON, não exibem uma ação de cópia. A aceitação desse caso será confirmada ao inspecionar os dados reais.

### Copiar um cupom

**Como cliente, quero copiar o cupom sem sair da tela para poder usá-lo na loja.**

- A ação de copiar fica separada da ação de abrir a loja.
- Após copiar, a interface confirma a ação de maneira acessível e não depende apenas de uma mudança de cor.
- O texto copiado corresponde exatamente ao cupom exibido.

### Abrir a loja

**Como cliente, quero abrir a loja pelo link disponibilizado para chegar à oferta.**

- A ação abre o destino do link de afiliado associado à oferta usando o mecanismo de links do sistema operacional.
- Links inválidos não são tratados como uma abertura bem-sucedida; a interface informa que não foi possível abrir a oferta.
- A interface informa, de forma concisa e próxima às ofertas, que alguns links são links de afiliado.

## Requisitos funcionais

1. **Catálogo:** carregar lojas e ofertas de uma fonte local compartilhada entre Android e iOS.
2. **Agrupamento:** associar ofertas à loja correspondente usando o nome presente nos dados; regras para nomes duplicados ou variações de escrita dependem da inspeção do JSON.
3. **Busca:** buscar lojas por nome, sem exigir conta ou conexão com a internet.
4. **Ações:** copiar o cupom e abrir o link de afiliado como ações distintas.
5. **Falhas de catálogo:** se os dados locais não puderem ser lidos ou validados, apresentar um estado de erro explícito; não substituir silenciosamente a falha por uma lista vazia.

## Requisitos de qualidade

- Paridade funcional da experiência entre Android e iOS.
- Conteúdo essencial legível com ampliação de fonte e tecnologias assistivas.
- Ações principais com áreas de toque adequadas às recomendações das plataformas.
- Busca e leitura do catálogo local devem funcionar sem rede.
- Links externos devem ser validados antes da abertura e apresentados de forma transparente como links de afiliado.

## Questões pendentes

- Confirmar se o formato e os valores do JSON atual permanecerão estáveis; a primeira versão deverá rejeitar arquivos incompatíveis em vez de ignorar campos ou registros.
- Confirmar se uma loja pode ter mais de uma oferta, se o cupom pode ficar vazio e como representar descontos sem cupom.
- Definir o tratamento de nomes repetidos ou variantes do nome da mesma loja.
- Confirmar se a fonte de dados terá validade, condições, categorias ou imagens em uma versão futura.
- Confirmar se existe uma fonte de marca a incorporar futuramente.
- Definir critérios de publicação e atualização do catálogo local.
