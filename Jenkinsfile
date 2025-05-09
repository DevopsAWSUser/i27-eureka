pipeline {
  agent { 
    label 'maven-slave' 
  }

  environment {
    JAVA_HOME = '/usr/lib/jvm/java-17-amazon-corretto.x86_64'
    MAVEN_HOME = '/opt/apache-maven-3.8.8'
    PATH+MAVEN = "${MAVEN_HOME}/bin"
    PATH+JAVA = "${JAVA_HOME}/bin"
    APPLICATION_NAME = 'eureka'
  }

  stages {
    stage('Build') {
      steps {
        echo "Building the ${env.APPLICATION_NAME} application"
        sh 'echo "Java Version:"'
        sh 'java -version'
        sh 'echo "Maven Version:"'
        sh 'mvn -version'
        sh 'mvn clean package -DskipTests=true'
      }
    }
  }
}
