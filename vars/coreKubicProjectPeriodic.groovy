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
def call(Map parameters = [:], Closure preBootstrapBody = null, Closure body = null) {
    String nodeLabel = parameters.get('nodeLabel', 'leap42.3&&32GB')
    String environmentType = parameters.get('environmentType', 'caasp-kvm')
    def environmentTypeOptions = parameters.get('environmentTypeOptions', null)
    boolean environmentDestroy = parameters.get('environmentDestroy', true)
    int masterCount = parameters.get('masterCount', 3)
    int workerCount = parameters.get('workerCount', 2)

    echo "Starting Kubic core project periodic"

    try {
        // TODO: Make this an OpenStack based deploy with 50+ nodes.
        withKubicEnvironment(
                nodeLabel: nodeLabel,
                environmentType: environmentType,
                environmentTypeOptions: environmentTypeOptions,
                environmentDestroy: environmentDestroy,
                gitBase: 'https://github.com/kubic-project',
                gitBranch: env.getEnvironment().get('CHANGE_TARGET', env.BRANCH_NAME),
                gitCredentialsId: 'github-token',
                masterCount: masterCount,
                workerCount: workerCount)
        {
            // preBootstrapBody
            preBootstrapBody()
        }{

            // Run the Core Project Tests
            coreKubicProjectTests(
              environment: environment,
              podName: 'default',
              replicaCount: 15,
              replicasCreationIntervalSeconds: 600
            )

            if (body != null) {
                // Prepare the body closure delegate
                def delegate = [:]
                // Set some context variables available inside the body() method
                delegate['environment'] = environment
                delegate['environmentTypeOptions'] = environmentTypeOptions
                body.delegate = delegate

                // Execute the body of the test
                def bodyResult = body()
                if (bodyResult instanceof Environment) {
                    // TODO: Update closures to always return the environment, to
                    // handle cases where the closure modify the environment.
                    environment = bodyResult
                }
            }

            return environment
        }
    } catch (e) {
        echo "Sending failure notification"

        withCredentials([string(credentialsId: 'caasp-team-email', variable: 'EMAIL')]) {
            mail(
              to: "${EMAIL}",
              subject: "Build Failed: ${env.JOB_NAME} [${env.BUILD_NUMBER}]",
              body: "Build Failed: ${env.JOB_NAME} [${env.BUILD_NUMBER}] - ${env.BUILD_URL}"
            )
        }

        throw e
    }
}
