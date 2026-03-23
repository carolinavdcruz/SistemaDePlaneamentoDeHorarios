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



