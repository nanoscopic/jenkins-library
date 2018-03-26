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
import com.suse.kubic.CaaspBareMetalTypeOptions


CaaspBareMetalTypeOptions call(Map parameters = [:]) {
    CaaspBareMetalTypeOptions options = parameters.get('typeOptions', null)
    boolean waitISOFetching = parameters.get('waitISOFetching', true)

    if (options == null) {
        options = new CaaspBareMetalTypeOptions()
    }

    if (options.image != null && options.image != '') {
        return options
    }

    // TODO: Set the iso name into options.image - then use that as the image
    // to deploy later in the job.

    timeout(10) {
        dir('automation/caasp-bare-metal/deployer') {
            withCredentials([file(credentialsId: 'caasp-bare-metal-serverlist', variable: 'SERVERLIST_PATH'),
                    file(credentialsId: 'caasp-bare-metal-conf', variable: 'CONFFILE')]) {
                // Start ISO fetching
                sh(script: 'set -o pipefail; ./deployer ${JOB_NAME}-${BUILD_NUMBER} --start-iso-fetching 2>&1 | tee ${WORKSPACE}/logs/caasp-bare-metal-start-iso-fetching.log')
            }
        }
    }

    if (waitISOFetching) {
        timeout(120) {
            dir('automation/caasp-bare-metal/deployer') {
                withCredentials([file(credentialsId: 'caasp-bare-metal-serverlist', variable: 'SERVERLIST_PATH'),
                        file(credentialsId: 'caasp-bare-metal-conf', variable: 'CONFFILE')]) {
                    // Wait for ISO fetching to complete
                    sh(script: 'set -o pipefail; ./deployer ${JOB_NAME}-${BUILD_NUMBER} --wait-iso-fetching 2>&1 | tee ${WORKSPACE}/logs/caasp-bare-metal-wait-iso-fetching.log')
                }
            }
        }
    }

    return options
}
