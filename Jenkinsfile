pipeline {
    agent {
        docker {
            image 'gradle:8.6-jdk17'
            label 'docker-gradle'
        }
    }

    environment {
        SONAR_TOKEN = credentials('sonar-token')
        NEXUS_CREDS = credentials('nexus-creds')
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                sh './gradlew clean build'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh "./gradlew sonarqube -PsonarToken=$SONAR_TOKEN"
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }

        stage('Publish to Nexus') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                }
            }
            steps {
                script {
                    if (env.BRANCH_NAME == 'develop') {
                        echo "Publishing SNAPSHOT from develop"
                        sh """
                            ./gradlew publishToNexus \
                                -PnexusUsername=$NEXUS_CREDS_USR \
                                -PnexusPassword=$NEXUS_CREDS_PSW
                        """
                    } else if (env.BRANCH_NAME == 'main') {
                        echo "Publishing RELEASE from main"
                        sh """
                            ./gradlew publishToNexus \
                                -PnexusUsername=$NEXUS_CREDS_USR \
                                -PnexusPassword=$NEXUS_CREDS_PSW
                        """
                    } else {
                        echo "Skipping Nexus publish for branch ${env.BRANCH_NAME}"
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
        }
    }
}
