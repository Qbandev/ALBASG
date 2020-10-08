pipeline {
    agent any
    options { disableConcurrentBuilds() }
    environment {
        StackName = "${env.PROJECT}"
        EnvType = "${env.ENV}"
        DisableTearDown = "${env.DisableTearDown}"
    }

    stages {
        stage('Validate template') {
            steps {
                cfnValidate(file:"${WORKSPACE}/ALBASG/ALBAutoScaling.yaml")
            }
        }           
        stage('Deploy template') {
            steps {
                cfnCreateChangeSet(stack:"${StackName}", changeSet:"${EnvType}", file:"${WORKSPACE}/ALBASG/ALBAutoScaling.yaml", params:["InstanceType=${InstanceType}", "KeyName=${SSHKey}", "StackName=${StackName}", "SSHLocation=${SSHLocation}", "EnvType=${EnvType}"], tags:["Name=${StackName}", "Environment=${EnvType}"], pollInterval:1000)
                cfnExecuteChangeSet(stack:"${StackName}", changeSet:"${EnvType}", pollInterval:1000)
            }
        }
        stage('Tear down template') {
              steps {
                  script {
                    if(EnableTearDown == false || EnableTearDown == "false") {
                      script { println "*** TearDown disabled ***" }
                    }
                    else {
                        sleep(time:30 ,unit:"MINUTES")
                        cfnDelete(stack:"${StackName}", pollInterval:5000, timeoutInMinutes:120)
                    }
                  }
              }
        }
        
   }
    post { 
        always { 
            cleanWs()
        }
    }
}