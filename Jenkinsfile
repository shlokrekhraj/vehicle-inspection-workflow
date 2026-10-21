pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven-3.10.0'
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Source code checked out from GitHub.'
            }
        }

        stage('Build and Test') {
            steps {
                bat 'mvn -B clean package'
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts(
                    artifacts: 'target/vehicle-inspection-workflow-0.0.1-SNAPSHOT.jar',
                    fingerprint: true
                )
            }
        }
    }

    post {
        success {
            echo 'CI Pipeline completed successfully.'
        }
        failure {
            echo 'CI Pipeline failed. Check the console output.'
        }
    }
}
