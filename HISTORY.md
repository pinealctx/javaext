```bash
mvn clean compile -Pskip-native
mvn clean compile -Pskip-native
mvn test -Pskip-native
mvn clean test -Pskip-native
mvn exec:java -Dexec.mainClass="com.xsyphon.javaext.PerformanceTest" -Pskip-native

./gradlew build -PskipNativeBuild=true
./gradlew test -PskipNativeBuild=true
./gradlew performanceTest -PskipNativeBuild=true
```