<<<<<<< HEAD
Hadrian
=======

Hadrian is a repository of storing meta-data about SOA services.

Features
--------
 - Create and manage meta-data about services.
 - Add images (logos, class diagrams, sequence diagrams, team photos, etc.) to a service.
 - Services can have multiple versions, each at a different stage in their life cycle.
 - Hadrian lets you record how services depend on each other.
 - Search the SOA service repository.
 - Graph the relationships of services.

**Service Level Attributes**
 - Abbreviation; Short name for the service.
 - Name; The service's full name
 - Team Name; The team that owns the service
 - Description; A paragraph or two that describes the function/domain/responsibilities of the service.
 - Access; If the service is externally accessible or only offered internally.
 - Type; Applications offers a human UI versus a service which can only be accessed by other services.
 - State; Statefull or Stateless
 - Technology; A list or paragraph that describes the technologies (components, libraries, etc.) used to by the service.
 - Links; A list of links, such as wiki pages, monitoring pages, etc.
 - Endpoints; A list of endpoint URLs
 - Images; A collection images associated to the service.
 - Business Value; Describes the Value of this service to the business
 - PII; Describes if and how the service interacts with Personally Identifying Information; Service stores PII, Service processes PII, and None.
 - Data Centers; record which data centers the service is deployed in, active or standby. The list of data centers is customizable.
 - HA Ratings; A customizable list of questions to define a services HA capabilities. Hadrian comes with a default list.
 
**Version Level Attributes**
 - API Version
 - Implementation Version
 - Life cycle; The state of the version; Proposed, Active, Life, Retiring, Retired.
 - Operations; A list of operations/URLs that can be performed on the versions, such as health, version, availability, etc.
 - Uses; The service/versions this service depends on.
 - Used; The service/versions that depend on this service.

Technologies
------------
 - Java
 - Jetty
 - Logback
 - CouchDB
 - LightCouch
 - AngularJS
 - Bootstrap
 - VisJS
 - Angular-File-Upload
 - Maven

Install
-------

 1. Download the latest version of Hadrian
 2. Compile Hadrian
 3. Optionally create a hadrian.properties and logback.xml
 4. Download the latest version of CouchDB
 5. Install CouchDB
 6. Start Hadrian Java process


=======
Tower
=====
>>>>>>> origin/master
