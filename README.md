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
   * [Order Service](#order-service)
      * [API Endpoints](#api-endpoints)
   * [Gateway Service](#gateway-service)
        * [Routing Logic](#routing-logic)

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

2. **Build All Service using Maven:**
   ```bash
   mvn clean package
   ```
   This command will compile the Java code and create a JAR file in each module `target` directory.

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

## Order Service
The Order Service runs on port 3300.

### API Endpoints
The Order Service exposes the following RESTful API endpoint:

`POST /purchase/:itemId:` Initiates the purchase of an item with the given :itemId. This endpoint communicates with the Catalog Service to check the stock and update it if the purchase is successful.

Example: Send a POST request to http://localhost:3300/purchase/1.
Response:

Success (HTTP 200): Returns the message: "Purchase successful for item ID: [itemId]".

Failure (HTTP 400): Returns the message: "Purchase failed for item ID: [itemId]. Item may be out of stock or not found.".

Note: The Order Service depends on the Catalog Service being available at http://catalog:4575 (when running within Docker Compose, the service name catalog can be used for inter-service communication).

## Gateway Service

The Gateway Service simplifies access to the system by forwarding user-facing HTTP requests to the appropriate backend services. It runs on port 4567.

### Routing Logic

The gateway listens for the following routes and forwards them internally:

Endpoint	Method	Forwards To

`/search/:topic	GET	http://catalog:4575/search/:topic`

`/info/:id	GET	http://catalog:4575/info/:id`

`/purchase/:itemId	POST	http://order:3300/purchase/:itemId`

These routes are handled by the `ForwardingHandler` class, which dynamically resolves path parameters and logs request/response details.

