# XLD / XLR Project

A local DevOps project built to explore Digital.ai Deploy (XLD) and Digital.ai Release (XLR) through a realistic application deployment workflow.

The entire stack runs locally with Docker.

## Architecture
```text
Git
 │
 ▼
Build & Test
 │
 ▼
Application Artifact
 │
 ▼
XLR
 │
 ▼
XLD
 │
 ├── DEV
 ├── TEST
 └── PROD
```

- Git — source code and versioning

- CI — build and test the application

- XLR — orchestrates releases and promotions

- XLD — executes application deployments

- Docker — provides the local infrastructure

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
├── infrastructure/
│   └── docker/
│       ├── xld/
│       └── xlr/
│
├── deploy/
│   └── xld/
│       ├── applications/
│       ├── environments/
│       ├── dictionaries/
│       └── infrastructure/
│
└── release/
    └── xlr/
        ├── templates/
        ├── releases/
        ├── variables/
        └── workflows/
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
        ├── TEST
        └── PROD
```

## Local Infrastructure

Docker Compose provides the local platform:

```text
┌─────────────────────────────────────┐
│              Docker                 │
│                                     │
│   ┌─────────┐      ┌─────────┐      │
│   │   XLD   │◄─────│   XLR   │      │
│   └────┬────┘      └─────────┘      │
│        │                            │
│        ├── Application DEV          │
│        ├── Application TEST         │
│        └── Application PROD         │
│                                     │
└─────────────────────────────────────┘
```

XLD and XLR are provided by their respective Digital.ai Docker images.

## Versioning

Application versions are managed through Git branches and tags.

Examples:

```text
main
release/1.0
release/2.0
```

Version tags identify deployable application versions:

```text
v1.0.0
v1.1.0
v2.0.0
```

Application code, XLD configuration and XLR configuration are versioned together.

## Deployment Model

**XLD** is responsible for **how the application is deployed**.

**XLR** is responsible for **how the release is orchestrated**.

For example:

```text
Release 1.0
    │
    ├── Deploy to DEV
    ├── Run tests
    ├── Deploy to TEST
    ├── Approval
    └── Deploy to PROD
```

Environment-specific configuration is handled by XLD rather than duplicated in the application.

## Local Access

The local services are exposed through Docker:

```text
XLD         http://localhost:4516
XLR         http://localhost:5516

Application DEV
            http://localhost:8081

Application TEST
            http://localhost:8082

Application PROD
            http://localhost:8083
```

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

## Project Goals

The project aims to progressively implement:

- Application build and packaging

- XLD application deployment

- DEV / TEST / PROD environments

- Environment-specific configuration

- XLR release orchestration

- XLR → XLD integration

- CI/CD integration

- Artifact management

- Deployment rollback

- Kubernetes-based deployment