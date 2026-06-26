Business Component Development I
J2EE Tutorial
Table of Content
Introduction to J2EE 4
Key characteristics of J2EE: 4
Common J2EE application servers include: 4
Enterprise JavaBeans (EJB) 5
Session Beans 5
Stateless Session Beans 6
Key characteristics: 6
Stateful Session Beans 6
Key characteristics: 6
Singleton Session Beans 8
Key characteristics: 8

Dependency Injection Techniques 14
@EJB Annotation 14
When to Use @EJB 14
InitialContext and JNDI Lookup 15
Dependency Injection vs. Initial Context 16
Dependency Injection (@EJB): 16
Initial Context Lookup: 16
Packaging Systems 17
EAR Files 18
EJB Modules 19
WAR Files 20
Java Message Service (JMS) 21
JMS Basics 22
Key components of JMS: 22
Message Types 22
JMS supports several message types: 22
Message Producers and Consumers 23
Message-Driven Beans (MDB) 25
MDB Architecture 25
Implementing MDBs 26
Testing Enterprise Applications 27
Load Testing 27
Key aspects: 27
Common tools: 27
Example JMeter test plan: 28
Key metrics: 28
Performance Testing 29
Types of performance tests: 30
Performance optimization techniques: 30

Introduction to J2EE
Java 2 Enterprise Edition (J2EE), now known as Jakarta EE or Java EE, is a
comprehensive platform for enterprise application development that evolved through several
major versions: J2EE (versions 1.2-1.4) from 1999-2003, Java EE (versions 5-8) from 2006-2017
under Oracle/Sun stewardship, and Jakarta EE (versions 8-11) from 2019-present under the
Eclipse Foundation. This enterprise platform extends the core Java SE with robust specifications
for distributed computing, web services, messaging, and persistence, enabling developers to build
scalable, reliable multi-tier enterprise applications with standardized APIs. Jakarta EE 10
(released 2022) and Jakarta EE 11 (released 2023) represent the latest evolution, bringing
modern cloud-native capabilities while maintaining backward compatibility with legacy J2EE
applications.
Key characteristics of J2EE:
● Platform-independent: Applications can be deployed on any J2EE-compliant
application server
● Component-based architecture: Modular components that can be reused and
assembled
● Multi-tier applications: Typically follows a multi-tier architecture (client tier, web tier,
business tier, EIS tier)
● Standard APIs: For database access, messaging, transaction management, etc.
Common J2EE application servers include:
● WildFly (formerly JBoss)
● GlassFish
● IBM WebSphere
● Oracle WebLogic
● Apache TomEE
Enterprise JavaBeans (EJB)
Enterprise JavaBeans (EJB) are server-side components that form the core of the
Java EE business tier, encapsulating complex business logic in a distributed application.
Introduced in 1998 with J2EE, EJBs have evolved significantly through EJB 1.0 (complex and
XML-heavy), EJB 2.0 (added message-driven beans), EJB 3.0 (simplified with annotations), and
EJB 3.2 (in Java EE 7). EJBs run inside an EJB container that automatically provides crucial
enterprise services including transaction management (both container-managed and beanmanaged), security (declarative and programmatic), concurrency control, dependency injection,
instance pooling, lifecycle management, and remoting capabilities. This container-managed
approach allows developers to focus on business logic rather than infrastructure concerns. EJBs
are particularly valuable for complex enterprise applications requiring distributed processing,
scalability, and robust transaction management. The three primary types of EJBs are session
beans (handling business logic), entity beans (persistence, largely replaced by JPA), and
message-driven beans (asynchronous processing).
Session Beans
Session beans are the workhorses of the EJB world, representing business
processes or actions within an enterprise application. They act as an extension of the client
application, executing business operations on the server side in response to client requests.
Session beans are instantiated when a client initiates a session with the application and are
typically tied to that specific client's interaction. Unlike entity beans (which represent persistent
data), session beans model behavior and implement the application's business logic. They
operate within a transaction context, with the EJB container managing transaction demarcation
automatically unless the bean specifies bean-managed transactions.
A key characteristic of session beans is their lifecycle management—they are
created when needed, pooled or cached by the container for efficiency, and don't survive server
crashes or restarts (except for singleton session beans). Session beans can be accessed locally
within the same JVM or remotely from different JVMs, making them versatile for different
deployment scenarios. Session beans are further categorized into three types based on their state
management approach:
1. Stateless session beans: Maintain no conversational state between method calls,
making them lightweight and highly scalable
2. Stateful session beans: Maintain client-specific state throughout a session, enabling
multi-step business processes
3. Singleton session beans: Shared instance across all clients, ideal for application-wide
coordination
Each type offers different guarantees around state preservation, concurrency,
and performance characteristics, allowing developers to choose the appropriate model for their
specific business requirements.
Stateless Session Beans
Stateless session beans don't maintain any conversational state with clients
between method calls.
Key characteristics:
❖ No client-specific state between method invocations
❖ Any instance can service any client
❖ More scalable and efficient for the container to manage
❖ Ideal for operations that complete in a single method call
Example:
@Stateless
public class CalculatorBean implements Calculator {
 @Override
 public int add(int a, int b) {
 return a + b;
 }
 @Override
 public int subtract(int a, int b) {
 return a - b;
 }
}
Client code:
@EJB
private Calculator calculator;
public void calculateSomething() {
 int result = calculator.add(5, 3);
 System.out.println("Result: " + result);
}
Stateful Session Beans
Stateful session beans maintain conversational state with clients across multiple
method invocations.
Key characteristics:
❖ Maintains client-specific state between method calls
❖ Each client is associated with a specific instance
❖ More resource-intensive than stateless beans
❖ Ideal for multi-step processes like shopping carts
Example
@Stateful
public class ShoppingCartBean implements ShoppingCart {

