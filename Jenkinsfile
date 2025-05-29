pipeline {
    agent any
    
    environment {
        // Android SDK and tools paths - adjust these according to your Jenkins server setup
        ANDROID_HOME = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${PATH}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                // Get code from GitHub repository
                checkout scm
            }
        }
        
        stage('Clean') {
            steps {
                // Clean the project
                sh './gradlew clean'
            }
        }
        
        stage('Code Analysis') {
            steps {
                parallel(
                    "Lint": {
                        // Run Android Lint
                        sh './gradlew lint'
                    },
                    "Static Code Analysis": {
                        // Run static code analysis if configured (e.g., SonarQube)
                        sh './gradlew sonarqube' // Uncomment if SonarQube is configured
                    }
                )
            }
            post {
                always {
                    // Archive the lint results
                    archiveArtifacts artifacts: '**/build/reports/lint-results-debug.html', allowEmptyArchive: true
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                // Run unit tests
                sh './gradlew test'
            }
            post {
                always {
                    // Archive the test results
                    junit '**/build/test-results/**/*.xml'
                }
            }
        }
        
        stage('Instrumentation Tests') {
            steps {
                // Run Android instrumentation tests (requires connected device or emulator)
                sh './gradlew connectedAndroidTest'
            }
            post {
                always {
                    // Archive the Android test results
                    junit '**/build/outputs/androidTest-results/**/*.xml'
                }
            }
        }
        
        stage('Build') {
            steps {
                // Build debug and release variants
                sh './gradlew assembleDebug'
                sh './gradlew assembleRelease'
            }
            post {
                success {
                    // Archive the APKs
                    archiveArtifacts artifacts: '**/build/outputs/apk/**/*.apk', fingerprint: true
                }
            }
        }
        
        stage('Deploy to Firebase App Distribution') {
            when {
                branch 'main' // Only deploy from main branch
            }
            steps {
                script {
                    // Deploy to Firebase App Distribution
                    // You'll need to configure Firebase CLI and add the firebase plugin to your project
                    sh './gradlew appDistributionUploadRelease'
                }
            }
        }
    }
    
    post {
        always {
            // Clean up workspace
            cleanWs()
        }
        success {
            // Notify on success (customize as needed)
            echo 'Build and tests passed!'
        }
        failure {
            // Notify on failure (customize as needed)
            echo 'Build or tests failed!'
        }
    }
} 