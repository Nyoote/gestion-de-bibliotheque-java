# Gestion de bibliotheque Java

Architecture microservices Spring Boot : Eureka, Config Server, API Gateway,
Book Service et Loan Service.

## Demarrage

Le moyen le plus simple de demarrer toute l'infrastructure est Docker Compose :

```bash
docker compose up --build -d
```

Les services sont alors accessibles via la gateway sur `http://localhost:8080`.
Eureka est disponible sur `http://localhost:8761` et le Config Server sur
`http://localhost:8888`.

Pour compiler et executer tous les tests :

```bash
mvn test
```

Le fichier [library.http](library.http) contient le parcours bout-en-bout via
la gateway : creation d'un livre, emprunt reussi, emprunt refuse faute de
stock, retour et double retour.

## Endpoints

### Book Service

- `GET /api/books`
- `GET /api/books/{id}`
- `POST /api/books`
- `PUT /api/books/{id}`
- `DELETE /api/books/{id}`
- `PATCH /api/books/{id}/decrement-stock`
- `PATCH /api/books/{id}/increment-stock`

### Loan Service

- `GET /api/loans`
- `GET /api/loans/{id}`
- `POST /api/loans`
- `PATCH /api/loans/{id}/return`
