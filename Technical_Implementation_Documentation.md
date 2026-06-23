# TechMart Modernization Project: Technical Implementation Documentation

**Student Name:** Kylie  
**NIC No:** 992345678V  
**Subject Name:** Business Component Development I  
**Subject Code:** JIAT/BCD I/EX/01  
**Branch:** Colombo Campus  

---

## 1. System Architecture and Component Relationships

The TechMart E-Commerce Platform has been modernized from a legacy monolithic structure to a highly scalable, multi-tier enterprise architecture built on Jakarta EE 10. The system leverages component-based decoupling using Enterprise JavaBeans (EJBs), Java Message Service (JMS), and Jakarta Persistence (JPA). 

The logical design is partitioned into four major layers:
1. **Client / Presentation Layer (WAR Module):** Houses JavaServer Pages (JSPs) that render dynamic HTML. HTTP requests are processed by Java Servlets acting as Front Controllers. A servlet filter is utilized to monitor HTTP performance.
2. **Business Logic Layer (EJB Module):** Comprises session beans that encapsulate the transactional rules of TechMart. This includes Stateless beans (`ProductBean`, `OrderServiceBean`, `NotificationServiceBean`), a Stateful bean (`AdminSessionStateBean`), and Singleton beans (`InventoryCacheBean`, `MetricsTrackerBean`).
3. **Integration Layer (MDBs & JMS):** Combines JMS Connection Factories and Destinations (Queues and Topics) with Message-Driven Beans (`OrderNotificationMDB`, `AuditMDB`) to enable asynchronous, non-blocking processing and event-driven logging.
4. **Enterprise Information Systems (EIS) / Data Layer:** Employs a MySQL database in production (and H2 in-memory database for testing). JPA is utilized via EclipseLink to handle the object-relational mapping (ORM) and manage database persistence.

The diagram below maps the runtime interactions and component bindings during checkout:

```mermaid
sequenceDiagram
    autonumber
    actor Admin as Administrator
    participant Web as OrderServlet (WAR)
    participant EJB as OrderServiceBean (Stateless)
    participant Cache as InventoryCacheBean (Singleton)
    participant DB as MySQL Database
    participant Queue as JMS OrderQueue
    participant MDB as OrderNotificationMDB (MDB)
    participant Async as NotificationService (Async)

    Admin->>Web: Submits Checkout Request
    Web->>EJB: placeOrder(customer, product, qty)
    Note over EJB: Starts Container-Managed Transaction
    EJB->>Cache: getStock(productId)
    Cache-->>EJB: Returns stock from memory
    alt Stock is sufficient
        EJB->>Cache: decrementStock(productId, qty)
        EJB->>DB: Persist Order & OrderItems
        EJB->>Queue: Sends MapMessage (JMS Point-to-Point)
        EJB->>Async: sendAsyncNotification() (non-blocking)
        Note over EJB: Commits Transaction
        EJB-->>Web: Order successful (Render page)
        par JMS Delivery
            Queue->>MDB: Delivers Message (Post-Commit)
            MDB->>MDB: processNotification()
        and Asynchronous Execution
            Async->>Async: Sleep 1.5s & send email
        end
    else Stock insufficient
        EJB-->>Web: Throws InsufficientStockException
        Note over EJB: Rolls back Transaction
        Web-->>Admin: Render error banner
    end
```

---

## 2. Session Bean Architecture and Lifecycle Optimization

TechMart utilizes stateless, stateful, and singleton session beans, each selected to optimize specific architectural patterns (Burke and Monson-Haefel, 2006):

### 2.1 Stateless Session Beans
- **ProductBean & OrderServiceBean:** Chosen for processing isolated, transaction-oriented logic. Since stateless beans do not maintain client state across method invocations, the EJB container pools a finite number of instances. Under high loads (e.g., 10,000+ concurrent requests), the container redistributes active threads across the pooled instances. This minimizes memory overhead, yielding sub-millisecond thread execution.
- **Lifecycle Optimization:** Pool sizing is optimized in the Payara console (`steady-pool-size=10`, `max-pool-size=128`). This prevents database connection starvation by limiting active transactional EJB contexts.

