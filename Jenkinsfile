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
     
    stage('sonar')
       steps {
         sh """
            echo "starting Sonar Scan"
            mvn clean verify sonar:sonar \
             -Dsonar.projectKey=i27-eureka \
              -Dsonar.host.url=http://13.213.37.119:9000 \
              -Dsonar.login=sqa_708d81e52c57aeef4a9a4ed2dd5ec7ce47e2770b

          """
       }
  }
  
  }
}
