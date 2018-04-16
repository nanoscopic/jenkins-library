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

    dir("automation/testinfra") {
        withEnv([
            "SSH_CONFIG=${WORKSPACE}/automation/misc-tools/environment.ssh_config",
            "ENVIRONMENT_JSON=${WORKSPACE}/environment.json"
        ]) {
            def parallelSteps = [:]

            environment.minions.each { minion ->
                def runTestInfraStep = {
                    lock("testinfra-venv-setup") {
                        sh("set -o pipefail; tox -e ${minion.role} --notest")
                    }

                    sh("set -o pipefail; tox -e ${minion.role}-${minion.status} -- --hosts ${minion.fqdn} --junit-xml testinfra-${minion.role}-${minion.index}.xml -v | tee -a ${WORKSPACE}/logs/testinfra-${minion.role}-${minion.index}.log")
                }

                parallelSteps.put("${minion.role}-${minion.index}", runTestInfraStep)
            }

            try {
                timeout(30) {
                    parallel(parallelSteps)
                }
            } finally {
                junit "testinfra-*.xml"
            }
        }
    }
}
