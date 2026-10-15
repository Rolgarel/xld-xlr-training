# XLD / XLR Project

A local DevOps project built to explore Digital.ai Deploy (XLD) and Digital.ai Release (XLR) through a realistic application deployment workflow.

The entire stack runs locally with Docker.

## Architecture

The project implements a local CI/CD workflow using Jenkins, Digital.ai Release (XLR), and Digital.ai Deploy (XLD).

```text
Git Repository
      |
      v
   Jenkins
      |
      ├── Checkout
      ├── Maven Tests
      ├── Read Application Version
      ├── Build Docker Image
      └── Push Image to Registry
                  |
                  v
            Local Registry
                  |
                  v
           Start XLR Release
                  |
                  v
             Digital.ai
               Release
                  |
                  ├── Create XLD Deployment Package
                  ├── Deploy to DEV
                  ├── Health Check + Smoke Test
                  ├── Deploy to TEST
                  ├── Health Check + Smoke Test
                  ├── Final Validation
                  ├── Deploy to PROD
                  └── Cleanup Phase
                        |
                        v
                    Digital.ai
                      Deploy
                        |
                        v
                   Docker Targets
                    DEV / TEST / PROD
```

- Git — application source code and infrastructure configuration

- Jenkins — continuous integration, automated testing, Docker image build and publication, and XLR release triggering

- Maven — application testing and application version retrieval from pom.xml

- Registry — local storage for versioned application images

- Digital.ai Release (XLR) — release orchestration, deployment sequencing, validation tasks, and XLD task execution

- Digital.ai Deploy (XLD) — deployment package management and application deployment to target environments

- Release Runner — executes tasks delegated by XLR

- Docker Socket Proxy — provides a filtered Docker API endpoint, currently available but not used by Jenkins, XLD, XLR, or Release Runner, which mount the Docker socket directly

- Docker Engine — runs containers and deployment targets

Jenkins is responsible for CI and release initiation. XLR orchestrates the release workflow, while XLD executes the deployments.

## Repository Structure
```text
xld-xlr-project/
│
├── README.md
├── docker-compose.yml
├── .env.example
├── .gitignore
│
├── application/
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│
├── ci/
│   └── Jenkinsfile
│
├── docker/
│   ├── jenkins/
│   │   ├── Dockerfile
│   │   └── certs/
│   │
│   ├── xld/
│   │   ├── Dockerfile
│   │   └── certs/
│   │
│   └── xlr/
│       ├── Dockerfile
│       └── certs/
│
├── xld/
│   ├── application.yaml
│   ├── configuration.yaml
│   ├── environment.yaml
│   └── infrastructure.yaml
│
└── xlr/
    └── templates/
        └── hello-xld-xlr-release.yaml
```

## Application

The project contains a small Spring Boot application used as the deployment target.

The application exposes:

```text
GET /
GET /api/info
GET /health
```

The response includes:

- application name

- application version

- deployment environment

The same application artifact is intended to be promoted across environments:

```text
hello-xld-xlr:1.0.0
        │
        ├── DEV
        │    ├── ENVIRONMENT=DEV
        │    └── HOST_PORT=8081
        │
        ├── TEST
        │    ├── ENVIRONMENT=TEST
        │    └── HOST_PORT=8082
        │
        └── PROD
             ├── ENVIRONMENT=PROD
             └── HOST_PORT=8083
```

## Local Infrastructure

Docker Compose provides the local platform:

```text
┌───────────────────────────────────────────────────────────────┐
│                           Docker                              │
│                                                               │
│  ┌────────────────┐                                           │
│  │    Jenkins     │                                           │
│  │    :8080       │                                           │
│  │                │                                           │
│  │  JDK 21        │                                           │
│  │  Maven         │                                           │
│  │  Docker CLI    │                                           │
│  └───────┬────────┘                                           │
│          │                                                    │
│          │ Docker Socket                                      │
│          ▼                                                    │
│     Docker Engine                                             │
│          │                                                    │
│          ├──────────────────────┐                             │
│          │                      │                             │
│          ▼                      ▼                             │
│  ┌──────────────┐       ┌──────────────┐                      │
│  │   Registry   │       │     XLR      │                      │
│  │    :5000     │       │    :5516     │                      │
│  └──────────────┘       └──────┬───────┘                      │
│                                │                              │
│                                ▼                              │
│                         ┌──────────────┐                      │
│                         │     XLD      │                      │
│                         │    :4516     │                      │
│                         └──────┬───────┘                      │
│                                │                              │
│                       Deployment targets                      │
│                         DEV / TEST / PROD                     │
│                                                               │
│                    Docker network: xld-xlr                    │
│                                                               │
└───────────────────────────────────────────────────────────────┘
```

All services are connected to the same Docker bridge network:

```text
xld-xlr
```

Jenkins, XLD, XLR and Release Runner mount the Docker socket directly. The Docker Socket Proxy is also available on the network but is not currently configured as the Docker endpoint for these services.

