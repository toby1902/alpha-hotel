# Alpha Hotel

`Alpha Hotel` est un projet academique de gestion hoteliere developpe avec `Spring Boot 3`, `Java 17`, `PostgreSQL`, `Spring Security`, `JWT`, `Thymeleaf`, `Bootstrap 5` et `JavaMailSender`.

L'objectif du projet est de proposer une application web complete permettant de gerer :
- les reservations clients
- les chambres et leurs disponibilites
- les paiements et la facturation
- les stocks PPN
- les notifications automatiques par email
- le suivi des sejours avec check-in et check-out

## 1. Objectif academique

Ce projet a ete concu comme une application de gestion hoteliere realiste afin de mettre en pratique plusieurs notions vues en developpement Java Full-Stack :
- architecture en couches
- developpement MVC
- persistance avec JPA
- securite applicative
- validation des donnees
- automatisation metier
- integration email

## 2. Technologies utilisees

- `Java 17`
- `Spring Boot 3`
- `Spring Data JPA`
- `PostgreSQL`
- `Spring Security`
- `JWT`
- `Thymeleaf`
- `Bootstrap 5`
- `JavaMailSender`
- `Maven`

## 3. Fonctionnalites principales

### Cote client

- page d'accueil avec presentation de l'hotel
- consultation des chambres
- recherche de disponibilites par date
- formulaire de reservation
- calcul automatique du montant total et de l'avance de 30 %
- choix du mode de paiement de l'avance
- numero obligatoire si le mode choisi est `Mobile Money`

### Cote administration

- connexion admin securisee
- dashboard de suivi
- validation, modification et annulation des reservations
- check-in manuel
- check-out manuel
- creation de reservation sur place par l'administration
- enregistrement des paiements
- ajout de chambres
- ajout de produits de stock
- consultation des chambres disponibles
- generation de facture PDF

### Automatisations metier

- notification admin lors d'une nouvelle reservation
- email de confirmation au client apres validation
- rappel client la veille du depart
- recapitulatif admin des departs du jour
- cloture automatique des sejours echus si un check-out a ete oublie

## 4. Architecture du code

Le projet suit une architecture en couches pour separer clairement les responsabilites.

### `model`

Ce package contient les entites JPA qui representent les donnees principales du systeme :
- `Client`
- `Chambre`
- `Reservation`
- `Paiement`
- `Facture`
- `StockPPN`
- `Utilisateur`

Exemple :
- `Reservation` contient les informations du sejour
- `Paiement` contient les avances et paiements complementaires
- `Facture` calcule le montant paye et le reste a payer

### `repository`

Ce package contient les interfaces `JpaRepository`.

Role :
- lire les donnees depuis la base
- enregistrer les entites
- effectuer les recherches metier

Exemples :
- recherche des reservations
- recherche des chambres occupees sur une periode
- recherche des utilisateurs admin

### `service`

C'est la couche metier la plus importante.

Elle contient la logique du projet :
- creation et validation des reservations
- verification des disponibilites
- mise a jour automatique des factures
- gestion des paiements
- gestion des alertes stock
- envoi des emails
- automatisations planifiees

Exemples de fichiers importants :
- [ReservationService.java](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/java/com/alpha/hotel/service/ReservationService.java>)
- [FacturationService.java](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/java/com/alpha/hotel/service/FacturationService.java>)
- [ChambreService.java](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/java/com/alpha/hotel/service/ChambreService.java>)
- [SejourSchedulerService.java](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/java/com/alpha/hotel/service/SejourSchedulerService.java>)

### `controller`

Cette couche recoit les requetes HTTP et relie l'interface a la logique metier.

On y trouve :
- les controllers MVC pour les pages web
- les controllers REST pour l'API

Exemples :
- [ClientController.java](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/java/com/alpha/hotel/controller/ClientController.java>)
- [AdminController.java](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/java/com/alpha/hotel/controller/AdminController.java>)

### `dto`

Les DTO servent a transporter et valider les donnees des formulaires et de l'API.

Ils permettent de :
- controler les champs
- eviter d'exposer directement les entites
- separer la validation de l'affichage

Exemples :
- `ReservationForm`
- `ReservationUpdateForm`
- `PaiementForm`
- `AdminPaiementForm`
- `ChambreForm`
- `StockForm`

### `templates`

Ce dossier contient les pages `Thymeleaf`.

Exemples :
- portail client
- dashboard admin
- formulaire de reservation admin
- formulaire de paiement admin
- pages de disponibilites

### `static`

Ce dossier contient les ressources front-end :
- CSS
- images
- logo

## 5. Explication du fonctionnement metier

### Reservation

Quand une reservation est creee :
- le montant total est calcule a partir du prix de la chambre et du nombre de nuits
- l'avance est calculee a `30 %`
- le statut initial devient `EN_ATTENTE`

### Validation

