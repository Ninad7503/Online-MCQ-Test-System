# Online MCQ Test System

## Requirements
- Java 21
- JavaFX SDK 21.0.10 → Download from https://gluonhq.com/products/javafx/
- PostgreSQL
- PostgreSQL JDBC Driver (already in lib/)

## Setup
1. Download JavaFX SDK 21.0.10 and place it as `javafx-sdk-21.0.10/` in the project root
2. Create the database and run the schema:
```
psql -U YOUR_USERNAME -d postgres -c "CREATE DATABASE \"Online_MCQ_Test_Management\";"
psql -U YOUR_USERNAME -d Online_MCQ_Test_Management -f sql/schema.sql
```
3. Update your DB username in `db/DBConnection.java`

## Compile & Run
```
javac --module-path javafx-sdk-21.0.10/lib \
      --add-modules javafx.controls,javafx.fxml \
      -cp ".:lib/postgresql-42.7.10.jar" \
      Main.java controller/*.java db/*.java model/*.java

java --module-path javafx-sdk-21.0.10/lib \
     --add-modules javafx.controls,javafx.fxml \
     -cp ".:lib/postgresql-42.7.10.jar" \
     Main
```

## Login
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@test.com | admin123 |
| Student | student@test.com | student123 |