 private List<Item> items = new ArrayList<>();

 @Override
 public void addItem(Item item) {
 items.add(item);
 }

 @Override
 public List<Item> getItems() {
 return Collections.unmodifiableList(items);
 }

 @Override
 public void checkout() {
 items.clear();
 }

 @Remove
 public void remove() {
 items = null;
 }
}
Client Code:
@EJB
private ShoppingCart cart;
public void addProductToCart(Product product) {
 Item item = new Item(product, 1);
 cart.addItem(item);
}
Singleton Session Beans
Singleton beans are instantiated once per application and shared among all
clients.
Key characteristics:
❖ Single instance per application
❖ Shared state across the application
❖ Can be eagerly or lazily initialized
❖ Useful for application-wide operations or caching
Example:
@Singleton
@Startup
public class ConfigurationBean implements Configuration {

 private Properties config = new Properties();

 @PostConstruct
 public void init() {
 try (InputStream is = getClass().getResourceAsStream("/config.properties")) {
 config.load(is);
 } catch (IOException e) {
 throw new RuntimeException("Failed to load configuration", e);
 }
 }

 @Override
 public String getProperty(String key) {
 return config.getProperty(key);
 }

 @Override
 @Lock(LockType.WRITE)
 public void setProperty(String key, String value) {
 config.setProperty(key, value);
 }
}
Client Code:
@EJB
private Configuration configuration;
public void doSomething() {
 String serverUrl = configuration.getProperty("server.url");
}

Dependency Injection Techniques
@EJB Annotation
The @EJB annotation is a powerful dependency injection mechanism that allows you to inject
EJB references without explicitly looking them up. This annotation is part of the Java Enterprise
Edition (Java EE, now Jakarta EE) specification. It allows you to automatically inject Enterprise
JavaBeans (EJBs) into your Java classes without manually looking them up using JNDI (Java
Naming and Directory Interface). This reduces boilerplate code and makes your application more
maintainable:.
Example:
public class OrderProcessor {

 @EJB
 private CustomerService customerService;

 @EJB
 private InventoryService inventoryService;

