pipeline {
    agent { label 'maven-slave' }

    parameters {
        choice(name: 'sonarScans',
               choices: 'no\nyes',
               description: 'This will scan the application using Sonar')
        choice(name: 'buildOnly',
               choices: 'no\nyes',
               description: 'This will only build the application')
        choice(name: 'dockerPush',
               choices: 'no\nyes',
               description: 'This will trigger the build, Docker build, and Docker push')
        choice(name: 'deployToDev',
               choices: 'no\nyes',
               description: 'This will deploy the app to the Dev environment')
        choice(name: 'deployToTest',
               choices: 'no\nyes',
               description: 'This will deploy the app to the Test environment')
        choice(name: 'deployToStage',
               choices: 'no\nyes',
               description: 'This will deploy the app to the Stage environment')
        choice(name: 'deployToProd',
               choices: 'no\nyes',
               description: 'This will deploy the app to the Prod environment')
    }

    environment {
        JAVA_HOME        = '/usr/lib/jvm/java-17-amazon-corretto.x86_64'
        MAVEN_HOME       = '/opt/apache-maven-3.8.8'
        APPLICATION_NAME = 'eureka'
        SONAR_URL        = 'http://18.141.190.245:9000'
        SONAR_TOKEN      = credentials('SONAR_CRED')
        POM_VERSION      = readMavenPom().getVersion()
        POM_PACKAGING    = readMavenPom().getPackaging()
        DOCKER_HUB       = 'docker.io/vanithascloud'
        DOCKER_REPO      = 'i27eurekaproject'
        USER_NAME        = 'vanithascloud'
        DOCKER_CREDS     = credentials('dockerhub_cred')
    }

    stages {
        stage('Build') {
            when {
                anyOf {
                    expression { params.dockerPush == 'yes' }
                    expression { params.buildOnly == 'yes' }
                }
            }
            // Build happens here 
            // Only build should happen, no tests should be available
            steps {
                script {
                    buildApp().call()
                }
            }
        }

        stage('Unit Tests') {
            when {
                anyOf {
                    expression { params.buildOnly == 'yes' }
                    expression { params.dockerPush == 'yes' }
                }
            }
            steps {
                echo "Performing Unit Tests for ${env.APPLICATION_NAME} application"
                sh '''
                    export PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$PATH
                    mvn test
                '''
            }
        }

        stage('SonarQube Analysis') {
            when {
                anyOf {
                    expression { params.sonarScans == 'yes' }
                    expression { params.buildOnly == 'yes' }
                    expression { params.dockerPush == 'yes' }
                }
            }
            steps {
                echo "Starting SonarQube analysis with quality gate"
                withSonarQubeEnv('SonarQube') {
                    sh """
                        mvn clean verify sonar:sonar \\
                          -Dsonar.projectKey=i27-${env.APPLICATION_NAME}  \\
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
                        echo "********Below is my expected output********"
                        echo "Destination Source is i27-${env.APPLICATION_NAME}-${currentBuild.number}-${BRANCH_NAME}.${env.POM_PACKAGING}"
                    """
                }
            }
        }

        stage('Docker Build and Push') {
            when {
                expression { params.dockerPush == 'yes' }
            }
            steps {
                script {
                    dockerBuildandPush().call()
                }
            }
        }

        stage('Deploy to Dev') { //5761
            when {
                expression { params.deployToDev == 'yes' }
            }
            steps {
                script {
                    imageValidation().call()
                    dockerDeploy('dev', '5761', '8761').call()
                }
            }
        }

        stage('Deploy to Test') { //6761
            when {
                expression { params.deployToTest == 'yes' }
            }
            steps {
                script {
                    imageValidation().call()
                    dockerDeploy('test', '6761', '8761').call()
                }
            }
        }

        stage('Deploy to Stage') { //7761
            when {
                expression { params.deployToStage == 'yes' }
            }
            steps {
                script {
                    imageValidation().call()
                    dockerDeploy('stage', '7761', '8761').call()
                }
            }
        }

        stage ('Deploy to Prod') { //6761
            when {
                allOf {
                    anyOf {
                        expression {
                            params.deployToProd == 'yes'
                        }
                    }
                    anyOf {
                        branch 'release/*'
                    }
                }

            }
            steps {
              script {
                imageValidation().call()
                dockerDeploy('prod', '8761', '8761').call()
              }
            }
        }
        stage ('Clean') {
            steps {
                cleanWs()
            }
        }

    }
}

// Deploys the Docker container
def dockerDeploy(envDeploy, hostPort, contPort) {
    return {
        echo "********** Deploying to $envDeploy Environment **********"
        withCredentials([usernamePassword(credentialsId: 'maha_docker_dev_server_cred', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
            script {
                sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker pull ${env.DOCKER_HUB}/${env.DOCKER_REPO}:$GIT_COMMIT\""

                echo "Stop the container"
                try {
                    sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker stop ${env.APPLICATION_NAME}-$envDeploy\""
                    echo "Removing the container"
                    sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker rm ${env.APPLICATION_NAME}-$envDeploy\""
                } catch (err) {
                    echo "Caught the error: $err"
                }

                sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker run --restart always --name ${env.APPLICATION_NAME}-$envDeploy -p $hostPort:$contPort -d ${env.DOCKER_HUB}/${env.DOCKER_REPO}:$GIT_COMMIT\""
            }
        }
    }
}

// Maven build
def buildApp() {
    return {
        echo "Building the ${env.APPLICATION_NAME} application"
        sh "mvn clean package -DskipTests=true"
        archiveArtifacts artifacts: 'target/*jar', followSymlinks: false
    }
}

// Pulls Docker image, builds & pushes if not found
def imageValidation() {
    return {
        println("Pulling the Docker image")
        try {
            sh "docker pull ${env.DOCKER_HUB}/${env.DOCKER_REPO}:$GIT_COMMIT"
            println("Pull Success,!!! Deploying !!!!!")
        } catch (Exception e) {
            println("OOPS, Docker image with this tag is not available")
            println("So, Building the app, creating the image and pushing to registry")
            buildApp().call()
            dockerBuildandPush().call()
        }
    }
}

// Builds and pushes Docker image
def dockerBuildandPush() {
    return {
        sh """
            ls -la
            cp ${workspace}/target/i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} ./.cicd
            echo "Listing files in .cicd folder"
            ls -la ./.cicd
            echo "**********Building Docker Image**********"
            sudo docker build \\
              --pull \\
              --no-cache \\
              --force-rm \\
              --rm=true \\
              --build-arg JAR_SOURCE=i27-${env.APPLICATION_NAME}-${env.POM_VERSION}.${env.POM_PACKAGING} \\
              --build-arg JAR_DEST=i27-${env.APPLICATION_NAME}-${currentBuild.number}-${BRANCH_NAME}.${env.POM_PACKAGING} \\
              -t ${env.DOCKER_HUB}/${env.DOCKER_REPO}:${GIT_COMMIT} \\
              ./.cicd

            echo "**********Logging in to Docker Registry**********"
            sudo docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}
            sudo docker push ${env.DOCKER_HUB}/${env.DOCKER_REPO}:${GIT_COMMIT}
        """
    }
}

// 8761 is the container port; we can't change it.
// If needed, it can be changed using -Dserver.port=9090, which becomes the container port.
// But we are considering the following host ports:
// dev   => 5761
// test  => 6761
// stage => 7761
// prod  => 8761
