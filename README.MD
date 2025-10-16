# Karaf Plugin Bug Reproduction

A multi-module Maven project demonstrating and investigating issues with Apache Karaf OSGi container configurations, specifically related to HTTP server implementations and Spring Boot bundle integration.

## Project Information

- **Group ID**: `com.example.osgi`
- **Artifact ID**: `karaf-plugin-bug`
- **Version**: `1.1`
- **Java Version**: 21
- **Karaf Version**: 4.4.8

## Important: Git Branches

This repository contains different approaches to Spring integration with Karaf:

- **`spring-2.x` branch**: Demonstrates **Spring Boot 2.x embedded** approach working with Felix HTTP
- **spring-no-boot-6.x-example**: Demonstrates **Spring (non-Boot) WAB deployment** with Pax Web using `web.xml` and `dispatcher-servlet.xml`

## Project Structure

This project consists of 8 modules organized in a specific architecture:

```
karaf-plugin-bug/
├── pom.xml                           # Parent POM
├── README.MD                         # This file
├── com.example.osgi.bundle1/         # OSGi Bundle 1
├── com.example.osgi.bundle2/         # OSGi Bundle 2
├── com.example.osgi.bundle3/         # OSGi Bundle 3
├── com.example.osgi.bundle4/         # Spring Bundle (see branch-specific details)
├── com.example.osgi.feature-repo-1/  # Karaf Feature Repository 1
├── com.example.osgi.feature-repo-2/  # Karaf Feature Repository 2
├── com.example.osgi.feature-repo-3/  # Karaf Feature Repository 3
└── karaf-assembly/                   # Custom Karaf Distribution
```

## Module Overview

### OSGi Bundles
- **bundle1**: Standard OSGi bundle
- **bundle2**: Standard OSGi bundle
- **bundle3**: Standard OSGi bundle
- **bundle4**: Spring bundle (implementation varies by branch - see Spring Integration Approaches below)

### Feature Repositories
- **feature-repo-1**: Karaf feature repository packaging bundle1
- **feature-repo-2**: Karaf feature repository packaging bundle2
- **feature-repo-3**: Karaf feature repository packaging bundle3 and bundle4 (Spring bundle)

### Karaf Assembly
- **karaf-assembly**: Custom Karaf distribution with pre-configured features and bundles

## Spring Integration Approaches

This project explores two distinct approaches to integrating Spring with Apache Karaf:

### Approach 1: Spring Boot 2.x Embedded with Felix HTTP (branch: `spring-2.x`)

**Bundle4** contains a **Spring Boot 2.x embedded application** that works with Felix HTTP when Tomcat dependencies are excluded.

**Key Points:**
- ✅ **Spring Boot 2.x works** with Felix HTTP in Karaf
- ❌ **Spring Boot 3.x does NOT work** due to:
  - Classpath scanning issues in OSGi environment
  - Felix lacking proper Jakarta EE integration
  - Incompatibilities between Spring Boot 3's Jakarta namespace and Felix HTTP
- Requires excluding Spring Boot's embedded Tomcat server
- Uses Felix HTTP as the HTTP service provider

### Approach 2: Spring WAB with Pax Web (main/other branches)

**Bundle4** is deployed as a **Web Application Bundle (WAB)** using traditional Spring (without Boot) and Pax Web.

**Key Points:**
- Uses `web.xml` for web application configuration
- Uses `dispatcher-servlet.xml` for Spring MVC configuration
- Deployed as a WAB (Web Application Bundle) to Pax Web
- Works well with Karaf's Pax Web implementation
- More traditional Java EE / Servlet approach
- Better compatibility with OSGi classloading

### Key Challenges

1. **Spring Boot Version Limitations**: 
   - Spring Boot 3.x is incompatible with Felix HTTP due to Jakarta namespace and classpath scanning issues
   - Spring Boot 2.x can work with Felix HTTP but requires careful dependency management
   
2. **HTTP Server Selection**:
   - **Felix HTTP**: Works with Spring Boot 2.x embedded applications (with exclusions)
   - **Pax Web**: Recommended for WAB deployments using traditional Spring
   
3. **Dependency Management**: 
   - Spring Boot's auto-configuration conflicts with Karaf's OSGi environment
   - Embedded server dependencies must be excluded when using Karaf's HTTP infrastructure
   - Jakarta vs javax namespace compatibility issues with Spring Boot 3.x

## Prerequisites

- **JDK 21** or higher
- **Apache Maven 3.6+**
- **Apache Karaf 4.4.8** (if running standalone)
- Understanding of **OSGi** and **Spring** / **Spring Boot** fundamentals
- Knowledge of **WAB (Web Application Bundle)** concepts for Pax Web approach

