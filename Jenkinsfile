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
        
        stage('Setup') {
            steps {
                // Make gradlew executable
                sh 'chmod +x ./gradlew'
                
                // Fix Android SDK permissions
                sh 'sudo chown -R jenkins:jenkins $ANDROID_HOME'
                sh 'sudo chmod -R 755 $ANDROID_HOME'
                
                // Accept Android SDK licenses
                sh 'yes | sudo -u jenkins $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses'
                
                // Print debug information
                sh 'echo $ANDROID_HOME'
                sh 'echo $PATH'
                sh 'which java'
                sh './gradlew --version'
            }
        }
        
        stage('Clean') {
            steps {
                // Clean the project
                sh './gradlew clean'
            }
        }
        
        stage('Lint') {
            steps {
                sh './gradlew lintDebug'
            }
            post {
                always {
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
        
        stage('Build Debug APK') {
            steps {
                // Build debug variant
                sh './gradlew assembleDebug'
            }
            post {
                success {
                    // Archive the APK
                    archiveArtifacts artifacts: '**/build/outputs/apk/debug/*.apk', fingerprint: true
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