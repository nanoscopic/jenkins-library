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
    OpenstackTypeOptions options = parameters.get('typeOptions', null)

    timeout(30) {
        dir('automation/caasp-openstack-heat') {
            String stackName = "${JOB_NAME}-${BUILD_NUMBER}".replace("/", "-")

            withCredentials([file(credentialsId: options.openrcCredentialId, variable: 'OPENRC')]) {
                retry(10) {
                    sh(script: "set -o pipefail; ./caasp-openstack --openrc ${OPENRC} --name ${stackName} -d 2>&1 | tee ${WORKSPACE}/logs/caasp-openstack-heat-destroy.log")
                }
            }
        }
    }
}
