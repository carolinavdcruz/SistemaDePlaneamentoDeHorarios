# Diário de Projeto: Sistema de Planeamento de Horários

**Faculdade** Instituto Superior de Engenharia de Lisboa - ISEL

**Curso** Engenharia Informática e de Computadores

**Unidade Curricular:** Projeto e Seminário (PS) semestre de verão de 2025/2026

**Equipa:** Carolina Cruz (n.º 50475) & Constança Costa (n.º 50541)  

**Orientador:** Paulo Pereira 

**Carga Horária Estimada:** ~15 a 20 horas semanais

---

## Visão Geral do Projeto
O objetivo central é desenvolver uma aplicação para gerir horários de professores e alunos, recolhendo disponibilidades e produzindo automaticamente um horário livre de conflitos, respeitando diversas restrições.

**Stack Tecnológico & Integrações:**
* **Frontend:** Aplicação Android em Kotlin com Jetpack Compose.
* **Backend:** Web API e base de dados PostgreSQL.
* **Integrações (Riscos/Desafios):** Google Calendar API e serviço de Email.
* **Inputs Suportados:** P
  * *Primeira fase:* Formulário de modo a ser mais simples a análise do input dos dados 
  * *Posteriormente:* Áudio, Imagens, PDF e Texto informal (com potencial uso futuro de um LLM para interpretação dos dados).

---

## Datas (Milestones)
* **9 de Março de 2026:** Entrega da Proposta do Projeto.
* **27 de Abril de 2026:** Apresentação de Progresso (e indicação do arguente).
* **1 de Junho de 2026:** Entrega da Versão Beta.
* **11 de Julho de 2026:** Entrega da Versão Final (Época Normal/Recurso).

---

## Registo Semanal de Atividades

### Fase 1: Arranque e Configuração Inicial
**Semana 1 (24/02 - 01/03)**
* [x] Reuniões para definição do projeto e do seu funcionamento.

**Semana 2 (02/03 - 08/03)**
* [x] Realização da proposta de projeto.
* [x] Realização do PowerPoint para a apresentação da proposta de projeto.
* [x] Criação do Repositório (GitHub).

**Semana 3 (09/03 - 15/03)**

*MILESTONE: Entrega da Proposta (09/03)*
* [x] Entrega formal da proposta de projeto.
* [x] Conclusão da fase de Setup do Sistema.

### Fase 2: Core da Aplicação e Participantes
**Semana 4 (16/03 - 22/03)**
* [x] Definição do modelo Kotlin das entidades
* [x] Criação da navegação principal da aplicação (Navigation Compose).
* [x] Criação do Dashboard inicial.
* [x] Implementação da barra lateral de navegação (Menu principal).
* [x] Criação da página de Perfil do utilizador.
* [x] Definição do modelo Kotlin das entidades (User, Participant, Availability).

**Semana 5 (23/03 - 29/03)**
* [x] Implementação da gestão de participantes (CRUD).
* [x] Criação da página de listagem de participantes.
* [x] Criação da página de criação/edição de participante.
* [x] Implementação do armazenamento local inicial (ViewModels + estado).



**Semana 6 (30/03 - 05/04)**
* [x] Trocar fake bd pela ROOM
* [x] Implementação do Login
* [x] Implementação da gestão de disponibilidades.
* [x] Criação da interface para inserir horários disponíveis.
* [x] Implementação de seleção de dias da semana.
* [x] Implementação de seleção de intervalos horários.
* [x] Persistência das disponibilidades na base de dados.



### Fase 3: Processamento e Algoritmo
**Semana 7 (06/04 - 12/04)**
* [x] Implementação do processamento de intervalos de disponibilidade.
* [x] Conversão de intervalos em blocos discretos (TimeSlots).
* [x] Definição da estrutura de dados para TimeSlots.
* [x] Implementação inicial do serviço de geração de horários.



**Semana 8 (13/04 - 19/04)**
* [x] Implementação do algoritmo de planeamento (versão inicial).
* [x] Implementação da ordenação de participantes por restrição.
* [x] Implementação da lógica de atribuição gulosa de blocos.
* [x] Testes iniciais do algoritmo com dados simulados.



**Semana 9 (20/04 - 26/04)**
* [x] Preparar PowerPoint para a Apresentação de Progresso.
* [x] Implementação da visualização do horário criado.




**Semana 10 (27/04 - 03/05)**

*MILESTONE: Apresentação de Progresso (27/04)*
* [x] Pedidos a funcionar no Postman
* [x] Criação dockerfile
* [x] Atualização da criação do schedule no backend



### Fase 4: Algoritmo e Visualização
**Semana 11 (04/05 - 10/05)**
* [x] Melhorias na ‘interface’ de visualização do horário.
* [x] Implementação da funcionalidade de criação de novos horários.
* [x] Implementação do armazenamento local de horários criados.
* [x] Liga a app Android ao backend local via emulador (10.0.2.2:8080)

**Semana 12 (11/05 - 17/05)**
* [x] Integração com a API backend.
* [x] Implementação de endpoints para geração de horários.
* [x] Implementação da confirmação ou rejeição do horário criado no frontend.


**Semana 13 (18/05 - 24/05)**
* [x] Realização da Apresentação de Progresso do Projeto.



**Semana 14 (25/05 - 31/05)**
* [x] Correção de erros e melhorias na aplicação.
* [x] Preparação da versão beta.
* [x] Preparação da demonstração da aplicação.


### Fase 5: Integrações, Versão Beta e Finalização
**Semana 15 (01/06 - 07/06)**
* [x] Entrega do Relatório Beta e da demonstração da aplicação.
* [x] Implementação da integração com Google Calendar.
* [x] Testes completos do fluxo de criação de horários.


*MILESTONE: Entrega da Versão Beta (01/06)*

* [x] Entrega do Relatório Beta e da demonstração da aplicação.


**Semana 16 (08/06 - 14/06)**
* [ ] Implementação do envio de emails aos participantes.
* [x] Histórico
* [x] Recorrência (backend)
* [ ] Recorrência (frontend)



**Semana 17 (15/06 - 21/06)**
* [ ] Implementação de testes funcionais.
* [ ] Validação do funcionamento do algoritmo com diferentes cenários.



**Semana 18 (22/06 - 28/06)**
* [ ] Melhorias de usabilidade na interface da aplicação.
* [ ] Otimização do desempenho do algoritmo de planeamento.
* [ ] Confirmação da aula ser dada


**Semana 19 (29/06 - 05/07)**
* [ ] Escrita do relatório final do projeto.
* [ ] Organização da documentação técnica do sistema.
* [ ] Atualização do repositório GitHub com documentação.



**Semana 20 (06/07 - 12/07)**
* [ ] Submissão final do projeto.
* [ ] Revisão final do relatório.
* [ ] Preparação da apresentação final.
* [ ] Preparação da demonstração da aplicação.
* [ ] Simulação da defesa do projeto.



*MILESTONE: Versão Final (11/07)*

* [ ] Submissão do Projeto, Relatório e Organização da Entrega.
* [ ] Preparação para a Prova Pública (25 minutos de apresentação seguidos de 65 minutos de discussão para o grupo de 2 elementos).
* [ ]


---

## Notas e Reflexões Livres
*