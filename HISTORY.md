```bash
mvn clean compile -Pskip-native
mvn clean compile -Pskip-native
mvn test -Pskip-native
mvn clean test -Pskip-native
mvn exec:java -Dexec.mainClass="com.xsyphon.javaext.PerformanceTest" -Pskip-native
mvn clean package -DskipNativeBuild=true

mvn exec:java -Dexec.mainClass="com.xsyphon.javaext.PerformanceTest" -Pskip-native
mvn clean compile -Pskip-native
mvn clean build -Pskip-native
mvn clean package -DskipNativeBuild=true
mvn test -Dtest=TimeXTest
mvn clean package -DskipNativeBuild=true

./gradlew build -PskipNativeBuild=true
./gradlew test -PskipNativeBuild=true
./gradlew performanceTest -PskipNativeBuild=true
./gradlew clean fatJar -PskipNativeBuild=true
```