### 2.2 Stateful Session Beans
- **AdminSessionStateBean:** Maintained to track active administrator contexts. Unlike stateless beans, the EJB instance remains bound to a single HTTP session until explicitly invalidated. This allows the system to build a temporary list of administrative audit logs in-memory before writing them to the database.
- **Lifecycle Optimization:** Stateful beans consume server memory. To prevent OutOfMemory (OOM) issues, we declare `@PrePassivate` and `@PostActivate` lifecycle callbacks. When memory usage triggers passivation, the container serializes the stateful bean to disk. When the administrator interacts with the UI again, the bean is deserialized. We invoke `@Remove` on the EJB instance during logout to immediately destroy the stateful instance and prevent memory leaks.

### 2.3 Singleton Session Beans
- **InventoryCacheBean:** Startup initialized (`@Startup`) bean acting as a shared in-memory inventory store. This prevents database read contention for checking stock levels, resolving legacy Monolith locking bottlenecks.
- **Concurrency Management:** We configure `@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)` to delegate thread safety to the container.
  - `@Lock(LockType.READ)` is used on `getStock()` to permit unlimited concurrent reading threads.
  - `@Lock(LockType.WRITE)` is used on `updateStock()` and `decrementStock()` to serialize write updates, ensuring inventory levels remain accurate without race conditions.
- **MetricsTrackerBean:** A Singleton EJB collecting real-time performance statistics, enabling the system dashboard to fetch metrics without hitting the disk.

---

## 3. JNDI and Dependency Management Strategy

In Jakarta EE 10, dependency resolution is performed through either declarative Dependency Injection (DI) or programmatic Java Naming and Directory Interface (JNDI) lookup.

### 3.1 Comparative Analysis
1. **Dependency Injection (`@EJB`, `@Inject`, `@Resource`):**
    - **Mechanism:** The EJB container resolves dependencies during deployment or bean instantiation.
    - **Pros:** Clean, type-safe, reduces boilerplate code, easier to unit test via mock injections.
    - **Cons:** Rigid compile-time binding. If a resource lookup fails, the entire application module may fail to deploy.
2. **Programmatic JNDI Lookup (`InitialContext.lookup`):**
    - **Mechanism:** The class requests a resource explicitly from the global directory service at runtime.
    - **Pros:** High flexibility, supports dynamic implementation loading, allows late binding.
    - **Cons:** Verbose exception handling, type casting errors, runtime overhead.

### 3.2 Performance and Monitoring Design (ServiceLocator Pattern)
To measure naming service performance and eliminate lookup overhead, TechMart implements a `ServiceLocator` utility class. The lookup times are measured using nanosecond timers:
```java
long startTime = System.nanoTime();
Object resolved = new InitialContext().lookup(jndiName);
long durationMs = (System.nanoTime() - startTime) / 1_000_000;
metricsTracker.recordJndiLookup(durationMs);
```
Looked-up EJB stubs are cached in a thread-safe `ConcurrentHashMap` map to eliminate JNDI context traversal overhead on subsequent accesses.

---

## 4. Asynchronous Communication and Failure Recovery

TechMart implements asynchronous execution using EJB `@Asynchronous` annotations. In `NotificationServiceBean`, this pattern decouples long-running email simulations:

```java
@Asynchronous
public Future<Boolean> sendAsyncNotification(String recipient, String message) { ... }
```

### 4.1 Future Object Handling & Timeout Optimization
When the client checks out, the main thread triggers the asynchronous call and continues without waiting. The execution is handed to the EJB container's thread pool.
To monitor success, the caller receives a `Future<Boolean>` handle. 

> [!WARNING]
> **EJB Specification Threading Constraint:** The EJB specification restricts creating unmanaged threads manually (`new Thread()`) because the container cannot track transaction contexts, security scopes, or lifecycle hooks for raw OS threads.
> To remain specification-compliant, TechMart injects a container-managed `ManagedExecutorService` via `@Resource` to poll the status of the `Future` handle asynchronously.