XLD and XLR are provided by their respective Digital.ai Docker images.

## CI Pipeline

Jenkins executes the following stages:

1. Checkout — retrieves the source code from the configured SCM.
2. Test — runs the Maven test suite in the application directory.
3. Get Version — reads the application version from application/pom.xml.
4. Docker Build — builds the application image using the application Dockerfile.
5. Docker Push — publishes the versioned image to the local Docker Registry.
6. Release on XLR — authenticates to the XLR API and starts the configured release template, passing the application version as app_version.

The pipeline disables concurrent builds to avoid overlapping executions.

Jenkins does not deploy the application directly. It initiates the release, and XLR coordinates the deployment activities through XLD.

## Release Workflow

The XLR template hello-xld-xlr-release orchestrates the deployment lifecycle.

### Phase 1 — Create Application Package

XLR creates the XLD deployment package for the requested application version. It then creates the Docker container specification and TCP port configuration.

The package references the image published by Jenkins:

localhost:5000/hello-xld-xlr:${app_version}

A validation gate is used to review the generated package configuration.

### Phase 2 — Deploy to DEV

XLR invokes XLD to deploy the selected version to DEV.

The release then executes:

- A health check against /health.
- A smoke test against /api/info.
- A validation gate before proceeding.

### Phase 3 — Deploy to TEST

After the DEV phase, XLR deploys the same application version to TEST.

The release performs the health check and smoke test again, followed by a QA validation gate.

### Phase 4 — Deploy to PROD

A final validation task precedes the PROD deployment.

After deployment, the release executes the same health check and smoke test endpoints.

### Phase 5 — Cleanup

The current template contains a cleanup phase with a manual validation task followed by undeploy tasks for DEV, TEST, and PROD.

**Important**: this cleanup phase removes the deployed applications. It should be treated as a teardown operation, not as an automatic rollback mechanism.

The current template does not explicitly restore a previous application version.

## Artifact Management

The application is packaged as a Docker image and stored in the local Docker Registry.

Example:

```text
localhost:5000/hello-xld-xlr:1.0.0
```

The application version is retrieved from the Maven project configuration in `application/pom.xml`.

Jenkins uses this version to tag the Docker image and passes it to XLR through the `app_version` release variable.

XLR then uses the same version to create the XLD deployment package and configure the application image.

The intended deployment model is build once, deploy the same image across DEV, TEST, and PROD.

## Versioning

The application version is currently derived from the Maven project.version property.

For example:

```xml
<version>1.0.0</version>
```

produces the following image tag:

```text
localhost:5000/hello-xld-xlr:1.0.0
```

Git tags can be used to mark source code releases, but they do not currently determine the image version in the Jenkins pipeline.

Application code, Jenkins pipeline configuration, XLD configuration, and XLR release configuration are versioned in the same repository.

## Local Access

The local services are exposed through Docker:

```text
Jenkins       http://localhost:8080
Registry      http://localhost:5000
XLD           http://localhost:4516
XLR           http://localhost:5516
```

Release Runner and Docker Socket Proxy do not publish host ports and are intended for internal communication within the Docker network.

Application endpoints are exposed only when an application instance is deployed by XLD.

## Environment Configuration

### .env

Create a local environment file from the provided example:

```bash
cp .env.example .env
```

Configure the following variables:

- XLD_ADMIN_PASSWORD
- XLR_ADMIN_PASSWORD
- RELEASE_RUNNER_TOKEN

Ensure that the Release Runner token is valid for the XLR instance.

Do not commit .env or expose credentials in version control.

### Jenkins

The Jenkins pipeline requires a username/password credential configured with the ID xlr-credentials. It is used to authenticate to the XLR API when starting a release.

The pipeline also references a specific XLR template ID. Ensure that the configured ID matches the template available in your XLR instance.

## Start the Project

Start the local environment:

```bash
docker compose up -d
```

Check the running containers:

```bash
docker compose ps
```

Stop the environment:

```bash
docker compose down
```

To completely reset the persistent XLD/XLR data:

```bash
docker compose down -v
```

## Project Status

### Configured Features

- Spring Boot application with health and information endpoints
- Maven-based application testing and version retrieval
- Jenkins CI pipeline
- Local Docker Registry
- Docker-based local infrastructure
- XLR API integration from Jenkins
- Automated XLR release initiation
- XLD deployment package and container specification creation
- DEV, TEST, and PROD deployment phases
- Health checks and smoke tests after deployments
- Validation gates in the release workflow
- Release Runner integration
- Cleanup tasks for undeploying applications

The repository contains the configuration for the complete CI/CD workflow. End-to-end execution, failure recovery, and environment-specific behavior must be validated in the running Docker environment.

### Further Improvements

- Automated rollback to a previously deployed version
- Production approval and access-control hardening
- Docker Socket Proxy integration as the actual Docker API endpoint
- Improved release failure handling and recovery
- Automated integration testing of the complete CI/CD workflow
- Kubernetes-based deployment targets