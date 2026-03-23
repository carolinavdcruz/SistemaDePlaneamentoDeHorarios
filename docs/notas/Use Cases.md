
# Use Case 1 — Teacher define disponibilidades

O professor regista os intervalos de tempo em que já tem compromissos.
1. Teacher insere dias e horas
2. App envia para backend
3. Backend guarda na BD
4. Backend devolve confirmação

---
# Use Case 2 — Teacher define restrições

O professor define as regras para a criação do horário
Inclui:
- duração da sessão
- máximo de alunos por sessão
- máximo de horas por dia

---
# Use Case 3 — Student define disponibilidades

O aluno regista os intervalos de tempo em que já tem compromissos.
1. Student insere dias e horas
2. App envia para backend
3. Backend guarda na BD
4. Backend devolve confirmação

---
# Use Case 4 — Normalização das disponibilidades

O sistema irá converter os intervalos em blocos uniformes
	*Exemplo:* 
		09:00 – 11:00 estaria disponível → blocos de 60 min → [09:00–10:00], [10:00–11:00]

---
# Use Case 5 — Sistema avalia as combinações possíveis

O sistema irá, utilizando um algoritmo, encontrar blocos onde as horas livres dos horários do professor e dos alunos coincidam.
Irá ter que respeitar as restrições:
- 1 sessão por dia por aluno
- máximo de alunos por sessão
- duração fixa das sessões que foi imposta pelo professor (ex: 1 hora)
- disponibilidade do professor

---
# Use Case 6 — Sistema cria proposta de horário

O sistema avaliando todas as use cases acima irá criar um horário semanal otimizado.
Irá ser mostrado um horário com as sessões e com os respetivos alunos por sessão.

---
# Use Case 7 — Teacher aceita ou rejeita proposta

O professor pode aceitar o horário ou rejeitar e pedir alterações.

---
# Use Case 8 — Sistema cria eventos no Google Calendar

O horário ao ser aceite pelo professor irão ser criados eventos reais no calendário Google do professor.

---
# Use Case 9 — Sistema envia emails aos alunos

O horário ao ser aceite pelo professor irão ser enviados emails a cada aluno a notificar quais serão as horas das suas sessões, ou seja do seu horário.