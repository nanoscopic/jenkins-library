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
import com.suse.kubic.OpenstackTypeOptions


OpenstackTypeOptions call(Map parameters = [:]) {
    OpenstackTypeOptions options = parameters.get('typeOptions', null)

    if (options == null) {
        options = new OpenstackTypeOptions()
    }

    if (options.image != null && options.image != '') {
        return options
    }

    // TODO: Trigger the OpenStack Image loading job, wait for it, and use the latest image..

    timeout(10) {
        withCredentials([file(credentialsId: options.openrcCredentialId, variable: 'OPENRC')]) {
            // Find the latest Devel image if we've not been given a specific image
            options.image = sh(script: "set -o pipefail; set +x; source $OPENRC; openstack image list --property caasp-channel='${options.channel}' --property caasp-version='3.0' -c Name -f value | sort -r -V | head -n1 | tr -d \"\n\"", returnStdout: true)
        }
    }

    return options
}