Quand l'admin clique sur `Valider` :
- le statut passe a `CONFIRMEE`
- un email de confirmation est envoye au client

### Paiement et facture

Quand un paiement est enregistre :
- le paiement est sauvegarde
- la facture est recalculée automatiquement
- le montant paye est mis a jour
- le reste a payer est mis a jour
- le statut de facture change selon le cas :
  - `BROUILLON`
  - `PARTIELLEMENT_REGLEE`
  - `REGLEE`

### Sejour

- `Check-in` : passe le sejour a `EN_COURS`
- `Check-out` : passe le sejour a `TERMINE`
- si le check-out est oublie, une cloture automatique peut etre effectuee chaque jour

## 6. Base de donnees

Base attendue :
- `alpha_hotelDb`

Commande de creation :

```sql
CREATE DATABASE "alpha_hotelDb";
```

Configuration principale :
- [application.properties](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/resources/application.properties>)
- [application-example.properties](</c:/Users/lebit's/Desktop/JAVA WEB/src/main/resources/application-example.properties>)

Pour GitHub, les secrets ne sont plus stockes en dur dans `application.properties`.
Le projet utilise des variables d'environnement avec valeurs par defaut.

Variables principales possibles :
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `APP_MAIL_FROM`
- `APP_MAIL_NOTIFICATION_ADMIN`
- `APP_JWT_SECRET`

## 7. Lancement du projet

Depuis le dossier du projet :

```powershell
mvn spring-boot:run
```

Compilation :

```powershell
mvn -q -DskipTests compile
```

Tests :

```powershell
mvn test
```

## 8. Comptes de demonstration

- `admin@alphahotel.com / Admin@123`
- `direction@alphahotel.com / Direction@123`

## 9. URLs utiles

### Interface web

- accueil client : `http://localhost:8080/`
- disponibilites client : `http://localhost:8080/reservations/disponibilites`
- formulaire reservation client : `http://localhost:8080/reservations/nouvelle`
- connexion admin : `http://localhost:8080/auth/login`
- dashboard admin : `http://localhost:8080/admin/dashboard`
- reservation admin : `http://localhost:8080/admin/reservations/nouvelle`
- paiement admin : `http://localhost:8080/admin/paiements/nouveau`
- chambre admin : `http://localhost:8080/admin/chambres/nouvelle`
- stock admin : `http://localhost:8080/admin/stocks/nouveau`
- disponibilites admin : `http://localhost:8080/admin/chambres/disponibles`

### API REST

- `POST /api/auth/login`
- `GET /api/chambres`
- `POST /api/reservations`
- `GET /api/admin/reservations`
- `POST /api/admin/reservations/{id}/valider`
- `POST /api/admin/reservations/{id}/check-in`
- `POST /api/admin/reservations/{id}/check-out`
- `POST /api/admin/reservations/{id}/paiements`
- `GET /api/admin/reservations/{id}/facture`
- `GET /api/admin/reservations/{id}/facture/pdf`
- `GET /api/admin/stocks`
- `GET /api/admin/stocks/alertes`
- `POST /api/admin/stocks`

## 10. Scenario de demonstration

### Demonstration client

1. ouvrir la page d'accueil
2. consulter les chambres
3. rechercher une chambre disponible
4. remplir le formulaire de reservation
5. soumettre la reservation avec l'avance

### Demonstration admin

1. se connecter a l'espace admin
2. ouvrir le dashboard
3. visualiser la reservation en attente
4. valider la reservation
5. faire le check-in
6. enregistrer un paiement complementaire
7. generer la facture PDF
8. faire le check-out

## 11. Automatisations planifiees

- rappel client la veille du depart : `18:00`
- recapitulatif admin des departs du jour : `07:00`
- cloture automatique des sejours echus : `00:10`

Configuration :

```properties
app.scheduler.admin-depart-summary-cron=0 0 7 * * *
app.scheduler.depart-reminder-cron=0 0 18 * * *
app.scheduler.auto-checkout-cron=0 10 0 * * *
```

## 12. Validation des donnees

Le projet integre plusieurs controles :
- noms sans chiffres ni symboles interdits
- telephone au format malgache
- email en minuscules
- verification des dates
- numero Mobile Money obligatoire si ce mode est choisi
- prevention des paiements superieurs au reste a payer
- prevention des conflits de disponibilite

## 13. Etat actuel du projet

Le projet est deja tres complet pour un projet academique :
- architecture claire
- fonctionnalites metier riches
- interface web exploitable
- securite admin
- base de donnees operationnelle
- systeme d'email
- automatisations metier
- generation PDF

## 14. Ameliorations possibles

Ameliorations futures envisageables :
- pagination
- audit des actions admin
- graphiques de statistiques
- export Excel
- historique detaille des mouvements de stock
- upload d'images de chambres
- augmentation de la couverture de tests
