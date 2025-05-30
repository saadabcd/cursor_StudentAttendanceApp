
pipeline {
    agent any
    
    environment {
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        PATH = "${env.PATH}:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/tools"
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        APPCENTER_API_TOKEN = credentials('appcenter-api-token')
        APPCENTER_OWNER_NAME = 'app-gestion-abscence'  // Replace with your org/username
        APPCENTER_APP_NAME = 'gestion_abs'            // Replace with your app name
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'ls -la'
            }
        }
        
        stage('Setup Environment') {
            steps {
                sh '''
                    chmod +x ./gradlew
                    echo "sdk.dir=$ANDROID_HOME" > local.properties
                    
                    # Optional: Pre-install Android components
                    yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \
                        "platforms;android-34" \
                        "build-tools;35.0.0" || true
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
                    archiveArtifacts '**/build/reports/lint-results-debug.html'
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
                    archiveArtifacts '**/build/outputs/apk/debug/*.apk'
                }
            }
        }
        
        stage('Deploy to App Center') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Find the APK file
                    def apkFiles = findFiles(glob: '**/build/outputs/apk/debug/*-debug.apk')
                    if (apkFiles.isEmpty()) {
                        error "No APK files found in build/outputs/apk/debug/"
                    }
                    def apkFile = apkFiles[0].path
                    
                    // Upload using App Center CLI
                    withCredentials([string(credentialsId: 'appcenter-api-token', variable: 'APPCENTER_TOKEN')]) {
                        sh """
                            # Install App Center CLI if needed
                            if ! command -v appcenter >/dev/null 2>&1; then
                                npm install -g appcenter-cli
                            fi
                            
                            # Authenticate and upload
                            appcenter login --token \$APPCENTER_TOKEN
                            appcenter distribute release \
                                --app \$APPCENTER_OWNER_NAME/\$APPCENTER_APP_NAME \
                                --file \$apkFile \
                                --group "Collaborators" \
                                --release-notes "Jenkins build ${env.BUILD_NUMBER}"
                            
                            echo "Successfully deployed to App Center"
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
            script {
                if (currentBuild.result == 'FAILURE') {
                    echo "Pipeline failed! Check logs above."
                } else {
                    echo "Pipeline succeeded!"
                }
            }
        }
    }
}