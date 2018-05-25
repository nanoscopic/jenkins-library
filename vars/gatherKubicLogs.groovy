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

def call(Map parameters = [:]) {
    Environment environment = parameters.get('environment')

    echo "Gathering Kubic Logs"

    // Use returnStatus: true, and don't bother checking the results, so log gathering always "works".
    // Ideally we would wait until the end, and fail this step if any of the returned statues was non-zero.

    // "Short" logs you care about most should be piped through tee, while long or rarely used logs should
    // go straight to a file.

    def parallelSteps = [:]

    environment.minions.each { minion ->
        def gatherLogs = {
            shOnMinion(minion: minion, script: "supportconfig -b")
            scpFromMinion(minion: minion, source: "/var/log/nts_*.tbz", destination: "${WORKSPACE}/logs/")
        }

        parallelSteps.put("${minion.role}-${minion.index}", gatherLogs)
    }

    timeout(30) {
        parallel(parallelSteps)
    }

    // Extract failed Salt events from supportconfig tarballs, if any
    timeout(10) {
      sh(script: "mkdir ${WORKSPACE}/logs/supportconfig_salt_events/")
      dir("${WORKSPACE}/logs/supportconfig_salt_events/") {
        sh(script: "find ${WORKSPACE}/logs/ -name 'nts_*.tbz' -print0 | xargs -I ! -0 sh -c 'export F=\"!\"; tar -Oxf \$F */salt-events-summary.txt > \"\${F%%tbz}salt_failures.txt\"'")
        sh(script: "find ${WORKSPACE}/logs/ -name 'nts_*.tbz' -print0 | xargs -I ! -0 sh -c 'export F=\"!\"; tar -Oxf \$F */salt-events.json > \"\${F%%tbz}salt_failures_full.json\"'")
      }
    }
}
