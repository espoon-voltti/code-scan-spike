# Integration Template

A template project:
1. Click *Use this template* button at repository.
2. Rename artifact details at *pom.xml*, *properties* at *\*.properties*, *packages* and *classes*.
3. Update *README.md*.
4. Remove any and all unnecessary code. e.g. JoTo stuff in case of non-JoTo projects.
   1. remove all routes
   2. remove excess beans
   3. remove all tests

## Running

### Run with Maven

```
mvn clean package
aws-vault exec voltti-sst -- ./run.sh
```

Use a remote debugger:
```
aws-vault exec voltti-sst -- ./remote-debug.sh
```
### Run with Java

```
java -jar target/integration-template-*.jar
```

### Run with Docker

```
mvn clean package
docker build . -t integration-template
aws-vault exec voltti-sst --server -- docker run integration-template
```

## Maven reporting

1. Execute: `mvn clean site`
2. Open *target/site/index.html* in browser and see the reports under *Project Reports*
    * Checkstyle
    * Owasp
    * SpotBugs
