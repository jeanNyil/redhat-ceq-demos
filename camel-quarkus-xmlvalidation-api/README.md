# Camel-Quarkus-XmlValidation-Api project

This project leverages **Red Hat build of Quarkus 1.7.x**, the Supersonic Subatomic Java Framework.

It exposes the following RESTful service endpoints  using the **Apache Camel Quarkus Platform extension**: 
- `/validateMembershipXML` : validates a sample `Membership` XML instance through the `POST` HTTP method.
- `/health` : returns the _Camel Quarkus MicroProfile_ health checks
- `/metrics` : the _Camel Quarkus MicroProfile_ metrics

## Prerequisites
- JDK 11 installed with `JAVA_HOME` configured appropriately
- Apache Maven 3.6.2+

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application locally

The application can be packaged using `./mvnw package`.
It produces the `camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner.jar`.

## Packaging and running the application on Red Hat OpenShift

### Pre-requisites
- Access to a [Red Hat OpenShift](https://access.redhat.com/documentation/en-us/openshift_container_platform) cluster v3 or v4
- User has self-provisioner privilege or has access to a working OpenShift project

1. Login to the OpenShift cluster
    ```zsh
    oc login ...
    ```
2. Create an OpenShift project or use your existing OpenShift project. For instance, to create `camel-quarkus`
    ```zsh
    oc new-project camel-quarkus-jvm --display-name="Apache Camel Quarkus Apps - JVM Mode"
    ```
3. Use either the _**S2I binary workflow**_ or _**S2I source workflow**_ to deploy the `Camel-Quarkus-XmlValidation-Api.postman_collection` app as described below.

### OpenShift S2I binary workflow 

This leverages the _Quarkus OpenShift_ extension and is only recommended for development and testing purposes.

```zsh
./mvnw clean package -Dquarkus.kubernetes.deploy=true
```
```zsh
[...]
[INFO] [io.quarkus.deployment.pkg.steps.JarResultBuildStep] Building thin jar: /Users/jeannyil/Workdata/myGit/Quarkus/upstream-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/target/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner.jar
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeploy] Kubernetes API Server at 'https://api.cluster-2422.sandbox1120.opentlc.com:6443/' successfully contacted.
[...]
[INFO] [io.quarkus.container.image.s2i.deployment.S2iProcessor] Performing s2i binary build with jar on server: https://api.cluster-2422.sandbox1120.opentlc.com:6443/ in namespace:camel-quarkus.
[...]
[INFO] [io.quarkus.container.image.s2i.deployment.S2iProcessor] Successfully pushed image-registry.openshift-image-registry.svc:5000/camel-quarkus/camel-quarkus-xmlvalidation-api@sha256:3d3d8060f906acb8d942fb28283cbbc344377ab20d982cd1eec2041a8f20f521
[INFO] [io.quarkus.container.image.s2i.deployment.S2iProcessor] Push successful
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Deploying to openshift server: https://api.cluster-2422.sandbox1120.opentlc.com:6443/ in namespace: camel-quarkus.
[...]
```

### OpenShift S2I source workflow (recommended for PRODUCTION use)

1. Make sure the latest supported OpenJDK 11 image is imported in OpenShift
    ```zsh
    oc import-image --confirm openjdk-11-ubi8 \
    --from=registry.access.redhat.com/ubi8/openjdk-11 \
    -n openshift
    ```
2. Create the `Camel-Quarkus-XmlValidation-Api.postman_collection` OpenShift application from the git repository
    ```zsh
    oc new-app https://github.com/jeanNyil/upstream-quarkus-camel-demos.git \
    --context-dir=camel-quarkus-xmlvalidation-api \
    --name=camel-quarkus-xmlvalidation-api \
    --image-stream="openshift/openjdk-11-ubi8"
    ```
3. Follow the log of the S2I build
    ```zsh
    oc logs bc/camel-quarkus-xmlvalidation-api -f
    ```
    ```zsh
    Cloning "https://github.com/jeanNyil/upstream-quarkus-camel-demos.git" ...
            Commit: 33b40b3149e109557b8587c14d4aaa88cca5a590 (Updated README)
            Author: Jean Armand Nyilimbibi <jean.nyilimbibi@gmail.com>
            Date:   Mon Sep 7 21:14:41 2020 +0200
    [...]
    Successfully pushed image-registry.openshift-image-registry.svc:5000/camel-quarkus/camel-quarkus-xmlvalidation-api-s2i@sha256:024fcbaf80ec49e5f044f9af96e176e5fb16ba3325e7d6cee5d70c4884289a69
    Push successful
    ```
4. Create a non-secure route to expose the `Camel-Quarkus-XmlValidation-Api.postman_collection` service outside the OpenShift cluster
    ```zsh
    oc expose svc/camel-quarkus-xmlvalidation-api
    ```

## Testing the application on OpenShift

1. Get the OpenShift route hostname
    ```zsh
    URL="http://$(oc get route camel-quarkus-xmlvalidation-api -o jsonpath='{.spec.host}')"
    ```
2. Test the `/validateMembershipJSON` endpoint
    ```zsh
    curl -w '\n' -X POST -H 'Content-Type: text/xml' \
    -d '<?xml version="1.0" encoding="UTF-8"?><p:membership xmlns:p="http://www.github.com/jeanNyil/schemas/membership/v1.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><p:requestType>API</p:requestType><p:requestID>5948</p:requestID><p:memberID>85623617</p:memberID><p:status>A</p:status><p:enrolmentDate>2020-09-05</p:enrolmentDate><p:changedBy>JaLiLa</p:changedBy><p:forcedLevelCode>69</p:forcedLevelCode><p:vipOnInvitation>Y</p:vipOnInvitation><p:startDate>2020-09-05</p:startDate><p:endDate>2100-09-05</p:endDate></p:membership>' \
    $URL/validateMembershipXML
    ```
    ```json
    {
        "validationResult" : {
            "status" : "OK"
        }
    }
    ```

3. Test the `/health` endpoint
    ```zsh
    curl -w '\n' $URL/health
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
                "name": "camel-context-check",
                "status": "UP",
                "data": {
                    "contextStatus": "Started",
                    "name": "camel-1"
                }
            },
            {
                "name": "camel-readiness-checks",
                "status": "UP"
            }
        ]
    }
    ```
4. Test the `/health/live` endpoint
    ```zsh
    curl -w '\n' $URL/health/live
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
5. Test the `/health/ready` endpoint
    ```zsh
    curl -w '\n' $URL/health/ready
    ```
    ```json
    {
        "status": "UP",
        "checks": [
            {
                "name": "camel-readiness-checks",
                "status": "UP"
            },
            {
                "name": "camel-context-check",
                "status": "UP",
                "data": {
                    "contextStatus": "Started",
                    "name": "camel-1"
                }
            }
        ]
    }
    ```
6. Test the `/metrics` endpoint
    ```zsh
    curl -w '\n' $URL/metrics
    ```
    ```zsh
    [...]
    # HELP application_camel_context_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_context_exchanges_total counter
    application_camel_context_exchanges_total{camelContext="camel-1"} 30.0
    [...]
    # HELP application_camel_route_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_route_exchanges_total counter
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="api-doc-route"} 0.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="common-500-http-code-route"} 0.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="custom-http-error-route"} 0.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="validate-membership-xml-route"} 30.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="xml-validation-api-route"} 30.0
    [...]
    ```

## Testing using [Postman](https://www.postman.com/)

Import the provided Postman Collection for testing: [tests/Camel-Quarkus-XmlValidation-Api.postman_collection.json](./tests/Camel-Quarkus-XmlValidation-Api.postman_collection.json) 
![Camel-Quarkus-XmlValidation-Api.postman_collection.png](../_images/Camel-Quarkus-XmlValidation-Api.postman_collection.png)

## Creating a native executable

### Running locally

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner`

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
    ```zsh
    oc login ...
    ```

