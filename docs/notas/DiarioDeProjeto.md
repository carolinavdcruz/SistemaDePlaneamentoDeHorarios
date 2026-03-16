# Diário de Projeto: Sistema de Planeamento de Horários

**Faculdade** Instituto Superior de Engenhatia de Lisboa - ISEL

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
* **09 de Março de 2026:** Entrega da Proposta do Projeto.
* **27 de Abril de 2026:** Apresentação de Progresso (e indicação do arguente).
* **01 de Junho de 2026:** Entrega da Versão Beta.
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
* [ ] Conclusão da fase de Setup do Sistema.

### Fase 2: Core da Aplicação e Participantes
**Semana 4 (16/03 - 22/03)**
* [ ] Criação da navegação principal da aplicação (Navigation Compose).
* [ ] Criação do Dashboard inicial.
* [ ] Implementação da barra lateral de navegação (Menu principal).
* [ ] Criação da página de Perfil do utilizador.
* [ ] Definição do modelo Kotlin das entidades (User, Participant, Availability).

**Semana 5 (23/03 - 29/03)**
* [ ] Implementação da gestão de participantes (CRUD).
* [ ] Criação da página de listagem de participantes.
* [ ] Criação da página de criação/edição de participante.
* [ ] Implementação do armazenamento local inicial (ViewModels + estado).



**Semana 6 (30/03 - 05/04)**
* [ ] Implementação da gestão de disponibilidades.
* [ ] Criação da interface para inserir horários disponíveis.
* [ ] Implementação de seleção de dias da semana.
* [ ] Implementação de seleção de intervalos horários.
* [ ] Persistência das disponibilidades na base de dados.



### Fase 3: Processamento e Algoritmo
**Semana 7 (06/04 - 12/04)**
* [ ] Implementação do processamento de intervalos de disponibilidade.
* [ ] Conversão de intervalos em blocos discretos (TimeSlots).
* [ ] Definição da estrutura de dados para TimeSlots.
* [ ] Implementação inicial do serviço de geração de horários.



**Semana 8 (13/04 - 19/04)**
* [ ] Implementação do algoritmo de planeamento (versão inicial).
* [ ] Implementação da ordenação de participantes por restrição.
* [ ] Implementação da lógica de atribuição gulosa de blocos.
* [ ] Testes iniciais do algoritmo com dados simulados.



**Semana 9 (20/04 - 26/04)**
* [ ] Preparar PowerPoint para a Apresentação de Progresso.
* [ ] Melhorias no algoritmo de planeamento.
* [ ] Implementação da verificação de restrições (capacidade máxima, horas diárias).
* [ ] Implementação do cálculo de métricas do horário gerado.
* [ ] Preparação do PowerPoint para a apresentação de progresso.



**Semana 10 (27/04 - 03/05)**

*MILESTONE: Apresentação de Progresso (27/04)*
* [ ] Realização da Apresentação de Progresso do Projeto.
* [ ] Implementação da visualização do horário gerado.
* [ ] Criação da página de visualização do horário semanal.
* [ ] Implementação da confirmação ou rejeição do horário gerado.



### Fase 4: Algoritmo e Visualização
**Semana 11 (04/05 - 10/05)**
* [ ] Melhorias na interface de visualização do horário.
* [ ] Implementação da funcionalidade de criação de novos horários.
* [ ] Implementação do armazenamento de horários gerados.



**Semana 12 (11/05 - 17/05)**
* [ ] Integração com a API backend.
* [ ] Implementação de endpoints para geração de horários.
* [ ] Implementação da persistência completa no PostgreSQL.



**Semana 13 (18/05 - 24/05)**
* [ ] Testes completos do fluxo de criação de horários.
* [ ] Implementação da visualização de sessões de grupo.
* [ ] Implementação de métricas de ocupação das sessões.



**Semana 14 (25/05 - 31/05)**
* [ ] Correção de erros e melhorias na aplicação.
* [ ] Preparação da versão beta.
* [ ] Preparação da demonstração da aplicação.



### Fase 5: Integrações, Versão Beta e Finalização
**Semana 15 (01/06 - 07/06)**
* [ ] Entrega do Relatório Beta e da demonstração da aplicação.
* [ ] Implementação da integração com Google Calendar.
* [ ] Criação automática de eventos no calendário do utilizador.



*MILESTONE: Entrega da Versão Beta (01/06)*

* [ ] Entrega do Relatório Beta e da demonstração da aplicação.
* [ ]



**Semana 16 (08/06 - 14/06)**
* [ ] Implementação do envio de emails aos participantes.
* [ ] Implementação da geração automática de notificações de horário.



**Semana 17 (15/06 - 21/06)**
* [ ] Implementação de testes funcionais.
* [ ] Validação do funcionamento do algoritmo com diferentes cenários.



**Semana 18 (22/06 - 28/06)**
* [ ] Melhorias de usabilidade na interface da aplicação.
* [ ] Otimização do desempenho do algoritmo de planeamento.



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