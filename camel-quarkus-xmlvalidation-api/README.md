# Camel-Quarkus-XmlValidation-Api project

This project leverages **Red Hat build of Quarkus 1.11.x**, the Supersonic Subatomic Java Framework.

It exposes the following RESTful service endpoints  using **Apache Camel REST DSL** and the **Apache Camel Quarkus Platform HTTP** extension:
- `/validateMembershipXML` : validates a sample `Membership` XML instance through the `POST` HTTP method.
- `/openapi.json`: returns the OpenAPI 3.0 specification for the service.
- `/q/health` : returns the _Camel Quarkus MicroProfile_ health checks
- `/q/metrics` : the _Camel Quarkus MicroProfile_ metrics

## Prerequisites
- JDK 11 installed with `JAVA_HOME` configured appropriately
- Apache Maven 3.8.1+
- **OPTIONAL**: [**Jaeger**](https://www.jaegertracing.io/), a distributed tracing system for observability ([_open tracing_](https://opentracing.io/)).  :bulb: A simple way of starting a Jaeger tracing server is with `docker` or `podman`:
    1. Start the Jaeger tracing server:
        ```
        podman run --rm -e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
        -p 5775:5775/udp -p 6831:6831/udp -p 6832:6832/udp \
        -p 5778:5778 -p 16686:16686 -p 14268:14268 -p 14250:14250 -p 9411:9411 \
        quay.io/jaegertracing/all-in-one:latest
        ```
    2. While the server is running, browse to http://localhost:16686 to view tracing events.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application locally

The application can be packaged using `./mvnw package`.
It produces the `camel-quarkus-xmlvalidation-api-1.0.0-runner.jar` file in the `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/camel-quarkus-xmlvalidation-api-1.0.0-runner.jar`.

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
./mvnw clean package -Dquarkus.kubernetes.deploy=true -Dquarkus.container-image.group=camel-quarkus-jvm
```
```zsh
[...]
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Selecting target 'openshift' since it has the highest priority among the implicitly enabled deployment targets
[WARNING] [io.quarkus.arc.deployment.SplitPackageProcessor] Detected a split package usage which is considered a bad practice and should be avoided. Following packages were detected in multiple archives: 
- "org.apache.camel.main" found in [org.apache.camel:camel-base-engine::jar, org.apache.camel:camel-main::jar]
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeploy] Kubernetes API Server at 'https://api.api.jeannyil.sandbox1789.opentlc.com:6443/' successfully contacted.
[INFO] Checking for existing resources in: /Users/jnyilimb/workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/src/main/kubernetes.
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Performing openshift binary build with jar on server: https://api.api.jeannyil.sandbox1789.opentlc.com:6443/ in namespace:camel-quarkus-jvm.
[...]
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Pushing image image-registry.openshift-image-registry.svc:5000/camel-quarkus-jvm/camel-quarkus-xmlvalidation-api:1.0.0 ...
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Getting image source signatures
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Copying blob sha256:4a1b2e71b0ec9ecbbaec252160f0b48be035515b04035a25aa50bfb2c7e4d6c2
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Copying blob sha256:4418ace46c3dd933f98d83f357f31048e72d5db3d97bccfdb0acef769ee8234f
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Copying blob sha256:2a99c93da16827d9a6254f86f495d2c72c62a916f9c398577577221d35d2c790
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Copying blob sha256:b4a4e359e27438bf3181a61aaa0dbe0ca8cc310a9d41f4470189170107ecff08
[INFO] [io.quarkus.container.image.openshift.deployment.OpenshiftProcessor] Copying config sha256:ddd77d2015664bcccedecbec9138f843340e0c04570085d8345f9c2c98cc4c48
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Deploying to openshift server: https://api.api.jeannyil.sandbox1789.opentlc.com:6443/ in namespace: camel-quarkus-jvm.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ServiceAccount camel-quarkus-xmlvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Service camel-quarkus-xmlvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Role view-secrets.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: RoleBinding camel-quarkus-xmlvalidation-api-view.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: RoleBinding camel-quarkus-xmlvalidation-api-view-secrets.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream camel-quarkus-xmlvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: ImageStream openjdk-11.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: BuildConfig camel-quarkus-xmlvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Deployment camel-quarkus-xmlvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] Applied: Route camel-quarkus-xmlvalidation-api.
[INFO] [io.quarkus.kubernetes.deployment.KubernetesDeployer] The deployed application can be accessed at: http://camel-quarkus-xmlvalidation-api-camel-quarkus-jvm.apps.api.jeannyil.sandbox1789.opentlc.com
[INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 64279ms
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:16 min
[INFO] Finished at: 2021-11-24T21:53:15+01:00
[INFO] ------------------------------------------------------------------------
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
    oc new-app https://github.com/jeanNyil/redhat-ceq-demos.git \
    --context-dir=camel-quarkus-xmlvalidation-api \
    --name=camel-quarkus-xmlvalidation-api \
    --image-stream="openshift/openjdk-11-ubi8" \
    --labels=app.kubernetes.io/name=camel-quarkus-xmlvalidation-api \
    --labels=app.kubernetes.io/version=1.0.0 \
    --labels=app.openshift.io/runtime=quarkus
    ```
3. Follow the log of the S2I build
    ```zsh
    oc logs bc/camel-quarkus-xmlvalidation-api -f
    ```
    ```zsh
    Cloning "https://github.com/jeanNyil/redhat-ceq-demos.git" ...
            Commit: 6272aa3b4f668a7a2fb64614afd71eb8d340988f (Upgraded to Red Hat build of Quarkus 1.11)
            Author: Jean Armand Nyilimbibi <jean.nyilimbibi@gmail.com>
            Date:   Thu May 27 12:55:19 2021 +0200
    [...]
    Successfully pushed image-registry.openshift-image-registry.svc:5000/camel-quarkus-jvm/camel-quarkus-xmlvalidation-api@sha256:ea4efcaf1444867e863dfddbe192754435a4c62ab616039ffd49659b09d9df43
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
3. Test the `/openapi.json` endpoint
    ```zsh
    curl -w '\n' $URL/openapi.json
    ```
    ```json
    {
        "openapi": "3.0.2",
        "info": {
            "title": "Sample XML Validation API",
            "version": "1.0.0",
            "description": "A simple API to test the Camel XML validator component",
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
                "url": "https://camel-quarkus-xml-validation-api.apps.jeannyil.sandbox1789.opentlc.com"
            }
        ],
        "paths": {
            "/validateMembershipXML": {
                "post": {
                    "requestBody": {
                        "description": "A `Membership` XML instance to be validated.",
                        "content": {
                            "text/xml": {
                                "schema": {
                                    "type": "string"
                                },
                                "examples": {
                                    "Membership": {
                                        "value": "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<p:membership xmlns:p=\"http://www.github.com/jeanNyil/schemas/membership/v1.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n  <p:requestType>API</p:requestType>\n  <p:requestID>5948</p:requestID>\n  <p:memberID>85623617</p:memberID>\n  <p:status>A</p:status>\n  <p:enrolmentDate>2019-06-29</p:enrolmentDate>\n  <p:changedBy>jeanNyil</p:changedBy>\n  <p:forcedLevelCode>69</p:forcedLevelCode>\n  <p:vipOnInvitation>Y</p:vipOnInvitation>\n  <p:startDate>2019-06-29</p:startDate>\n  <p:endDate>2100-06-29</p:endDate>\n</p:membership>"
                                    }
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
                            "description": "`Membership` XML data validated"
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
                                                    "errorMessage": "Validation failed for: com.sun.org.apache.xerces.internal.jaxp.validation.SimpleXMLSchema@493de94f\nerrors: [\norg.xml.sax.SAXParseException: cvc-datatype-valid.1.2.1: '20-06-29' is not a valid value for 'date'., Line : 7, Column : 46\norg.xml.sax.SAXParseException: cvc-type.3.1.3: The value '20-06-29' of element 'p:enrolmentDate' is not valid., Line : 7, Column : 46\n]. Exchange[ID-sample-xml-validation-api-1-2gh7r-1620389968693-0-4]"
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            "description": "`Membership` XML data not valid"
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
                    "operationId": "validateMembershipXML",
                    "summary": "Validate Membership XML instance",
                    "description": "Validates a `Membership` instance",
                    "x-codegen-request-body-name": "body"
                }
            }
        },
        "components": {
            "schemas": {
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
                    "example": "{\n\t\"validationResult\": {\n\t\t\"status\": \"KO\",\n\t\t\"errorMessage\": \"Validation failed for: com.sun.org.apache.xerces.internal.jaxp.validation.SimpleXMLSchema@5f86796e\\nerrors: [\\norg.xml.sax.SAXParseException: cvc-datatype-valid.1.2.1: '20-06-29' is not a valid value for 'date'., Line : 7, Column : 46\\norg.xml.sax.SAXParseException: cvc-type.3.1.3: The value '20-06-29' of element 'p:enrolmentDate' is not valid., Line : 7, Column : 46\\n]. Exchange[ID-jeansmacbookair-home-1561803539861-1-1]\"\n\t}\n}"
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
                    "example": "{\n\t\"error\": {\n\t\t\"id\": \"500\",\n\t\t\"description\": \"Internal Server Error\",\n\t\t\"messages\": [\n\t\t\t\"java.lang.Exception: Mocked error message\"\n\t\t]\n\t}\n}"
                }
            }
        }
    }
    ```
4. Test the `/q/health` endpoint
    ```zsh
    curl -w '\n' $URL/q/health
    ```
    ```json
    {
        "checks": [
            {
                "name": "camel-liveness-checks",
                "status": "UP"
            },
            {
                "data": {
                    "context": "UP",
                    "route:common-500-http-code-route": "UP",
                    "route:custom-http-error-route": "UP",
                    "route:get-openapi-spec-route": "UP",
                    "route:validate-membership-xml-route": "UP",
                    "route:xml-validation-api-route": "UP"
                },
                "name": "camel-readiness-checks",
                "status": "UP"
            }
        ],
        "status": "UP"
    }
    ```
5. Test the `/q/health/live` endpoint
    ```zsh
    curl -w '\n' $URL/q/health/live
    ```
    ```json
    {
        "checks": [
            {
                "name": "camel-liveness-checks",
                "status": "UP"
            }
        ],
        "status": "UP"
    }
    ```
6. Test the `/q/health/ready` endpoint
    ```zsh
    curl -w '\n' $URL/q/health/ready
    ```
    ```json
    {
        "checks": [
            {
                "data": {
                    "context": "UP",
                    "route:common-500-http-code-route": "UP",
                    "route:custom-http-error-route": "UP",
                    "route:get-openapi-spec-route": "UP",
                    "route:validate-membership-xml-route": "UP",
                    "route:xml-validation-api-route": "UP"
                },
                "name": "camel-readiness-checks",
                "status": "UP"
            }
        ],
        "status": "UP"
    }
    ```
7. Test the `/q/metrics` endpoint
    ```zsh
    curl -w '\n' $URL/q/metrics
    ```
    ```zsh
    [...]
    # HELP application_camel_context_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_context_exchanges_total counter
    application_camel_context_exchanges_total{camelContext="camel-quarkus-xmlvalidation-api"} 15.0
    [...]
    # HELP application_camel_route_exchanges_total The total number of exchanges for a route or Camel Context
    # TYPE application_camel_route_exchanges_total counter
    application_camel_route_exchanges_total{camelContext="camel-quarkus-xmlvalidation-api",routeId="common-500-http-code-route"} 0.0
    application_camel_route_exchanges_total{camelContext="camel-quarkus-xmlvalidation-api",routeId="custom-http-error-route"} 0.0
    application_camel_route_exchanges_total{camelContext="camel-quarkus-xmlvalidation-api",routeId="get-openapi-spec-route"} 4.0
    application_camel_route_exchanges_total{camelContext="camel-quarkus-xmlvalidation-api",routeId="validate-membership-xml-route"} 11.0
    application_camel_route_exchanges_total{camelContext="camel-quarkus-xmlvalidation-api",routeId="xml-validation-api-route"} 11.0
    [...]
    ```

## Testing using [Postman](https://www.postman.com/)

Import the provided Postman Collection for testing: [tests/Camel-Quarkus-XmlValidation-Api.postman_collection.json](./tests/Camel-Quarkus-XmlValidation-Api.postman_collection.json)
 
![Camel-Quarkus-XmlValidation-Api.postman_collection.png](../_images/Camel-Quarkus-XmlValidation-Api.postman_collection.png)

## Creating a native executable

### Running locally

You can create a native executable using: `./mvnw package -Pnative`.

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your native executable with: `./target/camel-quarkus-xmlvalidation-api-1.0.0-runner`

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
        -Dquarkus.native.native-image-xmx=7g
        ```
    2. For Podman use:
        ```zsh
        ./mvnw package -Pnative -Dquarkus.native.container-build=true \
        -Dquarkus.native.container-runtime=podman \
        -Dquarkus.native.native-image-xmx=7g
        ```
    ```zsh
    [...]
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Building native image from /Users/jnyilimb/workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/target/camel-quarkus-xmlvalidation-api-1.0.0-native-image-source-jar/camel-quarkus-xmlvalidation-api-1.0.0-runner.jar
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildContainerRunner] Using docker to run the native image builder
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildContainerRunner] Checking image status registry.access.redhat.com/quarkus/mandrel-21-rhel8:21.2
    [...]
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildStep] Running Quarkus native-image plugin on native-image 21.2.0.2-0b3 Mandrel Distribution (Java Version 11.0.13+8-LTS)
    [INFO] [io.quarkus.deployment.pkg.steps.NativeImageBuildRunner] docker run --env LANG=C --rm -v /Users/jnyilimb/workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/target/camel-quarkus-xmlvalidation-api-1.0.0-native-image-source-jar:/project:z --name build-native-UkVKn registry.access.redhat.com/quarkus/mandrel-21-rhel8:21.2 -J-Dsun.nio.ch.maxUpdateArraySize=100 -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager -J-Dcom.sun.xml.bind.v2.bytecode.ClassTailor.noOptimize=true -J-Dvertx.logger-delegate-factory-class-name=io.quarkus.vertx.core.runtime.VertxLogDelegateFactory -J-Dvertx.disableDnsResolver=true -J-Dio.netty.leakDetection.level=DISABLED -J-Dio.netty.allocator.maxOrder=3 -J-Duser.language=en -J-Duser.country=FR -J-Dfile.encoding=UTF-8 -H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy\$BySpaceAndTime -H:+JNI -H:+AllowFoldMethods -H:FallbackThreshold=0 -H:+ReportExceptionStackTraces -J-Xmx7g -H:+AddAllCharsets -H:EnableURLProtocols=http,https -H:-UseServiceLoaderFeature -H:+StackTrace -H:-ParseOnce camel-quarkus-xmlvalidation-api-1.0.0-runner -jar camel-quarkus-xmlvalidation-api-1.0.0-runner.jar
    [...]
    [INFO] Checking for existing resources in: /Users/jnyilimb/workdata/myGit/Quarkus/rh-build-quarkus-camel-demos/camel-quarkus-xmlvalidation-api/src/main/kubernetes.
    [INFO] [io.quarkus.deployment.QuarkusAugmentor] Quarkus augmentation completed in 751960ms
    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time:  12:40 min
    [INFO] Finished at: 2021-11-24T20:51:00+01:00
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
        Successfully pushed image-registry.openshift-image-registry.svc:5000/camel-quarkus-native/camel-quarkus-xmlvalidation-api@sha256:538fa66e80ad038dcf21d4e01c5380000365d64b5dfc9d2a442ca0d597d5d601
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