The system submits a task to the `ManagedExecutorService` which waits with a timeout limit:
```java
Boolean success = asyncResult.get(3, TimeUnit.SECONDS);
```
If the async thread stalls beyond 3 seconds, a `TimeoutException` is thrown. The monitoring thread catches this, triggers a circuit recovery action, logs a message warning, and prevents the main web request thread from blocking.

### 4.2 Failure Recovery
If the recipient email contains the keyword "error", a `RuntimeException` is thrown. The caller catches this via the `Future` object and logs it in the system registry, illustrating resilient recovery.

---

## 5. Java Messaging System (JMS) Architecture

The Java Message Service (JMS) architecture introduces decoupling to optimize system throughput:

### 5.1 Point-to-Point Pattern (Queue)
- **Use Case:** Order placement notifications.
- **Design:** `OrderServiceBean` sends a `MapMessage` containing checkout details to `OrderQueue`. A single `OrderNotificationMDB` instance processes messages sequentially. This shields the order database from write spikes. If 1,000 users buy items at the same second, the requests persist inside the queue buffer and process systematically, maintaining sub-second user responsiveness.

### 5.2 Publish-Subscribe Pattern (Topic)
- **Use Case:** System-wide audit logging.
- **Design:** `OrderServiceBean` sends messages to `AuditTopic`. Multiple observers can subscribe. In this application, `AuditMDB` consumes the message to write to the persistent MySQL audit table. This permits other services to listen to the same topic simultaneously (e.g. shipping services) without modifying checkout code.

---

## 6. Message-Driven Bean (MDB) Implementation

Message-Driven Beans (MDBs) are stateless, container-managed JMS consumers that decouple processing overhead:

### 6.1 Lifecycle Efficiency & Optimization
- **OrderNotificationMDB & AuditMDB:** MDBs do not have business interfaces and cannot be directly called by clients. They are managed in a container pool. When messages build up, the container scales active instances to process messages concurrently.
- **Throughput Tuning:** MDB execution is tuned in Payara:
    - `max-pool-size=30`: Ensures high concurrent message throughput without overwhelming database connections.
    - `acknowledgeMode=Auto-acknowledge`: Balances throughput and delivery guarantees. If processing fails, the message rolls back for redelivery.

---

## 7. Database Integration and Connection Pooling

TechMart integrates with MySQL using JPA/EclipseLink.

### 7.1 Connection Pool Optimization
We configure a container-managed Connection Pool inside the application using `@DataSourceDefinition`:
```java
@DataSourceDefinition(
    name = "java:app/jdbc/TechMartDS",
    className = "com.mysql.cj.jdbc.MysqlDataSource",
    url = "jdbc:mysql://localhost:3307/techmart_db?createDatabaseIfNotExist=true",
    user = "root",
    password = "dbms@java",
    initialPoolSize = 5,
    minPoolSize = 5,
    maxPoolSize = 50
)
```
- **Performance Trade-offs:** Initializing connections is expensive. By keeping a minimum pool size of 5 and maximum of 50, connections remain active. 
- **DB Auto-Seeding:** `@Singleton @Startup` `DatabaseSeederBean` automatically populates the schema with admin credentials and initial inventory, ensuring the system is ready for testing immediately after deployment.

### 7.2 Concurrency Control and Optimistic Locking Scenario
To prevent inventory overselling during concurrent user checkouts, TechMart employs JPA/EclipseLink optimistic concurrency control on the `Product` entity. The `Product` entity implements this protection using a `@Version` annotated `version` field mapped to an integer column in the database. When concurrent checkout threads attempt to update the same product inventory, the thread committing first succeeds and increments the version number. Subsequent concurrent threads fail the version check, throwing an `OptimisticLockException` which rolls back their transaction, preventing negative stock levels.

