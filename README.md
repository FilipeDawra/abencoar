# 🕊️ Projeto Abençoar — Sistema de Gestão e Agendamento

> **Aplicação web em pleno funcionamento operacional**, utilizada no dia a dia de um projeto social para triagem de beneficiários, controle de presença/frequência e gestão de atendimentos por especialidades.

[![Java](https://img.shields.io/badge/Java-21_LTS-orange.svg?style=flat-square&logo=openjdk)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.5-brightgreen.svg?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Production-blue.svg?style=flat-square&logo=postgresql)](https://www.postgresql.org/)
[![Deploy](https://img.shields.io/badge/Deploy-Railway-purple.svg?style=flat-square&logo=railway)](https://railway.app/)

---

## 📌 Sobre o Projeto

O **Projeto Abençoar** é uma solução backend/fullstack desenvolvida para automatizar e otimizar os processos operacionais e administrativos de uma instituição social. A aplicação resolve problemas reais de agendamento de consultas/atendimentos (como Fisioterapia, Pilates, Psicologia, entre outros), emissão de listas de chamada para recepção e controle de dados dos beneficiários.

### 🌟 Diferenciais da Aplicação
- **Aplicação Real em Produção:** Utilizada diariamente por operadores finais para gestão das turmas e recepção.
- **Resolução Prática de Regras de Negócio:** Tratamento de agendas por horário/dia, controle rigoroso de chaves primárias e geração dinâmica de listas de chamada para impressão em formato A4.
- **Infraestrutura em Nuvem:** Deploy contínuo com banco de dados PostgreSQL implantado no Railway.

---

## 🚀 Tecnologias Utilizadas

### **Backend & Frameworks**
- **Java 21 LTS** (Recursos modernos da linguagem)
- **Spring Boot 3.3.5**
- **Spring Data JPA & Hibernate** (Mapeamento Objeto-Relacional)
- **Bean Validation** (Validação segura de dados)
- **Lombok** (Produtividade no código)

### **Frontend & Interface**
- **Thymeleaf** (Renderização dinâmica de templates)
- **HTML5 / CSS3 / JavaScript**
- **Bootstrap 5** (Layout responsivo e modais dinâmicos)

### **Banco de Dados & Persistência**
- **PostgreSQL** (Ambiente local e produção)
- **JPQL & Java Streams** (Consultas otimizadas e filtragem em memória)

### **DevOps & Ferramentas**
- **Git & GitHub** (Controle de versão)
- **Railway** (Hospedagem e Cloud DB)
- **Maven** (Gerenciamento de dependências)

---

## ⚙️ Arquitetura e Funcionalidades Principais

```text
┌────────────────┐      ┌────────────────┐      ┌────────────────┐
│   Thymeleaf    │ ───> │   Controllers  │ ───> │ Service/Repository
│  (Views & UX)  │ <─── │   (Spring MVC) │ <─── │  (Spring Data JPA)
└────────────────┘      └────────────────┘      └────────────────┘
                                                        │
                                                        ▼
                                               ┌────────────────┐
                                               │   PostgreSQL   │
                                               │   (Cloud DB)   │
                                               └────────────────┘

Gestão de Beneficiários:

Cadastro, edição e listagem com busca rápida por matrículas (String), nomes e documentos.

Triagem e Agendamentos:

Mapeamento dinâmico entre Beneficiário, Especialidade e Horários/Dias da semana.

Geração e Impressão de Listas de Frequência:

Modal pop-up interativo para geração de listas de chamada filtradas por especialidade/dia, formatadas para impressão direta sem quebrar layout.

Resolução Dinâmica de Horários Vagos:

Exibição tratada para diferenciar horários ocupados e disponíveis em tempo de execução via Java Streams.

🛠️ Como Executar o Projeto Localmente
Pré-requisitos
Java 21 instalado

Maven instalado

PostgreSQL rodando localmente (ou ajuste para H2 no application.properties)

Passos:
Clone o repositório:

Bash
git clone [https://github.com/FilipeDawra/projeto-abencoar.git](https://github.com/FilipeDawra/projeto-abencoar.git)
cd projeto-abencoar
Configure o banco de dados (src/main/resources/application.properties):

Properties
spring.datasource.url=jdbc:postgresql://localhost:5432/abencoar_db
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
Compile e execute a aplicação:

Bash
mvn spring-boot:run
📄 Licença e Impacto Social
Este projeto foi desenvolvido com foco em Engenharia de Software de alto impacto social, atendendo às necessidades operacionais diretas da instituição e servindo de base para evolução contínua e reengenharia de arquitetura.

Desenvolvido por Filipe
