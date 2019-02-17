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
         sh "echo 'completed Unit Testing'"
      }
    }

    stage('Push QA Image to Repo') {
       //when {
              //push to refactory only when Unit Tests are successful
        //      expression { currentBuild.result == 'SUCCESS' }
        //    }
        steps {
            sh 'curl -X PUT -H "X-JFrog-Art-Api:AKCp5aUQJ2ZKc6qHBZGqwxmQ2ZK7f135XCzutzAJhsQx7J832PrjndHyiSJRYEfYVBUeSMRnu" -T ./target/restcountries-2.0.5.war "$ARTIFACTORY_QA_URL/restcountries-2.0.5.war"'
        }
      }

    stage('Pull QA Image'){

         steps {
             notifyStarted()

             sh 'curl -H "X-JFrog-Art-Api:AKCp5aUQJ2ZKc6qHBZGqwxmQ2ZK7f135XCzutzAJhsQx7J832PrjndHyiSJRYEfYVBUeSMRnu" -O "$ARTIFACTORY_QA_URL/restcountries-2.0.5.war"'
             sh 'cp restcountries-2.0.5.war /opt/integration-test-container/jboss-uat-deployments'
             sh 'chmod 777 /opt/integration-test-container/jboss-uat-deployments/restcountries-2.0.5.war'

             sh 'docker-compose -f /opt/integration-test-container/docker-compose.yml up -d'

            	/*timeout(2) {
               		waitUntil {
                    	script {
                          def r = sh script: 'wget -q http://localhost:8280/restcountries-2.0.5/index.html -O /dev/null', returnStatus: true
                          return (r == 0);
                    	}
              		}
           		}*/
          }
        }

       stage('Run Tests') {
         parallel {
        	stage('Test:Integration') {
          	  steps {
              script{
                  MAVEN_HOME = tool 'mvn'
               }
               sh "mvn -Dmaven.test.failure.ignore=true test"
           		 echo 'completed Integration Testing'
          	  }
           }

          stage('Test:Security') {
          	steps {
              script{
                MAVEN_HOME = tool 'mvn'
              }
              sh "mvn -Dmaven.test.failure.ignore=true test"
           		echo 'completed Security Testing'
          	}
          }

          stage('Test:Performance') {
          	    steps {
                 script{
                   MAVEN_HOME = tool 'mvn'
                 }
                 sh "mvn -Dmaven.test.failure.ignore=true test"
                // performanceReport parsers: [[$class: 'JMeterParser', glob: '**/JMeterResults.jtl']], sourceDataFiles: 'results.xml',relativeFailedThresholdNegative: 1.2, relativeFailedThresholdPositive: 1.89, relativeUnstableThresholdNegative: 1.8, relativeUnstableThresholdPositive: 1.5
           		  echo 'completed Performance Testing'
          	}
          }
         }
       }

       stage('Publish to Staging Repo') {
          steps {
            sh 'curl -X PUT -H "X-JFrog-Art-Api:AKCp5aUQJ2ZKc6qHBZGqwxmQ2ZK7f135XCzutzAJhsQx7J832PrjndHyiSJRYEfYVBUeSMRnu" -T restcountries-2.0.5.war "$ARTIFACTORY_STAGING_URL/restcountries-2.0.5.war"'
           	echo 'completed promotion to Staging'
          }
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
        sh "docker-compose -f /opt/integration-test-container/docker-compose.yml down -v"
        deleteDir()
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