## Building the Project

Build the entire project from the root directory:

```bash
mvn clean install
```

Build individual modules:

```bash
# Build a specific bundle
cd com.example.osgi.bundle1
mvn clean install

# Build the Spring bundle (bundle4) - implementation varies by branch
cd com.example.osgi.bundle4
mvn clean install

# Build a feature repository
cd com.example.osgi.feature-repo-1
mvn clean install

# Build the Karaf assembly
cd karaf-assembly
mvn clean install
```

## Running the Custom Karaf Distribution

After building, start the custom Karaf assembly:

```bash
cd karaf-assembly/target/assembly
bin/karaf
```

Or run in clean mode:

```bash
bin/karaf clean
```

## Technologies Used

- **Apache Karaf 4.4.8**: OSGi runtime environment
- **Spring Boot 2.x** (branch `spring-2.x`): Embedded application framework
- **Spring Framework** (WAB branches): Traditional Spring without Boot
- **Felix HTTP**: HTTP service implementation for Spring Boot 2.x approach
- **Pax Web**: HTTP service implementation for WAB approach
- **Maven Bundle Plugin 5.1.9**: OSGi bundle creation
- **Karaf Maven Plugin 4.4.8**: Karaf feature and assembly management
- **Java 21**: Target Java version

## Current Findings

### Working Configurations ✅

1. **Spring Boot 2.x + Felix HTTP** (branch: `spring-2.x`):
   - Embedded Spring Boot 2.x application works with Felix HTTP
   - Requires exclusion of Tomcat embedded server dependencies
   - Uses Felix HTTP as the HTTP service

2. **Spring WAB + Pax Web** (main/other branches):
   - Traditional Spring (non-Boot) deployed as WAB
   - Uses `web.xml` and `dispatcher-servlet.xml`
   - Works reliably with Pax Web in Karaf assembly

### Known Issues ❌

1. **Spring Boot 3.x Incompatibility**:
   - Classpath scanning fails in OSGi environment
   - Felix HTTP lacks Jakarta EE integration needed for Spring Boot 3.x
   - Jakarta namespace conflicts with Felix HTTP

2. **Spring Boot 2.x + Pax Web**:
   - Embedded Spring Boot conflicts with Pax Web implementations
   - Auto-configuration issues in OSGi environment

## Development

### Switching Between Approaches

To try different approaches:

```bash
# For Spring Boot 2.x embedded with Felix HTTP
git checkout spring-2.x
mvn clean install

# For Spring WAB with Pax Web
git checkout [main-or-relevant-branch]
mvn clean install
```

### Adding a New Bundle

1. Create a new module directory
2. Add the module to the parent `pom.xml`
3. Configure the bundle with `maven-bundle-plugin`
4. Create a corresponding feature repository if needed

### Working with Spring Bundle (bundle4)

#### For Spring Boot 2.x Approach (branch: `spring-2.x`):
1. Keep Spring Boot version at 2.x
2. Exclude embedded Tomcat/Jetty/Undertow dependencies
3. Configure for Felix HTTP compatibility
4. Do NOT upgrade to Spring Boot 3.x

#### For Spring WAB Approach (main branches):
1. Use Spring Framework without Spring Boot
2. Create `web.xml` for servlet configuration
3. Create `dispatcher-servlet.xml` for Spring MVC configuration
4. Package as a WAB (Web Application Bundle)
5. Configure for Pax Web deployment

### Modifying Features

Feature definitions are located in:
- `com.example.osgi.feature-repo-*/src/main/feature/feature.xml`

## Recommendations

### Use Spring Boot 2.x + Felix HTTP When:
- You want embedded Spring Boot simplicity
- You can accept Spring Boot 2.x limitations
- Your application doesn't require Spring Boot 3.x features
- You prefer Felix HTTP

### Use Spring WAB + Pax Web When:
- You need better OSGi compatibility
- You want to use latest Spring Framework (without Boot)
- You're comfortable with traditional Spring configuration
- You need Jakarta EE features with Pax Web
- You prefer more control over the deployment structure

## Contributing

This is a bug reproduction and exploration project. To contribute:

1. Document your findings with Spring Boot/Spring and Karaf integration
2. Add test cases demonstrating HTTP server compatibility issues
3. Propose solutions with working examples on appropriate branches
4. Share successful configurations for different approaches

## License

[License information to be added]

## Contact

For questions or issues, please open an issue in this repository.
