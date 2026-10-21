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
                echo 'Pipeline source checked out by Jenkins.'
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
            echo 'CI pipeline completed successfully.'
        }
        failure {
            echo 'CI pipeline failed. Check the console output.'
        }
    }
}
