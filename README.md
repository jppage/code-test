## Log Service

This project loads and processes a log file. It calculates the duration between events starting and finishing and flags an alert if a duration threshold is breached. This is a Spring Boot application in Java 11 which uses @EventListener to trigger the file load once the application starts. No input from the user is required other than passing the log file path in the startup command. Alternatively you can ommit this and add it to the application.properties instead. To run this project check it out locally and run the following (it uses a Maven wrapper so you don't need to have Maven installed)

`./mvnw clean install`

Before starting the application you will need the HSQL database installed locally. Start the database service using the following command from inside the hsqldb/data folder:

`java -cp ../lib/hsqldb.jar org.hsqldb.server.Server --database.0 file:testdb --dbname.0 testdb`

From inside the hsqldb/data folder, run the hsql Swing client:
`java -cp ../lib/hsqldb.jar org.hsqldb.util.DatabaseManagerSwing`
Set the client to standalone mode and set the url to 
`jdbc:hsqldb:hsql://localhost/testdb`


**Please note that the unit tests use an in memory HSQL database so the locally running one isn't required.**

To start the application and pass the file path as a parameter run:

`./mvnw spring-boot:run -Dspring-boot.run.arguments=--file.path=src/main/resources/logfile`

**This project uses Lombok so if loading in IntelliJ**
* **Ensure Preferences > Compiler > Annotation Processors is selected**
* **Install the Lombok plugin if not already present**

### Testing 

The project contains one main unit test class which contains separate tests that cover different scenarios. A test log file is loaded when the test starts.

### Performance considerations

The application uses Scanner which reads the log file line by line rather than in its entirety. Once a pair of lines are detected they are persisted and removed from the map. This approach means that the application will scale to process very large log files.

### To do

The application needs to be stress tested using a large log file.

