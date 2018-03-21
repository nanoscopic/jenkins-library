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
    String releaseName = parameters.get('releaseName')
    boolean purge = parameters.get('purge', false)

    echo "Deleting Helm release: ${releaseName}"

    String purgeFlag = ""
    if (purge) {
        purgeFlag = "--purge"
    }

    withEnv(["KUBECONFIG=${WORKSPACE}/kubeconfig"]) {
        sh(script: "set -o pipefail; ${WORKSPACE}/helm --home ${WORKSPACE}/.helm delete ${purgeFlag} ${releaseName} 2>&1 | tee ${WORKSPACE}/logs/helm-delete-${releaseName}.log")
        sh(script: "set -o pipefail; kubectl delete namespace ${releaseName} 2>&1 | tee -a ${WORKSPACE}/logs/helm-delete-${releaseName}.log")
    }
}
