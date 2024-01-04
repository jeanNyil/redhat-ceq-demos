## Red Hat build of Apache Camel for Quarkus Examples

This repository contains examples of _Red Hat build of Apache Camel for Quarkus_ projects to demonstrate various features.

All projects leverage **Red Hat build of Quarkus 3.2.x**, the Supersonic Subatomic Java Framework. More specifically, the projects are implemented using [**Red Hat build of Apache Camel 4.x for Quarkus**](https://access.redhat.com/documentation/en-us/red_hat_build_of_apache_camel).

Their purpose is to provide small, specific and working examples that can be used for reference in your own projects.

If you want to learn more:
- about **Red Hat build of Quarkus**, please visit: https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus .
- about **Red Hat build of Apache Camel for Quarkus**, please visit: https://access.redhat.com/documentation/en-us/red_hat_build_of_apache_camel .

### _Red Hat build of Apache Camel for Quarkus_ examples:

* [camel-quarkus-datagrid-tester](./camel-quarkus-datagrid-tester): demoing the implementation of the _Infinispan Idempotent Repository_ to synchronize concurrent access as well as the use of the _Apache Camel Quarkus Infinispan_ extension.
* [camel-quarkus-rhoam-webhook-handler-api](./camel-quarkus-rhoam-webhook-handler-api): exposing a RESTful service using the _Apache Camel REST DSL_, and integrating with a _Red Hat AMQ 7 Broker_ using the AMQP 1.0 JMS client - Apache Qpid JMS.
* [camel-quarkus-jsonvalidation-api](./camel-quarkus-jsonvalidation-api): exposing a RESTful service using the _Apache Camel REST DSL_ and validating a sample JSON payload using the _Apache Camel Quarkus JSON Schema Validator_ extension.
* [camel-quarkus-xmlvalidation-api](./camel-quarkus-xmlvalidation-api): exposing a RESTful service using the _Apache Camel REST DSL_ and validating a sample XML payload using the _Apache Camel Quarkus validator_ extension.
