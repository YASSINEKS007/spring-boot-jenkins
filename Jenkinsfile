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

    stage('Build') {
      steps {
        sh './gradlew clean build'
      }
    }

    stage('Test') {
      steps {
        sh './gradlew test'
      }
    }

    stage('SonarQube Analysis') {
      steps {
        withSonarQubeEnv('sonarqube') {
          sh """
            ./gradlew sonarqube \
              -Dsonar.login=$SONAR_TOKEN
          """
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
      steps {
        sh """
          ./gradlew publish \
            -PnexusUser=$NEXUS_CREDS_USR \
            -PnexusPassword=$NEXUS_CREDS_PSW
        """
      }
    }
  }

  post {
    always {
      archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
    }
  }
}
