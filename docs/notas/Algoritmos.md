# Backtracking / Depth‑First Search

Como funciona:
- Tenta construir o horário passo a passo.
- Se uma escolha leva a conflito volta atrás.
Vantagens:
- Fácil de implementar
- Garante solução ótima (se existir)
Desvantagens:
- Explode em complexidade com muitos alunos
- Lento para problemas grandes

# Greedy Algorithms (Heurísticas)

Como funciona:
- Escolhe sempre a melhor opção disponível naquele momento.
		- *Exemplo:* “Colocar primeiro os alunos com menos disponibilidade.”
Vantagens:
- Muito rápido
- Fácil de implementar
- Funciona bem em muitos casos reais
Desvantagens:
- Pode gerar soluções subótimas
- Pode falhar em casos complexos

# Google OR-Tools com CP-SAT

- Tem bindings em Kotlin/JVM via Java,
- Resolve o problema de forma exata e rápida para as dimensões previstas,
- É gratuito.

**pseudocódigo:**
Variáveis de decisão:
  x[ aluno ][ slot ] = 1 se o aluno é colocado no slot, 0 caso contrário

Restrições:
  1. x[a][s] = 0 se professor não disponível no slot s
  2. x[a][s] = 0 se aluno a não disponível no slot s
  3. sum(x[a][s] para todo a) <= max_alunos_por_sessao, para cada s
  4. sum(x[a][s] para todo s no mesmo dia) <= 1, para cada aluno a

Objetivo:
  Maximizar sum(x[a][s] para todo a, s)