- OpenShift Container Platform 4.8.19 running on AWS
- Compute nodes types: [m5.xlarge](https://aws.amazon.com/ec2/instance-types/m5/) (4 vCPU / 16 GiB Memory)

### JVM mode - 8.201s

```zsh
2021-11-24 20:53:28,558 INFO  [org.apa.cam.qua.cor.CamelBootstrapRecorder] (main) Bootstrap runtime: org.apache.camel.quarkus.main.CamelMainRuntime
2021-11-24 20:53:28,743 INFO  [org.apa.cam.mai.BaseMainSupport] (main) Auto-configuration summary
2021-11-24 20:53:28,744 INFO  [org.apa.cam.mai.BaseMainSupport] (main)     camel.context.name=camel-quarkus-xmlvalidation-api
2021-11-24 20:53:29,845 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Routes startup summary (total:5 started:5)
2021-11-24 20:53:29,845 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started get-openapi-spec-route (rest://get:openapi.json)
2021-11-24 20:53:29,845 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started xml-validation-api-route (rest://post:/validateMembershipXML)
2021-11-24 20:53:29,845 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started common-500-http-code-route (direct://common-500)
2021-11-24 20:53:29,845 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started custom-http-error-route (direct://custom-http-error)
2021-11-24 20:53:29,845 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started validate-membership-xml-route (direct://validateMembershipXML)
2021-11-24 20:53:29,845 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.11.1 (camel-quarkus-xmlvalidation-api) started in 808ms (build:0ms init:515ms start:293ms)
2021-11-24 20:53:30,239 INFO  [io.quarkus] (main) camel-quarkus-xmlvalidation-api 1.0.0 on JVM (powered by Quarkus 2.2.3.Final-redhat-00013) started in 8.201s. Listening on: http://0.0.0.0:8080
2021-11-24 20:53:30,240 INFO  [io.quarkus] (main) Profile prod activated.
2021-11-24 20:53:30,240 INFO  [io.quarkus] (main) Installed features: [camel-attachments, camel-bean, camel-core, camel-direct, camel-jackson, camel-microprofile-health, camel-microprofile-metrics, camel-openapi-java, camel-opentracing, camel-platform-http, camel-rest, camel-validator, camel-xml-jaxb, cdi, config-yaml, jaeger, kubernetes, kubernetes-client, smallrye-context-propagation, smallrye-health, smallrye-metrics, smallrye-opentracing, vertx, vertx-web]
```

### Native mode - 0.109s

```zsh
2021-11-24 21:01:07,394 INFO  [org.apa.cam.qua.cor.CamelBootstrapRecorder] (main) Bootstrap runtime: org.apache.camel.quarkus.main.CamelMainRuntime
2021-11-24 21:01:07,398 INFO  [org.apa.cam.mai.BaseMainSupport] (main) Auto-configuration summary
2021-11-24 21:01:07,398 INFO  [org.apa.cam.mai.BaseMainSupport] (main)     camel.context.name=camel-quarkus-xmlvalidation-api
2021-11-24 21:01:07,407 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Routes startup summary (total:5 started:5)
2021-11-24 21:01:07,407 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started validate-membership-xml-route (direct://validateMembershipXML)
2021-11-24 21:01:07,407 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started get-openapi-spec-route (rest://get:openapi.json)
2021-11-24 21:01:07,407 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started xml-validation-api-route (rest://post:/validateMembershipXML)
2021-11-24 21:01:07,407 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started common-500-http-code-route (direct://common-500)
2021-11-24 21:01:07,407 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main)     Started custom-http-error-route (direct://custom-http-error)
2021-11-24 21:01:07,407 INFO  [org.apa.cam.imp.eng.AbstractCamelContext] (main) Apache Camel 3.11.1 (camel-quarkus-xmlvalidation-api) started in 8ms (build:0ms init:6ms start:2ms)
2021-11-24 21:01:07,411 INFO  [io.quarkus] (main) camel-quarkus-xmlvalidation-api 1.0.0 native (powered by Quarkus 2.2.3.Final-redhat-00013) started in 0.109s. Listening on: http://0.0.0.0:8080
2021-11-24 21:01:07,411 INFO  [io.quarkus] (main) Profile prod activated.
2021-11-24 21:01:07,411 INFO  [io.quarkus] (main) Installed features: [camel-attachments, camel-bean, camel-core, camel-direct, camel-jackson, camel-microprofile-health, camel-microprofile-metrics, camel-openapi-java, camel-opentracing, camel-platform-http, camel-rest, camel-validator, camel-xml-jaxb, cdi, config-yaml, jaeger, kubernetes, kubernetes-client, smallrye-context-propagation, smallrye-health, smallrye-metrics, smallrye-opentracing, vertx, vertx-web]
[...]
```