2. Create an OpenShift project or use your existing OpenShift project. For instance, to create `camel-quarkus-native`
    ```zsh
    oc new-project camel-quarkus-native --display-name="Apache Camel Quarkus Apps - Native Mode"
    ```

3. Build a Linux executable using a container build. Compiling a Quarkus application to a native executable consumes a lot of memory during analysis and optimization. You can limit the amount of memory used during native compilation by setting the `quarkus.native.native-image-xmx` configuration property. Setting low memory limits might increase the build time.
    1. For Docker use:
        ```zsh
        ./mvnw package -Pnative -Dquarkus.native.container-build=true \
        -Dquarkus.native.builder-image=registry.access.redhat.com/quarkus/mandrel-20-rhel8:20.1 \
        -Dquarkus.native.native-image-xmx=6g
        ```
    2. For Podman use:
        ```zsh
        ./mvnw package -Pnative -Dquarkus.native.container-build=true \
        -Dquarkus.native.container-runtime=podman \
        -Dquarkus.native.builder-image=registry.access.redhat.com/quarkus/mandrel-20-rhel8:20.1 \
        -Dquarkus.native.native-image-xmx=6g
        ```
    ```zsh
    [...]
    [INFO] [io.quarkus.deployment.pkg.steps.JarResultBuildStep] Building native image source jar: /Users/jnyilimb/workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/target/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-native-image-source-jar/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner.jar
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Building native image from /Users/jnyilimb/workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/target/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-native-image-source-jar/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner.jar
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Checking image status registry.access.redhat.com/quarkus/mandrel-20-rhel8:20.1
    20.1: Pulling from quarkus/mandrel-20-rhel8
    Digest: sha256:f4914b8717883835a28f61b5bf25421671adc968474da7042c9ca93ab63251a2
    Status: Image is up to date for registry.access.redhat.com/quarkus/mandrel-20-rhel8:20.1
    registry.access.redhat.com/quarkus/mandrel-20-rhel8:20.1
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Running Quarkus native-image plugin on GraalVM Version 20.1.0.3_0-1 (Mandrel Distribution) (Java Version 11.0.9+11-LTS)
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] docker run -v /Users/jnyilimb/workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/target/camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-native-image-source-jar:/project:z --env LANG=C --rm registry.access.redhat.com/quarkus/mandrel-20-rhel8:20.1 -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=1 -J-Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true -J-Duser.language=en -J-Dfile.encoding=UTF-8 --initialize-at-build-time= -H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy\$BySpaceAndTime -H:+JNI -jar camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner.jar -H:FallbackThreshold=0 -H:+ReportExceptionStackTraces -J-Xmx6g -H:+AddAllCharsets -H:EnableURLProtocols=http,https --enable-all-security-services -H:-UseServiceLoaderFeature -H:+StackTrace camel-quarkus-xmlvalidation-api-1.0.0-SNAPSHOT-runner
    [...]
    [INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 867844ms
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time:  14:34 min
    [INFO] Finished at: 2020-11-03T22:22:42+01:00
    [INFO] ------------------------------------------------------------------------
    ```

