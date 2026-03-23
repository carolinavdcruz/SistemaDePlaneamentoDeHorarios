
# Arquitetura de um projeto Android com utilização de base de dados SQL através do ROOM

`data` → tudo o que é acesso a dados

- ***Room*** -> biblioteca oficial do Android para trabalhar com bases de dados. Guarda todos os dados para que nada seja perdido quando se termina a aplicação
				- **local** : 
					-> *entity* -> Representa as tabela da base de dados.
					-> *dao* -> Interface com as funções que representam queries SQL
					-> *database* -> Junta entity e dao
- ***Model*** -> corresponde ao domínio da aplicação
- ***Remote ***-> contém o repository e a api

`ui` → interface

- screens
- viewmodels
- components
- themes
- MainActivity

`navigation` → 