 public void processOrder(Order order) {
 boolean validCustomer = customerService.validateCustomer(order.getCustomerId());

 if (validCustomer) {
 boolean inStock = inventoryService.checkAvailability(order.getItems());

 if (inStock) {
 inventoryService.updateInventory(order.getItems());
 }
 }
 }
}
When to Use @EJB
The @EJB annotation is most appropriate when:
● You're working within a Java EE application server environment
● You need the services provided by EJBs (transactions, security, remoting)
InitialContext and JNDI Lookup
The InitialContext and JNDI (Java Naming and Directory Interface) lookup system
represents a foundational architecture within Java enterprise environments that enables
seamless resource discovery and connectivity across distributed systems. Acting as a directory
service specifically tailored for Java applications, JNDI provides a standardized mechanism for
applications to store, retrieve, and manage objects by name—similar to how a phone book
organizes contacts—where the InitialContext serves as the primary entry point and gateway to
this hierarchical naming structure. This approach allows developers to abstract away the physical
location and implementation details of resources like Enterprise JavaBeans (EJBs), data sources,
JMS queues, and other enterprise components, enabling client applications to locate and utilize
these resources through simple string-based names regardless of their actual network location or
underlying implementation. The power of this paradigm becomes particularly evident in enterprise
environments where applications need to dynamically discover services at runtime without
hardcoding connection details, supporting scenarios ranging from simple local deployments to
complex distributed systems spanning multiple servers and even cloud environments, while also
facilitating important enterprise capabilities such as load balancing, failover, and the separation
of configuration from code—ultimately promoting more flexible, maintainable, and scalable
enterprise Java applications.
Example:
public class OrderProcessor {

 private CustomerService customerService;
 private InventoryService inventoryService;

 @PostConstruct
 public void init() {
 try {
 InitialContext context = new InitialContext();
 customerService = (CustomerService)
context.lookup("java:global/app/CustomerServiceBean");
 inventoryService = (InventoryService)
context.lookup("java:global/app/InventoryServiceBean");
 } catch (NamingException e) {
 throw new RuntimeException("Failed to look up EJBs", e);
 }
 }

 public void processOrder(Order order) {
 }
}
Dependency Injection vs. Initial Context
Dependency Injection (@EJB):
Pros:
● Simpler, more concise code
● Container manages the lifecycle
● No exception handling needed
● Easier to test (can be mocked)
Cons:
● Less control over when the lookup occurs
● Less flexibility for different lookup strategies
Initial Context Lookup:
Pros:
● More control over when and how lookups happen
● Can look up different implementations dynamically
● Can work outside of a container (e.g., standalone clients)
Cons:
● More verbose and complex code
● Requires exception handling
● Harder to test
Packaging Systems
J2EE applications can be packaged in different formats depending on their
components and deployment requirements. The packaging systems utilized within Java 2
Enterprise Edition (J2EE), now evolved into Jakarta EE, encompass a sophisticated hierarchy of
standardized archive formats meticulously designed to encapsulate applications and their
components for streamlined deployment across diverse enterprise environments. These
specialized containers range from the fundamental JAR (Java Archive) files that bundle compiled
Java classes and resources, to the more specialized WAR (Web Application Archive) files that
encapsulate web applications complete with servlets, JSPs, HTML, and supporting resources
arranged in a standardized directory structure, and ultimately to the comprehensive EAR
(Enterprise Application Archive) files that serve as the all-encompassing container capable of
housing multiple WARs, JARs, and deployment descriptors to form a complete enterprise
application deployment unit. This graduated approach to packaging affords developers
remarkable flexibility in organizing application components based on their logical boundaries,
reusability requirements, and deployment scenarios, while simultaneously providing application
servers with the critical metadata necessary to properly configure, initialize, and orchestrate these
components within the runtime environment. The standardization of these packaging formats
ensures consistent deployment behavior across compliant application servers from different
vendors, facilitates the separation of development concerns from deployment considerations, and
enables sophisticated enterprise features like classloader isolation, component versioning, hot
deployment, and the ability to manage complex dependency relationships—ultimately
streamlining the challenging process of transitioning applications from development environments
to testing, staging, and production systems within enterprise ecosystems of varying complexity
and scale.
EAR Files
Enterprise Archive (EAR) files represent the highest level of packaging abstraction in Java
Enterprise environments, functioning as comprehensive containers that consolidate multiple
specialized modules into a cohesive, deployable application unit. These sophisticated archives
encapsulate the complete application ecosystem, bundling EJB modules (JAR files), web
modules (WAR files), application client modules, and resource adapter modules alongside
application-level deployment descriptors that govern the relationships and configurations between
these components. The elegance of this approach lies in its ability to maintain logical separation
between different application tiers while preserving their interconnectedness, allowing enterprise
applications with complex architectures spanning multiple layers to be deployed, initialized, and
managed as a single coordinated unit across distributed environments, thereby simplifying
deployment workflows, version management, and the propagation of applications across
development, testing, and production infrastructures.
application.xml example:
<?xml version="1.0" encoding="UTF-8"?>
<application xmlns="http://xmlns.jcp.org/xml/ns/javaee"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
http://xmlns.jcp.org/xml/ns/javaee/application_7.xsd"
 version="7">
 <display-name>BusinessApplication</display-name>
 <module>
 <web>
 <web-uri>web-module.war</web-uri>
 <context-root>/business-app</context-root>
 </web>
 </module>
 <module>
 <ejb>ejb-module.jar</ejb>
 </module>
 <library-directory>lib</library-directory>
