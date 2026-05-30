# fitness-auth-skeleton

Spring Boot 3.5.13 + JDK 17 + Spring Security 6 + JWT + MyBatis auth skeleton.

## Features
- POST /api/v1/auth/register
- POST /api/v1/auth/login
- POST /api/v1/auth/logout
- GET /api/v1/auth/me
- Role based access for /api/v1/admin/**

## Run
1. Create database `fitness_recommend_system`
2. Execute `src/main/resources/db/schema_auth.sql`
3. Execute `src/main/resources/db/data_auth.sql`
4. Edit datasource in `application.yml`
5. Run `mvn spring-boot:run`

## Default admin
- username: admin
- password: 123456

## Token claim
- userId
- username
- roleCode
