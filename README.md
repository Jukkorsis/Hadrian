Hadrian
=======

Hadrian is a repository of storing meta-data about SOA services.

Features
--------
 - Create and manage meta-data about services.
 - Webhooks to execute scripts in response to:
   - creating/modifying/deleting instances of a service.
   - creating/modifying/deleting end points.
   - adding/removing an instance of a service to an end point.
 - Rest API to access the service repository.
 - Hadrian lets you record how services depend on each other.
 - Graph the relationships of services.

Technologies
------------
 - Java
 - Jetty
 - Logback
 - AngularJS
 - Bootstrap
 - VisJS
 - Maven

Install
-------
 1. Download the latest version of Hadrian
 2. Compile Hadrian
 3. Optionally create a hadrian.properties and logback.xml
 4. Start Hadrian Java process

Config File hadrian.properties
------------------------------
property: jetty.port
Default:  9090

property: jetty.idleTimeout
Default:  1000

property: jetty.acceptQueueSize
Default:  100

property: webhook.callbackHost
Default:  127.0.0.1

property: webhook.instanceUrl
Default:  127.0.0.1:9090/webhook/instance

property: webhook.endPointUrl
Default:  127.0.0.1:9090/webhook/endpoint

property: webhook.endPointUrl
Default:  127.0.0.1:9090/webhook/instanceendpoint

property: maven.url
Default:  127.0.0.1/mvnrepo/internal/com/northernwall/

property: maven.maxVersions
Default:  15

property: logback.filename
Default:  logback.xml

Web Hooks
---------
Create or Delete Instance:
{
 "callbackUrl":"http://127.0.0.1:9090/webhook/callback",
 "service":{
  "serviceId":"61b4fc8b-7bf5-4dc0-be58-f75394b3cd3d",
  "serviceName":"MyService",
  "teamId":"1e0a5f68-3d4a-4875-a95d-f600fe0b1f5d",
  "description":"My Service does stuff",
  "maven":"myservice"
 },
 "instance":{
  "instanceId":"3fb219a7-d1ae-4258-bee4-39c3f762047f",
  "instanceName":"hostname",
  "serviceId":"61b4fc8b-7bf5-4dc0-be58-f75394b3cd3d",
  "dataCenter":"WDC",
  "env":"VM-Java8",
  "size":"S",
  "version":"2"
 }
}

Create or Delete End Point:
{
 "callbackUrl":"http://127.0.0.1:9090/webhook/callback",
 "service":{
  "serviceId":"61b4fc8b-7bf5-4dc0-be58-f75394b3cd3d",
  "serviceName":"MyService",
  "teamId":"1e0a5f68-3d4a-4875-a95d-f600fe0b1f5d",
  "description":"My Service does stuff",
  "maven":"myservice"
 },
 "endPoint":{
  "endPointId":"76f57a96-e657-4f42-a979-ebc778cf5d0a",
  "endPointName":"myservice-80",
  "serviceId":"61b4fc8b-7bf5-4dc0-be58-f75394b3cd3d",
  "dns":"myservice.mydomain.com",
  "vipPort":80,
  "servicePort":8080,
  "external":false
 }
}

Create or Delete InstanceEndPoint:
{
 "callbackUrl":"http://127.0.0.1:9090/webhook/callback",
 "service":{
  "serviceId":"61b4fc8b-7bf5-4dc0-be58-f75394b3cd3d",
  "serviceName":"MyService",
  "teamId":"1e0a5f68-3d4a-4875-a95d-f600fe0b1f5d",
  "description":"My Service does stuff",
  "maven":"myservice"
 },
 "instance":{
  "instanceId":"3fb219a7-d1ae-4258-bee4-39c3f762047f",
  "instanceName":"hostname",
  "serviceId":"61b4fc8b-7bf5-4dc0-be58-f75394b3cd3d",
  "dataCenter":"WDC",
  "env":"VM-Java8",
  "size":"S",
  "version":"2"
 },
 "endPoint":{
  "endPointId":"76f57a96-e657-4f42-a979-ebc778cf5d0a",
  "endPointName":"myservice-80",
  "serviceId":"61b4fc8b-7bf5-4dc0-be58-f75394b3cd3d",
  "dns":"myservice.mydomain.com",
  "vipPort":80,
  "servicePort":8080,
  "external":false
 }
}

Callback response:
{
 "type":"instance",
 "operation":"POST",
 "id":"3fb219a7-d1ae-4258-bee4-39c3f762047f",
 "status":200
}

Todo
----
- check that only the reversed top 15 maven entries are show
- test what happens when you use the check boxes, some code clears the list after.
- test update instance