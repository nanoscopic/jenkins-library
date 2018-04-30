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
def call(Map parameters = [:]) {
    try {
        timeout(15) {
            dir('automation/caasp-kvm') {
                ansiColor('xterm') {
                    withCredentials([string(credentialsId: 'caasp-proxy-host', variable: 'CAASP_PROXY')]) {
                        sh(script: "set -o pipefail; ./caasp-kvm -P ${CAASP_PROXY} --destroy 2>&1 | tee ${WORKSPACE}/logs/caasp-kvm-destroy.log")
                    }
                }
            }
        }
    } catch (Exception exc) {
        // TODO: Figure out if we can mark this stage as failed, while allowing the remaining stages to proceed?
        echo "Failed to Destroy KVM Environment, Removing this Jenkins worker"
        deleteJenkinsSlave()
        throw exc
    }
}