</application>
EJB Modules
Enterprise JavaBean (EJB) modules, packaged as JAR files with specialized metadata, serve as
dedicated containers for business logic components that require advanced enterprise services
such as transaction management, security, concurrency control, and remote accessibility. These
specialized archives house the compiled EJB classes implementing business interfaces, along
with their dependent utility classes and the crucial ejb-jar.xml deployment descriptor that defines
the beans' identities, relationships, and runtime behaviors within the container environment. EJB
modules encapsulate various bean types—session beans (stateless or stateful) for business
logic, message-driven beans for asynchronous processing, and historically, entity beans for data
persistence—providing a standardized structure that enables application servers to properly
instantiate, pool, monitor, and manage these components while enforcing their contractual
guarantees regarding transactional integrity, security constraints, and lifecycle management
across distributed business applications.
ejb-jar.xml example (optional with annotations):
<?xml version="1.0" encoding="UTF-8"?>
<ejb-jar xmlns="http://xmlns.jcp.org/xml/ns/javaee"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
http://xmlns.jcp.org/xml/ns/javaee/ejb-jar_3_2.xsd"
 version="3.2">
 <enterprise-beans>
 <session>
 <ejb-name>CustomerServiceBean</ejb-name>
 <ejb-class>com.example.ejb.CustomerServiceBean</ejb-class>
 <session-type>Stateless</session-type>
 <transaction-type>Container</transaction-type>
 </session>
 </enterprise-beans>
