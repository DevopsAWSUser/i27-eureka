pipeline {
  agent { label 'maven-slave' }

  environment {
    JAVA_HOME        = '/usr/lib/jvm/java-17-amazon-corretto.x86_64'
    MAVEN_HOME       = '/opt/apache-maven-3.8.8'
    APPLICATION_NAME = 'eureka'
    SONAR_URL        = 'http://13.229.228.67:9000'
    SONAR_TOKEN      = credentials('SONAR_CRED')
    POM_VERSION      = readMavenPom().getVersion()
    POM_PACKAGING    = readMavenPom().getPackaging()
    DOCKER_HUB       = 'docker.io/DevopsAwsUser'
    DOCKER_REPO      = 'i27eurekaproject'
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

    stage('Docker Build and Push') {
      steps {
        script {
          sh """
            ls -la
            cp ${workspace}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd
            echo "listing files in .cicd folder"
            ls -la ./.cicd
            echo "**********Building Docker Image *******************"
            sudo docker build \\
              --pull \\
              --no-cache \\
              --force-rm \\
              --rm=true \\
              --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} \\
              --build-arg JAR_DEST=i27-${env.APPLICATION_NAME}-${currentBuild.number}-${BRANCH_NAME}.${env.POM_PACKAGING} \\
              -t ${env.DOCKER_HUB}/${env.DOCKER_REPO}:${GIT_COMMIT} \\
              ./.cicd
          """
        }
      }
    }
  }
}
