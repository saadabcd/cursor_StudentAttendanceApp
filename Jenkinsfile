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
                // Basic setup
                sh 'chmod +x ./gradlew'
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
                // Run only lint, remove sonarqube
                sh './gradlew lint --stacktrace'
            }
            post {
                always {
                    archiveArtifacts artifacts: '**/build/reports/lint-results-*.html', allowEmptyArchive: true
                }
            }
        }
        
        stage('Build Debug') {
            steps {
                // Build debug variant
                sh './gradlew assembleDebug --stacktrace'
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
            echo 'Build successful!'
        }
        failure {
            // Notify on failure (customize as needed)
            echo 'Build failed!'
        }
    }
} 