</ejb-jar>
WAR Files
Web Archive (WAR) files function as specialized containers specifically engineered for packaging
and deploying web applications within Java Enterprise environments, featuring a standardized
directory structure that organizes presentation-tier components according to their roles and
relationships. These archives encapsulate the complete web application footprint, including Java
servlets for dynamic request handling, JavaServer Pages (JSP) for view generation,
HTML/CSS/JavaScript for client-side presentation, supporting images and media files, tag
libraries, utility classes, and the crucial web.xml deployment descriptor that defines the
application's servlets, filters, listeners, and security constraints. Modern WAR files have evolved
beyond their original presentation-tier focus to optionally include EJB components for tighter
integration between web and business tiers, particularly with the advent of lightweight EJBs, while
maintaining the critical WEB-INF directory that protects sensitive configuration files and classes
from direct client access—ensuring proper encapsulation of web application resources while
providing application servers with the structural information needed to correctly deploy and
initialize the application within the servlet container.
web.xml example:
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
 xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/webapp_4_0.xsd"
 version="4.0">
 <display-name>Business Web Application</display-name>
 <servlet>
 <servlet-name>OrderServlet</servlet-name>
 <servlet-class>com.example.web.OrderServlet</servlet-class>
 </servlet>
 <servlet-mapping>
 <servlet-name>OrderServlet</servlet-name>
 <url-pattern>/orders/*</url-pattern>
 </servlet-mapping>
</web-app>
Java Message Service (JMS)
The Java Message Service (JMS) represents a sophisticated enterprise-grade messaging
framework and API specification that fundamentally transforms how components communicate
within distributed systems by introducing a robust asynchronous messaging paradigm that
elegantly decouples message producers from consumers across time, space, and
implementation boundaries. This critical middleware technology establishes reliable
communication channels between disparate system components—potentially operating on
different platforms, implemented in various languages, or running on geographically distributed
servers—without requiring these components to maintain direct awareness of each other's
existence, availability, or implementation details. JMS accomplishes this architectural feat by
introducing message brokers that intelligently manage message queues and topics, supporting
both point-to-point communication models (where each message is delivered to exactly one
consumer from a queue) and publish-subscribe patterns (where messages are broadcast to
multiple interested subscribers), while providing enterprise-critical capabilities such as
guaranteed message delivery, transaction support, message persistence across system failures,
message prioritization, and time-to-live constraints that collectively ensure system reliability even
under challenging network conditions or during partial system outages. The comprehensive JMS
specification defines not only the core messaging patterns and delivery semantics but also
standardizes message types (including text, object, bytes, stream, and map messages),
connection factories, destinations, session management, message selectors for filtering, and
acknowledgment modes—creating a complete messaging ecosystem that has become
foundational for enterprise integration scenarios ranging from simple workload distribution to
complex event-driven architectures, service orchestration, system integration, real-time data
processing pipelines, and large-scale distributed transaction management across the enterprise
computing landscape.
JMS Basics
Key components of JMS:
● JMS Provider: Implements the JMS interface (e.g., OpenMQ, ActiveMQ, RabbitMQ)
● Destinations: Where messages are sent (queues or topics)
● Connection Factory: Creates connections to the JMS provider
● Connection: Active connection to the JMS provider
● Session: Single-threaded context for producing and consuming messages
● Message Producers: Create and send messages
● Message Consumers: Receive messages
Message Types
JMS supports several message types:
● TextMessage: Contains a string
● MapMessage: Contains name-value pairs
● BytesMessage: Contains a stream of bytes
● StreamMessage: Contains a stream of primitive values
● ObjectMessage: Contains a serializable Java object
Message Producers and Consumers
Producer Example:
@Stateless
public class NotificationSender {

 @Resource(lookup = "java:/ConnectionFactory")
 private ConnectionFactory connectionFactory;

 @Resource(lookup = "java:/queue/notifications")
 private Queue notificationQueue;

 public void sendNotification(String recipient, String subject, String content) {
 try (Connection connection = connectionFactory.createConnection();
 Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE))
{

 MessageProducer producer = session.createProducer(notificationQueue);

 MapMessage message = session.createMapMessage();
 message.setString("recipient", recipient);
 message.setString("subject", subject);
 message.setString("content", content);
 message.setLong("timestamp", System.currentTimeMillis());

 producer.send(message);

 } catch (JMSException e) {
 throw new RuntimeException("Failed to send notification", e);
 }
 }
}
Consumer Example (non-MDB):
@Stateless
public class NotificationReceiver {

 @Resource(lookup = "java:/ConnectionFactory")
 private ConnectionFactory connectionFactory;

 @Resource(lookup = "java:/queue/notifications")
 private Queue notificationQueue;

 public List<Map<String, Object>> receiveNotifications() {
 List<Map<String, Object>> notifications = new ArrayList<>();

 try (Connection connection = connectionFactory.createConnection();
 Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE))
{
 MessageConsumer consumer = session.createConsumer(notificationQueue);
 connection.start();

 for (int i = 0; i < 10; i++) {
 Message message = consumer.receive(1000);
 if (message == null) {
 break;
 }
 if (message instanceof MapMessage) {
 MapMessage mapMessage = (MapMessage) message;
 Map<String, Object> notification = new HashMap<>();
 notification.put("recipient", mapMessage.getString("recipient"));
 notification.put("subject", mapMessage.getString("subject"));
 notification.put("content", mapMessage.getString("content"));
 notification.put("timestamp", mapMessage.getLong("timestamp"));

 notifications.add(notification);
 }
 }
 } catch (JMSException e) {
 throw new RuntimeException("Failed to receive notifications", e);
 }
 return notifications;
 }
}
Message-Driven Beans (MDB)
Message-Driven Beans (MDBs) represent a sophisticated enterprise component
architecture within the Java EE ecosystem that elegantly bridges the gap between synchronous
application logic and asynchronous messaging systems, functioning as specialized, containermanaged components specifically engineered to consume and process messages arriving
through messaging channels such as JMS destinations without requiring client awareness or
direct coupling. These unique enterprise beans fundamentally differ from their session bean
counterparts in that they are exclusively invoked by the arrival of messages rather than direct
client method calls, effectively serving as enterprise-grade message endpoints that seamlessly
integrate the robust transaction management, security services, concurrency control, and
resource pooling capabilities of the EJB container with the powerful asynchronous communication
patterns enabled by enterprise messaging systems. The container provides critical infrastructure
services for MDBs, including automatic message acknowledgment, connection management,
instance pooling for scalable message consumption, transaction coordination that can span both
message receipt and subsequent processing operations, and message redelivery mechanisms in
failure scenarios—creating a highly resilient processing environment that gracefully handles
varying message loads while maintaining system integrity. MDBs excel in numerous enterprise
scenarios including request processing decoupling, workload distribution across server clusters,
implementation of the competing consumers pattern for scalable processing, event-driven
architectures, integration with external systems, reliable batch processing of resource-intensive
operations, and building responsive systems that maintain performance under varying load
conditions—all while shielding developers from the complexities of low-level messaging protocols,
concurrency management, and thread safety concerns through the container's comprehensive
management of the message lifecycle, from consumption through processing completion,
enabling architects to design loosely coupled, highly scalable distributed systems where
components can evolve independently while maintaining robust and reliable communication
channels.
MDB Architecture
MDBs are similar to stateless session beans but are invoked by the container when messages
arrive:
● No client interface
● Container-managed concurrency (multiple instances can process messages
concurrently)
● No state between message processings
● Cannot be directly accessed by clients
Implementing MDBs
Example:
@MessageDriven(
 activationConfig = {
 @ActivationConfigProperty(propertyName = "destinationType", propertyValue =
"javax.jms.Queue"),
 @ActivationConfigProperty(propertyName = "destination", propertyValue =
"java:/queue/notifications"),
 @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Autoacknowledge")
 }
)
public class NotificationProcessorMDB implements MessageListener {

 @EJB
 private EmailService emailService;

 @Resource
 private MessageDrivenContext mdbContext;

 @Override
 public void onMessage(Message message) {
 try {
 if (message instanceof MapMessage) {
 MapMessage mapMessage = (MapMessage) message;

 String recipient = mapMessage.getString("recipient");
 String subject = mapMessage.getString("subject");
 String content = mapMessage.getString("content");

 emailService.sendEmail(recipient, subject, content);

 System.out.println("Notification sent to " + recipient);
 }
 } catch (Exception e) {
 mdbContext.setRollbackOnly();
 throw new RuntimeException("Error processing notification", e);
 }
 }
}
Testing Enterprise Applications
Load Testing
Load testing constitutes a critical discipline within the enterprise application quality
assurance lifecycle that meticulously evaluates a system's behavior, responsiveness, and stability
under anticipated production workloads, deliberately subjecting the application to carefully
calibrated volumes of simulated user interactions, transaction processing demands, and data
throughput requirements that mirror expected real-world usage patterns. This specialized testing
methodology employs sophisticated tooling to generate precisely controlled concurrent user
sessions, transaction rates, and data volumes while simultaneously capturing comprehensive
performance metrics including response times, throughput rates, resource utilization patterns,
and system stability indicators across the entire technology stack—from database query
execution times to application server thread utilization, memory consumption, connection pool
behavior, and network latency characteristics. The primary objective extends beyond simply
determining whether the system functions correctly under load, focusing instead on quantifying
exactly how well it performs relative to established service level agreements (SLAs) and
identifying potential bottlenecks, resource constraints, or scalability limitations before they impact
actual users in production environments. Properly executed load testing follows a structured
methodology that progressively increases workload intensity from baseline levels through
expected average conditions to anticipated peak scenarios, allowing performance engineers to
establish critical benchmarks such as the system's maximum effective throughput, optimal
concurrency levels, and breaking points—the thresholds at which performance degradation
becomes unacceptable or system stability becomes compromised—while providing invaluable
data for capacity planning, resource allocation, configuration optimization, and architectural
refinement activities that collectively ensure the enterprise application can deliver consistent,
reliable performance throughout its operational lifecycle even as demand patterns evolve and
business requirements expand.
Key aspects:
● Simulates real-world user behavior
● Measures response times, throughput, resource utilization
● Identifies performance bottlenecks
● Determines if the system meets performance requirements
Common tools:
● Apache JMeter
● Gatling
● k6
● LoadRunner
Example JMeter test plan:
1. Create thread groups to simulate users
2. Add HTTP requests to simulate user actions
3. Configure ramp-up periods and loop counts
4. Add listeners to collect results
5. Configure assertions to verify responses
Key metrics:
● Response time (average, percentiles)
● Throughput (requests per second)
● Error rate
● CPU and memory usage
● Database connection pool usage
Performance Testing
Performance testing encompasses a comprehensive and multifaceted evaluation
framework that systematically analyzes an enterprise application's speed, responsiveness,
stability, and resource efficiency across a diverse spectrum of operating conditions, workload
patterns, and environmental configurations, incorporating not just load testing but also specialized
test categories including stress testing (pushing systems beyond normal capacity to identify
breaking points), endurance testing (maintaining moderate loads over extended periods to detect
memory leaks and resource depletion), spike testing (introducing sudden, dramatic workload
increases), capacity testing (determining maximum user/transaction volumes), volume testing
(processing extremely large datasets), and scalability testing (examining how performance
characteristics change as system resources are added or removed). This holistic approach to
performance evaluation examines the entire application ecosystem—including front-end
rendering efficiency, network transmission characteristics, middleware processing capabilities,
database query optimization, integration point behaviors, and infrastructure utilization patterns—
through precisely engineered test scenarios that isolate different architectural components while
measuring key performance indicators such as transaction response times, request throughput
rates, resource utilization efficiency, and system stability metrics under various operational
conditions. Performance testing produces actionable intelligence that drives critical engineering
decisions regarding hardware sizing, capacity planning, architectural optimization, caching
strategies, connection pooling configurations, thread management approaches, database
indexing schemes, and numerous other technical parameters that collectively determine an
application's ability to deliver consistent, responsive user experiences at scale. The discipline has
evolved from simple execution time measurements to sophisticated analysis incorporating
statistical modeling, trend analysis, correlation of metrics across system tiers, identification of
performance anomalies through machine learning, and predictive capacity planning—enabling
organizations to scientifically validate that their mission-critical applications will consistently meet
both explicit performance requirements documented in service level agreements and implicit user
expectations regarding system responsiveness, reliability, and consistency across varying usage
patterns and business cycles.
Types of performance tests:
● Stress testing: Tests beyond normal capacity to find breaking points
● Endurance testing (soak testing): Tests system behavior over extended periods
● Spike testing: Tests system response to sudden, large increases in load
● Scalability testing: Tests how the system scales with added resources
Performance optimization techniques:
● Caching: Use application-level caches (e.g., singleton beans)
● Connection pooling: Reuse database connections
● Stateless design: Prefer stateless over stateful components when possible
● Asynchronous processing: Use MDBs and JMS for non-critical operations
● Batch processing: Process multiple items in a single transaction
● Pagination: Limit result sets for queries
● Optimized queries: Use indexes, limit joins, avoid N+1 queries
Example: Monitoring EJB performance:
@Interceptor
public class PerformanceInterceptor {

 @AroundInvoke
 public Object measureMethodExecutionTime(InvocationContext context) throws Exception {
 long startTime = System.nanoTime();
 try {
 return context.proceed();
 } finally {
 long endTime = System.nanoTime();
 long executionTime = (endTime - startTime) / 1_000_000;

 String className = context.getTarget().getClass().getName();
 String methodName = context.getMethod().getName();
 System.out.printf("Method %s.%s executed in %d ms%n", className, methodName,
executionTime);
 }
 }
}
To enable the interceptor:
@Stateless
@Interceptors(PerformanceInterceptor.class)
public class ProductServiceBean implements ProductService {
}