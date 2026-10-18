# Changelog - AfelixPromo

Todas as alterações notáveis e versões liberadas do aplicativo **AfelixPromo** são documentadas neste arquivo.

---

## [1.1.7] - 2026-10-18
### Ajustes e Documentação
- **Política de Privacidade:** Atualização do texto e padronização do idioma para maior clareza e conformidade.
- **Documentação Técnica:** Adicionada especificação detalhada sobre o modo de exibição de lojas e o teste A/B via Firebase Remote Config.

---

## [1.1.6] - 2026-10-18
### DevOps & Deploy
- **Assinatura AAB:** Aprimoramento no processo de assinatura de pacotes Android (`.aab`) no script de deploy e fluxos de CI.

---

## [1.1.5] - 2026-10-18
### DevOps & Certificados
- **Gestão de Chaves e Certificados:** Melhorias na decodificação do Keystore do Android e suporte a arquivos P12 base64 para o pipeline de iOS nos scripts de deploy.

---

## [1.1.4] - 2026-10-18
### Configuração de Versão & CI
- **Versões e Configurações:** Atualização do `versionCode` para 4 e ajustes na configuração de versão do iOS nos fluxos de Integração Contínua (CI).

---

## [1.1.3] - 2026-10-18
### Automação de CI/CD
- **Workflows:** Atualização dos métodos de decodificação e configuração de variáveis de ambiente para deploy automatizado no Android.

---

## [1.1.2] - 2026-10-18
### Configuração do Firebase & Build
- **Provisionamento:** Adição de etapas automatizadas nos workflows do GitHub Actions para gerar os arquivos de configuração `google-services.json` (Android) e `GoogleService-Info.plist` (iOS).
- **Tooling:** Atualização da versão da action de setup do Java para a v5 nos workflows de CI.

---

## [1.1.1] - 2026-10-18
### DevOps & Deploy Inicial
- **Infraestrutura CI/CD:** Atualização e estabilização dos scripts e workflows de deployment para as plataformas Android e iOS.
