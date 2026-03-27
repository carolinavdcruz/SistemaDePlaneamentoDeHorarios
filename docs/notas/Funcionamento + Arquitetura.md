
```
Android App
   ↓
Room (SQLite local)
   ↓
Repository
   ↓
API (REST/GraphQL)
   ↓
Backend (Kotlin/Java/Node/etc.)
   ↓
PostgreSQL (servidor)
```

#   **Android App (UI + ViewModel)**

A app mostra dados ao utilizador e envia ações (criar, editar, apagar).

- A UI **não fala diretamente com Room nem com a API**.
- A UI fala com o **ViewModel**, que fala com o **Repository**.
    

# **Repository ("cérebro")**

O Repository decide:
- quando ir buscar dados ao Room
- quando sincronizar com o servidor
- quando atualizar o PostgreSQL via API

*Exemplo de lógica típica:*

```
suspend fun getUsers(): List<User> {
    val local = userDao.getAll()
    if (local.isNotEmpty()) return local

    val remote = api.getUsers()
    userDao.insertAll(remote)
    return remote
}
```

# **Room (local)**

Room serve para:
- guardar dados offline
- evitar chamadas constantes à API

# **API (REST)**

A app **nunca** fala diretamente com PostgreSQL mas sim com a API.

*Exemplo de endpoints:*

```
GET /users
POST /users
PUT /users/{id}
DELETE /users/{id}
```

A API recebe pedidos da app e comunica com o PostgreSQL.

# **PostgreSQL (servidor)**

Dados “reais”, persistentes, partilhados entre utilizadores.
- Robusto
- Suporta queries complexas
- É seguro

A app nunca toca diretamente nesta BD.

# **Fluxo completo de dados**

1. UI → ViewModel → Repository
2. Repository tenta Room
3. Se Room estiver vazio → chama API
4. API devolve dados
5. Repository guarda no Room
6. UI mostra dados
    
*Exemplo de criação de um novo utilizador*
1. UI → ViewModel → Repository
2. Repository:
    - guarda no Room (para resposta rápida)
    - envia para API
3. API grava no PostgreSQL
4. API devolve o ID real
5. Repository atualiza o Room com o ID correto