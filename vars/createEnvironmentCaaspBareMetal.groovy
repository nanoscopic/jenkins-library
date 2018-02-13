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


// Deploy admin node
// Create new environment.json with the real admin and bogus worker nodes

import com.suse.kubic.Environment

Environment call(Map parameters = [:]) {
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')
    timeout(60) {
        dir('automation/caasp-bare-metal/deployer') {
            withCredentials([file(credentialsId: 'caasp-bare-metal-serverlist', variable: 'SERVERLIST_PATH'),
                    file(credentialsId: 'caasp-bare-metal-conf', variable: 'CONFFILE')]) {
                // Lock required nodes, power them off, deploy admin node
                sh(script: "set -o pipefail; ./deployer ${JOB_NAME}-${BUILD_NUMBER} --admin --master-count ${masterCount} --worker-count ${workerCount} --logsdir ${WORKSPACE}/logs 2>&1 | tee ${WORKSPACE}/logs/caasp-bare-metal-deploy-admin.log")
            }

            sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            sh(script: "cat ${WORKSPACE}/environment.json")
        }
        // generate environment.ssh_config - it will be overwritten after nodes deployment
        // TODO: move this into the env tool
        sh(script: '${WORKSPACE}/automation/misc-tools/generate-ssh-config ${WORKSPACE}/environment.json')

        sh(script: 'chmod 400 ${WORKSPACE}/automation/misc-files/id_shared')

        archiveArtifacts(artifacts: 'environment.json', fingerprint: true)
    }

    // Read the generated environment file
    return new Environment(readJSON(file: 'environment.json'))
}
