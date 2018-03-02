// Copyright 2017 SUSE LINUX GmbH, Nuernberg, Germany.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
import com.suse.kubic.Environment

def call(Map parameters = [:], Closure body) {
    def nodeLabel = parameters.get('nodeLabel', 'leap42.3&&32GB')
    def environmentType = parameters.get('environmentType', 'caasp-kvm')
    def environmentTypeOptions = parameters.get('environmentTypeOptions', null)
    boolean environmentDestroy = parameters.get('environmentDestroy', true)
    def gitBase = parameters.get('gitBase', 'https://github.com/kubic-project')
    def gitBranch = parameters.get('gitBranch', env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME))
    def gitCredentialsId = parameters.get('gitCredentialsId', 'github-token')
    boolean gitIgnorePullRequest = parameters.get('gitIgnorePullRequest', false)
    int masterCount = parameters.get('masterCount', 3)
    int workerCount = parameters.get('workerCount', 2)

    echo "Creating Kubic Environment"

    // Allocate a node
    node (nodeLabel) {
        // Show some info about the node were running on
        stage('Node Info') {
            echo "Node: ${env.NODE_NAME}"
            echo "Workspace: ${env.WORKSPACE}"
            sh(script: 'ip a')
            sh(script: 'ip r')
            sh(script: 'cat /etc/resolv.conf')
            def response = httpRequest(url: 'http://169.254.169.254/latest/meta-data/public-ipv4')
            echo "Public IPv4: ${response.content}"
        }

        // Basic prep steps
        stage('Preparation') {
            cleanWs()
            sh(script: 'mkdir logs')
        }

        // Fetch the necessary code
        stage('Retrieve Code') {
            cloneAllKubicRepos(gitBase: gitBase, branch: gitBranch, credentialsId: gitCredentialsId, ignorePullRequest: gitIgnorePullRequest)
        }

        // Fetch the necessary images
        stage('Retrieve Image') {
            environmentTypeOptions = prepareImage(
                type: environmentType,
                typeOptions: environmentTypeOptions
            )
        }

        Environment environment;

        try {
            // Create the Kubic environment
            stage('Create Environment') {
                environment = createEnvironment(
                    type: environmentType,
                    typeOptions: environmentTypeOptions,
                    masterCount: masterCount,
                    workerCount: workerCount
                )
            }

            // Configure the Kubic environment
            stage('Configure Environment') {
                configureEnvironment(environment: environment)
            }

            // Create Workers
            stage('Create Environment Workers') {
                environment = createEnvironmentWorkers(
                    environment: environment,
                    type: environmentType,
                    typeOptions: environmentTypeOptions,
                    masterCount: masterCount,
                    workerCount: workerCount
                )
            }

            // Bootstrap the Kubic environment
            // and fetch ${WORKSPACE}/kubeconfig
            stage('Bootstrap Environment') {
                bootstrapEnvironment(environment: environment)
            }

            // Prepare the body closure delegate
            def delegate = [:]
            // Set some context variables available inside the body() method
            delegate['environment'] = environment
            body.delegate = delegate

            // Execute the body of the test
            body()
        } finally {
            // Gather logs from the environment
            stage('Gather Logs') {
                try {
                    gatherKubicLogs(environment: environment)
                } catch (Exception exc) {
                    // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                    echo "Failed to Gather Logs"
                }
            }

            // Destroy the Kubic Environment
            stage('Destroy Environment') {
                if (environmentDestroy) {
                    try {
                        cleanupEnvironment(
                            type: environmentType,
                            typeOptions: environmentTypeOptions,
                            masterCount: masterCount,
                            workerCount: workerCount
                        )
                    } catch (Exception exc) {
                        // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                        echo "Failed to Destroy Environment"
                    }
                } else {
                    echo "Skipping Destroy Environment as requested"
                }
            }

            // Archive the logs
            stage('Archive Logs') {
                try {
                    archiveArtifacts(artifacts: 'logs/**', fingerprint: true)
                } catch (Exception exc) {
                    // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                    echo "Failed to Archive Logs"
                }
                echo "Writing logs to database"
                try {
                  withCredentials([string(credentialsId: 'database-host', variable: 'DBHOST')]) {
                    withCredentials([string(credentialsId: 'database-password', variable: 'DBPASS')]) {
                      String status = currentBuild.currentResult
                      def starttime = new Date(currentBuild.startTimeInMillis).format("yyyy-MM-dd HH:mm")
                      sh(script: "/usr/bin/mysql -h ${DBHOST} -u jenkins -p${DBPASS} testplan -e \"INSERT INTO test_outcome (build_num, build_url, branch, status, pipeline, start_time) VALUES (\'$BUILD_NUMBER\', \'$BUILD_URL\', \'$BRANCH_NAME\', \'${status}\', \'$JOB_NAME\', \'${starttime}\') \" ")
                    }
                  }
                } catch (Exception exc) {
                    echo "Failed to write to database"
                }
            }

            // Cleanup the node
            stage('Cleanup') {
                try {
                    cleanWs()
                } catch (Exception exc) {
                    // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed.
                    echo "Failed to clean workspace"
                }
            }
        }
    }
}
