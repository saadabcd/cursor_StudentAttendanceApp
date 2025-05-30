pipeline {
    agent any
    
    environment {
        // Android SDK paths (verified from your system)
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        
        // Clean PATH without duplication
        PATH = "${env.PATH}:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/tools"
        
        // Force Java 11 (since you have both Java 11 and 17)
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                
                // Debug: Verify files were checked out
                sh 'ls -la'
            }
        }
        
        stage('Setup Environment') {
            steps {
                // Ensure gradlew is executable (with retry logic)
                sh '''
                    if [ ! -f ./gradlew ]; then
                        echo "ERROR: gradlew not found! Check repository structure."
                        exit 1
                    fi
                    chmod +x ./gradlew || { echo "Failed to make gradlew executable"; exit 1; }
                    echo "sdk.dir=$ANDROID_HOME" > local.properties
                '''
                
                // Debug environment
                sh '''
                    echo "=== ENVIRONMENT ==="
                    echo "ANDROID_HOME: $ANDROID_HOME"
                    echo "JAVA_HOME: $JAVA_HOME"
                    echo "PATH: $PATH"
                    java -version
                    ./gradlew --version
                    ls -la $ANDROID_HOME/cmdline-tools/latest/bin
                '''
            }
        }
        
        stage('Clean Project') {
            steps {
                sh './gradlew clean'
            }
        }
        
        stage('Run Lint') {
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
                sh './gradlew test --stacktrace --no-daemon'
            }
            post {
                always {
                    junit '**/build/test-results/**/*.xml'
                }
            }
        }
        
        stage('Build Debug APK') {
            steps {
                sh './gradlew assembleDebug --stacktrace --no-daemon'
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/build/outputs/apk/debug/*.apk', fingerprint: true
                }
            }
        }
        
        stage('Deploy to Firebase') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Ensure Firebase CLI is installed
                    sh '''
                        if ! command -v firebase &> /dev/null; then
                            curl -sL https://firebase.tools | bash
                        fi
                        ./gradlew appDistributionUploadRelease
                    '''
                }
            }
        }
    }
    
    post {
        always {
            // Clean workspace but keep important files
            cleanWs(
                cleanWhenAborted: true,
                cleanWhenFailure: true,
                cleanWhenNotBuilt: true,
                cleanWhenUnstable: true,
                deleteDirs: false,
                patterns: [[pattern: '.gradle/**', type: 'INCLUDE']]
            )
            
            // Final status
            script {
                if (currentBuild.result == 'FAILURE') {
                    echo "Pipeline failed! Check logs above."
                    // Add Slack/email notification here if configured
                } else {
                    echo "Pipeline succeeded!"
                }
            }
        }
    }
}