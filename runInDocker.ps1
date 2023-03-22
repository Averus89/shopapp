$java11 = (Get-ChildItem -Path (gcm -All java | Where-Object {$_.Source -like "*jdk-11*"} | Select-Object -Index 1).Path).Directory.Parent.FullName
$oldJavaHome = $env:JAVA_HOME
$env:JAVA_HOME = $java11
.\gradlew clean bootJar
$env:JAVA_HOME = $oldJavaHome
docker-compose -f docker-compose.yaml up --build