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
import com.suse.kubic.OpenstackTypeOptions


Environment call(Map parameters = [:]) {
    int masterCount = parameters.get('masterCount')
    int workerCount = parameters.get('workerCount')

    OpenstackTypeOptions options = parameters.get('typeOptions', null)

    if (options == null) {
        options = new OpenstackTypeOptions()
    }

    Environment environment

    timeout(60) {
        dir('automation/caasp-openstack-heat') {
            String stackName = "${JOB_NAME}-${BUILD_NUMBER}".replace("/", "-")

            writeFile(file: 'heat-environment.yaml', text: """
---
parameters:
  external_net: ext-net
  admin_flavor: ${options.adminFlavor}
  master_flavor: ${options.masterFlavor}
  worker_flavor: ${options.workerFlavor}
""")

            withCredentials([file(credentialsId: options.openrcCredentialId, variable: 'OPENRC')]) {
                sh(script: "set -o pipefail; ./caasp-openstack --openrc ${OPENRC} --heat-environment heat-environment.yaml -b -w ${workerCount} --image ${options.image} --name ${stackName} 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack-heat-build.log")
            }

            // Read the generated environment file
            environment = new Environment(readJSON(file: 'environment.json'))

            sh(script: "cp environment.json ${WORKSPACE}/environment.json")
            sh(script: "cat ${WORKSPACE}/environment.json")
        }
    }

    archiveArtifacts(artifacts: 'environment.json', fingerprint: true)

    return environment
}
