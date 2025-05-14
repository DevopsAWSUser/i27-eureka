pipeline {
    agent { label 'maven-slave' }

    parameters {
        choice(name: 'sonarScans',
               choices: 'no\nyes',
               description: 'This will scan the applicaiton using sonar')
        choice(name: 'buildOnly',
               choices: 'no\nyes',
               description: 'This will only build the application')
        choice(name: 'dockerPush',
               choices: 'no\nyes',
               description: 'This will trigger the build, docker build and docker push')
        choice(name: 'deployToDev',
               choices: 'no\nyes',
               description: 'This will Deploy my app to Dev env')
        choice(name: 'deployToTest',
               choices: 'no\nyes',
               description: 'This will Deploy my app to Test env')
        choice(name: 'deployToStage',
               choices: 'no\nyes',
               description: 'This will Deploy my app to Stage env')
        choice(name: 'deployToProd',
               choices: 'no\nyes',
               description: 'This will Deploy my app to Prod env')
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
            when {
                anyOf {
                    expression { params.dockerPush == 'yes' }
                }
            }
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

                        # Docker hub, JFROG
                        echo "**************** Logging to Docker Registry *****************"
                        sudo docker login -u ${DOCKER_CREDS_USR} -p ${DOCKER_CREDS_PSW}
                        sudo docker push ${env.DOCKER_HUB}/${env.DOCKER_REPO}:${GIT_COMMIT}
                    """
                }
            }
        }

        stage('Deploy to Dev') { //5761
            when {
                anyOf {
                    expression { params.deployToDev == 'yes' }
                }
            }
            steps {
                script {
                    dockerDeploy('dev', '5761', '8761').call()
                }
            }
        }

        stage('Deploy to Test') { //6761
            when {
                anyOf {
                    expression { params.deployToTest == 'yes' }
                }
            }
            steps {
                script {
                    dockerDeploy('test', '6761', '8761').call()
                }
            }
        }
    }
}

def dockerDeploy(envDeploy, hostPort, contPort) {
    return {
        echo "******************** Deploying to $envDeploy Environment ********************"
        withCredentials([usernamePassword(credentialsId: 'maha_docker_dev_server_cred', passwordVariable: 'PASSWORD', usernameVariable: 'USERNAME')]) {
            // some block
            // with this creddentials, i need to connect to dev environment 
            // sshpass
            script {
                // Test to Pull the container on the docker server
                sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker pull ${env.DOCKER_HUB}/${env.DOCKER_REPO}:$GIT_COMMIT\""
                //sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"***"
                echo "Stop the Container"
                // If we execute the below command it will fail for the first time,, as continers are not availble, stop/remove will cause a issue.
                // we can implement try catch block.
                try {
                    sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker stop ${env.APPLICATION_NAME}-$envDeploy\""
                    echo "Removing the Container"
                    sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker rm ${env.APPLICATION_NAME}-$envDeploy\""
                } catch (err) {
                    echo "Caught the error: $err"
                }
                // Run the container
                sh "sshpass -p '$PASSWORD' -v ssh -o StrictHostKeyChecking=no $USERNAME@$docker_dev_server_ip \"docker run --restart always --name ${env.APPLICATION_NAME}-$envDeploy -p $hostPort:$contPort -d ${env.DOCKER_HUB}/${env.DOCKER_REPO}:$GIT_COMMIT\""
            }
        }
    }
}

// 8761 is the container port , we cant change it.
// if we really want to change , we can change it using -Dserver.port=9090, this will be your container port
// but we are considering the below host ports 
// dev === > 5761
// test ===> 6761
// stage ===> 7761
// prod ====> 8761
