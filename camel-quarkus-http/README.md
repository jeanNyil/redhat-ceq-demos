# Camel-Quarkus-Http

This project leverages **Red Hat build of Quarkus 2.7.x**, the Supersonic Subatomic Java Framework. More specifically, the project is implemented using [**Red Hat Camel Extensions for Quarkus 2.7.x**](https://access.redhat.com/documentation/en-us/red_hat_integration/2022.q3/html/getting_started_with_camel_extensions_for_quarkus/index).

It exposes the following RESTful service endpoints  using the **Apache Camel Quarkus Platform** extension: 
- `/fruits` : returns a list of hard-coded fruits (`name` and `description`) in JSON format. It also allows to add a `fruit` through the `POST` HTTP method
- `/legumes` : returns a list of hard-coded legumes (`name`and `description`) in JSON format.
- `/q/health` : returns the _Camel Quarkus MicroProfile_ health checks
- `/q/metrics` : the _Camel Quarkus MicroProfile_ metrics 

## Prerequisites
- JDK 17 installed with `JAVA_HOME` configured appropriately
- Apache Maven 3.8.1+

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Packaging and running the application on Red Hat OpenShift

### Pre-requisites
- Access to a [Red Hat OpenShift](https://access.redhat.com/documentation/en-us/openshift_container_platform) cluster v3 or v4
- User has self-provisioner privilege or has access to a working OpenShift project

1. Login to the OpenShift cluster
    ```shell script
    oc login ...
    ```
2. Create an OpenShift project or use your existing OpenShift project. For instance, to create `ceq-services-jvm`
    ```shell script
    oc new-project ceq-services-jvm --display-name="Red Hat Camel Extensions for Quarkus Apps - JVM Mode"
    ```
3. Use either the _**S2I binary workflow**_ or _**S2I source workflow**_ to deploy the `camel-quarkus-http` app as described below.

### OpenShift S2I binary workflow 

This leverages the _Quarkus OpenShift_ extension and is only recommended for development and testing purposes.

```shell script
./mvnw clean package -Dquarkus.kubernetes.deploy=true
```
```shell script
[...]
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Successfully pushed image-registry.openshift-image-registry.svc:5000/ceq-services-jvm/camel-quarkus-http@sha256:b4936f8d532f8436841b2fc255ffbb4b536f03093acd199ddb978f9214177a61
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Push successful
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Deploying to openshift server: https://api.cluster-tjldv.tjldv.sandbox661.opentlc.com:6443/ in namespace: ceq-services-jvm.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ServiceAccount camel-quarkus-http.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Service camel-quarkus-http.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Role view-secrets.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: RoleBinding camel-quarkus-http-view.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: RoleBinding camel-quarkus-http-view-secrets.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream camel-quarkus-http.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream openjdk-17.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: BuildConfig camel-quarkus-http.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: DeploymentConfig camel-quarkus-http.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Route camel-quarkus-http.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] The deployed application can be accessed at: http://camel-quarkus-http-ceq-services-jvm.apps.cluster-tjldv.tjldv.sandbox661.opentlc.com
[INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 62873ms
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:59 min
[INFO] Finished at: 2022-07-26T20:13:53+02:00
[INFO] ------------------------------------------------------------------------
```

### OpenShift S2I source workflow (recommended for PRODUCTION use)

1. Make sure the latest supported OpenJDK 17 image is imported in OpenShift
    ```shell script
    oc import-image --confirm openjdk-17-ubi8 \
    --from=registry.access.redhat.com/ubi8/openjdk-17:1.11 \
    -n openshift
    ```
2. Create the `view-secrets` role and bind it, along with the `view` cluster role, to the `default` service account used to run the quarkus application. These permissions allow the `default` service account to access secrets.
    ```shell script
    oc apply -f - <<EOF
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: Role
    metadata:
        labels:
            app.kubernetes.io/name: camel-quarkus-http
            app.kubernetes.io/version: 1.0.0
        name: view-secrets
    rules:
        - apiGroups:
            - ""
          resources:
            - secrets
          verbs:
            - get
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: RoleBinding
    metadata:
        labels:
            app.kubernetes.io/name: camel-quarkus-http
            app.kubernetes.io/version: 1.0.0
        name: camel-quarkus-http-view
    roleRef:
        kind: ClusterRole
        apiGroup: rbac.authorization.k8s.io
        name: view
    subjects:
        - kind: ServiceAccount
          name: camel-quarkus-http
    ---
    apiVersion: rbac.authorization.k8s.io/v1
    kind: RoleBinding
    metadata:
        labels:
            app.kubernetes.io/name: camel-quarkus-http
            app.kubernetes.io/version: 1.0.0
        name: camel-quarkus-http-view-secrets
    roleRef:
        kind: Role
        apiGroup: rbac.authorization.k8s.io
        name: view-secrets
    subjects:
        - kind: ServiceAccount
          name: camel-quarkus-http
    ---
    EOF
    ```
3. Create the `quarkus-opentracing-endpoint-secret` containing the _QUARKUS OPENTRACING_ [endpoint configuration options](https://quarkus.io/version/main/guides/opentracing#configuration-reference). These options are leveraged by the _Camel Quarkus Opentracing_ extension to connect to the jaeger collector. Adapt the `quarkus.jaeger.endpoint`according to your environment.

    > :warning: _Replace values with your AMQP broker environment_
    ```shell script
    oc apply -f - <<EOF
    ---
    apiVersion: v1
    kind: Secret
    metadata:
        labels:
            app.kubernetes.io/name: camel-quarkus-http
            app.kubernetes.io/version: 1.0.0
        name: quarkus-opentracing-endpoint-secret
    stringData:
        quarkus.jaeger.endpoint: "http://jaeger-all-in-one-inmemory-collector.ceq-services-jvm.svc:14268/api/traces"
    type: Opaque
    EOF
    ``` 
4. Create the `camel-quarkus-http` OpenShift application from the git repository
    ```shell script
    oc new-app https://github.com/jeanNyil/redhat-ceq-demos.git \
    --context-dir=camel-quarkus-http \
    --name=camel-quarkus-http \
    --image-stream="openshift/openjdk-17-ubi8" \
    --labels=app.kubernetes.io/name=camel-quarkus-http \
    --labels=app.kubernetes.io/version=1.0.0 \
    --labels=app.openshift.io/runtime=quarkus
    ```
5. Follow the log of the S2I build
    ```shell script
    oc logs bc/camel-quarkus-http -f
    ```
    ```shell script
    Cloning "https://github.com/jeanNyil/redhat-ceq-demos.git" ...
            Commit: 00e44699862ae9203942d15f00f443f95fa000c5 (Upgraded to Red Hat CEQ 2.7.6 + some refactoring)
            Author: Jean Armand Nyilimbibi <jean.nyilimbibi@gmail.com>
            Date:   Tue Jul 26 20:18:04 2022 +0200
    [...]
    Successfully pushed image-registry.openshift-image-registry.svc:5000/ceq-services-jvm/camel-quarkus-http@sha256:d188b5e72dbdaf7ef266c3125d3593a1b5981300ee12288807a3a6f18f8ad6e9
    Push successful
    ```
6. Create a non-secure route to expose the `camel-quarkus-http` service outside the OpenShift cluster
    ```shell script
    oc expose svc/camel-quarkus-http
    ```

## Testing the application on OpenShift

1. Get the OpenShift route hostname
    ```shell script
    URL="http://$(oc get route camel-quarkus-http -o jsonpath='{.spec.host}')"
    ```
2. Test the `/fruits` endpoint
    ```shell script
    curl -w '\n' $URL/fruits
    ```
    ```json
    [ {
        "name" : "Apple",
        "description" : "Winter fruit"
    }, {
        "name" : "Pineapple",
        "description" : "Tropical fruit"
    }, {
        "name" : "Mango",
        "description" : "Tropical fruit"
    }, {
        "name" : "Banana",
        "description" : "Tropical fruit"
    } ]
    ```
3. Test the `/legumes` endpoint
    ```shell script
    curl -w '\n' $URL/legumes
    ```
    ```json
    [ {
        "name" : "Carrot",
        "description" : "Root vegetable, usually orange"
    }, {
        "name" : "Zucchini",
        "description" : "Summer squash"
    } ]
    ```
4. Test the `/q/health` endpoint
    ```shell script
    curl -w '\n' $URL/q/health
    ```
    ```json
    {
        "status": "UP",
        "checks": [
            {
                "name": "camel-liveness-checks",
                "status": "UP"
            },
            {
                "name": "camel-readiness-checks",
                "status": "UP",
                "data": {
                    "invocation.count": "2",
                    "context.name": "camel-quarkus-http",
                    "success.count": "2",
                    "invocation.time": "2022-07-26T19:58:41.547988+02:00[Europe/Paris]",
                    "context.version": "3.14.2.redhat-00047",
                    "context.status": "Started",
                    "failure.count": "0",
                    "context": "UP",
                    "route.id": "legumes-restful-route",
                    "route.context.name": "camel-quarkus-http",
                    "route.status": "Started",
                    "consumer:fruits-restful-route": "UP",
                    "consumer:legumes-restful-route": "UP",
                    "route:fruits-restful-route": "UP",
                    "route:legumes-restful-route": "UP"
                }
            }
        ]
    }
    ```
5. Test the `/q/health/live` endpoint
    ```shell script
    curl -w '\n' $URL/q/health/live
    ```
    ```json
    {
        "status": "UP",
        "checks": [
            {
                "name": "camel-liveness-checks",
                "status": "UP"
            }
        ]
    }
    ```
6. Test the `/q/health/ready` endpoint
    ```shell script
    curl -w '\n' $URL/q/health/ready
    ```
    ```json
    {
        "status": "UP",
        "checks": [
            {
                "name": "camel-readiness-checks",
                "status": "UP",
                "data": {
                    "invocation.count": "3",
                    "context.name": "camel-quarkus-http",
                    "success.count": "3",
                    "invocation.time": "2022-07-26T19:59:26.535274+02:00[Europe/Paris]",
                    "context.version": "3.14.2.redhat-00047",
                    "context.status": "Started",
                    "failure.count": "0",
                    "context": "UP",
                    "route.id": "legumes-restful-route",
                    "route.context.name": "camel-quarkus-http",
                    "route.status": "Started",
                    "consumer:fruits-restful-route": "UP",
                    "consumer:legumes-restful-route": "UP",
                    "route:fruits-restful-route": "UP",
                    "route:legumes-restful-route": "UP"
                }
            }
        ]
    }
    ```
7. Test the `/q/metrics` endpoint
    ```shell script
    curl -w '\n' $URL/q/metrics
    ```
    ```shell script
    [...]
    # HELP application_camel_context_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_context_exchanges_total counter
    application_camel_context_exchanges_total{camelContext="camel-1"} 14.0
    [...]
    # HELP application_camel_route_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_route_exchanges_total counter
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="fruits-restful-route"} 9.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="legumes-restful-route"} 5.0
    [...]
    ```

## Testing using [Postman](https://www.postman.com/)

Import the provided Postman Collection for testing: [tests/Camel-Quarkus-Http.postman_collection.json](./tests/Camel-Quarkus-Http.postman_collection.json)
 
![Camel-Quarkus-Http.postman_collection.png](../_images/Camel-Quarkus-Http.postman_collection.png)

## Creating a native executable

### Running locally

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/camel-quarkus-http-1.0.0-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/building-native-image.

### Deploying the native executable as an _OpenShift Serverless_ service

#### Prerequisites

- Access to a [Red Hat OpenShift](https://access.redhat.com/documentation/en-us/openshift_container_platform) 4 cluster
    - _[OpenShift Serverless](https://access.redhat.com/documentation/en-us/openshift_container_platform/4.6/html/serverless_applications/installing-openshift-serverless-1#installing-openshift-serverless)_ operator is installed
    - _[OpenShift Knative Serving](https://access.redhat.com/documentation/en-us/openshift_container_platform/4.6/html/serverless_applications/installing-openshift-serverless-1#installing-knative-serving)_ is installed
- _[Podman](https://podman.io/)_ or _[Docker](https://www.docker.com/)_ container-runtime environment for native compilation
- The _[kn](https://access.redhat.com/documentation/en-us/openshift_container_platform/4.6/html/serverless_applications/installing-openshift-serverless-1#installing-kn)_ CLI tool is installed
- User has self-provisioner privilege or has access to a working OpenShift project

#### Instructions

1. Login to the OpenShift cluster
    ```shell script
    oc login ...
    ```

2. Create an OpenShift project or use your existing OpenShift project. For instance, to create `ceq-services-native`
    ```shell script
    oc new-project ceq-services-native --display-name="Red Hat Camel Extensions for Quarkus Apps - Native Mode"
    ```

3. Build a Linux executable using a container build. Compiling a Quarkus application to a native executable consumes a lot of memory during analysis and optimization. You can limit the amount of memory used during native compilation by setting the `quarkus.native.native-image-xmx` configuration property. Setting low memory limits might increase the build time.
    1. For Docker use:
        ```shell script
        ./mvnw package -Pnative -Dquarkus.native.container-build=true \
        -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel:20.3-java11 \
        -Dquarkus.native.native-image-xmx=6g
        ```
    2. For Podman use:
        ```shell script
        ./mvnw package -Pnative -Dquarkus.native.container-build=true \
        -Dquarkus.native.container-runtime=podman \
        -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel:20.3-java11 \
        -Dquarkus.native.native-image-xmx=6g
        ```
    ```shell script
    [...]
    [INFO] [io.quarkus.deployment.pkg.steps.JarResultBuildStep] Building native image source jar: /Users/jeannyil/Workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-http/target/camel-quarkus-http-1.0.0-native-image-source-jar/camel-quarkus-http-1.0.0-runner.jar
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Building native image from /Users/jeannyil/Workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-http/target/camel-quarkus-http-1.0.0-native-image-source-jar/camel-quarkus-http-1.0.0-runner.jar
    [...]
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Running Quarkus native-image plugin on GraalVM Version 20.3.2.0-Final (Mandrel Distribution) (Java Version 11.0.11+9)
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] docker run -v /Users/jeannyil/Workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-http/target/camel-quarkus-http-1.0.0-native-image-source-jar:/project:z --env LANG=C --rm quay.io/quarkus/ubi-quarkus-mandrel:20.3-java11 -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=1 -J-Duser.language=en -J-Dfile.encoding=UTF-8 --initialize-at-build-time= -H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy\$BySpaceAndTime -H:+JNI -H:+AllowFoldMethods -jar camel-quarkus-http-1.0.0-runner.jar -H:FallbackThreshold=0 -H:+ReportExceptionStackTraces -J-Xmx6g -H:+AddAllCharsets -H:EnableURLProtocols=http,https --enable-all-security-services -H:-UseServiceLoaderFeature -H:+StackTrace camel-quarkus-http-1.0.0-runner
    [...]
    [INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 717650ms
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time:  12:17 min
    [INFO] Finished at: 2021-05-27T14:21:36+02:00
    [INFO] ------------------------------------------------------------------------
    ```

4. Create the `camel-quarkus-http` container image using the _OpenShift Docker build_ strategy. This strategy creates a container using a build configuration in the cluster.
    1. Create a build config based on the [`src/main/docker/Dockerfile.native`](./src/main/docker/Dockerfile.native) file:
        ```shell script
        cat src/main/docker/Dockerfile.native | oc new-build \
        --name camel-quarkus-http --strategy=docker --dockerfile -
        ```
    2. Build the project:
        ```shell script
        oc start-build camel-quarkus-http --from-dir . -F
        ```
        ```shell script
        Uploading directory "." as binary input for the build ...
        ....
        Uploading finished
        build.build.openshift.io/camel-quarkus-http-2 started
        Receiving source from STDIN as archive ...
        Replaced Dockerfile FROM image registry.access.redhat.com/ubi8/ubi-minimal:8.1
        [...]
        Successfully pushed image-registry.openshift-image-registry.svc:5000/ceq-services-native/camel-quarkus-http@sha256:c894bb0368acdf857e317f10a3e27f5b052a988de8c014542ea839ea6ad4fbb0
        Push successful
        ```

5. Deploy the `camel-quarkus-http` as a serverless application
    ```shell script
    kn service create camel-quarkus-http \
    --label app.openshift.io/runtime=quarkus \
    --image image-registry.openshift-image-registry.svc:5000/ceq-services-native/camel-quarkus-http:latest
    ```

6. To verify that the `camel-quarkus-http` service is ready, enter the following command.
    ```shell script
    kn service list camel-quarkus-http
    ```
    The output in the column called "READY" reads `True` if the service is ready.

## Start-up time comparison on the same OpenShift cluster

### JVM mode

```shell script
[...]
2021-05-27 10:10:38,433 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.7.0 (camel-1) is starting
[...]
2021-05-27 10:10:38,506 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: fruits-restful-route started and consuming from: platform-http:///fruits
2021-05-27 10:10:38,507 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: legumes-restful-route started and consuming from: platform-http:///legumes
2021-05-27 10:10:38,508 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Total 2 routes, of which 2 are started
2021-05-27 10:10:38,508 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.7.0 (camel-1) started in 75ms
2021-05-27 10:10:38,581 INFO  [io.quarkus] (main) camel-quarkus-http 1.0.0 on JVM (powered by Quarkus 1.11.6.Final-redhat-00001) started in 1.040s. Listening on: http://0.0.0.0:8080
2021-05-27 10:10:38,581 INFO  [io.quarkus] (main) Profile prod activated.
2021-05-27 10:10:38,581 INFO  [io.quarkus] (main) Installed features: [camel-attachments, camel-core, camel-jackson, camel-microprofile-health, camel-microprofile-metrics, camel-platform-http, camel-support-common, cdi, config-yaml, kubernetes, mutiny, smallrye-context-propagation, smallrye-health, smallrye-metrics, vertx, vertx-web]
```

### Native mode

```shell script
[...]
2021-05-27 12:59:44,863 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.7.0 (camel-1) is starting
[...]
2021-05-27 12:59:44,864 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: fruits-restful-route started and consuming from: platform-http:///fruits
2021-05-27 12:59:44,864 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: legumes-restful-route started and consuming from: platform-http:///legumes
2021-05-27 12:59:44,864 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Total 2 routes, of which 2 are started
2021-05-27 12:59:44,864 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.7.0 (camel-1) started in 1ms
2021-05-27 12:59:44,865 INFO  [io.quarkus] (main) camel-quarkus-http 1.0.0 native (powered by Quarkus 1.11.6.Final-redhat-00001) started in 0.020s. Listening on: http://0.0.0.0:8080
2021-05-27 12:59:44,865 INFO  [io.quarkus] (main) Profile prod activated.
2021-05-27 12:59:44,865 INFO  [io.quarkus] (main) Installed features: [camel-attachments, camel-core, camel-jackson, camel-microprofile-health, camel-microprofile-metrics, camel-platform-http, camel-support-common, cdi, config-yaml, kubernetes, mutiny, smallrye-context-propagation, smallrye-health, smallrye-metrics, vertx, vertx-web]
[...]
```

## Related Guides

- OpenShift ([guide](https://quarkus.io/guides/deploying-to-openshift)): Generate OpenShift resources from annotations
- Camel Platform HTTP ([guide](https://access.redhat.com/documentation/en-us/red_hat_integration/2.latest/html/camel_extensions_for_quarkus_reference/extensions-platform-http)): Expose HTTP endpoints using the HTTP server available in the current platform
- Camel MicroProfile Health ([guide](https://access.redhat.com/documentation/en-us/red_hat_integration/2.latest/html/camel_extensions_for_quarkus_reference/extensions-microprofile-health)): Expose Camel health checks via MicroProfile Health
- Camel MicroProfile Metrics ([guide](https://access.redhat.com/documentation/en-us/red_hat_integration/2.latest/html/camel_extensions_for_quarkus_reference/extensions-microprofile-metrics)): Expose metrics from Camel routes
- Camel Jackson ([guide](https://access.redhat.com/documentation/en-us/red_hat_integration/2.latest/html/camel_extensions_for_quarkus_reference/extensions-jackson)): Marshal POJOs to JSON and back using Jackson
- YAML Configuration ([guide](https://quarkus.io/guides/config#yaml)): Use YAML to configure your Quarkus application
- RESTEasy JAX-RS ([guide](https://quarkus.io/guides/rest-json)): REST endpoint framework implementing JAX-RS and more
- Kubernetes Config ([guide](https://quarkus.io/guides/kubernetes-config)): Read runtime configuration from Kubernetes ConfigMaps and Secrets
- Camel OpenTracing ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/opentracing.html)): Distributed tracing using OpenTracing

## Provided Code

### YAML Config

Configure your application with YAML

[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

The Quarkus application configuration is located in `src/main/resources/application.yml`.

### RESTEasy JAX-RS

Easily start your RESTful Web Services

[Related guide section...](https://quarkus.io/guides/getting-started#the-jax-rs-resources)