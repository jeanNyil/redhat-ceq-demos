## Red Hat Camel Extensions for Quarkus Examples

This repository contains examples of _Red Hat Camel Extensions for Quarkus_ projects to demonstrate various features.

All projects leverage **Red Hat build of Quarkus 2.13.x**, the Supersonic Subatomic Java Framework. More specifically, the projects are implemented using [**Red Hat Camel Extensions for Quarkus 2.13.x**](https://access.redhat.com/documentation/en-us/red_hat_integration/2023.q1/html/getting_started_with_camel_extensions_for_quarkus/index).

Their purpose is to provide small, specific and working examples that can be used for reference in your own projects.

If you want to learn more:
- about **Red Hat build of Quarkus**, please visit: https://access.redhat.com/documentation/en-us/red_hat_build_of_quarkus .
- about **Red Hat Camel Extensions for Quarkus**, please visit: https://access.redhat.com/documentation/en-us/red_hat_integration/2023.q1/html/getting_started_with_camel_extensions_for_quarkus/index .

### _Red Hat Camel Extensions for Quarkus_ examples:

* [camel-quarkus-datagrid-tester](./camel-quarkus-datagrid-tester): demoing the implementation of the _Infinispan Idempotent Repository_ to synchronize concurrent access as well as the use of the _Apache Camel Quarkus Infinispan_ extension.
* [camel-quarkus-rhoam-webhook-handler-api](./camel-quarkus-rhoam-webhook-handler-api): exposing a RESTful service using the _Apache Camel REST DSL_, and integrating with a _Red Hat AMQ 7 Broker_ using the AMQP 1.0 JMS client - Apache Qpid JMS.
* [camel-quarkus-jsonvalidation-api](./camel-quarkus-jsonvalidation-api): exposing a RESTful service using the _Apache Camel REST DSL_ and validating a sample JSON payload using the _Apache Camel Quarkus JSON Schema Validator_ extension.
* [camel-quarkus-xmlvalidation-api](./camel-quarkus-xmlvalidation-api): exposing a RESTful service using the _Apache Camel REST DSL_ and validating a sample XML payload using the _Apache Camel Quarkus validator_ extension.
