/////// ******************************* Code for fectching Failed Stage Name ******************************* ///////
import io.jenkins.blueocean.rest.impl.pipeline.PipelineNodeGraphVisitor
import io.jenkins.blueocean.rest.impl.pipeline.FlowNodeWrapper
import org.jenkinsci.plugins.workflow.support.steps.build.RunWrapper
import org.jenkinsci.plugins.workflow.actions.ErrorAction

// Get information about all stages, including the failure cases
// Returns a list of maps: [[id, failedStageName, result, errors]]
@NonCPS
List < Map > getStageResults(RunWrapper build) {

  // Get all pipeline nodes that represent stages
  def visitor = new PipelineNodeGraphVisitor(build.rawBuild)
  def stages = visitor.pipelineNodes.findAll {
    it.type == FlowNodeWrapper.NodeType.STAGE
  }

  return stages.collect {
    stage ->

      // Get all the errors from the stage
      def errorActions = stage.getPipelineActions(ErrorAction)
    def errors = errorActions?.collect {
      it.error
    }.unique()

    return [
      id: stage.id,
      failedStageName: stage.displayName,
      result: "${stage.status.result}",
      errors: errors
    ]
  }
}

// Get information of all failed stages
@NonCPS
List < Map > getFailedStages(RunWrapper build) {
  return getStageResults(build).findAll {
    it.result == 'FAILURE'
  }
}

/////// ******************************* Code for fectching Failed Stage Name ******************************* ///////

