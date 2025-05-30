 pipeline {
    agent any
    
    environment {
        ANDROID_HOME = '/opt/android-sdk'
        ANDROID_SDK_ROOT = '/opt/android-sdk'
        PATH = "${env.PATH}:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools:/opt/android-sdk/tools"
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        
        // App Center config
        APPCENTER_OWNER_NAME = 'app-gestion-abscence'  // Replace with your org/username
        APPCENTER_APP_NAME = 'gestion_abs'            // Replace with your app name
    }
    
    stages {
        // [Keep all your existing stages until Build Debug APK...]
        
        stage('Deploy to App Center') {
            when {
                branch 'main'
            }
            steps {
                script {
                    // Find the APK file
                    def apkFiles = findFiles(glob: '**/build/outputs/apk/debug/*-debug.apk')
                    if (apkFiles.length == 0) {
                        error "No APK files found"
                    }
                    def apkFile = apkFiles[0].path
                    
                    // Upload using App Center CLI (recommended approach)
                    withCredentials([string(credentialsId: 'appcenter-api-token', variable: 'APPCENTER_TOKEN')]) {
                        sh """
                            # Install App Center CLI if not present
                            if ! command -v appcenter >/dev/null; then
                                npm install -g appcenter-cli
                            fi
                            
                            # Authenticate and upload
                            appcenter login --token \$APPCENTER_TOKEN
                            appcenter distribute release \
                                --app \$APPCENTER_OWNER_NAME/\$APPCENTER_APP_NAME \
                                --file \$apkFile \
                                --group "Collaborators" \
                                --release-notes "Automated build from Jenkins"
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