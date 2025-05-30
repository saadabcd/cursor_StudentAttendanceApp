pipeline {
    agent any
    
    environment {
        // Android SDK and tools paths - adjust these according to your Jenkins server setup
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        PATH = "${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/tools:${PATH}"
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
                sh 'chmod +x ./gradlew'
                sh "echo 'sdk.dir=${ANDROID_HOME}' > local.properties"
                
                // Debug commands
                sh '''
                    echo "=== ENVIRONMENT CHECK ==="
                    echo "ANDROID_HOME: $ANDROID_HOME"
                    echo "PATH: $PATH"
                    echo "Java version:"
                    java -version
                    echo "Android SDK tools:"
                    ls -la $ANDROID_HOME/cmdline-tools/latest/bin
                    echo "Gradle version:"
                    ./gradlew --version
                '''
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
                sh './gradlew test --stacktrace'
            }
            post {
                always {
                    // Archive the test results
                    junit '**/build/test-results/**/*.xml'
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