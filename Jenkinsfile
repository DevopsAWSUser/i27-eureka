pipeline {
  agent { label 'maven-slave' }

  environment {
    JAVA_HOME = '/usr/lib/jvm/java-17-amazon-corretto.x86_64'
    MAVEN_HOME = '/opt/apache-maven-3.8.8'
    APPLICATION_NAME = 'eureka'
    SONAR_URL = "http://13.213.37.119:9000"
    SONAR_TOKEN = credentials('SONAR_CRED')
    POM_VERSION = readMavenPom().getVersion()
    POM_PACKAGING = readMavenPom().getPackaging()
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

    stage('SonarQube Analysis') {
      steps {
        echo "Starting SonarQube analysis with quality gate"
        withSonarQubeEnv('SonarQube') {
          sh """
            mvn clean verify sonar:sonar \\
              -Dsonar.projectKey=i27-eureka \\
              -Dsonar.host.url=${env.SONAR_URL} \\
              -Dsonar.login=${env.SONAR_TOKEN}
          """
        }

        timeout(time: 2, unit: 'MINUTES') {
          script {
            waitForQualityGate abortPipeline: true
          }
        }
      }
    }

    stage('Build Format') {
      steps {
        script {
          sh """
            echo "Existing JAR format: i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING}"
            echo "********Below is my expected output****"
            echo "Destination Source is i27-${env.APPLICATION_NAME}-${currentBuild.number}-${BRANCH_NAME}.${env.POM_PACKAGING}"
          """
        }
      }
    }
    stage ('Docker Build and Push') {
      steps {
        script {
          sh "ls -la"
        }
      }
    }
  }
}