### 7.3 Soft Delete Audit Trail Strategy
Rather than performing hard deletes (`em.remove()`) which destroy transaction history and violate data auditing standards, TechMart implements a soft delete strategy:
*   A `deleted` boolean column is added to the `products` table.
*   `ProductBean.deleteProduct()` is overridden to toggle `deleted = true` and call `em.merge()` rather than removing the entity.
*   Catalog listings (`ProductBean.getAllProducts()`) and cache loading (`InventoryCacheBean.refreshCache()`) explicitly append `WHERE p.deleted = false` to filter out inactive inventory while preserving historical DB rows for foreign key constraints on past checkout audits.

Below is a sequential walkthrough of how two concurrent checkout transactions for the same product are resolved using optimistic locking:

```mermaid
sequenceDiagram
    autonumber
    participant DB as MySQL Database
    participant TxA as OrderServiceBean (Tx A)
    participant TxB as OrderServiceBean (Tx B)

    Note over DB: Product 1 (Laptop): Stock = 1, Version = 5
    TxA->>DB: Read Product 1 (Stock=1, Version=5)
    TxB->>DB: Read Product 1 (Stock=1, Version=5)
    
    Note over TxA: Verify Stock (1 >= 1) -> Decrement stock to 0
    TxA->>DB: UPDATE products SET quantity=0, version=6 WHERE id=1 AND version=5
    Note over DB: Version matches! Update succeeds (1 row modified)
    DB-->>TxA: Success (1 row updated)
    Note over TxA: Commit Transaction A

    Note over TxB: Verify Stock (1 >= 1) -> Decrement stock to 0
    TxB->>DB: UPDATE products SET quantity=0, version=6 WHERE id=1 AND version=5
    Note over DB: Version mismatch! (Current DB Version is 6)
    DB-->>TxB: Update fails (0 rows updated)
    Note over TxB: Throws OptimisticLockException
    Note over TxB: Rollback Transaction B
```

#### Detailed Flow Explanation:
1. **Initial State:** The product "Laptop" (ID `1`) has a stock `quantity` of `1` and a JPA `@Version` column set to `5`.
2. **Concurrent Fetch:** Two concurrent requests, Transaction A (Customer A) and Transaction B (Customer B), read the same product state simultaneously. Both retrieve the product object with `quantity = 1` and `version = 5`.
3. **Local Modification:**
    - Transaction A verifies that quantity is sufficient ($1 \ge 1$), decrements local quantity to `0`, and schedules a merge.
    - Transaction B verifies that quantity is sufficient ($1 \ge 1$), decrements local quantity to `0`, and schedules a merge.
4. **Transaction A Commits First:** The persistence provider executes:
   `UPDATE products SET quantity = 0, version = 6 WHERE id = 1 AND version = 5;`
   Since the version in the database is indeed `5`, the database updates the row and increments the version to `6`. The statement returns an update count of `1`. Transaction A completes and commits.
5. **Transaction B Attempts to Commit:** The persistence provider executes:
   `UPDATE products SET quantity = 0, version = 6 WHERE id = 1 AND version = 5;`
   Since Transaction A already modified the row, the version in the database is now `6`. The database update fails to match any row where `version = 5`, returning an update count of `0`.
6. **Exception and Rollback:** The persistence provider detects that zero rows were updated, throws a `jakarta.persistence.OptimisticLockException`, and automatically rolls back Transaction B. The stock cache is rolled back, preventing the stock level from going negative or registering an invalid double-sale. Customer B is shown a user-friendly error dialog ("Item was purchased by another customer. Please refresh and try again."), securing system integrity.

---

## References

- Burke, B. and Monson-Haefel, R., 2006. *Enterprise JavaBeans 3.0*. Sebastopol: O'Reilly Media.
- Oracle, 2023. *Jakarta Enterprise Edition (Jakarta EE) 10 Specification*. Eclipse Foundation. Available at: https://jakarta.ee/specifications/ [Accessed 22 June 2026].
- Payara Foundation, 2024. *Payara Server 6 Enterprise Documentation*. Available at: https://docs.payara.fish/enterprise/ [Accessed 22 June 2026].
