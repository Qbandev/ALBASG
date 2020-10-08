pipeline {
    agent any
    options { disableConcurrentBuilds() }
    environment {
        StackName = "${env.PROJECT}"
        EnvType = "${env.ENV}"
        DisableTearDown = "${env.DisableTearDown}"
    }

    stages {
        stage('Download template') {
            steps {
                dir('code') {
                    sh "git clone https://github.com/Qbandev/ALBASG.git"
                }
            }
        }
        stage('Validate template') {
            steps {
                cfnValidate(file:"${WORKSPACE}/code/ALBASG/ALBAutoScaling.yaml")
            }
        }           
        stage('Deploy template') {
            steps {
                cfnCreateChangeSet(stack:"${StackName}", changeSet:"${EnvType}", file:"${WORKSPACE}/code/ALBASG/ALBAutoScaling.yaml", params:["InstanceType=${InstanceType}", "KeyName=${SSHKey}", "Stackname=${StackName}", "SSHLocation=${SSHLocation}", "EnvType=${EnvType}"], tags:["Name=${StackName}", "Environment=${EnvType}"], pollInterval:1000)
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