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

def call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')
    def podName = parameters.get('podName', 'default')
    int replicaCount = parameters.get('replicaCount', 15)
    int replicasCreationIntervalSeconds = parameters.get('replicasCreationIntervalSeconds', 300)

    echo "Starting Kubic core project tests"

    stage('Node Tests') {
        runTestInfra(environment: environment)
    }

    stage('Cluster Tests') {
        parallel 'K8S Pod Tests': {
            runK8SPodTests(
                podName: podName,
                replicaCount: replicaCount,
                replicasCreationIntervalSeconds: replicasCreationIntervalSeconds
            )
        },
        // TODO: Hardcoding this list of charts and values isn't nice...
        'Helm: MariaDB': {
            helmInstallClient()

            String releaseName = "helm-" + UUID.randomUUID()
            
            helmInstallChart(
                environment: environment,
                releaseName: releaseName,
                chartName: "stable/mariadb",
                chartVersion: "3.0.0",
                wait: true,
                values: [
                    service: [
                        type: "NodePort"
                    ],
                    persistence: [
                        enabled: false
                    ]
                ]
            )

            helmTestRelease(
                environment: environment,
                releaseName: releaseName
            )

            helmDeleteRelease(
                environment: environment,
                releaseName: releaseName,
                purge: true
            )
        }
    }
}
