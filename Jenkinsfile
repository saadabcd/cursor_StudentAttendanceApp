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
            steps {
                script {
                    def apkFile = findFiles(glob: '**/build/outputs/apk/debug/*.apk')[0]
                    sh """
                        curl -X POST "https://api.appcenter.ms/v0.1/apps/badr.saad/StudentAttendanceApp/release_uploads" \
                        -H "Content-Type: application/json" \
                        -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                        -d '{}'
                    """
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