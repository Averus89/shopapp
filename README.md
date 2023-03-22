# ShopApp
This project is a microservice that provides an API for a small grocery store. It is written in Java and uses Spring Boot as the main framework. The project is divided into two steps, each with its own commit in the GitHub repository.

## OpenAPI Definition

This is an OpenAPI definition for a server. The current version of this definition is `v0`.

### Servers

There is one server defined in this definition:
- **URL:** `http://localhost:8080`
- **Description:** Generated server url

### Paths

There are several paths defined in this definition:
- `/orders/v1/add/{orderId}`: This path supports a `PUT` operation to add a product to an order. It requires an `orderId` parameter in the path and a `productId` parameter in the query. It also accepts an optional `quantity` parameter in the query with a default value of 1.
- `/orders/v1/getOrders`: This path supports a `GET` operation to retrieve all orders.
- `/orders/v1/getOrderSummary/{orderId}`: This path supports a `GET` operation to retrieve the summary of an order. It requires an `orderId` parameter in the path.
- `/orders/v1/getOrderIds`: This path supports a `GET` operation to retrieve all order IDs.

### Components

This definition includes several schemas for components such as `Status`, `LineItem`, `Order`, and `Product`.

- `Status`: This schema defines an object with properties for a status code and a status message.
- `LineItem`: This schema defines an object with properties for a product, discount, and total.
- `Order`: This schema defines an object with properties for an order ID, an array of line items, and an order total.
- `Product`: This schema defines an object with properties for a base price and a name.

## Next Steps

The second step adds some promotions to the API, which apply discounts to the total cost of an order based on the number of products purchased. The promotions are:

- Buy two apples, get one for 70% off: For every pair of apples in an order, one apple will have a 30% discount applied to its price ($0.5 * 0.7 = $0.35).
- Buy two oranges, get one free: For every three oranges in an order, one orange will have a 100% discount applied to its price ($0.7 * 0 = $0).

The promotions are applied automatically when calculating the total cost of an order, and they are reflected in the response of the `GET /orders/v1/getOrderSummary/{orderId}`

## Requirements

To run the project locally, you need to have Java 17 and Docker installed.

## How to run project locally

On Windows PowerShell run `runInDocker.ps1`

On Linux run `runInDocker.sh`

The API will be available at http://localhost:8080.

List of usefull endpoints:
* http://localhost:8080/actuator/prometheus - micrometer metrics for prometheus
* http://localhost:8080/actuator/health  - health endpoint
* http://localhost:8080/swagger-ui.html - swagger web ui for testing api

## Ideas to improve an application

//TODO