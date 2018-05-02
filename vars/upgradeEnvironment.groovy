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
import com.suse.kubic.Minion


Environment call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')
    boolean fakeUpdatesAvailable = parameters.get('fakeUpdatesAvailable', false)

    // Find the admin minion
    Minion adminMinion = null

    environment.minions.each { minion ->
        if (minion.role == "admin") {
            adminMinion = minion
        }
    }

    stage('Upgrade Environment') {
        if (fakeUpdatesAvailable) {
            // Fake the need for updates
            shOnMinion(
                minion: adminMinion,
                script: "docker exec -i \\\$(docker ps | grep salt-master | awk '{print \\\$1}') salt '*' grains.setval tx_update_reboot_needed true"
            )
        }

        // Refresh Salt Grains (we could wait 10 mins, but that's 10 minutes wasted in CI)
        shOnMinion(minion: adminMinion, script: "docker exec -i \\\$(docker ps | grep salt-master | awk '{print \\\$1}') salt '*' saltutil.refresh_grains")

        // Perform the upgrade
        timeout(185) {
            try {
                dir('automation/velum-bootstrap') {
                    sh(script: "./velum-interactions --update-admin --environment ${WORKSPACE}/environment.json")
                    sh(script: "./velum-interactions --update-minions --environment ${WORKSPACE}/environment.json")
                }
            } finally {
                dir('automation/velum-bootstrap') {
                    junit "velum-bootstrap.xml"
                    try {
                        archiveArtifacts(artifacts: "screenshots/**")
                    } catch (Exception exc) {
                        echo "Failed to Archive Artifacts"
                    }
                }
            }
        }
    }
}
