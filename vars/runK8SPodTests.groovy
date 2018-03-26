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


// Create a pod running few example services and scale it up

def call(Map parameters = [:]) {
    def podName = parameters.get('podName', 'default')
    int replicaCount = parameters.get('replicaCount', 1000)
    int replicasCreationIntervalSeconds = parameters.get('replicasCreationIntervalSeconds', 300)

    dir("automation/k8s-pod-tests") {
        timeout(5) {
            echo "Show running pods:"
            sh(script: "set -o pipefail; ${WORKSPACE}/automation/k8s-pod-tests/k8s-pod-tests -k ${WORKSPACE}/kubeconfig -l | tee -a ${WORKSPACE}/logs/kubectl-get-pods.log")

            echo "Create pod"
            sh(script: "set -o pipefail; ${WORKSPACE}/automation/k8s-pod-tests/k8s-pod-tests -k ${WORKSPACE}/kubeconfig -c ${WORKSPACE}/automation/k8s-pod-tests/yaml/${podName}.yml | tee -a ${WORKSPACE}/logs/kubectl-create-pod.log")

            echo "Show running pods:"
            sh(script: "set -o pipefail; ${WORKSPACE}/automation/k8s-pod-tests/k8s-pod-tests -k ${WORKSPACE}/kubeconfig -l | tee -a ${WORKSPACE}/logs/kubectl-get-pods.log")
        }

        timeout(25) {
            echo "Scaling up"
            sh(script: "set -o pipefail; ${WORKSPACE}/automation/k8s-pod-tests/k8s-pod-tests -k ${WORKSPACE}/kubeconfig --wait --slowscale ${podName} ${replicaCount} ${replicasCreationIntervalSeconds} | tee -a ${WORKSPACE}/logs/kubectl-scale.log")
        }

        timeout(5) {
            echo "Tearing down"
            sh(script: "set -o pipefail; ${WORKSPACE}/automation/k8s-pod-tests/k8s-pod-tests -k ${WORKSPACE}/kubeconfig -d ${WORKSPACE}/automation/k8s-pod-tests/yaml/${podName}.yml | tee -a ${WORKSPACE}/logs/kubectl-delete.log")
        }
    }
}
