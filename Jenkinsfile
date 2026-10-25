// Vehicle Inspection Workflow - Commit-to-Container pipeline (Steps 8, 10 and 12)
//
// Checkout -> Build & Unit Test -> Package -> Docker Build -> Infrastructure
//   -> Selenium Test (against the new container) -> Publish Image -> Deploy Container -> Smoke Test
//
// Any failing test stops the pipeline before Publish/Deploy, so only tested images ship.

pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven-3.10.0'
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'staging'],
               description: 'Target environment - selects container name, database and state file')
        string(name: 'APP_PORT', defaultValue: '8085',
               description: 'Host port mapped to the deployed container (direct access)')
        string(name: 'NGINX_PORT', defaultValue: '8088',
               description: 'Public port of the Nginx reverse proxy')
        string(name: 'REGISTRY', defaultValue: 'localhost:5000',
               description: 'Registry to publish to (local registry by default)')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    environment {
        // The Jenkins service runs as SYSTEM: point it at Docker Desktop (installed per-user)
        // and at the user's Docker config so it uses the same engine and CLI plugins (compose).
        PATH = "C:\\Users\\ASUS\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        DOCKER_CONFIG = 'C:\\Users\\ASUS\\.docker'
        JAR_NAME    = 'vehicle-inspection-workflow-0.0.1-SNAPSHOT.jar'
        IMAGE_NAME  = 'vehicle-inspection-workflow'
        APP_VERSION = "1.2.${BUILD_NUMBER}"
        TEST_PORT   = '8091'
        DEPLOY_ROOT = 'C:\\vehicle-inspection-deploy'
        PS          = 'powershell -NoProfile -ExecutionPolicy Bypass -File'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat 'java -version && mvn -v && docker version'
                script {
                    env.GIT_SHORT = bat(script: '@git rev-parse --short HEAD', returnStdout: true).trim()
                }
                echo "Building ${APP_VERSION} from commit ${env.GIT_SHORT}"
            }
        }

        stage('Build & Unit Test') {
            steps {
                bat 'mvn -B clean test'
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/TEST-*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Package') {
            steps {
                bat 'mvn -B package -DskipTests'
                archiveArtifacts artifacts: "target/${JAR_NAME}", fingerprint: true
            }
        }

        stage('Docker Build') {
            steps {
                bat """
                    docker build -t ${IMAGE_NAME}:${APP_VERSION} ^
                        --build-arg APP_VERSION=${APP_VERSION} --build-arg GIT_COMMIT=${env.GIT_SHORT} .
                """
                bat "docker image inspect ${IMAGE_NAME}:${APP_VERSION} --format \"Image {{.Id}} size={{.Size}} labels={{json .Config.Labels}}\""
            }
        }

        stage('Infrastructure') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'vehicle-inspection-db',
                        usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')]) {
                    bat "%PS% deploy\\windows\\ensure-infra.ps1"
                }
            }
        }

        stage('Selenium Test') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'vehicle-inspection-db',
                        usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')]) {
                    script {
                        try {
                            bat "%PS% deploy\\windows\\run-container.ps1 -Image ${IMAGE_NAME}:${APP_VERSION} -Name vi-app-test -Environment ci -DbName vehicle_inspection_ci -HostPort ${TEST_PORT}"
                            bat "mvn -B -Pselenium test -Dapp.baseUrl=http://localhost:${TEST_PORT}"
                        } finally {
                            bat(returnStatus: true, script: '(if not exist test-run mkdir test-run) & docker logs vi-app-test > test-run\\app-test-container.log 2>&1 & docker rm -f vi-app-test')
                        }
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/selenium-reports/TEST-*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'test-run/*.log, target/selenium-screenshots/**', allowEmptyArchive: true
                }
            }
        }

        stage('Publish Image') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps {
                bat """
                    docker tag ${IMAGE_NAME}:${APP_VERSION} ${params.REGISTRY}/${IMAGE_NAME}:${APP_VERSION}
                    docker tag ${IMAGE_NAME}:${APP_VERSION} ${params.REGISTRY}/${IMAGE_NAME}:latest
                    docker push ${params.REGISTRY}/${IMAGE_NAME}:${APP_VERSION} || exit /b 1
                    docker push ${params.REGISTRY}/${IMAGE_NAME}:latest || exit /b 1
                """
                // Registry evidence: list the tags the registry now holds
                bat "curl -s http://${params.REGISTRY}/v2/${IMAGE_NAME}/tags/list > registry-tags.json & type registry-tags.json"
                archiveArtifacts artifacts: 'registry-tags.json', allowEmptyArchive: true
            }
        }

        stage('Deploy Container') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps {
                withCredentials([usernamePassword(credentialsId: 'vehicle-inspection-db',
                        usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')]) {
                    // Retire the Step 8 jar deployment if it is still running
                    bat(returnStatus: true, script: "%PS% deploy\\windows\\stop-app.ps1 -WorkDir \"${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\"")
                    bat "%PS% deploy\\windows\\deploy-container.ps1 -Image ${params.REGISTRY}/${IMAGE_NAME}:${APP_VERSION} -Environment ${params.DEPLOY_ENV} -HostPort ${params.APP_PORT} -StateDir ${DEPLOY_ROOT}"
                }
                bat "%PS% deploy\\windows\\deploy-nginx.ps1 -Upstream vi-app-${params.DEPLOY_ENV}:8080 -Network vi-net -ConfigDir \"${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\\nginx\" -Port ${params.NGINX_PORT} -Name vi-nginx-${params.DEPLOY_ENV}"
            }
        }

        stage('Smoke Test') {
            when { expression { currentBuild.currentResult == 'SUCCESS' } }
            steps {
                bat "%PS% deploy\\windows\\wait-for-health.ps1 -Url http://localhost:${params.NGINX_PORT}/actuator/health -TimeoutSeconds 90"
                bat "curl -s http://localhost:${params.NGINX_PORT}/actuator/info"
                bat "docker ps --filter name=vi- --format \"table {{.Names}}\\t{{.Image}}\\t{{.Status}}\\t{{.Ports}}\""
            }
        }
    }

    post {
        success {
            echo "Deployed ${params.REGISTRY}/${IMAGE_NAME}:${APP_VERSION} to ${params.DEPLOY_ENV} - http://localhost:${params.NGINX_PORT}/"
        }
        failure {
            echo 'Pipeline failed - nothing was published or deployed. Check stage logs and test reports.'
        }
    }
}
