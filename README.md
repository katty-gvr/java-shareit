Сервис для поиска и обмена вещей между пользователями

Севрис обеспечивает пользователям возможность рассказать какими вещами они готовы поделиться, а также находить нужную вещь и брать её в аренду на определенное время. 

Функциональность:
1. CRUD-операции пользователей
2. CRUD-операции вещей
3. Просмотр статуса вещи (доступна ли она для аренды)
4. Бронирование вещи на опреденные даты (при бронировании доступ к вещи для других пользователей закрыт)
5. Поиск вещи
6. Запрос на определенную вещь (в том случае, если нужная пользователю вещь не найдена при поиске)
7. Добавление новой вещи по запросу от других пользователей
8. Возможность оставлять отзывы на вещи

Технологический стек проекта:
Java 11, Spring Boot, REST API, PostgreSQL, Hibernate ORM, Lombok, Docker.
Сервис имеет мномогодульную структуру, один из которых отвечает за валидацию входящих данных, второй - за работу бизнес-логики.

Для запуска сервиса в Docker нужно выполнить docker-compose.yml в IntelliJ IDEA.
