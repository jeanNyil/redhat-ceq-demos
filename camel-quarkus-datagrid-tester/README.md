# camel-quarkus-datagrid-tester Project

This project leverages **Red Hat build of Quarkus 3.15.x**, the Supersonic Subatomic Java Framework. More specifically, the project is implemented using [**Red Hat build of Apache Camel v4.8.x for Quarkus**](https://access.redhat.com/documentation/en-us/red_hat_build_of_apache_camel).

The purpose is to demo the implementation of the _Infinispan Idempotent Repository_ to synchronize concurrent access as well as the use of the _Apache Camel Quarkus Infinispan_ extension.

The following REST endpoints are exposed:
- `/api/v1/fruits-and-legumes-api/fruits` : 
    - `GET` returns a list of hard-coded and added fruits.
    - `POST` adds a fruit in the list of fruits.
- `/api/v1/fruits-and-legumes-api/legumes` :
    - Only `GET` method is supported. Returns a list of hard-coded legumes
- `/api/v1/fruits-and-legumes-api/openapi.json`: returns the OpenAPI 3.0 specification for the Fruits and Legumes API service.
- `/api/v1/minio-file-uploader-service/csv` :
    - Only `POST`method is supported. Uploads the fruits.csv file to MinIO server.
- `/api/v1/minio-file-uploader-service/json` :
    - Only `POST`method is supported. Uploads the fruits.json file to MinIO server.
- `/api/v1/minio-file-uploader-service/xml` :
    - Only `POST`method is supported. Uploads the fruits.xml file to MinIO server.
- `/api/v1/minio-file-uploader-service/openapi.json`: returns the OpenAPI 3.0 specification for the MinIO File Uploader service.
- `/q/health` : returns the _Camel Quarkus MicroProfile_ health checks
- `/q/metrics` : the _Camel Quarkus Micrometer_ metrics in prometheus format

## Prerequisites

- Maven 3.8.1+
- JDK 21 installed with `JAVA_HOME` configured appropriately
- A running [_Red Hat OpenShift 4_](https://access.redhat.com/documentation/en-us/openshift_container_platform) cluster
- A running [_Red Hat Data Grid v8.3_](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3) cluster. 
    >_**NOTE**_: The [`config/datagrid`](./config/datagrid) folder contains OpenShift _Cache Custom Resources_ to be created. For instance, the following command line would create the `fruits-legumes-replicated-cache` and `idempotency-replicated-cache` replicated caches if the _Red Hat Data Grid_ cluster is deployed in the `datagrid-cluster` namespace: `oc -n datagrid-cluster apply -f ./config/datagrid`
    - [`fruits-legumes-replicated-cache-definition`](./config/datagrid/fruits-legumes-replicated-cache_cr.yaml) : `fruits-legumes-replicated-cache` used by the [`FruitsAndLegumesAPI`](./src/main/java/io/jeannyil/routes/FruitsAndLegumesApiRoute.java).
    - [`idempotency-replicated-cache-definition`](./config/datagrid/idempotency-replicated-cache_cr.yaml) : `idempotency-replicated-cache` used for idempotency purposes by the [`FilePollerRoute`](./src/main/java/io/jeannyil/routes/FilePollerRoute.java).
- A running [MinIO](https://min.io/) server to provide object storage used by the idempotent consumer route.
    >_**NOTE**_: The [`config/minio`](./config/minio/) folder contains resources to deploy a simple MinIO server in the `ceq-services-jvm` namespace on OpenShift.
    - You can run a simple MinIO server locally in container with the following `podman` instructions:
        1. Create the podman volume to persist MinIO data:
            ```shell
            podman volume create minio-data
            ```
        2. Run the MinIO container:
            ```shell
            podman run -d --name minio \
            -p 9000:9000 \
            -p 9090:9090 \
            -v minio-data:/data \
            -e "MINIO_ROOT_USER=minioadmin" \
            -e "MINIO_ROOT_PASSWORD=d-XT,YJ.XF3c_WT[" \
            quay.io/minio/minio server /data --console-address ":9090"
            ```
            - The MinIO administration web console is then available at http://localhost:9090/login
            - The MinIO API endpoint is also available at http://localhost:9000
- A truststore containing the [_Red Hat Data Grid v8.3_](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3) server public certificate. Below are sample command lines to generate one:
    ```shell
    # Use the Java cacerts as the basis for the truststore
    cp ${JAVA_HOME}/lib/security/cacerts ./tls-keys/truststore.p12
    keytool -storepasswd -keystore ./tls-keys/truststore.p12 -storepass changeit -new 'P@ssw0rd'
    # Importing the Red Hat Data Grid server public certificate into the truststore
    keytool -importcert -trustcacerts -keystore ./tls-keys/truststore.p12 -file ./tls-keys/rhdg.fullchain.pem -storepass P@ssw0rd -v -noprompt
    ```

    > :bulb: **Example on how to obtain the Red Hat Data Grid server public certificate:**
    ```shell
    openssl s_client -showcerts -servername <Red Hat Data Grid cluster OpenShift route> -connect <Red Hat Data Grid cluster OpenShift route>:443 | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p'
    ```
    with `<Red Hat Data Grid cluster OpenShift route>`: OpenShift route hostname for the Red Hat Data Grid cluster. E.g.: `datagrid.apps.ocp4.jnyilimb.eu`

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell
./mvnw clean compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using:
```shell
java -Dquarkus.kubernetes-config.enabled=false -jar target/quarkus-app/quarkus-run.jar
```

If you want to build an _über-jar_, execute the following command:
```shell
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using:
```shell
java -Dquarkus.kubernetes-config.enabled=false -jar target/*-runner.jar`
```

## Creating a native executable

You can create a native executable using: 
```shell
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/camel-quarkus-datagrid-tester-1.0.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Deploy to OpenShift

### Prerequisites

- The `fruits-legumes-replicated-cache` and `idempotency-replicated-cache` caches have been created in the _Red Hat Data Grid_ cluster.

    >_**NOTE**_: The [`config/datagrid`](./config/datagrid) folder contains OpenShift _Cache Custom Resources_ to be created. For instance, the following command line would create the `fruits-legumes-replicated-cache` and `idempotency-replicated-cache` replicated caches if the _Red Hat Data Grid_ cluster is deployed in the `datagrid-cluster` namespace: `oc -n datagrid-cluster apply -f ./config/datagrid`

- The MinIO server 

### Instructions

1. Login to the OpenShift cluster:
    ```shell
    oc login ...
    ```

2. Create an OpenShift project to host the service:
    ```shell
    oc new-project ceq-services-jvm --display-name="Red Hat build of Apache Camel for Quarkus Apps - JVM Mode"
    ```

3. Deploy the simple MinIO server if not already deployed:
    ```shell
    oc apply -f ./config/minio
    ```

4. Create secret containing the camel-quarkus-datagrid-tester truststore

    a. With custom certificates

    > :white_check_mark: USE THIS

    ```
    oc create secret generic camel-quarkus-datagrid-tester-truststore-secret --from-file=./tls-keys/truststore.p12
    ```

    b. **OPTIONAL:** With OpenShift signed certificates
    
    >:bulb: THIS IS FOR INFORMATION PURPOSES ONLY

    ```
    oc get secrets/signing-key -n openshift-service-ca -o template='{{index .data "tls.crt"}}' | openssl base64 -d -A > ./tls-keys/server.crt
    # Use the Java cacerts as the basis for the truststore
    cp ${JAVA_HOME}/lib/security/cacerts ./tls-keys/truststore.p12
    keytool -storepasswd -keystore ./tls-keys/truststore.p12 -storepass changeit -new 'P@ssw0rd'
    # Importing the OpenShift signing service certificate into the truststore
    keytool -importcert -keystore ./tls-keys/truststore.p12 -storepass 'P@ssw0rd' -file ./tls-keys/server.crt -trustcacerts -noprompt
    # Create camel-quarkus-datagrid-tester-truststore-secret
    oc create secret generic camel-quarkus-datagrid-tester-truststore-secret --from-file=./tls-keys/truststore.p12
    ```

5. Deploy the CEQ service
    ```shell
    ./mvnw clean package -Dquarkus.openshift.deploy=true -Dquarkus.container-image.group=ceq-services-jvm
    ```

### OpenTelemetry with Jaeger

[**Jaeger**](https://www.jaegertracing.io/), a distributed tracing system for observability ([_open tracing_](https://opentracing.io/)). 

#### Running Jaeger locally

:bulb: A simple way of starting a Jaeger tracing server is with `docker` or `podman`:

1. Start the Jaeger tracing server:
    ```
    podman run --rm -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 -e COLLECTOR_OTLP_ENABLED=true \
    -p 6831:6831/udp -p 6832:6832/udp \
    -p 5778:5778 -p 16686:16686 -p 4317:4317 -p 4318:4318 -p 14250:14250  -p 14268:14268 -p 14269:14269 -p 9411:9411 \
    quay.io/jaegertracing/all-in-one:latest
    ```
2. While the server is running, browse to http://localhost:16686 to view tracing events.

#### Deploying Jaeger on OpenShift

1. If not already installed, install the Red Hat OpenShift distributed tracing platform (Jaeger) operator with an AllNamespaces scope.
_**:warning: cluster-admin privileges are required**_
    ```
    oc apply -f - <<EOF
    apiVersion: operators.coreos.com/v1alpha1
    kind: Subscription
    metadata:
        name: jaeger-product
        namespace: openshift-operators
    spec:
        channel: stable
        installPlanApproval: Automatic
        name: jaeger-product
        source: redhat-operators
        sourceNamespace: openshift-marketplace
    EOF
    ```

2. Verify the successful installation of the Red Hat OpenShift distributed tracing platform operator
    ```shell
    watch oc get sub,csv
    ```

3. Create the allInOne Jaeger instance in the dsna-pilot OpenShift project
    ```shell
    oc apply -f - <<EOF
    apiVersion: jaegertracing.io/v1
    kind: Jaeger
    metadata:
        name: jaeger-all-in-one-inmemory
    spec:
        allInOne:
            options:
                log-level: info
        strategy: allInOne
    EOF
    ```

## Related Guides

- OpenShift ([guide](https://quarkus.io/guides/deploying-to-openshift)): Generate OpenShift resources from annotations
- Camel Minio ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-minio-whats-inside)): Store and retrieve objects from Minio Storage Service using Minio SDK
- Camel Platform HTTP ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-platform-http-whats-inside)): Expose HTTP endpoints using the HTTP server available in the current platform
- Camel Direct ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-direct-whats-inside)): Call another endpoint from the same Camel Context synchronously
- Camel Jackson ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-jackson-maven-coordinates)): Marshal POJOs to JSON and back using Jackson
- YAML Configuration ([guide](https://quarkus.io/guides/config#yaml)): Use YAML to configure your Quarkus application
- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more
- Camel Bean ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-bean-whats-inside)): Invoke methods of Java beans
- Camel OpenTelemetry ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-opentelemetry-whats-inside)): Distributed tracing using OpenTelemetry
- Camel MicroProfile Health ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-microprofile-health-whats-inside)): Expose Camel health checks via MicroProfile Health
- Camel Micrometer ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-micrometer-whats-inside)): Collect various metrics directly from Camel routes using the Micrometer library
- Camel Timer ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-timer-whats-inside)): Generate messages in specified intervals using java.util.Timer
- Camel Infinispan ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-infinispan-whats-inside)): Read and write from/to Infinispan distributed key/value store and data grid
- Kubernetes Config ([guide](https://quarkus.io/guides/kubernetes-config)): Read runtime configuration from Kubernetes ConfigMaps and Secrets
- Camel Rest ([guide](https://docs.redhat.com/en/documentation/red_hat_build_of_apache_camel/4.4/html/red_hat_build_of_apache_camel_for_quarkus_reference/camel-quarkus-extensions-reference#extensions-rest-whats-inside)): Expose REST services and their OpenAPI Specification or call external REST services

## Provided Code

### YAML Config

Configure your application with YAML

[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

The Quarkus application configuration is located in `src/main/resources/application.yml`.

## OPTIONAL - How to secure the API using _Red Hat 3scale API Management_ and _Red Hat SSO 7_

### :bulb: Pre-requisite

- A running [_Red Hat 3scale API Management v2.13_](https://access.redhat.com/documentation/en-us/red_hat_3scale_api_management/2.13) platform and [_Red Hat SSO 7.6_](https://access.redhat.com/documentation/en-us/red_hat_single_sign-on/7.6) instance to secure the API.
- The [_3scale Toolbox_](https://access.redhat.com/documentation/en-us/red_hat_3scale_api_management/2.13/html/operating_3scale/the-threescale-toolbox) CLI installed.

### Create the API Product from the OpenAPI Specification

1. The following [3scale Toolbox](https://access.redhat.com/documentation/en-us/red_hat_3scale_api_management/2.13/html/operating_3scale/the-threescale-toolbox) command line imports the API in _Red Hat 3scale API Management_ and secures it using OpenID Connect from the OpenAPI Specification. _Red Hat SSO 7_ is used as the OpenID Connect Authorization Server.
    > :bulb: **NOTE:** Adapt the values according to your environment.

    ```shell
    3scale import openapi \
    --override-private-base-url='http://camel-quarkus-datagrid-tester.ceq-services-jvm.svc' \
    --oidc-issuer-type=keycloak \
    --oidc-issuer-endpoint='https://<replace_me_with_client_id>:<replace_me_with_client_secret>@sso.apps.cluster-njb6f.njb6f.sandbox2810.opentlc.com/auth/realms/openshift-cluster' \
    --target_system_name=fruits_and_legumes_api \
    --verbose -d rhpds-apim-demo ./src/main/resources/openapi/openapi.json
    ```

2. The following command lines create the application plans and update the policy chain respectively using the resources in the [`config/threescale`](./config/threescale) folder.

    - **Application plans**:
        ```shell
        ### Basic plan
        3scale application-plan import \
        --file=./config/threescale/application_plans/basic-plan.yaml \
        --verbose rhpds-apim-demo fruits_and_legumes_api

        ### Premium plan
        3scale application-plan import \
        --file=./config/threescale/application_plans/premium-plan.yaml \
        --verbose rhpds-apim-demo fruits_and_legumes_api
        ```

    - **Policy chain**:
        ```shell
        3scale policies import \
        --file='./config/threescale/policies/policy_chain.yaml' \
        --verbose rhpds-apim-demo fruits_and_legumes_api
        ```

    - **Promotion of the new configuration to 3scale _staging_ and _production_ environments**:
        ```shell
        ## Promote the APIcast configuration to the Staging Environment
        3scale proxy deploy rhpds-apim-demo fruits_and_legumes_api --verbose

        ## Promote latest staging Proxy Configuration to the production environment
        3scale proxy-config promote rhpds-apim-demo fruits_and_legumes_api --verbose
        ```