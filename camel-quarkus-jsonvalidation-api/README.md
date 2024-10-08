# Camel-Quarkus-JsonValidation-Api project

This project leverages **Red Hat build of Quarkus 2.2.x**, the Supersonic Subatomic Java Framework.

It exposes the following RESTful service endpoints  using **Apache Camel REST DSL** and the **Apache Camel Quarkus Platform HTTP** extension:
- `/validateMembershipJSON` : validates a sample `Membership` JSON instance through the `POST` HTTP method.
- `/openapi.json`: returns the OpenAPI 3.0 specification for the service.
- `/q/health` : returns the _Camel Quarkus MicroProfile_ health checks
- `/q/metrics` : the _Camel Quarkus MicroProfile_ metrics

## Prerequisites
- JDK 21 installed with `JAVA_HOME` configured appropriately
- Apache Maven 3.8.1+
- **OPTIONAL**: [**Jaeger**](https://www.jaegertracing.io/), a distributed tracing system for observability ([_open tracing_](https://opentracing.io/)).  :bulb: A simple way of starting a Jaeger tracing server is with `docker` or `podman`:
    1. Start the Jaeger tracing server:
        ```
        podman run --rm -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 -e COLLECTOR_OTLP_ENABLED=true \
        -p 6831:6831/udp -p 6832:6832/udp \
        -p 5778:5778 -p 16686:16686 -p 4317:4317 -p 4318:4318 -p 14250:14250  -p 14268:14268 -p 14269:14269 -p 9411:9411 \
        quay.io/jaegertracing/all-in-one:latest
        ```
    2. While the server is running, browse to http://localhost:16686 to view tracing events.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw clean compile quarkus:dev -Dquarkus.kubernetes-config.enabled=false
```

## Packaging and running the application locally

The application can be packaged using:
```shell
./mvnw clean package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using:
```shell
java -Dquarkus.kubernetes-config.enabled=false -jar target/quarkus-app/quarkus-run.jar
```

If you want to build an _über-jar_, execute the following command:
```shell
./mvnw clean package -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using:
```shell
java -Dquarkus.kubernetes-config.enabled=false -jar target/camel-quarkus-jsonvalidation-api-1.0.0-runner.jar
```

According to your environment, you may want to customize the Jaeger collector endpoint by adding the following run-time _system properties_:
- `quarkus.opentelemetry.tracer.exporter.otlp.endpoint`

Example:
```
java -Dquarkus.kubernetes-config.enabled=false -Dquarkus.opentelemetry.tracer.exporter.otlp.endpoint="http://localhost:4317" -jar target/quarkus-app/quarkus-run.jar
```

## Packaging and running the application on Red Hat OpenShift

### Pre-requisites
- Access to a [Red Hat OpenShift](https://access.redhat.com/documentation/en-us/openshift_container_platform) cluster v3 or v4
- User has self-provisioner privilege or has access to a working OpenShift project

1. Login to the OpenShift cluster
    ```shell
    oc login ...
    ```

2. Create an OpenShift project or use your existing OpenShift project. For instance, to create `camel-quarkus`
    ```shell
    oc new-project ceq-services-jvm --display-name="Red Hat build of Apache Camel for Quarkus Apps - JVM Mode"
    ```

3. Create an `allInOne` Jaeger instance.
    1. **IF NOT ALREADY INSTALLED**:
        1. Install, via OLM, the `Red Hat OpenShift distributed tracing platform` (Jaeger) operator with an `AllNamespaces` scope. :warning: Needs `cluster-admin` privileges
            ```shell
            oc create --save-config -f - <<EOF
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
        2. Verify the successful installation of the `Red Hat OpenShift distributed tracing platform` operator
            ```shell
            watch oc get sub,csv
            ```
    2. Create the `allInOne` Jaeger instance.
        ```shell
        oc create --save-config -f - <<EOF
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

4. Use either the _**S2I binary workflow**_ or _**S2I source workflow**_ to deploy the `camel-quarkus-jsonvalidation-api` app as described below.

### OpenShift S2I binary workflow 

This leverages the _Quarkus OpenShift_ extension and is only recommended for development and testing purposes.

```shell
./mvnw clean package -Dquarkus.openshift.deploy=true -Dquarkus.container-image.group=ceq-services-jvm
```
```shell
[...]
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Selecting target 'openshift' since it has the highest priority among the implicitly enabled deployment targets
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeploy] Kubernetes API Server at 'https://api.ocp4.jnyilimb.eu:6443/' successfully contacted.
[...]
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Performing openshift binary build with jar on server: https://api.ocp4.jnyilimb.eu:6443/ in namespace:ceq-services-jvm.
[...]
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Pushing image image-registry.openshift-image-registry.svc:5000/ceq-services-jvm/camel-quarkus-jsonvalidation-api:1.0.0 ...
[...]
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Deploying to openshift server: https://api.ocp4.jnyilimb.eu:6443/ in namespace: ceq-services-jvm.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ServiceAccount camel-quarkus-jsonvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Service camel-quarkus-jsonvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Role view-secrets.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: RoleBinding camel-quarkus-jsonvalidation-api-view.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: RoleBinding camel-quarkus-jsonvalidation-api-view-secrets.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream camel-quarkus-jsonvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream openjdk-11.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: BuildConfig camel-quarkus-jsonvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Deployment camel-quarkus-jsonvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Route camel-quarkus-jsonvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] The deployed application can be accessed at: http://camel-quarkus-jsonvalidation-api-ceq-services-jvm.apps.ocp4.jnyilimb.eu
[INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 74311ms
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:36 min
[INFO] Finished at: 2021-11-20T22:35:03+01:00
[INFO] ------------------------------------------------------------------------
```

### OpenShift S2I source workflow (recommended for PRODUCTION use)

1. Make sure the latest supported OpenJDK 21 image is imported in OpenShift
    ```shell
    oc import-image --confirm openjdk-11-ubi8 \
    --from=registry.access.redhat.com/ubi8/openjdk-11 \
    -n openshift
    ```
2. Create the `camel-quarkus-jsonvalidation-api` OpenShift application from the git repository
    ```shell
    oc new-app https://github.com/jeanNyil/redhat-ceq-demos.git \
    --context-dir=camel-quarkus-jsonvalidation-api \
    --name=camel-quarkus-jsonvalidation-api \
    --image-stream="openshift/openjdk-11-ubi8" \
    --labels=app.kubernetes.io/name=camel-quarkus-jsonvalidation-api \
    --labels=app.kubernetes.io/version=1.0.0 \
    --labels=app.openshift.io/runtime=camel
    ```
3. Follow the log of the S2I build
    ```shell
    oc logs bc/camel-quarkus-jsonvalidation-api -f
    ```
    ```shell
    Cloning "https://github.com/jeanNyil/redhat-ceq-demos.git" ...
            Commit: 7da594c26cdcb2e85cd94032415d72726b6eb162 (Upgraded to Red Hat build of Quarkus 1.11)
            Author: Jean Armand Nyilimbibi <jean.nyilimbibi@gmail.com>
            Date:   Thu May 27 12:32:37 2021 +0200
    [...]
    Successfully pushed image-registry.openshift-image-registry.svc:5000/ceq-services-jvm/camel-quarkus-jsonvalidation-api@sha256:75ba25fb35b1954ce10c6a4865facf32b56dbfeff43c709e58c82b6a0d5cd530
    Push successful
    ```
4. Create a non-secure route to expose the `camel-quarkus-jsonvalidation-api` service outside the OpenShift cluster
    ```shell
    oc expose svc/camel-quarkus-jsonvalidation-api
    ```

## Testing the application on OpenShift

1. Get the OpenShift route hostname
    ```shell
    URL="https://$(oc get route camel-quarkus-jsonvalidation-api -o jsonpath='{.spec.host}')"
    ```
2. Test the `/validateMembershipJSON` endpoint
    ```shell
    curl -w '\n' -X POST -H 'Content-Type: application/json' \
    -d '{"requestType": "API","requestID": 5948,"memberID": 85623617,"status": "A","enrolmentDate": "2020-09-05","changedBy": "JaLiLa","forcedLevelCode": "69","vipOnInvitation": "Y","startDate": "2020-09-05","endDate": "2100-09-05"}' \
    $URL/validateMembershipJSON
    ```
    ```json
    {
        "validationResult" : {
            "status" : "OK"
        }
    }
    ```
3. Test the `/openapi.json` endpoint
    ```shell
    curl -w '\n' $URL/openapi.json
    ```
    ```json
    {
        "openapi": "3.0.2",
        "info": {
            "title": "Sample JSON Validation API",
            "version": "1.0.0",
            "description": "A simple API to test the Camel json-schema-validator component",
            "contact": {
                "name": "Jean Nyilimbibi"
            },
            "license": {
                "name": "MIT License",
                "url": "https://opensource.org/licenses/MIT"
            }
        },
        "servers": [
            {
                "url": "http://camel-quarkus-json-validation-api.apps.ocp4.jnyilimb.eu",
                "description": "API Backend URL"
            }
        ],
        "paths": {
            "/validateMembershipJSON": {
                "post": {
                    "requestBody": {
                        "description": "A `Membership` JSON instance to be validated.",
                        "content": {
                            "application/json": {
                                "schema": {
                                    "$ref": "#/components/schemas/Membership"
                                }
                            }
                        },
                        "required": true
                    },
                    "responses": {
                        "200": {
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "$ref": "#/components/schemas/ValidationResult"
                                    },
                                    "examples": {
                                        "validationResult_200": {
                                            "value": {
                                                "validationResult": {
                                                    "status": "OK"
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "description": "`Membership`JSON data validated"
                        },
                        "400": {
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "$ref": "#/components/schemas/ValidationResult"
                                    },
                                    "examples": {
                                        "validationResult_400": {
                                            "value": {
                                                "validationResult": {
                                                    "status": "KO",
                                                    "errorMessage": "JSon validation error with 2 errors. Exchange[ID-sample-json-validation-api-1-nxgnq-1620389968195-0-427]"
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "description": "`Membership`JSON data not valid"
                        },
                        "500": {
                            "content": {
                                "application/json": {
                                    "schema": {
                                        "$ref": "#/components/schemas/Error"
                                    },
                                    "examples": {
                                        "error_500": {
                                            "value": {
                                                "error": {
                                                    "id": "500",
                                                    "description": "Internal Server Error",
                                                    "messages": [
                                                        "java.lang.Exception: Mocked error message"
                                                    ]
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "description": "Internal server error"
                        }
                    },
                    "operationId": "validateMembershipJSON",
                    "summary": "Validate Membership JSON instance",
                    "description": "Validates a `Membership` JSON instance",
                    "x-codegen-request-body-name": "body"
                }
            }
        },
        "components": {
            "schemas": {
                "Membership": {
                    "description": "Membership data ",
                    "required": [
                        "changedBy",
                        "endDate",
                        "enrolmentDate",
                        "memberID",
                        "requestID",
                        "requestType",
                        "vipOnInvitation"
                    ],
                    "type": "object",
                    "properties": {
                        "requestType": {
                            "type": "string"
                        },
                        "requestID": {
                            "format": "int32",
                            "type": "integer"
                        },
                        "memberID": {
                            "format": "int32",
                            "type": "integer"
                        },
                        "status": {
                            "maxLength": 1,
                            "minLength": 1,
                            "enum": [
                                "A",
                                "B",
                                "C"
                            ],
                            "type": "string"
                        },
                        "enrolmentDate": {
                            "format": "date",
                            "type": "string"
                        },
                        "changedBy": {
                            "type": "string"
                        },
                        "forcedLevelCode": {
                            "type": "string"
                        },
                        "vipOnInvitation": {
                            "maxLength": 1,
                            "minLength": 1,
                            "enum": [
                                "N",
                                "Y"
                            ],
                            "type": "string"
                        },
                        "startDate": {
                            "format": "date",
                            "type": "string"
                        },
                        "endDate": {
                            "format": "date",
                            "type": "string"
                        }
                    },
                    "example": {
                        "requestType": "API",
                        "requestID": 5948,
                        "memberID": 85623617,
                        "status": "A",
                        "enrolmentDate": "2019-06-16",
                        "changedBy": "jeanNyil",
                        "forcedLevelCode": "69",
                        "vipOnInvitation": "Y",
                        "startDate": "2019-06-16",
                        "endDate": "2100-06-16"
                    }
                },
                "ValidationResult": {
                    "description": "Validation Result   ",
                    "type": "object",
                    "properties": {
                        "validationResult": {
                            "type": "object",
                            "properties": {
                                "status": {
                                    "maxLength": 2,
                                    "minLength": 2,
                                    "enum": [
                                        "OK",
                                        "KO"
                                    ],
                                    "type": "string"
                                },
                                "errorMessage": {
                                    "type": "string"
                                }
                            }
                        }
                    },
                    "example": "{\n    \"validationResult\": {\n        \"status\": \"KO\",\n        \"errorMessage\": \"6 errors found\"\n    }\n}"
                },
                "Error": {
                    "description": "Error message structure",
                    "type": "object",
                    "properties": {
                        "error": {
                            "type": "object",
                            "properties": {
                                "id": {
                                    "type": "string"
                                },
                                "description": {
                                    "type": "string"
                                },
                                "messages": {
                                    "type": "array",
                                    "items": {
                                        "type": "object"
                                    }
                                }
                            }
                        }
                    },
                    "example": {
                        "error": {
                            "id": "500",
                            "description": "Internal Server Error",
                            "messages": [
                                "java.lang.Exception: Mocked error message"
                            ]
                        }
                    }
                }
            }
        }
    }
    ```
4. Test the `/q/health` endpoint
    ```shell
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
5. Test the `/q/health/live` endpoint
    ```shell
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
    ```shell
    curl -w '\n' $URL/q/health/ready
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
7. Test the `/q/metrics` endpoint
    ```shell
    curl -w '\n' $URL/q/metrics
    ```
    ```shell
    [...]
    # HELP application_camel_context_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_context_exchanges_total counter
    application_camel_context_exchanges_total{camelContext="camel-1"} 20.0
    [...]
    # HELP application_camel_route_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_route_exchanges_total counter
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="common-500-http-code-route"} 0.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="custom-http-error-route"} 0.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="get-openapi-spec-route"} 2.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="json-validation-api-route"} 20.0
    application_camel_route_exchanges_total{camelContext="camel-1",routeId="validate-membership-json-route"} 20.0
    [...]
    ```

## Testing using [Postman](https://www.postman.com/)

Import the provided Postman Collection for testing: [tests/Camel-Quarkus-JsonValidation-Api.postman_collection.json](./tests/Camel-Quarkus-JsonValidation-Api.postman_collection.json)
 
![Camel-Quarkus-JsonValidation-Api.postman_collection.png](../_images/Camel-Quarkus-JsonValidation-Api.postman_collection.png)

## Creating a native executable

### Running locally

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/camel-quarkus-jsonvalidation-api-1.0.0-runner`

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
    ```shell
    oc login ...
    ```

2. Create an OpenShift project or use your existing OpenShift project. For instance, to create `ceq-services-native`
    ```shell
    oc new-project ceq-services-native --display-name="Red Hat build of Apache Camel for Quarkus - Native Mode"
    ```

3. Build a Linux executable using a container build. Compiling a Quarkus application to a native executable consumes a lot of memory during analysis and optimization. You can limit the amount of memory used during native compilation by setting the `quarkus.native.native-image-xmx` configuration property. Setting low memory limits might increase the build time.
    1. For Docker use:
        ```shell
        ./mvnw package -Pnative -Dquarkus.native.container-build=true \
        -Dquarkus.native.native-image-xmx=6g
        ```
    2. For Podman use:
        ```shell
        ./mvnw package -Pnative -Dquarkus.native.container-build=true \
        -Dquarkus.native.container-runtime=podman \
        -Dquarkus.native.native-image-xmx=6g
        ```
    ```shell
    [...]
    INFO] [io.quarkus.deployment.pkg.steps.JarResultBuildStep] Building native image source jar: /Users/jeannyil/Workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-jsonvalidation-api/target/camel-quarkus-jsonvalidation-api-1.0.0-native-image-source-jar/camel-quarkus-jsonvalidation-api-1.0.0-runner.jar
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Building native image from /Users/jeannyil/Workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-jsonvalidation-api/target/camel-quarkus-jsonvalidation-api-1.0.0-native-image-source-jar/camel-quarkus-jsonvalidation-api-1.0.0-runner.jar
    [...]
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Running Quarkus native-image plugin on GraalVM Version 20.3.2.0-Final (Mandrel Distribution) (Java Version 11.0.11+9)
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] docker run -v /Users/jeannyil/Workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-jsonvalidation-api/target/camel-quarkus-jsonvalidation-api-1.0.0-native-image-source-jar:/project:z --env LANG=C --rm quay.io/quarkus/ubi-quarkus-mandrel:20.3-java11 -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=1 -J-Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true -J-Duser.language=en -J-Dfile.encoding=UTF-8 --initialize-at-build-time= -H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy\$BySpaceAndTime -H:+JNI -H:+AllowFoldMethods -jar camel-quarkus-jsonvalidation-api-1.0.0-runner.jar -H:FallbackThreshold=0 -H:+ReportExceptionStackTraces -J-Xmx6g -H:+AddAllCharsets -H:EnableURLProtocols=http,https --enable-all-security-services -H:-UseServiceLoaderFeature -H:+StackTrace camel-quarkus-jsonvalidation-api-1.0.0-runner
    [...]
    [INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 1141555ms
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time:  19:11 min
    [INFO] Finished at: 2021-05-27T13:58:01+02:00
    [INFO] ------------------------------------------------------------------------
    ```

4. Create the `camel-quarkus-jsonvalidation-api` container image using the _OpenShift Docker build_ strategy. This strategy creates a container using a build configuration in the cluster.
    1. Create a build config based on the [`src/main/docker/Dockerfile.native`](./src/main/docker/Dockerfile.native) file:
        ```shell
        cat src/main/docker/Dockerfile.native | oc new-build \
        --name camel-quarkus-jsonvalidation-api --strategy=docker --dockerfile -
        ```
    2. Build the project:
        ```shell
        oc start-build camel-quarkus-jsonvalidation-api --from-dir . -F
        ```
        ```shell
        Uploading directory "." as binary input for the build ...
        ....
        Uploading finished
        build.build.openshift.io/camel-quarkus-jsonvalidation-api-2 started
        Receiving source from STDIN as archive ...
        Replaced Dockerfile FROM image registry.access.redhat.com/ubi8/ubi-minimal:8.1
        [...]
        Successfully pushed image-registry.openshift-image-registry.svc:5000/ceq-services-native/camel-quarkus-jsonvalidation-api@sha256:af65ac4fd4d960b1403425ea465036b711b4be43b1d0381ba321e2d5214912e9
        Push successful
        ```

5. Deploy the `camel-quarkus-jsonvalidation-api` as a serverless application
    ```shell
    kn service create camel-quarkus-jsonvalidation-api \
    --label app.openshift.io/runtime=camel \
    --image image-registry.openshift-image-registry.svc:5000/ceq-services-native/camel-quarkus-jsonvalidation-api:latest
    ```

6. To verify that the `camel-quarkus-jsonvalidation-api` service is ready, enter the following command.
    ```shell
    kn service list camel-quarkus-jsonvalidation-api
    ```
    The output in the column called "READY" reads `True` if the service is ready.

## Start-up time comparison on the same OpenShift cluster

### JVM mode

```shell
2021-11-20 21:35:16,664 INFO  [org.apa.cam.qua.cor.CamelBootstrapRecorder] (main) Bootstrap runtime: org.apache.camel.quarkus.main.CamelMainRuntime
2021-11-20 21:35:16,864 INFO  [org.apa.cam.mai.BaseMainSupport] (main) Auto-configuration summary
2021-11-20 21:35:16,865 INFO  [org.apa.cam.mai.BaseMainSupport] (main)     camel.context.name=camel-quarkus-jsonvalidation-api
2021-11-20 21:35:17,963 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Routes startup summary (total:5 started:5)
2021-11-20 21:35:17,963 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started common-500-http-code-route (direct://common-500)
2021-11-20 21:35:17,963 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started custom-http-error-route (direct://custom-http-error)
2021-11-20 21:35:17,964 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started validate-membership-json-route (direct://validateMembershipJSON)
2021-11-20 21:35:17,964 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started get-openapi-spec-route (rest://get:openapi.json)
2021-11-20 21:35:17,964 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started json-validation-api-route (rest://post:/validateMembershipJSON)
2021-11-20 21:35:17,964 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.11.1 (camel-quarkus-jsonvalidation-api) started in 791ms (build:0ms init:606ms start:185ms)
2021-11-20 21:35:18,366 INFO  [io.quarkus] (main) camel-quarkus-jsonvalidation-api 1.0.0 on JVM (powered by Quarkus 2.2.3.Final-redhat-00013) started in 8.485s. Listening on: http://0.0.0.0:8080
2021-11-20 21:35:18,366 INFO  [io.quarkus] (main) Profile prod activated.
2021-11-20 21:35:18,367 INFO  [io.quarkus] (main) Installed features: [camel-attachments, camel-bean, camel-core, camel-direct, camel-jackson, camel-json-validator, camel-microprofile-health, camel-microprofile-metrics, camel-openapi-java, camel-opentracing, camel-platform-http, camel-rest, camel-xml-jaxb, cdi, config-yaml, jaeger, kubernetes, kubernetes-client, smallrye-context-propagation, smallrye-health, smallrye-metrics, smallrye-opentracing, vertx, vertx-web]
[...]
```

### Native mode

```shell
[...]
2021-05-27 12:57:16,930 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.7.0 (camel-1) is starting
[...]
2021-05-27 12:57:16,932 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: get-openapi-spec-route started and consuming from: platform-http:///openapi.json
2021-05-27 12:57:16,932 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: json-validation-api-route started and consuming from: platform-http:///validateMembershipJSON
2021-05-27 12:57:16,932 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: common-500-http-code-route started and consuming from: direct://common-500
2021-05-27 12:57:16,932 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: custom-http-error-route started and consuming from: direct://custom-http-error
2021-05-27 12:57:16,932 INFO  [org.apa.cam.imp.eng.InternalRouteStartupManager] (main) Route: validate-membership-json-route started and consuming from: direct://validateMembershipJSON
2021-05-27 12:57:16,932 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Total 5 routes, of which 5 are started
2021-05-27 12:57:16,932 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.7.0 (camel-1) started in 2ms
2021-05-27 12:57:16,934 INFO  [io.quarkus] (main) camel-quarkus-jsonvalidation-api 1.0.0 native (powered by Quarkus 1.11.6.Final-redhat-00001) started in 0.025s. Listening on: http://0.0.0.0:8080
2021-05-27 12:57:16,934 INFO  [io.quarkus] (main) Profile prod activated.
2021-05-27 12:57:16,934 INFO  [io.quarkus] (main) Installed features: [camel-attachments, camel-bean, camel-core, camel-direct, camel-jackson, camel-json-validator, camel-microprofile-health, camel-microprofile-metrics, camel-openapi-java, camel-platform-http, camel-rest, camel-suport-xalan, camel-support-common, camel-support-jackson-dataformat-xml, camel-xml-jaxb, cdi, config-yaml, kubernetes, mutiny, smallrye-context-propagation, smallrye-health, smallrye-metrics, vertx, vertx-web]
[...]
```