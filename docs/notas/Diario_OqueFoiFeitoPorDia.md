# O que foi realizado à data de:
### 22 março 2026 ###

-> Fase inicial de definição e estruturação do sistema.

-> No desenvolvimento da aplicação Android, foi definida a arquitetura base:
- MVVM (Model–View–ViewModel)
- Room

-> Foi criado o modelo de dados, incluindo todas as entidades necessárias ao funcionamento do sistema (professores, alunos, restrições e disponibilidades). 
- TeacherEntity
- StudentEntity
- RestrictionsEntity
- AvailabilityEntity

-> Foi implementado os respetivos DAOs:
- TeacherDao
- StudentDao
- RestrictionsDao
- AvailabilityDao

-> Foi realizada a configuração da base de dados Room (AppDatabase) e o mecanismo de acesso centralizado através do DatabaseProvider. 

### 27 março 2026

-> Foi criada a camada de repositórios para acesso a dados:
- StudentRepository
- AvailabilityRepository
- TeacherRepository
- RestrictionsRepository

-> Foi desenvolvida a navegação principal da aplicação com Navigation Compose, permitindo a ligação entre os vários ecrãs já implementados.

-> Foram desenvolvidos os principais ecrãs da interface em Jetpack Compose, nomeadamente:
- Home
- Login
- Register
- Dashboard
- Profile
- Listagem de alunos
- Gestão de disponibilidades

-> Foi iniciada a implementação dos ViewModels responsáveis pela gestão de estado dos ecrãs e pela ligação à camada de dados:
- StudentViewModel
- AvailabilityViewModel
- LoginViewModel
- RegisterViewModel

-> No ecrã de login foi separada a lógica de estado da interface para um ViewModel próprio, permitindo gerir os campos de email e password, a visibilidade da password e a validação básica do formulário.

-> Foi criada uma estrutura baseada em ViewModel , responsável por gerir o email, a password, o tipo de utilizador selecionado e a validação dos dados inseridos.

-> Foram definidas regras de validação para os dados de autenticação, incluindo verificação do formato do email e restrições para a password, como comprimento mínimo e presença de diferentes tipos de caracteres.

### 07 abril 2026 ###

-> Foi realizada uma revisão estrutural da base da aplicação Android, com o objetivo de alinhar a implementação com a abordagem atualmente adotada no projeto: persistência local com Room como primeira fase de desenvolvimento.

-> Foi simplificada a arquitetura da aplicação para seguir o fluxo:
- UI
- ViewModel
- Repository
- Room

-> Foi decidido adiar a integração com API e base de dados remota para uma fase posterior, evitando manter uma arquitetura híbrida antes da consolidação da versão local.

-> Foi reorganizado o AppModule de forma a centralizar corretamente a criação das dependências da aplicação, incluindo:
- inicialização da base de dados
- criação dos repositórios
- criação dos ViewModels
- gestão da sessão local

-> Foi alinhada a injeção de dependências dos ViewModels para que estes deixassem de comunicar diretamente com os DAOs, passando a depender dos respetivos repositórios.

-> Foram revistos e ajustados os seguintes ViewModels:
- LoginViewModel
- RegisterViewModel
- ProfileViewModel
- StudentViewModel
- TeacherViewModel
- AvailabilityViewModel


## LOGIN

### GMAIL
    
    c@gmail.com
    tf@gmail.com

### Pass

    Teacher2026
    TestFirst26