@Library('slack') _
pipeline {
  agent any
  tools{
        maven 'maven3'
    }
environment {
    KUBE_BENCH_SCRIPT = "cis-master.sh"
    deploymentName = "devsecops"
    containerName = "devsecops-container"
    serviceName = "devsecops-svc"
    imageName = "mafike1/numeric-app:prod-${GIT_COMMIT}"
    SimageName = "mafike1/numeric-app:staging-${GIT_COMMIT}"
    applicationURL = "http://192.168.33.11"
    applicationURI = "/increment/99"
    NEXUS_VERSION = "nexus3"
    NEXUS_PROTOCOL = "http"
    NEXUS_URL = "172.31.40.209:8081"
    NEXUS_REPOSITORY = "devsecops"
  	NEXUS_REPO_ID    = "devsecops"
    NEXUS_CREDENTIAL_ID = "nexus_login"
    ARTVERSION = "${env.BUILD_ID}"
}

  stages {
     stage('Build my Artifact') {
            when {
              anyOf {
                branch 'develop'
                branch 'main'
                expression { env.BRANCH_NAME.startsWith('feature/')}
              }
            }
            steps {
              script {
                cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
              try{
              sh "mvn clean package -DskipTests=true"
              archiveArtifacts 'target/*.jar' //so tfhat they can be downloaded later
            }
            catch (e){
              echo "Error building artifact: ${e.message}"
            }
         }   
        }
        }
     }
     stage('Unit Tests - JUnit and Jacoco tests') {
      when {
                expression { env.BRANCH_NAME.startsWith('feature/') }
            }
       steps {
        script{
        cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
        try{
        sh "mvn test"
        }
        catch (e) {
          echo "Error running unit tests: ${e.message}"
        }
       }
       }
       }
      } 
     stage('Mutation Tests - PIT') {
      when {
                branch 'develop'
            }
      steps {
        script{
          cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
        try {
        sh "mvn org.pitest:pitest-maven:mutationCoverage"
      }
      catch (e) {
        echo "Error running mutation tests: ${e.message}"
      }
      }
      }
      }
    } 
     /* stage('SAST Scan With Sonarqube') {
      when {
       anyOf {
        branch 'develop'
        expression { env.BRANCH_NAME.startsWith('feature/') }
        }
       }

      steps {
      script{
      cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
      try {
        withSonarQubeEnv('sonarqube') {
        sh "mvn clean verify sonar:sonar \
            -Dsonar.projectKey=numeric_app \
            -Dsonar.projectName='numeric_app' \
            -Dsonar.host.url=http://192.168.33.10:9000 "
      }
        timeout(time: 2, unit: 'MINUTES') {
          script {
            waitForQualityGate abortPipeline: true
          }
        }
        catch (e) {
          echo "Error running SAST Analysis test: ${e.message}"
        }
      }   
      }
      }
       } 
      } 

     stage("Publish to Nexus Repository Manager") {
      when {
        branch 'main'
        }

            steps {
                script {
                    pom = readMavenPom file: "pom.xml";
                    filesByGlob = findFiles(glob: "target/*.${pom.packaging}");
                    echo "${filesByGlob[0].name} ${filesByGlob[0].path} ${filesByGlob[0].directory} ${filesByGlob[0].length} ${filesByGlob[0].lastModified}"
                    artifactPath = filesByGlob[0].path;
                    artifactExists = fileExists artifactPath;
                    if(artifactExists) {
                        echo "*** File: ${artifactPath}, group: ${pom.groupId}, packaging: ${pom.packaging}, version ${pom.version} ARTVERSION";
                        nexusArtifactUploader(
                            nexusVersion: NEXUS_VERSION,
                            protocol: NEXUS_PROTOCOL,
                            nexusUrl: NEXUS_URL,
                            groupId: pom.groupId,
                            version: ARTVERSION,
                            repository: NEXUS_REPOSITORY,
                            credentialsId: NEXUS_CREDENTIAL_ID,
                            artifacts: [
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: artifactPath,
                                type: pom.packaging],
                                [artifactId: pom.artifactId,
                                classifier: '',
                                file: "pom.xml",
                                type: "pom"]
                            ]
                        );
                   } 
		           else {
                        error "*** File: ${artifactPath}, could not be found";
                    }
                }
            }
        } */
     stage('Vulnerability Scan - Docker') {
      when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    expression { env.BRANCH_NAME.startsWith('feature/') }
                }
            }
      steps {
        script {
          cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
            def errors = [:]
            parallel(
                "Dependency Scan": {
                    try {
                        sh "mvn dependency-check:check"
                    } catch (e) {
                        errors["Dependency Scan"] = e.message
                    }
                },
                "Trivy Scan": {
                    try {
                        timeout(time: 10, unit: 'MINUTES') {
                            sh "bash trivy-docker-image-scan.sh"
                        }
                    } catch (e) {
                        errors["Trivy Scan"] = e.message
                    }
                },
                "OPA Conftest": {
                    try {
                        sh 'docker run --rm -v $(pwd):/project openpolicyagent/conftest test --policy dockerfile_security.rego Dockerfile'
                    } catch (e) {
                        errors["OPA Conftest"] = e.message
                    }
                }
            )
            if (errors) {
                errors.each { key, value ->
                    echo "Error in ${key}: ${value}"
                }
                error "One or more Docker vulnerability scans failed. See logs above."
            }
        }
        }
    }
}
    stage('Docker Build and Push') {
    when {
        anyOf {
            branch 'develop'
            branch 'main'
        }
    }
    steps {
        withCredentials([usernamePassword(credentialsId: 'docker-hub', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
            script {
                cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                    arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                ]) {
                    try {
                        def dockerTag
                        def sanitizedBranchName = env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9\\-_.]', '-') // Sanitize branch name
                        
                        if (env.BRANCH_NAME == 'develop') {
                            dockerTag = "staging-${GIT_COMMIT}"
                        } else if (env.BRANCH_NAME == 'main') {
                            dockerTag = "prod-${GIT_COMMIT}"
                        }

                        // Build the Docker image
                        sh """
                        echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                        docker build -t mafike1/numeric-app:${dockerTag} .
                        """

                        // Push the Docker image
                        if (env.BRANCH_NAME == 'main' || env.BRANCH_NAME == 'develop') {
                            sh """
                            docker push mafike1/numeric-app:${dockerTag}
                            """
                            echo "Docker image mafike1/numeric-app:${dockerTag} successfully pushed."
                        }
                    } catch (e) {
                        echo "Error building and pushing Docker image: ${e.message}"
                    }
                }
            }
        }
    }
}


