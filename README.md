# DOS - Distributed Online Book Store

### Description

The DOS project is a Distributed Online Book Store.

**Catalog Service**:

This service is responsible for managing information about books, including retrieving details, searching by
topic, and updating the stock of available books. It is built using Java with the Spark framework for creating RESTful
APIs and Maven for project management. The service utilizes a CSV file (`books.csv`) for storing book data and can be
containerized using Docker.

### Table of Contents

* [Installation](#installation)
* [Usage](#usage)
    * [Running with Docker Compose](#running-with-docker-compose)
    * [Catalog Service](#catalog-service)
      * [API Endpoints](#api-endpoints)

### Installation

To run the DOS Catalog Service, you will need the following prerequisites:

* **Java Development Kit (JDK):** Version 17 or higher.
* **Maven:** For building the Java project.
* **Docker:** For containerizing and running the service.
* **Docker Compose:** For managing the Docker containers.
* **Git:** To clone the repository (optional).

**Steps:**

1. **Clone the repository (if you haven't already):**
   ```bash
   git clone https://github.com/amrkurdi12027757/DOS
   cd DOS
   ```

2. **Build the Catalog Service using Maven:**
   ```bash
   cd catalog-module
   mvn clean package
   cd ..
   ```
   This command will compile the Java code and create a JAR file in the `catalog-module/target` directory.

### Usage

### Running with Docker Compose

The easiest way to run the Catalog Service is using Docker Compose, which is defined in the `docker-compose.yml` file.

1. **Navigate to the root directory of the project (where `docker-compose.yml` is located).**

2. **Run Docker Compose:**
   ```bash
   docker-compose up
   ```
   This command will build the Docker image for the catalog service (if it hasn't been built before) and start the
   container. The Catalog Service will be accessible at `http://localhost:4575`.

3. **To stop the service, run:**
   ```bash
   docker-compose down
   ```
## Catalog Service

### API Endpoints

The Catalog Service exposes the following RESTful API endpoints:

* **`GET /info/:id`**: Retrieves detailed information (title, price, stock) for a book with the given `:id`.
    * **Example:** `http://localhost:4575/info/1`

* **`GET /search/:topic`**: Searches for books based on the given `:topic` and returns a list of book IDs and titles.
    * **Example:** `http://localhost:4575/search/distributed%20systems`

* **`PUT /updateStock/:bookID`**: Decrements the stock of the book with the given `:bookID`.
    * **Example:** `http://localhost:4575/updateStock/1`

**Note:** The initial book data is loaded from the `books.csv` file.
