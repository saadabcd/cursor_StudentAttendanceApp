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
        GRADLE_OPTS = '-Dorg.gradle.daemon=true -Dorg.gradle.parallel=true -Dorg.gradle.jvmargs="-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError"'
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
        
        stage('Deploy to App Center') {
            steps {
                script {
                    // Find the APK file
                    def apkPath = sh(
                        script: 'find . -name "*.apk" -type f -path "*/build/outputs/apk/debug/*"',
                        returnStdout: true
                    ).trim()
                    
                    if (apkPath) {
                        // Step 1: Create upload URL
                        def uploadResponse = sh(
                            script: """
                                curl -X POST "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}/release_uploads" \
                                -H "accept: application/json" \
                                -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                                -H "Content-Type: application/json" \
                                -d '{}'
                            """,
                            returnStdout: true
                        ).trim()
                        
                        // Extract upload_url using grep and sed
                        def uploadUrl = sh(
                            script: """echo '${uploadResponse}' | grep -o '"upload_url":"[^"]*' | sed 's/"upload_url":"//'""",
                            returnStdout: true
                        ).trim()
                        
                        def uploadId = sh(
                            script: """echo '${uploadResponse}' | grep -o '"upload_id":"[^"]*' | sed 's/"upload_id":"//'""",
                            returnStdout: true
                        ).trim()
                        
                        // Step 2: Upload the APK
                        sh """
                            curl -F "ipa=@${apkPath}" "${uploadUrl}"
                        """
                        
                        // Step 3: Commit the release
                        sh """
                            curl -X PATCH "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}/release_uploads/${uploadId}" \
                            -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                            -H "Content-Type: application/json" \
                            -d '{"status":"committed"}'
                        """
                        
                        // Step 4: Get the release ID
                        def releaseId = sh(
                            script: """
                                curl -X GET "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}/releases/latest" \
                                -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                                -H "Content-Type: application/json" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2
                            """,
                            returnStdout: true
                        ).trim()
                        
                        // Step 5: Distribute the release
                        sh """
                            curl -X PATCH "https://api.appcenter.ms/v0.1/apps/${APPCENTER_OWNER_NAME}/${APPCENTER_APP_NAME}/releases/${releaseId}" \
                            -H "X-API-Token: ${APPCENTER_API_TOKEN}" \
                            -H "Content-Type: application/json" \
                            -d '{
                                "enabled":true,
                                "destinations":[{"name":"Collaborators"}],
                                "release_notes":"New build from Jenkins Pipeline"
                            }'
                        """
                        
                        echo "Successfully deployed to App Center. Check https://appcenter.ms/users/${APPCENTER_OWNER_NAME}/apps/${APPCENTER_APP_NAME}"
                    } else {
                        error "No debug APK found"
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