4. Create the `camel-quarkus-xmlvalidation-api` container image using the _OpenShift Docker build_ strategy. This strategy creates a container using a build configuration in the cluster.
    1. Create a build config based on the [`src/main/docker/Dockerfile.native`](./src/main/docker/Dockerfile.native) file:
        ```zsh
        cat src/main/docker/Dockerfile.native | oc new-build \
        --name camel-quarkus-xmlvalidation-api --strategy=docker --dockerfile -
        ```
    2. Build the project:
        ```zsh
        oc start-build camel-quarkus-xmlvalidation-api --from-dir . -F
        ```
        ```zsh
        Uploading directory "." as binary input for the build ...
        ....
        Uploading finished
        build.build.openshift.io/camel-quarkus-xmlvalidation-api-2 started
        Receiving source from STDIN as archive ...
        Replaced Dockerfile FROM image registry.access.redhat.com/ubi8/ubi-minimal:8.1
        [...]
        Successfully pushed image-registry.openshift-image-registry.svc:5000/camel-quarkus-native/camel-quarkus-xmlvalidation-api@sha256:50ed8d92c8871756d223826ffff4959628b4368ff715e9deb05585781c747d28
        Push successful
        ```

5. Deploy the `camel-quarkus-xmlvalidation-api` as a serverless application
    ```zsh
    kn service create camel-quarkus-xmlvalidation-api \
    --label app.openshift.io/runtime=quarkus \
    --image image-registry.openshift-image-registry.svc:5000/camel-quarkus-native/camel-quarkus-xmlvalidation-api:latest
    ```

6. To verify that the `camel-quarkus-xmlvalidation-api` service is ready, enter the following command.
    ```zsh
    kn service list camel-quarkus-xmlvalidation-api
    ```
    The output in the column called "READY" reads `True` if the service is ready.

## Start-up time comparison on the same OpenShift cluster

### JVM mode

```zsh
[...]
2020-11-04 19:45:48,218 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: common-500-http-code-route started and consuming from: direct://common-500
2020-11-04 19:45:48,219 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: custom-http-error-route started and consuming from: direct://custom-http-error
2020-11-04 19:45:48,219 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: validate-membership-xml-route started and consuming from: direct://validateMembershipXML
2020-11-04 19:45:48,222 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: api-doc-route started and consuming from: platform-http:///validateMembershipXML/api-doc
2020-11-04 19:45:48,225 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: xml-validation-api-route started and consuming from: platform-http:///validateMembershipXML
2020-11-04 19:45:48,226 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Total 5 routes, of which 5 are started
2020-11-04 19:45:48,226 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.4.2 (camel-1) started in 0.255 seconds
2020-11-04 19:45:48,311 INFO  [io.quarkus] (main) camel-quarkus-xmlvalidation-api 1.0.0-SNAPSHOT on JVM (powered by Quarkus 1.7.5.Final-redhat-00007) started in 1.476s. Listening on: http://0.0.0.0:8080
[...]
```

### Native mode

```zsh
[...]
2020-11-04 20:06:01,688 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: api-doc-route started and consuming from: platform-http:///validateMembershipXML/api-doc
2020-11-04 20:06:01,688 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: xml-validation-api-route started and consuming from: platform-http:///validateMembershipXML
2020-11-04 20:06:01,688 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: validate-membership-xml-route started and consuming from: direct://validateMembershipXML
2020-11-04 20:06:01,688 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: common-500-http-code-route started and consuming from: direct://common-500
2020-11-04 20:06:01,688 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: custom-http-error-route started and consuming from: direct://custom-http-error
2020-11-04 20:06:01,689 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Total 5 routes, of which 5 are started
2020-11-04 20:06:01,689 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.4.2 (camel-1) started in 0.003 seconds
2020-11-04 20:06:01,694 INFO  [io.quarkus] (main) camel-quarkus-xmlvalidation-api 1.0.0-SNAPSHOT native (powered by Quarkus 1.7.5.Final-redhat-00007) started in 0.069s. Listening on: http://0.0.0.0:8080
[...]
```