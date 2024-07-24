
# Digital Disclosure Service Store

This protected microservice is part of the Digital Disclosure Service. This service is designed to allow customers to tell HMRC about unpaid tax from previous years for both onshore and offshore liabilities. It is the replacement for the DO4SUB iForm which is the original digital incarnation of the Disclosure Service.

This microservice provides long term draft storage functionality via Mongo, such that customers can come back to the service after signing out and pick up where they left off.

## How to run the service

### Start a MongoDB instance

### Start the microservice
This service is written in Scala and Play, so needs at a JRE to run and a JDK for development. In order to run the application you need to have SBT installed. Then, it is enough to start the service with:
`sbt run`

### Accessing the service
Endpoints will be available from the host `http://localhost:15005/digital-disclosure-service-store`. The easiest way to test the functionality of this microservice is to access the `digital-disclosure-service-frontend` microservice which will be available on `http://localhost:15003/digital-disclosure` if you follow the instructions below on starting relevant services.

### Start all relevant services
In order to test the functionality of this service, we need to start the following two microservices and all dependent microservices:
- Digital Disclosure Service Frontend
- Digital Disclosure Service

The easiest way to set up required microservices is to use Service Manager and the DDS_ALL profile from service-manager-config repository:
`sm2 --start DDS_ALL`

## Testing the application
This repository contains unit tests for the service. In order to run them, simply execute:
`sbt test`
This repository contains integration tests for the service. In order to run them, simply execute:
`sbt it:test`

## License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
