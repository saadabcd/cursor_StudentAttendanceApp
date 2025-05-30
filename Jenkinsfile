
pipeline {
    agent any
    
    environment {
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        PATH = "${env.PATH}:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/tools"
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        
        // App Center config (set these in Jenkins credentials)
        APPCENTER_API_TOKEN = credentials('appcenter-api-token')
        APPCENTER_OWNER_NAME = 'app-gestion-abscence'  // Replace with your org/username
        APPCENTER_APP_NAME = 'gestion_abs'            // Replace with your app name
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                sh 'ls -la'  // Verify checkout
            }
        }
        
        stage('Setup Environment') {
            steps {
                sh '''
                    chmod +x ./gradlew
                    echo "sdk.dir=$ANDROID_HOME" > local.properties
                    
                    # Install required SDK components upfront
                    yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager \
                        "platforms;android-34" \
                        "build-tools;35.0.0"
                '''
            }
        }
        
        stage('Inject Firebase Config') {
            steps {
                script {
                    // Create debug flavor directory
                    sh 'mkdir -p app/src/debug'
                    
                    // Add google-services.json from Jenkins credentials
                    withCredentials([file(credentialsId: 'google-services-json', variable: 'GOOGLE_SERVICES_JSON')]) {
                        sh 'cp $GOOGLE_SERVICES_JSON app/src/debug/google-services.json'
                    }
                }
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
                    def apkFile = findFiles(glob: '**/build/outputs/apk/debug/*-debug.apk')[0].path
                    
                    // Upload to App Center
                    withCredentials([string(credentialsId: 'appcenter-api-token', variable: 'APPCENTER_API_TOKEN']) {
                        sh """
                            curl -X POST \
                            "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}/release_uploads" \
                            -H "accept: application/json" \
                            -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                            -H "Content-Type: application/json" \
                            -d '{}'
                            
                            # Complete the upload (add proper parsing of upload_id and upload_url from response)
                            curl -X POST \
                            "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}/release_uploads/<upload_id>" \
                            -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                            -H "Content-Type: application/json" \
                            -d '{"status": "committed"}'
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