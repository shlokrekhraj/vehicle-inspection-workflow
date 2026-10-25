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
        stage('Build Application') {
            steps {
                bat 'mvn -B clean package -DskipTests'
            }
        }

        stage('Selenium Integration Test') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: 'vehicle-inspection-db',
                        usernameVariable: 'DB_USERNAME',
                        passwordVariable: 'DB_PASSWORD'
                    )
                ]) {
                    script {
                        try {
                            bat '''
                                powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $app = Start-Process -FilePath 'java' -ArgumentList '-jar','target/vehicle-inspection-workflow-0.0.1-SNAPSHOT.jar' -WorkingDirectory (Get-Location).Path -RedirectStandardOutput 'app.log' -RedirectStandardError 'app-error.log' -PassThru; Set-Content -Path 'app.pid' -Value $app.Id; Write-Host ('Started application with PID ' + $app.Id)"
                            '''

                            bat '''
                                powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference = 'Stop'; $ready = $false; for ($i = 0; $i -lt 60; $i++) { try { $r = Invoke-WebRequest -UseBasicParsing 'http://localhost:8080/inspection-test.html' -TimeoutSec 3; if ($r.StatusCode -eq 200) { $ready = $true; break } } catch { Start-Sleep -Seconds 2 }; if (-not $ready) { Start-Sleep -Seconds 2 } }; if (-not $ready) { if (Test-Path 'app-error.log') { Get-Content 'app-error.log' }; throw 'Application did not become ready on port 8080 within the timeout.' }; Write-Host 'Application is ready.'"
                            '''

                            bat 'mvn -B -Dtest=InspectionSeleniumTest test'
                        } finally {
                            bat(returnStatus: true, script: '''
                                powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "if (Test-Path 'app.pid') { $appId = [int](Get-Content 'app.pid'); $p = Get-Process -Id $appId -ErrorAction SilentlyContinue; if ($p) { Stop-Process -Id $appId -Force; Write-Host 'Stopped application process.' } else { Write-Host 'Application process has already stopped.' } }"
                            ''')
                        }
                    }
                }
            }
            post {
                always {
                    junit testResults: 'target/surefire-reports/TEST-*.xml', allowEmptyResults: true
                }
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
            echo 'CI pipeline and Selenium integration test completed successfully.'
        }
        failure {
            echo 'CI pipeline failed. Check the console output and test reports.'
        }
        always {
            archiveArtifacts artifacts: 'app.log,app-error.log', allowEmptyArchive: true
        }
    }
}