/*
   stage('Run Docker Container') {
    when {
        expression { env.BRANCH_NAME.startsWith('feature/') }
    }
    steps {
        script {
            def dockerTag = "mafike1/numeric-app:feature-${env.BRANCH_NAME.replaceAll('[^a-zA-Z0-9\\-_.]', '-')}-${GIT_COMMIT}"
            def mysqlContainerName = "mysql-service"
            def appContainerName = "test-app"

            echo "Starting MySQL and application containers for validation on feature branch..."

            try {
                // Create a Docker network
                sh """
                docker network create test-network
                """

                // Start the MySQL container
                sh """
                docker run --rm -d \
                --name mysql-service \
                --network test-network \
                -e MYSQL_ROOT_PASSWORD=rootpassword \
                mysql:8.0
                """

                // Wait for MySQL to initialize
                sh """
                for i in {1..30}; do
                    docker exec ${mysqlContainerName} mysqladmin ping -h localhost --silent && break
                    echo "Waiting for MySQL..."
                    sleep 2
                done
                """
                
                // Start the application container
                sh """
                docker run --rm -d \
                --name test-app \
                --network test-network \
                -p 8080:8080 \
                -e DB_USERNAME=root \
                -e DB_PASSWORD=rootpassword  ${dockerTag}
                """
                // Wait for the application to initialize
                echo "Waiting for the application to be ready..."
                sh "sleep 120"

                // Validate the application with a specific HTML check
                echo "Validating application running inside the Docker container..."
                sh """
                response=\$(curl -s http://localhost:8080/ || exit 1)
                if echo \$response | grep -q '<title>Welcome to My DevOps Project</title>'; then
                    echo "Validation successful: HTML content matches!"
                else
                    echo "Validation failed: HTML content does not match or is missing!"
                    exit 1
                fi
                """
            } catch (e) {
                // Dump logs for debugging if validation fails
                echo "Validation failed. Dumping logs for debugging..."
                sh "docker logs ${appContainerName} || true"
                sh "docker logs ${mysqlContainerName} || true"
                throw e
            } finally {
                // Ensure containers and network are stopped/cleaned up
                echo "Stopping Docker containers and cleaning up network..."
                sh "docker stop ${appContainerName} || true"
                sh "docker stop ${mysqlContainerName} || true"
                sh "docker network rm ${networkName} "
            }
        }
    }
}

  */
    
    stage('Vulnerability Scan - Kubernetes') {
    when {
                branch 'develop'
            }
    steps {
        script {
          cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
            def errors = [:]
            parallel(
                "OPA Scan": {
                    try {
                        sh 'docker run --rm -v $(pwd):/project openpolicyagent/conftest test --policy opa-k8s-security.rego k8s_deployment_service.yaml'
                    } catch (e) {
                        errors["OPA Scan"] = e.message
                    }
                },
                "Kubesec Scan": {
                    try {
                        timeout(time: 10, unit: 'MINUTES') {
                            sh "bash kubesec-scan.sh"
                        }
                    } catch (e) {
                        errors["Kubesec Scan"] = e.message
                    }
                },
                "Trivy Scan": {
                    try {
                        sh "bash trivy-k8s-scan.sh"
                    } catch (e) {
                        errors["Trivy Scan"] = e.message
                    }
                }
            )
            if (errors) {
                errors.each { key, value ->
                    echo "Error in ${key}: ${value}"
                }
                error "One or more Kubernetes vulnerability scans failed. See logs above."
            }
        }
        }
    }
}
   /*stage('Kubernetes Deployment - DEV') {
      steps {
        withKubeConfig([credentialsId: 'kubeconfig']) {
          sh "sed -i 's#replace#mafike1/numeric-app:staging-${GIT_COMMIT}#g' k8s_deployment_service.yaml"
          sh "kubectl apply -f k8s_deployment_service.yaml --validate=false"
        }
      }
    } 
     stage('Scale Up Spot Node Group') {
        when {
                branch 'develop'
            }// only run on develop branch
            steps {
                script {
                    sh '''
                    aws eks update-nodegroup-config \
                        --cluster-name ${CLUSTER_NAME} \
                        --nodegroup-name ${CLUSTER_NAME}-spot-nodes \
                        --scaling-config minSize=1,maxSize=10,desiredSize=3
                    '''
                }
            }
        } */
    stage('Run CIS Benchmark') {
    when {
        anyOf {
            branch 'develop'
            branch 'main'
        }
    }
    steps {
        script {
            echo "Running CIS Benchmark for ${env.BRANCH_NAME}..."
            withCredentials([file(credentialsId: 'kubeconfig', variable: 'KUBECONFIG_FILE')]) {
                // Run benchmark tasks in parallel for efficiency
                parallel(
                    "Run Master Benchmark": {
                        sh """
                        chmod +x cis-master.sh
                        KUBECONFIG_PATH=\$KUBECONFIG_FILE ./cis-master.sh
                        """
                    },
                    "Run ETCD Benchmark": {
                        sh """
                        chmod +x cis-etcd.sh
                        KUBECONFIG_PATH=\$KUBECONFIG_FILE ./cis-etcd.sh
                        """
                    },
                    "Run Kubelet Benchmark": {
                        sh """
                        chmod +x cis-kubelet.sh
                        KUBECONFIG_PATH=\$KUBECONFIG_FILE ./cis-kubelet.sh
                        """
                    }
                )
                // Combine results and generate report
                sh """
                chmod +x ./combine_kube_bench_json.sh
                ./combine_kube_bench_json.sh
                if [ ! -s combined-bench.json ]; then
                    echo "{}" > combined-bench.json  # Generate empty report if kube-bench fails
                fi
                python3 generate_kube_bench_report.py
                """
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '.', reportFiles: 'kube-bench-combined-report.html', reportName: 'CIS Benchmark Report'])
            }
        }
    }
}
  /*
     stage('K8S Deployment - DEV') {
       when {
                branch 'develop'
            }
      steps {
        script {
          cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
            def errors = [:]
            parallel(
                "Deployment": {
                    try {
                        withKubeConfig([credentialsId: 'kubeconfig']) {
                            sh "bash k8s-deployment.sh"
                        }
                    } catch (e) {
                        errors["Deployment"] = e.message
                    }
                },
                "Rollout Status": {
                    try {
                        withKubeConfig([credentialsId: 'kubeconfig']) {
                            sh "bash k8s-deployment-rollout-status.sh"
                        }
                    } catch (e) {
                        errors["Rollout Status"] = e.message
                    }
                }
            )
            if (errors) {
                errors.each { key, value ->
                    echo "Error in ${key}: ${value}"
                }
                error "K8S Deployment - DEV stage failed. See logs above."
            }
        }
        }
    }
}
    stage('Integration Tests - DEV') {
      when {
                branch 'develop'
            }
      steps {
        script {
          cache(maxCacheSize: 1073741824, defaultBranch: 'main', caches: [
                        arbitraryFileCache(path: 'target', cacheValidityDecidingFile: 'pom.xml')
                    ]) {
          try {
            withKubeConfig([credentialsId: 'kubeconfig']) {
              sh "bash integration-test.sh"
            }
          } catch (e) {
            withKubeConfig([credentialsId: 'kubeconfig']) {
              sh "kubectl -n default rollout undo deploy ${deploymentName}"
            }
            throw e
          }
        }
        }
      }
    }  

  stage('OWASP ZAP - DAST') {
     when {
                branch 'develop'
            }
    
      steps {
        withKubeConfig([credentialsId: 'kubeconfig']) {
          sh 'bash zap.sh'
        }
      }
    } */
   stage ('Manual Approval'){
     when {
      branch 'main'
     }
    steps {
     timeout(time: 2, unit: 'DAYS') {
      input 'Do you want to Approve the Deployment to Production Environment/Namespace?'
    }
   }
    }
  /* stage('Scale Down Spot Node Group') {
     when {
      branch 'main'
    }

            steps {
                script {
                    sh '''
                    aws eks update-nodegroup-config \
                        --cluster-name ${CLUSTER_NAME} \
                        --nodegroup-name ${CLUSTER_NAME}-spot-nodes \
                        --scaling-config minSize=0,maxSize=10,desiredSize=0
                    '''
                }
            }
        }
 */
   stage('K8S Deployment - PROD') {
    when {
      branch 'main'
    }
    steps {
        script {
            def errors = [:]
            parallel(
                "Deployment": {
                    try {
                        withKubeConfig([credentialsId: 'kubeconfig']) {
                            sh "sed -i 's#replace#${imageName}#g' k8s_PROD-deployment_service.yaml"
                            sh "kubectl -n prod apply -f k8s_PROD-deployment_service.yaml"
                        }
                    } catch (e) {
                        errors["Deployment"] = e.message
                    }
                },
                "Rollout Status": {
                    try {
                        withKubeConfig([credentialsId: 'kubeconfig']) {
                            sh "bash k8s-PROD-deployment-rollout-status.sh"
                        }
                    } catch (e) {
                        errors["Rollout Status"] = e.message
                    }
                }
            )
            if (errors) {
                errors.each { key, value ->
                    echo "Error in ${key}: ${value}"
                }
                error "K8S Deployment - PROD stage failed. See logs above."
            }
        }
    }
}
    stage('Integration Tests - PROD') {
      when {
      branch 'main'
    }
      steps {
        script {
          try {
            withKubeConfig([credentialsId: 'kubeconfig']) {
              sh "bash integration-test-PROD.sh"
            }
          } catch (e) {
            withKubeConfig([credentialsId: 'kubeconfig']) {
              sh "kubectl -n prod rollout undo deploy ${deploymentName}"
            }
            throw e
          }
        }
      }
    }
  }
    post {
    always {
        script {
            // Publish JUnit test results if they exist
            if (fileExists('target/surefire-reports/*.xml')) {
                echo "Publishing JUnit test results..."
                junit 'target/surefire-reports/*.xml'
            } else {
                echo "JUnit test results not found. Skipping..."
            }

            // Record code coverage if the coverage report exists
            if (fileExists('target/site/jacoco/jacoco.xml')) {
                echo "Recording code coverage..."
                recordCoverage enabledForFailure: true, 
                    qualityGates: [[criticality: 'NOTE', integerThreshold: 60, metric: 'MODULE', threshold: 60.0]], 
                    tools: [[pattern: 'target/site/jacoco/jacoco.xml'], [parser: 'JUNIT', pattern: 'target/surefire-reports/*.xml']]
            } else {
                echo "Code coverage report not found. Skipping..."
            }

            // Publish PIT mutation report if it exists
            if (fileExists('**/target/pit-reports/**/mutations.xml')) {
                echo "Publishing PIT mutation results..."
                pitmutation mutationStatsFile: '**/target/pit-reports/**/mutations.xml'
            } else {
                echo "PIT mutation results not found. Skipping..."
            }

            // Publish Dependency Check report if it exists
            if (fileExists('target/dependency-check-report.xml')) {
                echo "Publishing Dependency Check report..."
                dependencyCheckPublisher pattern: 'target/dependency-check-report.xml'
            } else {
                echo "Dependency Check report not found. Skipping..."
            }

            // Publish OWASP ZAP report if it exists
            if (fileExists('owasp-zap-report/zap_report.html')) {
                echo "Publishing OWASP ZAP HTML report..."
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: 'owasp-zap-report', reportFiles: 'zap_report.html', reportName: 'OWASP ZAP HTML Report'])
            } else {
                echo "OWASP ZAP HTML report not found. Skipping..."
            }

            // Publish Kube-Bench report if it exists
            if (fileExists('kube-bench-combined-report.html')) {
                echo "Publishing Kube-Bench HTML report..."
                publishHTML([allowMissing: false, alwaysLinkToLastBuild: true, keepAll: true, reportDir: '.', reportFiles: 'kube-bench-combined-report.html', reportName: 'Kube-Bench HTML Report'])
            } else {
                echo "Kube-Bench HTML report not found. Skipping..."
            }
        }
    }
    
    
   success {
        script {
            try {
                env.failedStage = "none"
                env.emoji = ":white_check_mark: :tada: :thumbsup_all:"
                sendNotification currentBuild.result
            } catch (e) {
                echo "Error sending success notification: ${e.message}"
            }
            // Confirmation for Feature Branch
            if (env.BRANCH_NAME.startsWith('feature/')) {
                echo "Feature branch ${env.BRANCH_NAME} pipeline passed. Confirming readiness for merging..."
                timeout(time: 1, unit: 'HOURS') {
                    input message: "Feature branch ${env.BRANCH_NAME} passed all checks. Approve merging to develop?"
                }
            }

            // Automerge Logic with Conflict Resolution
            try {
                if (env.BRANCH_NAME.startsWith('feature/')) {
                    echo "Attempting to merge feature branch into develop"
                    sh '''
                    git config --global user.email "jenkins@example.com"
                    git config --global user.name "Jenkins"
                    git config rerere.enabled true
                    git checkout develop
                    git merge ${env.BRANCH_NAME}
                    git push origin develop
                    '''
                    sendNotification('SUCCESS') // Notify Slack about the successful merge
                } else if (env.BRANCH_NAME == 'develop') {
                    echo "Attempting to merge develop branch into main"
                    sh '''
                    git config --global user.email "jenkins@example.com"
                    git config --global user.name "Jenkins"
                    git config rerere.enabled true
                    git checkout main
                    git merge develop
                    git push origin main
                    '''
                    sendNotification('SUCCESS') // Notify Slack about the successful merge
                }
            } catch (e) {
                echo "Error during automerge: ${e.message}"
                env.failedStage = "Automerge"
                sendNotification('FAILURE') // Notify Slack about the failure
            }
        }
    }

    failure {
        script {
            try {
                def failedStages = getFailedStages(currentBuild)
                env.failedStage = failedStages.failedStageName
                echo "Failed Stage: ${env.failedStage}"
                env.emoji = ":x: :red_circle: :sos:"
                sendNotification currentBuild.result
            } catch (e) {
                echo "Error fetching failed stages or sending failure notification: ${e.message}"
            }
        }
    }
}
}


