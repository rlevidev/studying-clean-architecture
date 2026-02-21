# Product - Sistema de Autenticação

## Problema Resolvido
Sistema de estudo que demonstra implementação de autenticação segura seguindo Clean Architecture, servindo como referência para projetos futuros.

## O que Construímos
- API REST para registro e login de usuários
- Sistema de autenticação stateless com JWT
- Controle de acesso por papéis (ADMIN, USER)
- CRUD completo de usuários (protegido por role)
- Validação de dados com Bean Validation
- Tratamento centralizado de exceções
- Migrações de banco com Flyway

## Experiência do Usuário
- Registro rápido com validação de email/senha
- Login retorna token JWT para autenticação
- Admin pode gerenciar todos os usuários
- Respostas de erro padronizadas e informativas

## O que NÃO Construímos
- Refresh token rotation
- Email verification
- Password reset
- Rate limiting
- Frontend web/mobile
- Deploy automatizado
