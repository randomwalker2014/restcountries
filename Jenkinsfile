pipeline {

  agent {
    label ""
  }

  environment {
    ARTIFACTORY_QA_URL = "${env.ARTIFACTORY_BASE_URL}/generic-local/rest-geo-api/deployments/latest/qa"
    ARTIFACTORY_STAGING_URL = "${env.ARTIFACTORY_BASE_URL}/generic-local/rest-geo-api/deployments/latest/staging"
  }

  stages {

    stage('Build') {

      steps {
         notifyStarted()

         script{
             MAVEN_HOME = tool 'mvn'
         }
         sh "mvn -Dmaven.test.failure.ignore=true install"
      }
    }

    stage('Analyze Code Quality') {
      steps {
        script {
             SCANNER_HOME = tool 'sonarQube-scanner'
             BRANCH_NAME = "${env.BRANCH_NAME.replaceAll("/","-")}"
             JOB_NAME = "${env.JOB_NAME.replaceAll("/","-")}"
             BUILD_NUMBER = "${env.BUILD_NUMBER}"
        }
       withSonarQubeEnv('spinsys-sonarQube') {

         sh "${SCANNER_HOME}/bin/sonar-scanner " +
          '-Dsonar.projectKey=' + "$JOB_NAME" + "-" + "$BUILD_NUMBER " +
          '-Dsonar.language=java ' +
          '-Dsonar.java.source=1.8 ' +
          '-Dsonar.sources=./src/main/java ' +
          '-Dsonar.tests=./src/test/java ' +
          '-Dsonar.test.inclusions=**/*test*/** ' +
          '-Dsonar.exclusions=**/*test*/** ' +
          '-Dsonar.java.binaries=./target/classes'
       }
      }
    }

    stage('Unit Test and Coverage') {
      steps {
        script{
            MAVEN_HOME = tool 'mvn'
         }
         sh "mvn -Dmaven.test.failure.ignore=true test"
        # step([$class: 'XUnitBuilder',
        #    thresholds: [
        #                [$class: 'FailedThreshold', unstableThreshold: '40'],
        #                [$class: 'FailedThreshold', failureThreshold: '40']],
        #    tools: [[$class: 'JUnitType', pattern: 'target/reports/junit/TESTS-*.xml']]])
         echo 'completed Unit Testing'

         step( [ $class: 'JacocoPublisher',buildOverBuild: false, changeBuildStatus: true, deltaBranchCoverage: '20', deltaInstructionCoverage: '20', exclusionPattern: '**/*Test*.class', maximumBranchCoverage: '30', maximumClassCoverage: '30', maximumComplexityCoverage: '30', maximumInstructionCoverage: '30', maximumLineCoverage: '30', maximumMethodCoverage: '30', minimumBranchCoverage: '25', minimumClassCoverage: '25', minimumComplexityCoverage: '25', minimumInstructionCoverage: '25', minimumLineCoverage: '25', minimumMethodCoverage: '25' ] )
      }
    }

    stage('Push QA Image to Repo') {
       when {
              //push to refactory only when Unit Tests are successful
              expression { currentBuild.result == 'SUCCESS' }
            }
        steps {
            sh 'curl -X PUT -H "X-JFrog-Art-Api:AKCp5aUZyj5VG54DqeuTYqEf4qeAH7HcJmeTxjq63AJBBY5NZLLsZcjd3DgZCEx3BUiZ9dCGU" -T ./dist/lroc.war "$ARTIFACTORY_QA_URL/lroc.war"'
        }
      }
 }

    post {
     always {

    //generate notifications
     script{
         if(currentBuild.result == 'SUCCESS') {
             notifySuccessful()
         }else if(currentBuild.result == 'UNSTABLE'){
             notifyUnstable()
         }else{
             notifyFailed()
         }
     }
     // wipe out the workspace
       sh "docker container prune -f"
       deleteDir()
     }
  }
}


 def notifyStarted() {
  // send to email
  emailext (
      subject: "STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
      body: """<p>STARTED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]':</p>
        <p>Check console output at "<a href="${env.BUILD_URL}">${env.JOB_NAME} [${env.BUILD_NUMBER}]</a>"</p>""",
      recipientProviders: [[$class: 'RequesterRecipientProvider']]
    )
 }
  def notifyUnstable() {
  // send to email
  emailext (
      subject: "UNSTABLE: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
      body: '''${SCRIPT, template="groovy-html.template"}''',
      recipientProviders: [[$class: 'RequesterRecipientProvider']]
    )
 }
 def notifySuccessful() {
  emailext (
      subject: "SUCCESSFUL: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
      body: '''${SCRIPT, template="groovy-html.template"}''',
      recipientProviders: [[$class: 'RequesterRecipientProvider']]
    )
 }
 def notifyFailed() {
  emailext (
      subject: "FAILED: Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
      body: '''${SCRIPT, template="groovy-html.template"}''',
      recipientProviders: [[$class: 'RequesterRecipientProvider']]
    )
}
