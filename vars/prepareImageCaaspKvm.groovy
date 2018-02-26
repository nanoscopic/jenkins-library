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
import com.suse.kubic.CaaspKvmTypeOptions


CaaspKvmTypeOptions call(Map parameters = [:]) {
    CaaspKvmTypeOptions options = parameters.get('typeOptions', null)

    if (options == null) {
        options = new CaaspKvmTypeOptions()
    }

    if (options.image != null && options.image != '') {
        return options
    }

    def proxyFlag = ""
    if (env.hasProperty("http_proxy")) {
        proxyFlag = "-P ${env.http_proxy}"
    }

    timeout(240) {
        dir('automation/misc-tools') {
            withCredentials([string(credentialsId: 'caasp-proxy-host', variable: 'CAASP_PROXY')]) {
                parallel 'CaaSP KVM': {
                    sh(script: "set -o pipefail; ./download-image --proxy ${CAASP_PROXY} --type kvm channel://${options.channel} 2>&1 | tee ${WORKSPACE}/logs/caasp-kvm-prepare-image-caasp.log")
                    options.image = "file://${WORKSPACE}/automation/downloads/kvm-${options.channel}"
                },
                'Velum Docker': {
                    sh(script: "set -o pipefail; ./download-image --proxy ${CAASP_PROXY} --type docker --image-name sles12-velum-development channel://${options.channel} 2>&1 | tee ${WORKSPACE}/logs/caasp-kvm-prepare-image-velum-development.log")
                    options.velumImage = "file://${WORKSPACE}/automation/downloads/docker-sles12-velum-development-${options.channel}"
                }
            }
        }
    }

    return options
}
