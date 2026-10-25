// Vehicle Inspection Workflow - Pipeline as Code (Step 8)
// Checkout -> Build & Unit Test -> Package -> Selenium Test -> Deploy (jar behind Nginx)

pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven-3.10.0'
    }

    parameters {
        choice(name: 'DEPLOY_ENV', choices: ['dev', 'staging'],
               description: 'Target environment - selects the database and the deploy folder')
        string(name: 'APP_PORT', defaultValue: '9090',
               description: 'Port the deployed application listens on (behind Nginx)')
        string(name: 'NGINX_PORT', defaultValue: '8088',
               description: 'Public port of the Nginx reverse proxy')
    }

    options {
        timestamps()
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '30'))
    }

    triggers {
        // Poll GitHub every 5 minutes; a webhook can replace this later.
        pollSCM('H/5 * * * *')
    }

    environment {
        // The Jenkins service runs as SYSTEM: point it at Docker Desktop (installed per-user)
        // and at the user's Docker config so it uses the same engine and CLI plugins (compose).
        PATH = "C:\\Users\\ASUS\\AppData\\Local\\Programs\\DockerDesktop\\resources\\bin;C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"
        DOCKER_CONFIG = 'C:\\Users\\ASUS\\.docker'
        JAR_NAME    = 'vehicle-inspection-workflow-0.0.1-SNAPSHOT.jar'
        APP_VERSION = "1.1.${BUILD_NUMBER}"
        TEST_PORT   = '8080'
        DEPLOY_ROOT = 'C:\\vehicle-inspection-deploy'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
                bat 'java -version && mvn -v && docker version'
                bat 'git log -1 --pretty="Building commit %%h - %%s (%%an)"'
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

        stage('Selenium Test') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'vehicle-inspection-db',
                        usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')]) {
                    script {
                        try {
                            bat """
                                powershell -NoProfile -ExecutionPolicy Bypass -File deploy\\windows\\start-app.ps1 ^
                                    -Jar "%WORKSPACE%\\target\\${JAR_NAME}" -WorkDir "%WORKSPACE%\\test-run" ^
                                    -Port ${TEST_PORT} -Environment ci -Version ${APP_VERSION} -DbName vehicle_inspection_db
                            """
                            bat """
                                powershell -NoProfile -ExecutionPolicy Bypass -File deploy\\windows\\wait-for-health.ps1 ^
                                    -Url http://localhost:${TEST_PORT}/actuator/health -ErrorLog "%WORKSPACE%\\test-run\\app-error.log"
                            """
                            bat "mvn -B -Pselenium test -Dapp.baseUrl=http://localhost:${TEST_PORT}"
                        } finally {
                            bat 'powershell -NoProfile -ExecutionPolicy Bypass -File deploy\\windows\\stop-app.ps1 -WorkDir "%WORKSPACE%\\test-run"'
                        }
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/TEST-*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'test-run/*.log', allowEmptyArchive: true
                }
            }
        }

        stage('Deploy') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'vehicle-inspection-db',
                        usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')]) {
                    // dontKillMe: keep the deployed app alive after the build finishes
                    withEnv(['JENKINS_NODE_COOKIE=dontKillMe', 'BUILD_ID=dontKillMe']) {
                        bat """
                            if not exist "${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\\releases" mkdir "${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\\releases"
                            copy /Y "target\\${JAR_NAME}" "${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\\releases\\vehicle-inspection-${APP_VERSION}.jar" || exit /b 1
                            powershell -NoProfile -ExecutionPolicy Bypass -File deploy\\windows\\start-app.ps1 ^
                                -Jar "${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\\releases\\vehicle-inspection-${APP_VERSION}.jar" ^
                                -WorkDir "${DEPLOY_ROOT}\\${params.DEPLOY_ENV}" -Port ${params.APP_PORT} ^
                                -Environment ${params.DEPLOY_ENV} -Version ${APP_VERSION} -DbName vehicle_inspection_db || exit /b 1
                        """
                    }
                }
                bat """
                    powershell -NoProfile -ExecutionPolicy Bypass -File deploy\\windows\\wait-for-health.ps1 ^
                        -Url http://localhost:${params.APP_PORT}/actuator/health -ErrorLog "${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\\app-error.log" || exit /b 1
                    powershell -NoProfile -ExecutionPolicy Bypass -File deploy\\windows\\deploy-nginx.ps1 ^
                        -Upstream host.docker.internal:${params.APP_PORT} -ConfigDir "${DEPLOY_ROOT}\\${params.DEPLOY_ENV}\\nginx" ^
                        -Port ${params.NGINX_PORT} -Name vi-nginx-${params.DEPLOY_ENV} || exit /b 1
                    powershell -NoProfile -ExecutionPolicy Bypass -File deploy\\windows\\wait-for-health.ps1 ^
                        -Url http://localhost:${params.NGINX_PORT}/actuator/health -TimeoutSeconds 60
                """
                echo "Deployed ${APP_VERSION} to ${params.DEPLOY_ENV}: http://localhost:${params.NGINX_PORT}/"
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded - application available at http://localhost:${params.NGINX_PORT}/"
        }
        failure {
            echo 'Pipeline failed - deployment was skipped. Check the stage logs and test reports.'
        }
    }
}
