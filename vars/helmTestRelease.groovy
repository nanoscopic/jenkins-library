// Copyright 2018 SUSE LINUX GmbH, Nuernberg, Germany.
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
    boolean cleanup = parameters.get('cleanup', true)
    int timeout = parameters.get('timeout', 600)

    echo "Testing Helm release: ${releaseName}"

    String cleanupFlag = ""
    if (cleanup) {
        cleanupFlag = "--cleanup"
    }

    withEnv(["KUBECONFIG=${WORKSPACE}/kubeconfig"]) {
        sh(script: "set -o pipefail; ${WORKSPACE}/helm --home ${WORKSPACE}/.helm test ${cleanupFlag} --timeout ${timeout} ${releaseName} 2>&1 | tee ${WORKSPACE}/logs/helm-test-${releaseName}.log")
    }
}
