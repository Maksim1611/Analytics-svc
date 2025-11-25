# Analytics Service (analytics-svc)

The **Analytics Service** is a lightweight REST microservice responsible
for storing and providing analytical snapshots for **tasks** and
**projects**. It is consumed by the monolithic Task Manager application.

------------------------------------------------------------------------

## Overview

The service receives snapshot data from the monolith and stores
aggregated analytics for each user. It also provides read endpoints that
the monolith uses to build analytics dashboards.

This microservice does **not** render UI pages --- it only exposes REST
APIs.

------------------------------------------------------------------------

## Technology Stack

-   Java 17\
-   Spring Boot\
-   MySQL\
-   REST API\
-   Maven

------------------------------------------------------------------------

## API Endpoints

### Project Analytics

#### **POST `/api/v1/projects/{userId}`**

Stores or updates analytics for the user's projects.

-   **Request Body:** `List<ProjectAnalyticsRequest>`
-   **Response:** `ProjectAnalytics`

#### **GET `/api/v1/projects/{userId}`**

Fetches stored project analytics for the given user.

------------------------------------------------------------------------

### Task Analytics

#### **POST `/api/v1/tasks/{userId}`**

Stores or updates analytics for the user's tasks.

-   **Request Body:** `List<TaskAnalyticsRequest>`
-   **Response:** `TaskAnalytics`

#### **GET `/api/v1/tasks/{userId}`**

Returns previously saved task analytics.

------------------------------------------------------------------------

## Data Flow

1.  The monolith collects task/project statistics.
2.  It sends aggregated snapshots to this microservice via the POST
    endpoints.
3.  The analytics service stores the snapshot in the database.
4.  The monolith later retrieves the analytics for dashboards and
    charts.

------------------------------------------------------------------------