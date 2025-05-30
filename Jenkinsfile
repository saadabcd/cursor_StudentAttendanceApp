pipeline {
    agent any
    
    environment {
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        PATH = "${env.PATH}:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/tools"
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        APPCENTER_API_TOKEN = credentials('appcenter-api-token')
        APPCENTER_OWNER_NAME = 'saadabcd'  // Replace with your organization name from App Center
        APPCENTER_APP_NAME = 'abs'    // Replace with your app name from App Center
        GRADLE_OPTS = '-Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.jvmargs="-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError"'
        EMAIL_RECIPIENT = 'saadabcd123789@gmail.com' // Replace with your email address
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
                sh './gradlew clean --parallel'
            }
        }
        
        stage('Build Debug APK') {
            steps {
                sh './gradlew assembleDebug --parallel --stacktrace'
            }
            post {
                success {
                    archiveArtifacts '**/build/outputs/apk/debug/*.apk'
                }
            }
        }

        stage('Email APK') {
            steps {
                script {
                    def apkPath = sh(
                        script: 'find . -name "*.apk" -type f -path "*/build/outputs/apk/debug/*"',
                        returnStdout: true
                    ).trim()
                    
                    if (apkPath) {
                        emailext (
                            subject: "Android App Build - ${currentBuild.currentResult}",
                            body: """<p>Build Status: ${currentBuild.currentResult}</p>
                                    <p>Build Number: ${currentBuild.number}</p>
                                    <p>Check the build console output at: ${BUILD_URL}</p>""",
                            to: "${EMAIL_RECIPIENT}",
                            attachmentsPattern: "**/build/outputs/apk/debug/*.apk",
                            mimeType: 'text/html'
                        )
                        echo "APK has been sent to ${EMAIL_RECIPIENT}"
                    } else {
                        error "No debug APK found to send via email"
                    }
                }
            }
        }
        
        stage('Deploy to App Center') {
            steps {
                script {
                    // First verify the app exists
                    def appCheckStatus = sh(
                        script: """
                            curl -s -o /dev/null -w "%{http_code}" \
                            "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}" \
                            -H "X-API-Token: ${APPCENTER_API_TOKEN}"
                        """,
                        returnStdout: true
                    ).trim()

                    if (appCheckStatus != "200") {
                        error """
                            App not found in App Center! Please verify:
                            1. Organization name: ${APPCENTER_OWNER_NAME}
                            2. App name: ${APPCENTER_APP_NAME}
                            3. API token has correct permissions
                            
                            Create the app in App Center first, then update the Jenkinsfile with correct details.
                        """
                    }

                    def apkPath = sh(
                        script: 'find . -name "*.apk" -type f -path "*/build/outputs/apk/debug/*"',
                        returnStdout: true
                    ).trim()
                    
                    if (!apkPath) {
                        error "No debug APK found"
                        return
                    }

                    // Upload using the App Center CLI (more reliable than raw API calls)
                    def appCenterCommand = """
                        curl -X POST "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}/releases" \
                        -H "accept: application/json" \
                        -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                        -H "Content-Type: multipart/form-data" \
                        -F "file=@${apkPath}" \
                        -F "build_version=1.0.0" \
                        -F "build_number=1" \
                        -F "release_notes=Build from Jenkins Pipeline" \
                        -F "distribution_group_name=Collaborators"
                    """
                    
                    def response = sh(script: appCenterCommand, returnStdout: true).trim()
                    
                    echo """
                        App Center deployment completed!
                        
                        You can find your app at:
                        https://appcenter.ms/orgs/${APPCENTER_OWNER_NAME}/apps/${APPCENTER_APP_NAME}
                        
                        Response: ${response}
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