pipeline {
  agent { label 'maven-slave' }

  environment {
    JAVA_HOME = '/usr/lib/jvm/java-17-amazon-corretto.x86_64'
    MAVEN_HOME = '/opt/apache-maven-3.8.8'
    APPLICATION_NAME = 'eureka'
  }

  stages {
    stage('Build') {
      steps {
        echo "Building the ${env.APPLICATION_NAME} application"
        sh '''
          export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
          echo "Java Version:"
          java -version
          echo "Maven Version:"
          mvn -version
          mvn clean package -DskipTests=true
        '''
        archiveArtifacts artifacts: 'target/*.jar', followSymlinks: false
      }
    }
    stage('Unit Test') {
      steps {
        echo "Performing Unit Tests for ${env.APPLICATION_NAME} application"
        sh '''
          export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
          mvn test
        '''
      }
    }
  }
}
